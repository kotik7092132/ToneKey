package com.example.ui.i18n

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ime.EmojiCategory
import com.example.model.AnimationPreset
import com.example.model.AppLanguage
import com.example.model.ClipCategory
import com.example.model.ClipboardFilter
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.OneHandedMode
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import com.example.ui.studio.StudioTab
import java.util.Locale

data class AppStrings(
  val isRussian: Boolean,
  val appName: String,
  val navStudio: String,
  val navPalette: String,
  val navGeometry: String,
  val navTyping: String,
  val studioTitle: String,
  val studioSubtitle: String,
  val heroBadgeDynamic: String,
  val heroBadgePresetPrefix: String,
  val setupStatusTitle: String,
  val setupStepEnable: String,
  val setupStepSettings: String,
  val setupStepSelect: String,
  val setupReadyBadge: String,
  val setupEnabledBadge: String,
  val setupPendingBadge: String,
  val profilesHeader: String,
  val profilesSubtitle: String,
  val previewSandboxLabel: String,
  val previewSandboxPlaceholder: String,
  val previewClear: String,
  val previewExpand: String,
  val previewCollapse: String,
  // Palette tab
  val dynamicColorHeader: String,
  val dynamicColorSubtitle: String,
  val dynamicColorTitle: String,
  val dynamicColorDescApi31: String,
  val dynamicColorDescLegacy: String,
  val tokenPrimary: String,
  val tokenContainer: String,
  val tokenSecondary: String,
  val tokenTertiary: String,
  val palettesHeader: String,
  val palettesSubtitle: String,
  val themeModeHeader: String,
  val themeModeSubtitle: String,
  val surfaceStyleHeader: String,
  val surfaceStyleSubtitle: String,
  val keyOpacityTitle: String,
  val keyOpacitySubtitle: String,
  val keyFontHeader: String,
  val keyFontSubtitle: String,
  val keyFontSample: String,
  val animPresetHeader: String,
  val animPresetSubtitle: String,
  // Geometry tab
  val geometryHeader: String,
  val geometrySubtitle: String,
  val heightTitle: String,
  val heightSubtitle: String,
  val radiusTitle: String,
  val radiusSubtitle: String,
  val gapHorizontalTitle: String,
  val gapVerticalTitle: String,
  val fontSizeTitle: String,
  val bottomPaddingTitle: String,
  val bottomPaddingSubtitle: String,
  val keyShadowTitle: String,
  val keyShadowSubtitle: String,
  val oneHandedHeader: String,
  val oneHandedSubtitle: String,
  // Typing tab
  val appLanguageHeader: String,
  val appLanguageSubtitle: String,
  val defaultKeyboardLangHeader: String,
  val defaultKeyboardLangSubtitle: String,
  val layoutElementsHeader: String,
  val numberRowTitle: String,
  val numberRowSubtitle: String,
  val keyHintsTitle: String,
  val keyHintsSubtitle: String,
  val keyPopupTitle: String,
  val keyPopupSubtitle: String,
  val toolbarTitle: String,
  val toolbarSubtitle: String,
  val hapticsSoundHeader: String,
  val hapticEnabledTitle: String,
  val hapticEnabledSubtitle: String,
  val hapticDurationTitle: String,
  val hapticTestButton: String,
  val soundEnabledTitle: String,
  val soundEnabledSubtitle: String,
  val gesturesSmartHeader: String,
  val spaceCursorTitle: String,
  val spaceCursorSubtitle: String,
  val autoCapTitle: String,
  val autoCapSubtitle: String,
  val doubleSpaceTitle: String,
  val doubleSpaceSubtitle: String,
  val longPressDelayTitle: String,
  val longPressDelaySubtitle: String,
  val msUnit: String,
  val resetDefaults: String,
  val resetDialogTitle: String,
  val resetDialogBody: String,
  val resetConfirm: String,
  val cancel: String,
  // Keyboard UI
  val cursorLabel: String,
  val moveLeft: String,
  val moveRight: String,
  val clipboardTitle: String,
  val backToLetters: String,
  val lettersKeyLabel: String,
  val cdShift: String,
  val cdBackspace: String,
  val cdEnter: String,
  val cdLanguage: String,
  val cdEmoji: String,
  val cdClipboard: String,
  val cdOneHanded: String,
  val cdSettings: String,
  val cdHeroBanner: String,
  // Smart Clipboard & Studio Redesign
  val tokenSurface: String,
  val smartClipboardHeader: String,
  val smartClipboardSubtitle: String,
  val smartClipboardAddPlaceholder: String,
  val smartClipboardAddButton: String,
  val smartClipboardSyncButton: String,
  val smartClipboardEmptyTitle: String,
  val smartClipboardEmptySubtitle: String,
  val clipClearUnpinned: String,
  val clipPinAction: String,
  val clipUnpinAction: String,
  val clipDeleteAction: String,
  val clipPasteCodePrefix: String,
  val clipCleanUrlAction: String,
  val clipQuickPhrasesTitle: String,
  val clipSmartPillCodePrefix: String,
  val pcModeTitle: String,
  val pcModeSubtitle: String,
  val pcToolbarLabel: String,
  val cdPcMode: String,
) {
  fun tabTitle(tab: StudioTab): String =
    when (tab) {
      StudioTab.STUDIO -> navStudio
      StudioTab.PALETTE -> navPalette
      StudioTab.GEOMETRY -> navGeometry
      StudioTab.TYPING -> navTyping
    }

  fun appLanguageLabel(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SYSTEM -> if (isRussian) "Системный" else "System"
      AppLanguage.RUSSIAN -> "Русский"
      AppLanguage.ENGLISH -> "English"
    }

  fun themeModeLabel(mode: ThemeMode): String =
    when (mode) {
      ThemeMode.SYSTEM -> if (isRussian) "Авто" else "Auto"
      ThemeMode.LIGHT -> if (isRussian) "Светлая" else "Light"
      ThemeMode.DARK -> if (isRussian) "Тёмная" else "Dark"
      ThemeMode.AMOLED -> "AMOLED"
    }

  fun paletteTitle(preset: PalettePreset): String =
    when (preset) {
      PalettePreset.DYNAMIC -> "Material You"
      PalettePreset.SAGE -> if (isRussian) "Шалфей" else "Sage"
      PalettePreset.TERRACOTTA -> if (isRussian) "Терракота" else "Terracotta"
      PalettePreset.INDIGO -> if (isRussian) "Индиго" else "Indigo"
      PalettePreset.LAVENDER -> if (isRussian) "Лаванда" else "Lavender"
      PalettePreset.SAKURA -> if (isRussian) "Сакура" else "Sakura"
      PalettePreset.OBSIDIAN -> if (isRussian) "Графит" else "Obsidian"
    }

  fun paletteSubtitle(preset: PalettePreset): String =
    when (preset) {
      PalettePreset.DYNAMIC -> if (isRussian) "Палитра системы" else "System wallpaper"
      PalettePreset.SAGE -> if (isRussian) "Мятный и оливковый" else "Mint & olive"
      PalettePreset.TERRACOTTA -> if (isRussian) "Тёплая глина" else "Warm clay"
      PalettePreset.INDIGO -> if (isRussian) "Глубокий синий" else "Deep blue"
      PalettePreset.LAVENDER -> if (isRussian) "Мягкий фиалковый" else "Soft violet"
      PalettePreset.SAKURA -> if (isRussian) "Розовый кварц" else "Rose quartz"
      PalettePreset.OBSIDIAN -> if (isRussian) "Строгий монохром" else "Crisp mono"
    }

  fun surfaceStyleTitle(style: KeySurfaceStyle): String =
    when (style) {
      KeySurfaceStyle.EXPRESSIVE -> if (isRussian) "Выразительный" else "Expressive"
      KeySurfaceStyle.CLASSIC_M3 -> if (isRussian) "Классика M3" else "Classic M3"
      KeySurfaceStyle.OUTLINED -> if (isRussian) "Контурный" else "Outlined"
      KeySurfaceStyle.SOFT_MONOLITH -> if (isRussian) "Монолит" else "Soft Monolith"
    }

  fun surfaceStyleDescription(style: KeySurfaceStyle): String =
    when (style) {
      KeySurfaceStyle.EXPRESSIVE ->
        if (isRussian) "Акцентные пилюли M3" else "Bold tonal M3 pills"
      KeySurfaceStyle.CLASSIC_M3 ->
        if (isRussian) "Мягкий объём клавиш" else "Soft tactile key elevation"
      KeySurfaceStyle.OUTLINED ->
        if (isRussian) "Тонкая рамка без фона" else "Minimal hairline borders"
      KeySurfaceStyle.SOFT_MONOLITH ->
        if (isRussian) "Плоские клавиши без теней" else "Flat seamless keycaps"
    }

  fun fontFamilyTitle(family: KeyFontFamily): String =
    when (family) {
      KeyFontFamily.PLUS_JAKARTA -> "Plus Jakarta"
      KeyFontFamily.OUTFIT -> "Outfit"
      KeyFontFamily.JETBRAINS_MONO -> "JetBrains Mono"
      KeyFontFamily.SYSTEM -> if (isRussian) "Системный" else "System"
    }

  fun animationPresetTitle(preset: AnimationPreset): String =
    when (preset) {
      AnimationPreset.OPTIMIZED -> if (isRussian) "Оптимизированная" else "Optimized"
      AnimationPreset.HEAVY -> if (isRussian) "Тяжёлая" else "Rich & Heavy"
    }

  fun animationPresetDescription(preset: AnimationPreset): String =
    when (preset) {
      AnimationPreset.OPTIMIZED ->
        if (isRussian) {
          "Минимум эффектов для максимальной скорости на слабых устройствах"
        } else {
          "Minimal motion overhead for snappy speed on entry-level devices"
        }
      AnimationPreset.HEAVY ->
        if (isRussian) {
          "Все пружины, морфинг клавиш и плавные переливы Material You"
        } else {
          "Full spring physics, keycap shape morphing, and Material You color flow"
        }
    }

  fun oneHandedLabel(mode: OneHandedMode): String =
    when (mode) {
      OneHandedMode.OFF -> if (isRussian) "Выкл" else "Off"
      OneHandedMode.LEFT -> if (isRussian) "Слева" else "Left"
      OneHandedMode.RIGHT -> if (isRussian) "Справа" else "Right"
    }

  fun keyboardLanguageLabel(lang: KeyboardLanguage): String =
    when (lang) {
      KeyboardLanguage.RUSSIAN -> "Русский (ЙЦУКЕН)"
      KeyboardLanguage.ENGLISH -> "English (QWERTY)"
    }

  fun profileTitle(id: String): String =
    when (id) {
      "pixel_expressive" -> "Pixel Expressive"
      "sage_minimal" -> if (isRussian) "Шалфей Дзен" else "Sage Zen"
      "terracotta_warm" -> if (isRussian) "Терракота Уют" else "Warm Terracotta"
      "amoled_mono" -> if (isRussian) "AMOLED Код" else "AMOLED Mono"
      "compact_one_hand" -> if (isRussian) "Быстрый хват" else "Quick Reach"
      else -> id
    }

  fun profileSubtitle(id: String): String =
    when (id) {
      "pixel_expressive" ->
        if (isRussian) "Живой Material You с пилюлями" else "Vivid Material You pills"
      "sage_minimal" ->
        if (isRussian) "Воздушные контуры и мягкий шрифт" else "Airy outlines and soft type"
      "terracotta_warm" ->
        if (isRussian) "Тёплая глина и классический рельеф" else "Warm clay with classic depth"
      "amoled_mono" ->
        if (isRussian) "Глубокий чёрный и моноширинный шрифт" else "True black with monospace keys"
      "compact_one_hand" ->
        if (isRussian) "Под правую руку и приподнятый низ" else "Right-hand reach & lifted chin"
      else -> ""
    }

  fun emojiCategoryLabel(category: EmojiCategory): String =
    when (category.id) {
      "smileys" -> if (isRussian) "Эмоции" else "Smileys"
      "gestures" -> if (isRussian) "Жесты" else "Gestures"
      "objects" -> if (isRussian) "Знаки" else "Symbols"
      "kaomoji" -> if (isRussian) "Каомодзи" else "Kaomoji"
      else -> category.label
    }

  fun clipFilterLabel(filter: ClipboardFilter): String =
    when (filter) {
      ClipboardFilter.ALL -> if (isRussian) "Все" else "All"
      ClipboardFilter.PINNED -> if (isRussian) "Закреп" else "Pinned"
      ClipboardFilter.SMART -> if (isRussian) "Смарт" else "Smart"
    }

  fun clipCategoryLabel(category: ClipCategory): String =
    when (category) {
      ClipCategory.OTP -> if (isRussian) "Код" else "Code"
      ClipCategory.URL -> if (isRussian) "Ссылка" else "Link"
      ClipCategory.EMAIL -> "Email"
      ClipCategory.PHONE -> if (isRussian) "Телефон" else "Phone"
      ClipCategory.COLOR_HEX -> if (isRussian) "Цвет" else "Color"
      ClipCategory.TEXT -> if (isRussian) "Текст" else "Text"
    }

  fun quickTemplates(): List<String> =
    if (isRussian) {
      listOf(
        "Привет! Как дела?",
        "Скоро буду на связи",
        "Спасибо большое!",
      )
    } else {
      listOf(
        "Hey! How are you?",
        "I'll get back to you soon",
        "Thank you so much!",
      )
    }
}

