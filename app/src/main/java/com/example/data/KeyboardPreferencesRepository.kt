package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AnimationPreset
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.toneKeyDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "tonekey_settings"
)

class KeyboardPreferencesRepository(private val context: Context) {

  private object Keys {
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val PALETTE_PRESET = stringPreferencesKey("palette_preset")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val KEY_SURFACE_STYLE = stringPreferencesKey("key_surface_style")
    val KEY_FONT_FAMILY = stringPreferencesKey("key_font_family")
    val ANIMATION_PRESET = stringPreferencesKey("animation_preset")
    val KEYBOARD_HEIGHT_SCALE = floatPreferencesKey("keyboard_height_scale")
    val KEY_CORNER_RADIUS_DP = floatPreferencesKey("key_corner_radius_dp")
    val KEY_GAP_HORIZONTAL_DP = floatPreferencesKey("key_gap_horizontal_dp")
    val KEY_GAP_VERTICAL_DP = floatPreferencesKey("key_gap_vertical_dp")
    val KEY_FONT_SIZE_SP = floatPreferencesKey("key_font_size_sp")
    val BOTTOM_PADDING_DP = floatPreferencesKey("bottom_padding_dp")
    val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
    val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
    val SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
    val SHOW_KEY_HINTS = booleanPreferencesKey("show_key_hints")
    val SHOW_KEY_POPUP = booleanPreferencesKey("show_key_popup")
    val SHOW_TOOLBAR = booleanPreferencesKey("show_toolbar")
    val SHOW_KEY_SHADOW = booleanPreferencesKey("show_key_shadow")
    val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    val HAPTIC_DURATION_MS = intPreferencesKey("haptic_duration_ms")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val SPACE_CURSOR_CONTROL = booleanPreferencesKey("space_cursor_control")
    val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
    val DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
    val LONG_PRESS_DELAY_MS = intPreferencesKey("long_press_delay_ms")
  }

