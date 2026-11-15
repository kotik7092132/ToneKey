package com.example.ui.studio

import android.app.Application
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClipboardHistoryRepository
import com.example.data.KeyboardPreferencesRepository
import com.example.data.KeyboardSettingsBackupCodec
import com.example.ime.InputSink
import com.example.ime.TextFieldSandboxSink
import com.example.ime.ToneKeyImeService
import com.example.model.AnimationPreset
import com.example.model.AppLanguage
import com.example.model.ClipboardItem
import com.example.model.CustomizableActionKey
import com.example.model.KeyColorOverride
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.model.PalettePreset
import com.example.model.StudioProfile
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StudioTab(val title: String) {
  STUDIO("Студия"),
  PALETTE("Палитра"),
  GEOMETRY("Форма"),
  TYPING("Ввод"),
}

data class ImeActivationStatus(
  val isEnabled: Boolean = false,
  val isDefault: Boolean = false,
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = KeyboardPreferencesRepository(application.applicationContext)
  private val clipboardRepository = ClipboardHistoryRepository(application.applicationContext)
  private val clipboardManager =
    application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

  val settings: StateFlow<KeyboardSettings> =
    repository.settingsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000L),
      initialValue = KeyboardSettings(),
    )

  val clipboardItems: StateFlow<List<ClipboardItem>> =
    clipboardRepository.clipsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000L),
      initialValue = emptyList(),
    )

  private val _dismissedSuggestionId = MutableStateFlow<String?>(null)

  val smartSuggestionClip: StateFlow<ClipboardItem?> =
    combine(clipboardItems, _dismissedSuggestionId) { items, dismissedId ->
      val newest = items.maxByOrNull { it.timestamp } ?: return@combine null
      if (newest.id == dismissedId) return@combine null
      val ageMs = System.currentTimeMillis() - newest.timestamp
      if (ageMs <= SMART_SUGGESTION_TTL_MS) newest else null
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000L),
      initialValue = null,
    )

  private val _imeStatus = MutableStateFlow(ImeActivationStatus())
  val imeStatus: StateFlow<ImeActivationStatus> = _imeStatus.asStateFlow()

  private val _activeTab = MutableStateFlow(StudioTab.STUDIO)
  val activeTab: StateFlow<StudioTab> = _activeTab.asStateFlow()

  private val _previewLanguage = MutableStateFlow(KeyboardLanguage.RUSSIAN)
  val previewLanguage: StateFlow<KeyboardLanguage> = _previewLanguage.asStateFlow()

  private val _sandboxText =
    MutableStateFlow(
      TextFieldValue(
        text = "ToneKey ✨",
        selection = TextRange(9),
      ),
    )
  val sandboxText: StateFlow<TextFieldValue> = _sandboxText.asStateFlow()

  private val _isLivePreviewExpanded = MutableStateFlow(true)
  val isLivePreviewExpanded: StateFlow<Boolean> = _isLivePreviewExpanded.asStateFlow()

  val sandboxSink: InputSink =
    TextFieldSandboxSink(
      getValue = { _sandboxText.value },
      setValue = { _sandboxText.value = it },
    )

  private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
    syncClipboard(fromChangeListener = true)
  }

  init {
    refreshImeStatus()
    syncClipboard()
    runCatching { clipboardManager?.addPrimaryClipChangedListener(clipChangedListener) }
  }

  override fun onCleared() {
    runCatching { clipboardManager?.removePrimaryClipChangedListener(clipChangedListener) }
    super.onCleared()
  }

  fun refreshImeStatus() {
    val context = getApplication<Application>().applicationContext
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val expectedId = ComponentName(context, ToneKeyImeService::class.java).flattenToShortString()
    val expectedFullId = ComponentName(context, ToneKeyImeService::class.java).flattenToString()

    val isEnabled =
      imm?.enabledInputMethodList?.any { info ->
        info.packageName == context.packageName ||
          info.id == expectedId ||
          info.id == expectedFullId
      } ?: false

    val currentDefault =
      runCatching {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
      }.getOrNull().orEmpty()

    val isDefault =
      currentDefault == expectedId ||
        currentDefault == expectedFullId ||
        currentDefault.startsWith("${context.packageName}/")

    _imeStatus.value = ImeActivationStatus(isEnabled = isEnabled, isDefault = isDefault)
    syncClipboard()
  }

  fun syncClipboard(
    fromChangeListener: Boolean = false,
    force: Boolean = false,
  ) {
    viewModelScope.launch {
      clipboardRepository.syncFromSystemClipboard(
        fromChangeListener = fromChangeListener,
        force = force,
      )
    }
  }

  fun addClipboardSnippet(text: String, pinned: Boolean = false) {
    viewModelScope.launch {
      clipboardRepository.addOrUpdateClip(text, pinned = pinned)
    }
  }

  fun togglePinClip(id: String) {
    viewModelScope.launch {
      clipboardRepository.togglePin(id)
    }
  }

  fun deleteClip(id: String) {
    viewModelScope.launch {
      clipboardRepository.deleteClip(id)
    }
  }

  fun clearUnpinnedClips() {
    viewModelScope.launch {
      clipboardRepository.clearUnpinned()
    }
  }

  fun dismissSmartSuggestion(id: String) {
    _dismissedSuggestionId.value = id
  }

  fun selectTab(tab: StudioTab) {
    _activeTab.value = tab
  }

  fun setPreviewLanguage(language: KeyboardLanguage) {
    _previewLanguage.value = language
  }

  fun updateSandboxText(value: TextFieldValue) {
    _sandboxText.value = value
  }

  fun clearSandbox() {
    _sandboxText.value = TextFieldValue("")
  }

  fun toggleLivePreviewExpanded() {
    _isLivePreviewExpanded.value = !_isLivePreviewExpanded.value
  }

  fun applyProfile(profile: StudioProfile) {
    viewModelScope.launch {
      repository.updateSettings { current ->
        profile.settings.copy(
          appLanguage = current.appLanguage,
          animationPreset = current.animationPreset,
          enablePcMode = current.enablePcMode,
          hapticEnabled = current.hapticEnabled,
          hapticDurationMs = current.hapticDurationMs,
          soundEnabled = current.soundEnabled,
        )
      }
      _previewLanguage.value = profile.settings.defaultLanguage
    }
  }

  fun setUseDynamicColor(enabled: Boolean) {
    update {
      it.copy(
        useDynamicColor = enabled,
        palettePreset =
          if (enabled) {
            PalettePreset.DYNAMIC
          } else if (it.palettePreset == PalettePreset.DYNAMIC) {
            PalettePreset.SAGE
          } else {
            it.palettePreset
          },
      )
    }
  }

  fun selectPalettePreset(preset: PalettePreset) {
    update {
      it.copy(
        palettePreset = preset,
        useDynamicColor = preset == PalettePreset.DYNAMIC,
      )
    }
  }

  fun setAppLanguage(language: AppLanguage) = update { it.copy(appLanguage = language) }
  fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
  fun setKeySurfaceStyle(style: KeySurfaceStyle) = update { it.copy(keySurfaceStyle = style) }
  fun setKeyAlpha(alpha: Float) = update { it.copy(keyAlpha = alpha) }
  fun setKeyFontFamily(family: KeyFontFamily) = update { it.copy(keyFontFamily = family) }
  fun setAnimationPreset(preset: AnimationPreset) = update { it.copy(animationPreset = preset) }
  fun setHeightScale(scale: Float) = update { it.copy(keyboardHeightScale = scale) }
  fun setCornerRadius(dp: Float) = update { it.copy(keyCornerRadiusDp = dp) }
  fun setHorizontalGap(dp: Float) = update { it.copy(keyGapHorizontalDp = dp) }
  fun setVerticalGap(dp: Float) = update { it.copy(keyGapVerticalDp = dp) }
  fun setFontSize(sp: Float) = update { it.copy(keyFontSizeSp = sp) }
  fun setBottomPadding(dp: Float) = update { it.copy(bottomPaddingDp = dp) }
  fun setOneHandedMode(mode: OneHandedMode) = update { it.copy(oneHandedMode = mode) }

  fun setDefaultLanguage(language: KeyboardLanguage) {
    _previewLanguage.value = language
    update { it.copy(defaultLanguage = language) }
  }

  fun setShowNumberRow(show: Boolean) = update { it.copy(showNumberRow = show) }
  fun setShowKeyHints(show: Boolean) = update { it.copy(showKeyHints = show) }
  fun setShowKeyPopup(show: Boolean) = update { it.copy(showKeyPopup = show) }
  fun setShowToolbar(show: Boolean) = update { it.copy(showToolbar = show) }
  fun setEnablePcMode(enabled: Boolean) = update { it.copy(enablePcMode = enabled) }
  fun setShowKeyShadow(show: Boolean) = update { it.copy(showKeyElevationShadow = show) }
  fun setHapticEnabled(enabled: Boolean) = update { it.copy(hapticEnabled = enabled) }
  fun setHapticDuration(ms: Int) = update { it.copy(hapticDurationMs = ms) }
  fun setSoundEnabled(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }
  fun setSpaceCursorControl(enabled: Boolean) = update { it.copy(spaceCursorControl = enabled) }
  fun setAutoCapitalize(enabled: Boolean) = update { it.copy(autoCapitalize = enabled) }
  fun setDoubleSpacePeriod(enabled: Boolean) = update { it.copy(doubleSpacePeriod = enabled) }
  fun setLongPressDelay(ms: Int) = update { it.copy(longPressDelayMs = ms) }

  fun setActionKeyColor(
    key: CustomizableActionKey,
    override: KeyColorOverride,
  ) = update { it.withColorOverride(key, override) }

  fun resetAllActionKeyColors() = update {
    it.copy(
      enterColorOverride = KeyColorOverride(),
      backspaceColorOverride = KeyColorOverride(),
      shiftColorOverride = KeyColorOverride(),
      symbolsColorOverride = KeyColorOverride(),
      globeColorOverride = KeyColorOverride(),
    )
  }

  fun exportSettingsJson(): String =
    KeyboardSettingsBackupCodec.exportToJson(settings.value)

  suspend fun importSettingsJson(rawJson: String): Result<Unit> {
    val parsed = KeyboardSettingsBackupCodec.importFromJson(rawJson)
    return parsed.map { imported ->
      repository.saveSettings(imported)
      _previewLanguage.value = imported.defaultLanguage
    }
  }

  fun resetDefaults() {
    viewModelScope.launch {
      repository.resetToDefaults()
      _previewLanguage.value = KeyboardLanguage.RUSSIAN
    }
  }

  private fun update(block: (KeyboardSettings) -> KeyboardSettings) {
    viewModelScope.launch {
      repository.updateSettings(block)
    }
  }

  companion object {
    private const val SMART_SUGGESTION_TTL_MS = 3 * 60_000L
  }
}