val RussianStrings = AppStrings(
  isRussian = true,
  appName = "ToneKey",
  navStudio = "Студия",
  navPalette = "Палитра",
  navGeometry = "Форма",
  navTyping = "Ввод",
  studioTitle = "Студия ToneKey",
  studioSubtitle = "Тонкая настройка цвета, геометрии и тактильности",
  heroBadgeDynamic = "Material You • Системный цвет",
  heroBadgePresetPrefix = "Material You • ",
  setupStatusTitle = "Подключение клавиатуры",
  setupStepEnable = "1. Включить в настройках",
  setupStepSettings = "Настройки IME",
  setupStepSelect = "2. Выбрать ToneKey",
  setupReadyBadge = "Активна в системе",
  setupEnabledBadge = "Включена, выберите активной",
  setupPendingBadge = "Требуется активация",
  profilesHeader = "Готовые профили стиля",
  profilesSubtitle = "Мгновенная настройка палитры, геометрии и шрифта",
  previewSandboxLabel = "Живая песочница (жмите на клавиши)",
  previewSandboxPlaceholder = "Наберите текст с превью или системной клавиатурой…",
  previewClear = "Очистить",
  previewExpand = "Развернуть превью",
  previewCollapse = "Свернуть превью",
  dynamicColorHeader = "Динамические цвета Material You",
  dynamicColorSubtitle = "Адаптация тональной палитры под обои Android",
  dynamicColorTitle = "Системная палитра (Dynamic Color)",
  dynamicColorDescApi31 = "Автоматически извлекает оттенки темы вашего устройства",
  dynamicColorDescLegacy = "Используется встроенная тональная схема Material 3",
  tokenPrimary = "Primary",
  tokenContainer = "Container",
  tokenSecondary = "Secondary",
  tokenTertiary = "Tertiary",
  palettesHeader = "Авторские палитры",
  palettesSubtitle = "Готовые гармоничные оттенки в духе Pixel",
  themeModeHeader = "Режим темы",
  themeModeSubtitle = "Включая чистый чёрный для OLED-экранов",
  surfaceStyleHeader = "Стиль поверхности клавиш",
  surfaceStyleSubtitle = "Характер контраста, контуров и акцентных кнопок",
  keyOpacityTitle = "Прозрачность клавиш",
  keyOpacitySubtitle = "От воздушной полупрозрачности до плотного цвета",
  keyFontHeader = "Шрифт символов на клавишах",
  keyFontSubtitle = "Локальные шрифты для максимальной чёткости",
  keyFontSample = "Аб",
  animPresetHeader = "Пресет анимаций Material You",
  animPresetSubtitle = "Баланс между плавными эффектами и производительностью",
  geometryHeader = "Размеры и пропорции",
  geometrySubtitle = "Настройте габариты клавиатуры под размер ладони",
  heightTitle = "Высота клавиатуры",
  heightSubtitle = "Масштаб рядов от компактного до высокого",
  radiusTitle = "Скругление клавиш",
  radiusSubtitle = "От строгих прямоугольников до мягких пилюль",
  gapHorizontalTitle = "Горизонтальный зазор",
  gapVerticalTitle = "Вертикальный зазор",
  fontSizeTitle = "Размер букв на клавишах",
  bottomPaddingTitle = "Нижний отступ («подбородок»)",
  bottomPaddingSubtitle = "Приподнимает клавиши выше от системной панели",
  keyShadowTitle = "Мягкая тень клавиш",
  keyShadowSubtitle = "Лёгкий объём в стилях «Классика» и «Выразительный»",
  oneHandedHeader = "Режим одной руки",
  oneHandedSubtitle = "Сдвигает раскладку к удобному краю экрана",
  appLanguageHeader = "Язык интерфейса",
  appLanguageSubtitle = "Определяется автоматически из системы или задаётся вручную",
  defaultKeyboardLangHeader = "Язык клавиатуры по умолчанию",
  defaultKeyboardLangSubtitle = "Раскладка при открытии клавиатуры",
  layoutElementsHeader = "Элементы раскладки",
  numberRowTitle = "Цифровой ряд сверху",
  numberRowSubtitle = "Постоянный ряд 1–0 над буквами",
  keyHintsTitle = "Подсказки доп. символов",
  keyHintsSubtitle = "Миниатюрные символы в углу клавиш (например, ё и цифры)",
  keyPopupTitle = "Всплывающая клавиша при нажатии",
  keyPopupSubtitle = "Увеличенный предпросмотр буквы над пальцем",
  toolbarTitle = "Верхняя панель инструментов",
  toolbarSubtitle = "Быстрый доступ к эмодзи, буферу обмена и символам",
  hapticsSoundHeader = "Тактильность и звук",
  hapticEnabledTitle = "Виброотклик при печати",
  hapticEnabledSubtitle = "Короткий чёткий импульс при касании клавиш",
  hapticDurationTitle = "Длительность виброотклика",
  hapticTestButton = "Проверить тактильный отклик",
  soundEnabledTitle = "Звук нажатия клавиш",
  soundEnabledSubtitle = "Системный щелчок при каждом нажатии",
  gesturesSmartHeader = "Жесты и умный ввод",
  spaceCursorTitle = "Курсор свайпом по пробелу",
  spaceCursorSubtitle = "Проведите влево или вправо по пробелу для перемещения курсора",
  autoCapTitle = "Автозаглавная буква",
  autoCapSubtitle = "Заглавная буква в начале предложения и после переноса строки",
  doubleSpaceTitle = "Точка по двойному пробелу",
  doubleSpaceSubtitle = "Быстрый двойной пробел ставит «. » в конце слова",
  longPressDelayTitle = "Задержка долгого нажатия",
  longPressDelaySubtitle = "Время удержания для буквы «ё», цифр и повтора удаления",
  msUnit = "мс",
  resetDefaults = "Сбросить настройки",
  resetDialogTitle = "Сбросить настройки?",
  resetDialogBody = "Палитра, геометрия и параметры ввода вернутся к исходным значениям ToneKey.",
  resetConfirm = "Сбросить",
  cancel = "Отмена",
  cursorLabel = "Курсор",
  moveLeft = "Перенести влево",
  moveRight = "Перенести вправо",
  clipboardTitle = "Буфер обмена и быстрые фразы",
  backToLetters = "Вернуться к буквам",
  lettersKeyLabel = "АБВ",
  cdShift = "Регистр",
  cdBackspace = "Удалить",
  cdEnter = "Ввод",
  cdLanguage = "Сменить язык",
  cdEmoji = "Эмодзи",
  cdClipboard = "Буфер обмена",
  cdOneHanded = "Режим одной руки",
  cdSettings = "Настройки клавиатуры",
  cdHeroBanner = "Абстрактная иллюстрация клавиш Material You",
  tokenSurface = "Surface",
  smartClipboardHeader = "Умный буфер обмена",
  smartClipboardSubtitle = "Распознаёт коды, ссылки, почту и номера • закрепляйте важное",
  smartClipboardAddPlaceholder = "Добавить быструю фразу, ссылку или заметку…",
  smartClipboardAddButton = "Добавить",
  smartClipboardSyncButton = "Из буфера",
  smartClipboardEmptyTitle = "История буфера пока пуста",
  smartClipboardEmptySubtitle = "Скопируйте код, ссылку или текст — они появятся здесь автоматически",
  clipClearUnpinned = "Очистить незакреплённые",
  clipPinAction = "Закрепить",
  clipUnpinAction = "Открепить",
  clipDeleteAction = "Удалить запись",
  clipPasteCodePrefix = "Вставить код ",
  clipCleanUrlAction = "Без трекеров",
  clipQuickPhrasesTitle = "Быстрые фразы",
  clipSmartPillCodePrefix = "Код: ",
  pcModeTitle = "Режим ПК-клавиш (Fn / F1–F12 / Ctrl / Alt)",
  pcModeSubtitle = "Включает кнопку быстрого вызова компьютерных клавиш прямо на клавиатуре",
  pcToolbarLabel = "ПК",
  cdPcMode = "Компьютерные клавиши (Fn, F1–F12, Ctrl, Alt)",
)