  val settingsFlow: Flow<KeyboardSettings> =
    context.toneKeyDataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(androidx.datastore.preferences.core.emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { prefs ->
        val defaults = KeyboardSettings()
        KeyboardSettings(
          useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: defaults.useDynamicColor,
          palettePreset = parseEnum(prefs[Keys.PALETTE_PRESET], defaults.palettePreset),
          themeMode = parseEnum(prefs[Keys.THEME_MODE], defaults.themeMode),
          keySurfaceStyle = parseEnum(prefs[Keys.KEY_SURFACE_STYLE], defaults.keySurfaceStyle),
          keyFontFamily = parseEnum(prefs[Keys.KEY_FONT_FAMILY], defaults.keyFontFamily),
          animationPreset = parseEnum(prefs[Keys.ANIMATION_PRESET], defaults.animationPreset),
          keyboardHeightScale =
            (prefs[Keys.KEYBOARD_HEIGHT_SCALE] ?: defaults.keyboardHeightScale).coerceIn(0.80f, 1.25f),
          keyCornerRadiusDp =
            (prefs[Keys.KEY_CORNER_RADIUS_DP] ?: defaults.keyCornerRadiusDp).coerceIn(2f, 24f),
          keyGapHorizontalDp =
            (prefs[Keys.KEY_GAP_HORIZONTAL_DP] ?: defaults.keyGapHorizontalDp).coerceIn(1f, 8f),
          keyGapVerticalDp =
            (prefs[Keys.KEY_GAP_VERTICAL_DP] ?: defaults.keyGapVerticalDp).coerceIn(2f, 10f),
          keyFontSizeSp =
            (prefs[Keys.KEY_FONT_SIZE_SP] ?: defaults.keyFontSizeSp).coerceIn(14f, 24f),
          bottomPaddingDp =
            (prefs[Keys.BOTTOM_PADDING_DP] ?: defaults.bottomPaddingDp).coerceIn(0f, 28f),
          oneHandedMode = parseEnum(prefs[Keys.ONE_HANDED_MODE], defaults.oneHandedMode),
          defaultLanguage = parseEnum(prefs[Keys.DEFAULT_LANGUAGE], defaults.defaultLanguage),
          showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: defaults.showNumberRow,
          showKeyHints = prefs[Keys.SHOW_KEY_HINTS] ?: defaults.showKeyHints,
          showKeyPopup = prefs[Keys.SHOW_KEY_POPUP] ?: defaults.showKeyPopup,
          showToolbar = prefs[Keys.SHOW_TOOLBAR] ?: defaults.showToolbar,
          showKeyElevationShadow = prefs[Keys.SHOW_KEY_SHADOW] ?: defaults.showKeyElevationShadow,
          hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: defaults.hapticEnabled,
          hapticDurationMs =
            (prefs[Keys.HAPTIC_DURATION_MS] ?: defaults.hapticDurationMs).coerceIn(5, 40),
          soundEnabled = prefs[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
          spaceCursorControl = prefs[Keys.SPACE_CURSOR_CONTROL] ?: defaults.spaceCursorControl,
          autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: defaults.autoCapitalize,
          doubleSpacePeriod = prefs[Keys.DOUBLE_SPACE_PERIOD] ?: defaults.doubleSpacePeriod,
          longPressDelayMs =
            (prefs[Keys.LONG_PRESS_DELAY_MS] ?: defaults.longPressDelayMs).coerceIn(200, 600),
        )
      }

  suspend fun saveSettings(settings: KeyboardSettings) {
    context.toneKeyDataStore.edit { prefs ->
      prefs[Keys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
      prefs[Keys.PALETTE_PRESET] = settings.palettePreset.name
      prefs[Keys.THEME_MODE] = settings.themeMode.name
      prefs[Keys.KEY_SURFACE_STYLE] = settings.keySurfaceStyle.name
      prefs[Keys.KEY_FONT_FAMILY] = settings.keyFontFamily.name
      prefs[Keys.ANIMATION_PRESET] = settings.animationPreset.name
      prefs[Keys.KEYBOARD_HEIGHT_SCALE] = settings.keyboardHeightScale
      prefs[Keys.KEY_CORNER_RADIUS_DP] = settings.keyCornerRadiusDp
      prefs[Keys.KEY_GAP_HORIZONTAL_DP] = settings.keyGapHorizontalDp
      prefs[Keys.KEY_GAP_VERTICAL_DP] = settings.keyGapVerticalDp
      prefs[Keys.KEY_FONT_SIZE_SP] = settings.keyFontSizeSp
      prefs[Keys.BOTTOM_PADDING_DP] = settings.bottomPaddingDp
      prefs[Keys.ONE_HANDED_MODE] = settings.oneHandedMode.name
      prefs[Keys.DEFAULT_LANGUAGE] = settings.defaultLanguage.name
      prefs[Keys.SHOW_NUMBER_ROW] = settings.showNumberRow
      prefs[Keys.SHOW_KEY_HINTS] = settings.showKeyHints
      prefs[Keys.SHOW_KEY_POPUP] = settings.showKeyPopup
      prefs[Keys.SHOW_TOOLBAR] = settings.showToolbar
      prefs[Keys.SHOW_KEY_SHADOW] = settings.showKeyElevationShadow
      prefs[Keys.HAPTIC_ENABLED] = settings.hapticEnabled
      prefs[Keys.HAPTIC_DURATION_MS] = settings.hapticDurationMs
      prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
      prefs[Keys.SPACE_CURSOR_CONTROL] = settings.spaceCursorControl
      prefs[Keys.AUTO_CAPITALIZE] = settings.autoCapitalize
      prefs[Keys.DOUBLE_SPACE_PERIOD] = settings.doubleSpacePeriod
      prefs[Keys.LONG_PRESS_DELAY_MS] = settings.longPressDelayMs
    }
  }

  suspend fun updateSettings(transform: (KeyboardSettings) -> KeyboardSettings) {
    context.toneKeyDataStore.edit { prefs ->
      val current = readFromPrefs(prefs)
      val updated = transform(current)
      writeToPrefs(prefs, updated)
    }
  }

  suspend fun resetToDefaults() {
    saveSettings(KeyboardSettings())
  }

  private fun readFromPrefs(prefs: Preferences): KeyboardSettings {
    val defaults = KeyboardSettings()
    return KeyboardSettings(
      useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: defaults.useDynamicColor,
      palettePreset = parseEnum(prefs[Keys.PALETTE_PRESET], defaults.palettePreset),
      themeMode = parseEnum(prefs[Keys.THEME_MODE], defaults.themeMode),
      keySurfaceStyle = parseEnum(prefs[Keys.KEY_SURFACE_STYLE], defaults.keySurfaceStyle),
      keyFontFamily = parseEnum(prefs[Keys.KEY_FONT_FAMILY], defaults.keyFontFamily),
      animationPreset = parseEnum(prefs[Keys.ANIMATION_PRESET], defaults.animationPreset),
      keyboardHeightScale = prefs[Keys.KEYBOARD_HEIGHT_SCALE] ?: defaults.keyboardHeightScale,
      keyCornerRadiusDp = prefs[Keys.KEY_CORNER_RADIUS_DP] ?: defaults.keyCornerRadiusDp,
      keyGapHorizontalDp = prefs[Keys.KEY_GAP_HORIZONTAL_DP] ?: defaults.keyGapHorizontalDp,
      keyGapVerticalDp = prefs[Keys.KEY_GAP_VERTICAL_DP] ?: defaults.keyGapVerticalDp,
      keyFontSizeSp = prefs[Keys.KEY_FONT_SIZE_SP] ?: defaults.keyFontSizeSp,
      bottomPaddingDp = prefs[Keys.BOTTOM_PADDING_DP] ?: defaults.bottomPaddingDp,
      oneHandedMode = parseEnum(prefs[Keys.ONE_HANDED_MODE], defaults.oneHandedMode),
      defaultLanguage = parseEnum(prefs[Keys.DEFAULT_LANGUAGE], defaults.defaultLanguage),
      showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: defaults.showNumberRow,
      showKeyHints = prefs[Keys.SHOW_KEY_HINTS] ?: defaults.showKeyHints,
      showKeyPopup = prefs[Keys.SHOW_KEY_POPUP] ?: defaults.showKeyPopup,
      showToolbar = prefs[Keys.SHOW_TOOLBAR] ?: defaults.showToolbar,
      showKeyElevationShadow = prefs[Keys.SHOW_KEY_SHADOW] ?: defaults.showKeyElevationShadow,
      hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: defaults.hapticEnabled,
      hapticDurationMs = prefs[Keys.HAPTIC_DURATION_MS] ?: defaults.hapticDurationMs,
      soundEnabled = prefs[Keys.SOUND_ENABLED] ?: defaults.soundEnabled,
      spaceCursorControl = prefs[Keys.SPACE_CURSOR_CONTROL] ?: defaults.spaceCursorControl,
      autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: defaults.autoCapitalize,
      doubleSpacePeriod = prefs[Keys.DOUBLE_SPACE_PERIOD] ?: defaults.doubleSpacePeriod,
      longPressDelayMs = prefs[Keys.LONG_PRESS_DELAY_MS] ?: defaults.longPressDelayMs,
    )
  }

  private fun writeToPrefs(
    prefs: androidx.datastore.preferences.core.MutablePreferences,
    settings: KeyboardSettings,
  ) {
    prefs[Keys.USE_DYNAMIC_COLOR] = settings.useDynamicColor
    prefs[Keys.PALETTE_PRESET] = settings.palettePreset.name
    prefs[Keys.THEME_MODE] = settings.themeMode.name
    prefs[Keys.KEY_SURFACE_STYLE] = settings.keySurfaceStyle.name
    prefs[Keys.KEY_FONT_FAMILY] = settings.keyFontFamily.name
    prefs[Keys.ANIMATION_PRESET] = settings.animationPreset.name
    prefs[Keys.KEYBOARD_HEIGHT_SCALE] = settings.keyboardHeightScale
    prefs[Keys.KEY_CORNER_RADIUS_DP] = settings.keyCornerRadiusDp
    prefs[Keys.KEY_GAP_HORIZONTAL_DP] = settings.keyGapHorizontalDp
    prefs[Keys.KEY_GAP_VERTICAL_DP] = settings.keyGapVerticalDp
    prefs[Keys.KEY_FONT_SIZE_SP] = settings.keyFontSizeSp
    prefs[Keys.BOTTOM_PADDING_DP] = settings.bottomPaddingDp
    prefs[Keys.ONE_HANDED_MODE] = settings.oneHandedMode.name
    prefs[Keys.DEFAULT_LANGUAGE] = settings.defaultLanguage.name
    prefs[Keys.SHOW_NUMBER_ROW] = settings.showNumberRow
    prefs[Keys.SHOW_KEY_HINTS] = settings.showKeyHints
    prefs[Keys.SHOW_KEY_POPUP] = settings.showKeyPopup
    prefs[Keys.SHOW_TOOLBAR] = settings.showToolbar
    prefs[Keys.SHOW_KEY_SHADOW] = settings.showKeyElevationShadow
    prefs[Keys.HAPTIC_ENABLED] = settings.hapticEnabled
    prefs[Keys.HAPTIC_DURATION_MS] = settings.hapticDurationMs
    prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
    prefs[Keys.SPACE_CURSOR_CONTROL] = settings.spaceCursorControl
    prefs[Keys.AUTO_CAPITALIZE] = settings.autoCapitalize
    prefs[Keys.DOUBLE_SPACE_PERIOD] = settings.doubleSpacePeriod
    prefs[Keys.LONG_PRESS_DELAY_MS] = settings.longPressDelayMs
  }

  private inline fun <reified T : Enum<T>> parseEnum(raw: String?, fallback: T): T {
    if (raw == null) return fallback
    return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
  }
}
