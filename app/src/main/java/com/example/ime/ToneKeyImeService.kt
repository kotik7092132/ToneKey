package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ClipboardHistoryRepository
import com.example.data.KeyboardPreferencesRepository
import com.example.model.AnimationPreset
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.theme.ToneKeyTheme
import com.example.ui.theme.buildKeyboardColorTokens
import com.example.ui.theme.rememberAnimatedKeyboardColorTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ToneKeyImeService :
  InputMethodService(),
  LifecycleOwner,
  ViewModelStoreOwner,
  SavedStateRegistryOwner {

  private val lifecycleRegistry = LifecycleRegistry(this)
  private val store = ViewModelStore()
  private val savedStateRegistryController = SavedStateRegistryController.create(this)
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private lateinit var repository: KeyboardPreferencesRepository
  private lateinit var clipboardRepository: ClipboardHistoryRepository
  private var clipboardManager: ClipboardManager? = null

  private var activeLanguage by mutableStateOf(KeyboardLanguage.RUSSIAN)
  private var currentImeOptions by mutableIntStateOf(EditorInfo.IME_ACTION_UNSPECIFIED)
  private var currentInputType by mutableIntStateOf(InputType.TYPE_CLASS_TEXT)
  private var inputSessionTick by mutableIntStateOf(0)
  private var selectionUpdateTick by mutableIntStateOf(0)
  private var lastSelStart = -1
  private var lastSelEnd = -1
  private var lastKnownIsDark: Boolean = false
  private var dismissedSuggestionId by mutableStateOf<String?>(null)

  private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
    serviceScope.launch {
      clipboardRepository.syncFromSystemClipboard(fromChangeListener = true)
    }
  }

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val viewModelStore: ViewModelStore
    get() = store

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateRegistryController.savedStateRegistry

  override fun onCreate() {
    super.onCreate()
    savedStateRegistryController.performRestore(null)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    repository = KeyboardPreferencesRepository(applicationContext)
    clipboardRepository = ClipboardHistoryRepository(applicationContext)
    clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    runCatching { clipboardManager?.addPrimaryClipChangedListener(clipChangedListener) }

    serviceScope.launch {
      val initial = repository.settingsFlow.first()
      activeLanguage = initial.defaultLanguage
      clipboardRepository.syncFromSystemClipboard()
    }
  }

  override fun onCreateInputView(): View {
    val decorView = window?.window?.decorView
    decorView?.let {
      it.setViewTreeLifecycleOwner(this)
      it.setViewTreeViewModelStoreOwner(this)
      it.setViewTreeSavedStateRegistryOwner(this)
    }
    configureImeWindow(isDark = lastKnownIsDark)

    val sink = InputConnectionSink(
      inputConnectionProvider = { currentInputConnection },
      editorInfoProvider = { currentInputEditorInfo },
    )

    return ComposeView(this).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setViewTreeLifecycleOwner(this@ToneKeyImeService)
      setViewTreeViewModelStoreOwner(this@ToneKeyImeService)
      setViewTreeSavedStateRegistryOwner(this@ToneKeyImeService)

      setContent {
        val settings by repository.settingsFlow.collectAsStateWithLifecycle(
          initialValue = KeyboardSettings(),
        )
        val clipboardItems by clipboardRepository.clipsFlow.collectAsStateWithLifecycle(
          initialValue = emptyList(),
        )
        val smartSuggestion = remember(clipboardItems, dismissedSuggestionId, inputSessionTick) {
          val newest = clipboardItems.maxByOrNull { it.timestamp }
          if (newest != null &&
            newest.id != dismissedSuggestionId &&
            (System.currentTimeMillis() - newest.timestamp) <= SMART_SUGGESTION_TTL_MS
          ) {
            newest
          } else {
            null
          }
        }

        val systemDark = isSystemInDarkTheme()
        val targetTokens = remember(settings, systemDark) {
          buildKeyboardColorTokens(this@ToneKeyImeService, settings, systemDark)
        }
        val tokens = rememberAnimatedKeyboardColorTokens(
          target = targetTokens,
          animate = settings.animationPreset == AnimationPreset.HEAVY,
        )

        LaunchedEffect(tokens.isDark) {
          lastKnownIsDark = tokens.isDark
          configureImeWindow(isDark = tokens.isDark)
        }

        ToneKeyTheme(settings = settings) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(tokens.boardBackground)
              .navigationBarsPadding()
              .windowInsetsPadding(
                WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
              )
              .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
              ),
          ) {
            ToneKeyKeyboardView(
              settings = settings,
              sink = sink,
              activeLanguage = activeLanguage,
              onLanguageChange = { activeLanguage = it },
              imeOptions = currentImeOptions,
              editorInputType = currentInputType,
              inputSessionTick = inputSessionTick,
              selectionUpdateTick = selectionUpdateTick,
              clipboardItems = clipboardItems,
              smartSuggestionClip = smartSuggestion,
              onTogglePinClip = { id ->
                serviceScope.launch { clipboardRepository.togglePin(id) }
              },
              onDeleteClip = { id ->
                serviceScope.launch { clipboardRepository.deleteClip(id) }
              },
              onClearUnpinnedClips = {
                serviceScope.launch { clipboardRepository.clearUnpinned() }
              },
              onDismissSmartSuggestion = { id ->
                dismissedSuggestionId = id
              },
              onOpenSettings = {
                val intent = Intent(this@ToneKeyImeService, MainActivity::class.java).apply {
                  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
              },
              onOpenImePicker = {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
              },
              onToggleOneHanded = {
                serviceScope.launch {
                  repository.updateSettings { current ->
                    val next =
                      if (current.oneHandedMode == OneHandedMode.OFF) {
                        OneHandedMode.RIGHT
                      } else {
                        OneHandedMode.OFF
                      }
                    current.copy(oneHandedMode = next)
                  }
                }
              },
              onSwapOneHandedSide = {
                serviceScope.launch {
                  repository.updateSettings { current ->
                    val next =
                      if (current.oneHandedMode == OneHandedMode.RIGHT) {
                        OneHandedMode.LEFT
                      } else {
                        OneHandedMode.RIGHT
                      }
                    current.copy(oneHandedMode = next)
                  }
                }
              },
            )
          }
        }
      }
    }
  }

  override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
    super.onStartInputView(editorInfo, restarting)
    currentImeOptions = editorInfo?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED
    currentInputType = editorInfo?.inputType ?: InputType.TYPE_CLASS_TEXT
    lastSelStart = editorInfo?.initialSelStart ?: -1
    lastSelEnd = editorInfo?.initialSelEnd ?: -1
    inputSessionTick++
    configureImeWindow(isDark = lastKnownIsDark)
    serviceScope.launch {
      clipboardRepository.syncFromSystemClipboard()
    }
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
  }

  override fun onUpdateSelection(
    oldSelStart: Int,
    oldSelEnd: Int,
    newSelStart: Int,
    newSelEnd: Int,
    candidatesStart: Int,
    candidatesEnd: Int,
  ) {
    super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
    if (newSelStart != lastSelStart || newSelEnd != lastSelEnd) {
      lastSelStart = newSelStart
      lastSelEnd = newSelEnd
      selectionUpdateTick++
    }
  }

  override fun onWindowShown() {
    super.onWindowShown()
    configureImeWindow(isDark = lastKnownIsDark)
    window?.window?.decorView?.let { ViewCompat.requestApplyInsets(it) }
  }

  override fun onFinishInputView(finishingInput: Boolean) {
    super.onFinishInputView(finishingInput)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
  }

  override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype?) {
    super.onCurrentInputMethodSubtypeChanged(newSubtype)
    val locale = newSubtype?.locale.orEmpty()
    if (locale.startsWith("en", ignoreCase = true)) {
      activeLanguage = KeyboardLanguage.ENGLISH
    } else if (locale.startsWith("ru", ignoreCase = true)) {
      activeLanguage = KeyboardLanguage.RUSSIAN
    }
  }

  override fun onDestroy() {
    runCatching { clipboardManager?.removePrimaryClipChangedListener(clipChangedListener) }
    super.onDestroy()
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    store.clear()
    serviceScope.cancel()
  }

  @Suppress("DEPRECATION")
  private fun configureImeWindow(isDark: Boolean) {
    val win = window?.window ?: return
    WindowCompat.setDecorFitsSystemWindows(win, false)
    win.navigationBarColor = AndroidColor.TRANSPARENT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      win.isNavigationBarContrastEnforced = false
    }
    WindowInsetsControllerCompat(win, win.decorView).isAppearanceLightNavigationBars = !isDark
  }

  companion object {
    private const val SMART_SUGGESTION_TTL_MS = 3 * 60_000L
  }
}
