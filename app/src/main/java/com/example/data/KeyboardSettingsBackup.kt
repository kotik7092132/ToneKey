package com.example.data

import com.example.model.AnimationPreset
import com.example.model.AppLanguage
import com.example.model.KeyColorOverride
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import org.json.JSONObject

object KeyboardSettingsBackupCodec {
  private const val FORMAT_MARKER = "tonekey_backup"
  private const val SCHEMA_VERSION = 1

  fun exportToJson(
    settings: KeyboardSettings,
    prettyPrint: Boolean = true,
  ): String {
    val root = JSONObject().apply {
      put("app", FORMAT_MARKER)
      put("version", SCHEMA_VERSION)
      put(
        "settings",
        JSONObject().apply {
          put("useDynamicColor", settings.useDynamicColor)
          put("palettePreset", settings.palettePreset.name)
          put("themeMode", settings.themeMode.name)
          put("keySurfaceStyle", settings.keySurfaceStyle.name)
          put("keyFontFamily", settings.keyFontFamily.name)
          put("animationPreset", settings.animationPreset.name)
          put("appLanguage", settings.appLanguage.name)
          put("keyAlpha", settings.keyAlpha.toDouble())
          put("keyboardHeightScale", settings.keyboardHeightScale.toDouble())
          put("keyCornerRadiusDp", settings.keyCornerRadiusDp.toDouble())
          put("keyGapHorizontalDp", settings.keyGapHorizontalDp.toDouble())
          put("keyGapVerticalDp", settings.keyGapVerticalDp.toDouble())
          put("keyFontSizeSp", settings.keyFontSizeSp.toDouble())
          put("bottomPaddingDp", settings.bottomPaddingDp.toDouble())
          put("oneHandedMode", settings.oneHandedMode.name)
          put("defaultLanguage", settings.defaultLanguage.name)
          put("showNumberRow", settings.showNumberRow)
          put("showKeyHints", settings.showKeyHints)
          put("showKeyPopup", settings.showKeyPopup)
          put("showToolbar", settings.showToolbar)
          put("enablePcMode", settings.enablePcMode)
          put("showKeyElevationShadow", settings.showKeyElevationShadow)
          put("hapticEnabled", settings.hapticEnabled)
          put("hapticDurationMs", settings.hapticDurationMs)
          put("soundEnabled", settings.soundEnabled)
          put("spaceCursorControl", settings.spaceCursorControl)
          put("autoCapitalize", settings.autoCapitalize)
          put("doubleSpacePeriod", settings.doubleSpacePeriod)
          put("longPressDelayMs", settings.longPressDelayMs)
          put(
            "actionKeyColors",
            JSONObject().apply {
              put("enter", overrideToJson(settings.enterColorOverride))
              put("backspace", overrideToJson(settings.backspaceColorOverride))
              put("shift", overrideToJson(settings.shiftColorOverride))
              put("symbols", overrideToJson(settings.symbolsColorOverride))
              put("globe", overrideToJson(settings.globeColorOverride))
            },
          )
        },
      )
    }
    return if (prettyPrint) root.toString(2) else root.toString()
  }

