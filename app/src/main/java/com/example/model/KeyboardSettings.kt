package com.example.model

enum class ThemeMode(val label: String) {
  SYSTEM("Авто"),
  LIGHT("Светлая"),
  DARK("Тёмная"),
  AMOLED("AMOLED"),
}

enum class PalettePreset(
  val title: String,
  val subtitle: String,
) {
  DYNAMIC("Material You", "Палитра системы"),
  SAGE("Шалфей", "Мятный и оливковый"),
  TERRACOTTA("Терракота", "Тёплая глина"),
  INDIGO("Индиго", "Глубокий синий"),
  LAVENDER("Лаванда", "Мягкий фиалковый"),
  SAKURA("Сакура", "Розовый кварц"),
  OBSIDIAN("Графит", "Строгий монохром"),
}

enum class KeySurfaceStyle(
  val title: String,
  val description: String,
) {
  EXPRESSIVE("Выразительный", "Акцентные пилюли M3"),
  CLASSIC_M3("Классика M3", "Мягкий объём клавиш"),
  OUTLINED("Контурный", "Тонкая рамка без фона"),
  SOFT_MONOLITH("Монолит", "Плоские клавиши без теней"),
}

enum class KeyFontFamily(val title: String) {
  PLUS_JAKARTA("Plus Jakarta"),
  OUTFIT("Outfit"),
  JETBRAINS_MONO("JetBrains Mono"),
  SYSTEM("Системный"),
}

enum class OneHandedMode(val title: String) {
  OFF("Выкл"),
  LEFT("Слева"),
  RIGHT("Справа"),
}

enum class KeyboardLanguage(
  val title: String,
  val shortLabel: String,
  val layoutLabel: String,
) {
  RUSSIAN("Русский", "RU", "ЙЦУКЕН"),
  ENGLISH("English", "EN", "QWERTY"),
}

enum class AnimationPreset(
  val title: String,
  val description: String,
) {
  OPTIMIZED("Оптимизированная", "Минимум эффектов для максимальной скорости на слабых устройствах"),
  HEAVY("Тяжёлая", "Все пружины, морфинг клавиш и плавные переливы Material You"),
}

data class KeyboardSettings(
  val useDynamicColor: Boolean = true,
  val palettePreset: PalettePreset = PalettePreset.DYNAMIC,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val keySurfaceStyle: KeySurfaceStyle = KeySurfaceStyle.EXPRESSIVE,
  val keyFontFamily: KeyFontFamily = KeyFontFamily.PLUS_JAKARTA,
  val animationPreset: AnimationPreset = AnimationPreset.HEAVY,
  val keyboardHeightScale: Float = 1.0f,
  val keyCornerRadiusDp: Float = 14f,
  val keyGapHorizontalDp: Float = 4f,
  val keyGapVerticalDp: Float = 6f,
  val keyFontSizeSp: Float = 19f,
  val bottomPaddingDp: Float = 8f,
  val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
  val defaultLanguage: KeyboardLanguage = KeyboardLanguage.RUSSIAN,
  val showNumberRow: Boolean = true,
  val showKeyHints: Boolean = true,
  val showKeyPopup: Boolean = true,
  val showToolbar: Boolean = true,
  val showKeyElevationShadow: Boolean = true,
  val hapticEnabled: Boolean = true,
  val hapticDurationMs: Int = 14,
  val soundEnabled: Boolean = false,
  val spaceCursorControl: Boolean = true,
  val autoCapitalize: Boolean = true,
  val doubleSpacePeriod: Boolean = true,
  val longPressDelayMs: Int = 350,
)

data class StudioProfile(
  val id: String,
  val title: String,
  val subtitle: String,
  val settings: KeyboardSettings,
)

object StudioProfiles {
  val all = listOf(
    StudioProfile(
      id = "pixel_expressive",
      title = "Pixel Expressive",
      subtitle = "Живой Material You с пилюлями",
      settings = KeyboardSettings(
        useDynamicColor = true,
        palettePreset = PalettePreset.DYNAMIC,
        themeMode = ThemeMode.SYSTEM,
        keySurfaceStyle = KeySurfaceStyle.EXPRESSIVE,
        keyFontFamily = KeyFontFamily.PLUS_JAKARTA,
        keyboardHeightScale = 1.0f,
        keyCornerRadiusDp = 16f,
        keyGapHorizontalDp = 4.5f,
        keyGapVerticalDp = 6.5f,
        keyFontSizeSp = 19f,
        showNumberRow = true,
        showToolbar = true,
      ),
    ),
    StudioProfile(
      id = "sage_minimal",
      title = "Шалфей Дзен",
      subtitle = "Воздушные контуры и мягкий шрифт",
      settings = KeyboardSettings(
        useDynamicColor = false,
        palettePreset = PalettePreset.SAGE,
        themeMode = ThemeMode.LIGHT,
        keySurfaceStyle = KeySurfaceStyle.OUTLINED,
        keyFontFamily = KeyFontFamily.OUTFIT,
        keyboardHeightScale = 0.96f,
        keyCornerRadiusDp = 20f,
        keyGapHorizontalDp = 5f,
        keyGapVerticalDp = 7f,
        keyFontSizeSp = 18.5f,
        showNumberRow = false,
        showKeyHints = true,
      ),
    ),
    StudioProfile(
      id = "terracotta_warm",
      title = "Терракота Уют",
      subtitle = "Тёплая глина и классический рельеф",
      settings = KeyboardSettings(
        useDynamicColor = false,
        palettePreset = PalettePreset.TERRACOTTA,
        themeMode = ThemeMode.SYSTEM,
        keySurfaceStyle = KeySurfaceStyle.CLASSIC_M3,
        keyFontFamily = KeyFontFamily.PLUS_JAKARTA,
        keyboardHeightScale = 1.04f,
        keyCornerRadiusDp = 12f,
        keyGapHorizontalDp = 4f,
        keyGapVerticalDp = 6f,
        keyFontSizeSp = 20f,
        showNumberRow = true,
        showKeyElevationShadow = true,
      ),
    ),
    StudioProfile(
      id = "amoled_mono",
      title = "AMOLED Код",
      subtitle = "Глубокий чёрный и моноширинный шрифт",
      settings = KeyboardSettings(
        useDynamicColor = false,
        palettePreset = PalettePreset.OBSIDIAN,
        themeMode = ThemeMode.AMOLED,
        keySurfaceStyle = KeySurfaceStyle.SOFT_MONOLITH,
        keyFontFamily = KeyFontFamily.JETBRAINS_MONO,
        keyboardHeightScale = 0.94f,
        keyCornerRadiusDp = 8f,
        keyGapHorizontalDp = 3f,
        keyGapVerticalDp = 4.5f,
        keyFontSizeSp = 17.5f,
        showNumberRow = true,
        showKeyElevationShadow = false,
      ),
    ),
    StudioProfile(
      id = "compact_one_hand",
      title = "Быстрый хват",
      subtitle = "Под правую руку и приподнятый низ",
      settings = KeyboardSettings(
        useDynamicColor = true,
        palettePreset = PalettePreset.INDIGO,
        themeMode = ThemeMode.SYSTEM,
        keySurfaceStyle = KeySurfaceStyle.EXPRESSIVE,
        keyFontFamily = KeyFontFamily.OUTFIT,
        keyboardHeightScale = 0.95f,
        keyCornerRadiusDp = 18f,
        bottomPaddingDp = 18f,
        oneHandedMode = OneHandedMode.RIGHT,
        showNumberRow = false,
      ),
    ),
  )
}
