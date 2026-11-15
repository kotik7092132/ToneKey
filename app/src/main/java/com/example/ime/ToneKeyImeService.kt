package com.example.ime

import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.example.data.KeyboardPreferencesRepository
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.theme.ToneKeyTheme
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

  private var activeLanguage by mutableStateOf(KeyboardLanguage.RUSSIAN)
  private var currentImeOptions by mutableIntStateOf(EditorInfo.IME_ACTION_UNSPECIFIED)
  private var inputSessionTick by mutableIntStateOf(0)

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

    serviceScope.launch {
      val initial = repository.settingsFlow.first()
      activeLanguage = initial.defaultLanguage
    }
  }

  override fun onCreateInputView(): View {
    val decorView = window?.window?.decorView
    decorView?.let {
      it.setViewTreeLifecycleOwner(this)
      it.setViewTreeViewModelStoreOwner(this)
      it.setViewTreeSavedStateRegistryOwner(this)
    }

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
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        ToneKeyTheme(settings = settings) {
          ToneKeyKeyboardView(
            settings = settings,
            sink = sink,
            activeLanguage = activeLanguage,
            onLanguageChange = { activeLanguage = it },
            imeOptions = currentImeOptions,
            inputSessionTick = inputSessionTick,
            bottomInsetPadding = navBarBottom,
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

  override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
    super.onStartInputView(editorInfo, restarting)
    currentImeOptions = editorInfo?.imeOptions ?: EditorInfo.IME_ACTION_UNSPECIFIED
    inputSessionTick++
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
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
    super.onDestroy()
    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    store.clear()
    serviceScope.cancel()
  }
}