  fun importFromJson(rawJson: String): Result<KeyboardSettings> =
    runCatching {
      val trimmed = rawJson.trim()
      require(trimmed.isNotEmpty()) { "Empty backup JSON" }
      val root = JSONObject(trimmed)
      val settingsObj =
        if (root.has("settings") && root.optJSONObject("settings") != null) {
          require(root.optString("app", FORMAT_MARKER) == FORMAT_MARKER) { "Unsupported backup format" }
          root.getJSONObject("settings")
        } else {
          // Support direct settings object if valid ToneKey fields exist
          require(
            root.has("palettePreset") ||
              root.has("keySurfaceStyle") ||
              root.has("keyboardHeightScale"),
          ) { "Invalid ToneKey backup JSON" }
          root
        }

      val defaults = KeyboardSettings()
      val colorsObj = settingsObj.optJSONObject("actionKeyColors")

      KeyboardSettings(
        useDynamicColor = settingsObj.optBoolean("useDynamicColor", defaults.useDynamicColor),
        palettePreset = parseEnum(settingsObj.optString("palettePreset"), defaults.palettePreset),
        themeMode = parseEnum(settingsObj.optString("themeMode"), defaults.themeMode),
        keySurfaceStyle = parseEnum(settingsObj.optString("keySurfaceStyle"), defaults.keySurfaceStyle),
        keyFontFamily = parseEnum(settingsObj.optString("keyFontFamily"), defaults.keyFontFamily),
        animationPreset = parseEnum(settingsObj.optString("animationPreset"), defaults.animationPreset),
        appLanguage = parseEnum(settingsObj.optString("appLanguage"), defaults.appLanguage),
        keyAlpha = settingsObj.optDouble("keyAlpha", defaults.keyAlpha.toDouble()).toFloat().coerceIn(0.15f, 1.0f),
        keyboardHeightScale = settingsObj.optDouble("keyboardHeightScale", defaults.keyboardHeightScale.toDouble()).toFloat().coerceIn(0.80f, 1.25f),
        keyCornerRadiusDp = settingsObj.optDouble("keyCornerRadiusDp", defaults.keyCornerRadiusDp.toDouble()).toFloat().coerceIn(2f, 24f),
        keyGapHorizontalDp = settingsObj.optDouble("keyGapHorizontalDp", defaults.keyGapHorizontalDp.toDouble()).toFloat().coerceIn(1f, 8f),
        keyGapVerticalDp = settingsObj.optDouble("keyGapVerticalDp", defaults.keyGapVerticalDp.toDouble()).toFloat().coerceIn(2f, 10f),
        keyFontSizeSp = settingsObj.optDouble("keyFontSizeSp", defaults.keyFontSizeSp.toDouble()).toFloat().coerceIn(14f, 24f),
        bottomPaddingDp = settingsObj.optDouble("bottomPaddingDp", defaults.bottomPaddingDp.toDouble()).toFloat().coerceIn(0f, 28f),
        oneHandedMode = parseEnum(settingsObj.optString("oneHandedMode"), defaults.oneHandedMode),
        defaultLanguage = parseEnum(settingsObj.optString("defaultLanguage"), defaults.defaultLanguage),
        showNumberRow = settingsObj.optBoolean("showNumberRow", defaults.showNumberRow),
        showKeyHints = settingsObj.optBoolean("showKeyHints", defaults.showKeyHints),
        showKeyPopup = settingsObj.optBoolean("showKeyPopup", defaults.showKeyPopup),
        showToolbar = settingsObj.optBoolean("showToolbar", defaults.showToolbar),
        enablePcMode = settingsObj.optBoolean("enablePcMode", defaults.enablePcMode),
        showKeyElevationShadow = settingsObj.optBoolean("showKeyElevationShadow", defaults.showKeyElevationShadow),
        hapticEnabled = settingsObj.optBoolean("hapticEnabled", defaults.hapticEnabled),
        hapticDurationMs = settingsObj.optInt("hapticDurationMs", defaults.hapticDurationMs).coerceIn(5, 40),
        soundEnabled = settingsObj.optBoolean("soundEnabled", defaults.soundEnabled),
        spaceCursorControl = settingsObj.optBoolean("spaceCursorControl", defaults.spaceCursorControl),
        autoCapitalize = settingsObj.optBoolean("autoCapitalize", defaults.autoCapitalize),
        doubleSpacePeriod = settingsObj.optBoolean("doubleSpacePeriod", defaults.doubleSpacePeriod),
        longPressDelayMs = settingsObj.optInt("longPressDelayMs", defaults.longPressDelayMs).coerceIn(200, 600),
        enterColorOverride = overrideFromJson(colorsObj?.optJSONObject("enter")),
        backspaceColorOverride = overrideFromJson(colorsObj?.optJSONObject("backspace")),
        shiftColorOverride = overrideFromJson(colorsObj?.optJSONObject("shift")),
        symbolsColorOverride = overrideFromJson(colorsObj?.optJSONObject("symbols")),
        globeColorOverride = overrideFromJson(colorsObj?.optJSONObject("globe")),
      )
    }

  private fun overrideToJson(override: KeyColorOverride): JSONObject =
    JSONObject().apply {
      put("backgroundHex", override.backgroundArgb?.let { String.format("#%08X", it) } ?: JSONObject.NULL)
      put("contentHex", override.contentArgb?.let { String.format("#%08X", it) } ?: JSONObject.NULL)
    }

  private fun overrideFromJson(obj: JSONObject?): KeyColorOverride {
    if (obj == null) return KeyColorOverride()
    val bg = parseHexColor(obj.opt("backgroundHex")?.toString())
    val fg = parseHexColor(obj.opt("contentHex")?.toString())
    return KeyColorOverride(backgroundArgb = bg, contentArgb = fg)
  }

  private fun parseHexColor(raw: String?): Int? {
    if (raw.isNullOrBlank() || raw == "null") return null
    val normalized = if (raw.startsWith("#")) raw else "#$raw"
    return runCatching { android.graphics.Color.parseColor(normalized) }.getOrNull()
  }

  private inline fun <reified T : Enum<T>> parseEnum(raw: String?, fallback: T): T {
    if (raw.isNullOrBlank()) return fallback
    return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
  }
}