val EnglishStrings = AppStrings(
  isRussian = false,
  appName = "ToneKey",
  navStudio = "Studio",
  navPalette = "Palette",
  navGeometry = "Shape",
  navTyping = "Typing",
  studioTitle = "ToneKey Studio",
  studioSubtitle = "Fine-tune colors, geometry, and tactile feedback",
  heroBadgeDynamic = "Material You • System Color",
  heroBadgePresetPrefix = "Material You • ",
  setupStatusTitle = "Keyboard Setup",
  setupStepEnable = "1. Enable in Settings",
  setupStepSettings = "IME Settings",
  setupStepSelect = "2. Select ToneKey",
  setupReadyBadge = "Active system keyboard",
  setupEnabledBadge = "Enabled — select as active",
  setupPendingBadge = "Activation required",
  profilesHeader = "Curated Style Profiles",
  profilesSubtitle = "One-tap presets for palette, shape, and typography",
  previewSandboxLabel = "Live Sandbox (tap keys to test)",
  previewSandboxPlaceholder = "Type using the live preview or system keyboard…",
  previewClear = "Clear",
  previewExpand = "Expand preview",
  previewCollapse = "Collapse preview",
  dynamicColorHeader = "Material You Dynamic Color",
  dynamicColorSubtitle = "Adapts tonal palette from your Android wallpaper",
  dynamicColorTitle = "System Palette (Dynamic Color)",
  dynamicColorDescApi31 = "Automatically extracts colors from your device theme",
  dynamicColorDescLegacy = "Uses built-in Material 3 tonal palette",
  tokenPrimary = "Primary",
  tokenContainer = "Container",
  tokenSecondary = "Secondary",
  tokenTertiary = "Tertiary",
  palettesHeader = "Curated Palettes",
  palettesSubtitle = "Harmonious Pixel-inspired color schemes",
  themeModeHeader = "Theme Mode",
  themeModeSubtitle = "Includes pure black for OLED displays",
  surfaceStyleHeader = "Key Surface Style",
  surfaceStyleSubtitle = "Contrast, borders, and expressive accent keys",
  keyOpacityTitle = "Key Opacity",
  keyOpacitySubtitle = "From airy translucent glass to solid keycaps",
  keyFontHeader = "Keycap Typography",
  keyFontSubtitle = "Bundled crisp fonts for maximum legibility",
  keyFontSample = "Aa",
  animPresetHeader = "Material You Animation Preset",
  animPresetSubtitle = "Balance between expressive motion and performance",
  geometryHeader = "Dimensions & Proportions",
  geometrySubtitle = "Adjust keyboard sizing to fit your grip",
  heightTitle = "Keyboard Height",
  heightSubtitle = "Scale rows from compact to tall",
  radiusTitle = "Key Corner Radius",
  radiusSubtitle = "From sharp rectangles to soft pills",
  gapHorizontalTitle = "Horizontal Key Gap",
  gapVerticalTitle = "Vertical Row Gap",
  fontSizeTitle = "Key Label Size",
  bottomPaddingTitle = "Bottom Padding (Chin)",
  bottomPaddingSubtitle = "Raises keys comfortably above the bottom edge",
  keyShadowTitle = "Soft Key Shadow",
  keyShadowSubtitle = "Subtle depth in Classic M3 and Expressive styles",
  oneHandedHeader = "One-Handed Mode",
  oneHandedSubtitle = "Docks keys toward your preferred screen edge",
  appLanguageHeader = "Interface Language",
  appLanguageSubtitle = "Follows your system language or sets a fixed locale",
  defaultKeyboardLangHeader = "Default Keyboard Layout",
  defaultKeyboardLangSubtitle = "Initial layout when the keyboard opens",
  layoutElementsHeader = "Layout Elements",
  numberRowTitle = "Top Number Row",
  numberRowSubtitle = "Dedicated 1–0 row above letters",
  keyHintsTitle = "Secondary Key Hints",
  keyHintsSubtitle = "Subtle corner hints for digits and alternate letters",
  keyPopupTitle = "Key Press Popup",
  keyPopupSubtitle = "Floating preview bubble above pressed keys",
  toolbarTitle = "Top Action Toolbar",
  toolbarSubtitle = "Quick access to emoji, clipboard, and symbols",
  hapticsSoundHeader = "Haptics & Sound",
  hapticEnabledTitle = "Haptic Feedback",
  hapticEnabledSubtitle = "Crisp tactile pulse on key press",
  hapticDurationTitle = "Vibration Duration",
  hapticTestButton = "Test Haptic Feedback",
  soundEnabledTitle = "Key Click Sound",
  soundEnabledSubtitle = "System key press sound effect",
  gesturesSmartHeader = "Gestures & Smart Typing",
  spaceCursorTitle = "Spacebar Cursor Control",
  spaceCursorSubtitle = "Swipe left or right across the spacebar to move cursor",
  autoCapTitle = "Auto-Capitalization",
  autoCapSubtitle = "Capitalize first letter of sentences and new lines",
  doubleSpaceTitle = "Double-Space Period",
  doubleSpaceSubtitle = "Tap space twice quickly to insert \". \"",
  longPressDelayTitle = "Long-Press Delay",
  longPressDelaySubtitle = "Hold duration for alternate chars and repeat delete",
  msUnit = "ms",
  resetDefaults = "Reset to Defaults",
  resetDialogTitle = "Reset all settings?",
  resetDialogBody = "Palette, geometry, and typing options will return to ToneKey defaults.",
  resetConfirm = "Reset",
  cancel = "Cancel",
  cursorLabel = "Cursor",
  moveLeft = "Move left",
  moveRight = "Move right",
  clipboardTitle = "Clipboard & Quick Phrases",
  backToLetters = "Back to letters",
  lettersKeyLabel = "ABC",
  cdShift = "Shift",
  cdBackspace = "Backspace",
  cdEnter = "Enter",
  cdLanguage = "Switch language",
  cdEmoji = "Emoji",
  cdClipboard = "Clipboard",
  cdOneHanded = "One-handed mode",
  cdSettings = "Keyboard settings",
  cdHeroBanner = "Abstract Material You floating keycaps illustration",
  tokenSurface = "Surface",
  smartClipboardHeader = "Smart Clipboard",
  smartClipboardSubtitle = "Auto-detects OTP codes, links, emails & phones • pin favorites",
  smartClipboardAddPlaceholder = "Add a quick phrase, link, or snippet…",
  smartClipboardAddButton = "Add",
  smartClipboardSyncButton = "Read Clip",
  smartClipboardEmptyTitle = "Clipboard history is empty",
  smartClipboardEmptySubtitle = "Copy a code, link, or snippet and it will appear here automatically",
  clipClearUnpinned = "Clear unpinned",
  clipPinAction = "Pin",
  clipUnpinAction = "Unpin",
  clipDeleteAction = "Delete item",
  clipPasteCodePrefix = "Paste ",
  clipCleanUrlAction = "Clean URL",
  clipQuickPhrasesTitle = "Quick Phrases",
  clipSmartPillCodePrefix = "Code: ",
  pcModeTitle = "PC Keys Mode (Fn / F1–F12 / Ctrl / Alt)",
  pcModeSubtitle = "Adds a one-tap PC keys toggle right on the keyboard toolbar",
  pcToolbarLabel = "PC",
  cdPcMode = "PC keys mode (Fn, F1–F12, Ctrl, Alt)",
)

val LocalAppStrings = staticCompositionLocalOf { RussianStrings }

fun isSystemLocaleRussian(configuration: Configuration? = null): Boolean {
  val locale: Locale =
    if (configuration != null && !configuration.locales.isEmpty) {
      configuration.locales[0]
    } else {
      Locale.getDefault()
    }
  return locale.language.equals("ru", ignoreCase = true)
}

fun resolveAppStrings(
  appLanguage: AppLanguage,
  configuration: Configuration? = null,
): AppStrings {
  val useRussian =
    when (appLanguage) {
      AppLanguage.RUSSIAN -> true
      AppLanguage.ENGLISH -> false
      AppLanguage.SYSTEM -> isSystemLocaleRussian(configuration)
    }
  return if (useRussian) RussianStrings else EnglishStrings
}

@Composable
fun rememberAppStrings(appLanguage: AppLanguage): AppStrings {
  val configuration = LocalConfiguration.current
  return resolveAppStrings(appLanguage, configuration)
}

fun resolveAppStrings(
  appLanguage: AppLanguage,
  context: Context,
): AppStrings = resolveAppStrings(appLanguage, context.resources.configuration)
