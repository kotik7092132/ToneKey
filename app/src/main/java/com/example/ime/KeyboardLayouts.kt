package com.example.ime

import com.example.model.KeyboardLanguage

enum class KeyboardPage {
  LETTERS,
  SYMBOLS_PRIMARY,
  SYMBOLS_SECONDARY,
}

enum class KeyboardPanel {
  BOARD,
  EMOJI,
  CLIPBOARD,
}

enum class ShiftState {
  OFF,
  SINGLE,
  CAPS_LOCK,
}

enum class KeyActionType {
  SHIFT,
  BACKSPACE,
  SYMBOLS,
  SYMBOLS_ALT,
  LETTERS,
  LANGUAGE,
  SPACE,
  ENTER,
  EMOJI,
}

sealed interface KeySpec {
  val weight: Float

  data class CharacterKey(
    val primary: String,
    val shifted: String = primary.uppercase(),
    val hint: String? = null,
    val longPressOptions: List<String> = emptyList(),
    override val weight: Float = 1f,
  ) : KeySpec

  data class ActionKey(
    val action: KeyActionType,
    val label: String? = null,
    override val weight: Float = 1f,
  ) : KeySpec
}

data class EmojiCategory(
  val id: String,
  val label: String,
  val items: List<String>,
)

object KeyboardLayouts {

  val numberRow: List<KeySpec> = listOf(
    KeySpec.CharacterKey("1", "1", longPressOptions = listOf("¹", "½", "⅓", "¼")),
    KeySpec.CharacterKey("2", "2", longPressOptions = listOf("²", "⅔")),
    KeySpec.CharacterKey("3", "3", longPressOptions = listOf("³", "¾")),
    KeySpec.CharacterKey("4", "4", longPressOptions = listOf("⁴")),
    KeySpec.CharacterKey("5", "5", longPressOptions = listOf("⁵", "‰")),
    KeySpec.CharacterKey("6", "6", longPressOptions = listOf("⁶")),
    KeySpec.CharacterKey("7", "7", longPressOptions = listOf("⁷")),
    KeySpec.CharacterKey("8", "8", longPressOptions = listOf("⁸", "∞")),
    KeySpec.CharacterKey("9", "9", longPressOptions = listOf("⁹")),
    KeySpec.CharacterKey("0", "0", longPressOptions = listOf("⁰", "°", "∅")),
  )

  private val russianRows: List<List<KeySpec>> = listOf(
    listOf(
      KeySpec.CharacterKey("й", "Й", "1", listOf("1")),
      KeySpec.CharacterKey("ц", "Ц", "2", listOf("2")),
      KeySpec.CharacterKey("у", "У", "3", listOf("3")),
      KeySpec.CharacterKey("к", "К", "4", listOf("4")),
      KeySpec.CharacterKey("е", "Е", "ё", listOf("ё", "Ё", "5")),
      KeySpec.CharacterKey("н", "Н", "6", listOf("6")),
      KeySpec.CharacterKey("г", "Г", "7", listOf("7")),
      KeySpec.CharacterKey("ш", "Ш", "8", listOf("8")),
      KeySpec.CharacterKey("щ", "Щ", "9", listOf("9")),
      KeySpec.CharacterKey("з", "З", "0", listOf("0")),
      KeySpec.CharacterKey("х", "Х", "[", listOf("[", "{")),
      KeySpec.CharacterKey("ъ", "Ъ", "]", listOf("]", "}")),
    ),
    listOf(
      KeySpec.CharacterKey("ф", "Ф", "@", listOf("@")),
      KeySpec.CharacterKey("ы", "Ы", "#", listOf("#", "№")),
      KeySpec.CharacterKey("в", "В", "₽", listOf("₽", "$")),
      KeySpec.CharacterKey("а", "А", "%", listOf("%")),
      KeySpec.CharacterKey("п", "П", "&", listOf("&")),
      KeySpec.CharacterKey("р", "Р", "-", listOf("-", "—")),
      KeySpec.CharacterKey("о", "О", "+", listOf("+", "=")),
      KeySpec.CharacterKey("л", "Л", "(", listOf("(")),
      KeySpec.CharacterKey("д", "Д", ")", listOf(")")),
      KeySpec.CharacterKey("ж", "Ж", ":", listOf(":", ";")),
      KeySpec.CharacterKey("э", "Э", "\"", listOf("\"", "«", "»")),
    ),
    listOf(
      KeySpec.ActionKey(KeyActionType.SHIFT, weight = 1.35f),
      KeySpec.CharacterKey("я", "Я", "!", listOf("!")),
      KeySpec.CharacterKey("ч", "Ч", "?", listOf("?")),
      KeySpec.CharacterKey("с", "С", "/", listOf("/")),
      KeySpec.CharacterKey("м", "М", "*", listOf("*")),
      KeySpec.CharacterKey("и", "И", "'", listOf("'")),
      KeySpec.CharacterKey("т", "Т", "_", listOf("_")),
      KeySpec.CharacterKey("ь", "Ь", "ъ", listOf("ъ", "Ъ")),
      KeySpec.CharacterKey("б", "Б", ",", listOf(",")),
      KeySpec.CharacterKey("ю", "Ю", ".", listOf(".")),
      KeySpec.ActionKey(KeyActionType.BACKSPACE, weight = 1.35f),
    ),
  )

  private val englishRows: List<List<KeySpec>> = listOf(
    listOf(
      KeySpec.CharacterKey("q", "Q", "1", listOf("1")),
      KeySpec.CharacterKey("w", "W", "2", listOf("2")),
      KeySpec.CharacterKey("e", "E", "3", listOf("3", "é", "è", "ê", "ë")),
      KeySpec.CharacterKey("r", "R", "4", listOf("4")),
      KeySpec.CharacterKey("t", "T", "5", listOf("5")),
      KeySpec.CharacterKey("y", "Y", "6", listOf("6")),
      KeySpec.CharacterKey("u", "U", "7", listOf("7", "ü", "ú", "ù")),
      KeySpec.CharacterKey("i", "I", "8", listOf("8", "í", "ï", "î")),
      KeySpec.CharacterKey("o", "O", "9", listOf("9", "ó", "ö", "ô")),
      KeySpec.CharacterKey("p", "P", "0", listOf("0")),
    ),
    listOf(
      KeySpec.CharacterKey("a", "A", "@", listOf("@", "á", "à", "ä", "â")),
      KeySpec.CharacterKey("s", "S", "#", listOf("#", "ß")),
      KeySpec.CharacterKey("d", "D", "$", listOf("$", "₽")),
      KeySpec.CharacterKey("f", "F", "_", listOf("_")),
      KeySpec.CharacterKey("g", "G", "&", listOf("&")),
      KeySpec.CharacterKey("h", "H", "-", listOf("-", "—")),
      KeySpec.CharacterKey("j", "J", "+", listOf("+")),
      KeySpec.CharacterKey("k", "K", "(", listOf("(")),
      KeySpec.CharacterKey("l", "L", ")", listOf(")")),
    ),
    listOf(
      KeySpec.ActionKey(KeyActionType.SHIFT, weight = 1.45f),
      KeySpec.CharacterKey("z", "Z", "*", listOf("*")),
      KeySpec.CharacterKey("x", "X", "\"", listOf("\"")),
      KeySpec.CharacterKey("c", "C", "'", listOf("'", "ç")),
      KeySpec.CharacterKey("v", "V", ":", listOf(":")),
      KeySpec.CharacterKey("b", "B", ";", listOf(";")),
      KeySpec.CharacterKey("n", "N", "!", listOf("!", "ñ")),
      KeySpec.CharacterKey("m", "M", "?", listOf("?")),
      KeySpec.ActionKey(KeyActionType.BACKSPACE, weight = 1.45f),
    ),
  )

  private val symbolsPrimaryRows: List<List<KeySpec>> = listOf(
    listOf(
      KeySpec.CharacterKey("1", "1"),
      KeySpec.CharacterKey("2", "2"),
      KeySpec.CharacterKey("3", "3"),
      KeySpec.CharacterKey("4", "4"),
      KeySpec.CharacterKey("5", "5"),
      KeySpec.CharacterKey("6", "6"),
      KeySpec.CharacterKey("7", "7"),
      KeySpec.CharacterKey("8", "8"),
      KeySpec.CharacterKey("9", "9"),
      KeySpec.CharacterKey("0", "0"),
    ),
    listOf(
      KeySpec.CharacterKey("@", "@"),
      KeySpec.CharacterKey("#", "#"),
      KeySpec.CharacterKey("₽", "₽", "$", listOf("$", "€", "£", "¥")),
      KeySpec.CharacterKey("_", "_"),
      KeySpec.CharacterKey("&", "&"),
      KeySpec.CharacterKey("-", "-", "—", listOf("—", "–")),
      KeySpec.CharacterKey("+", "+", "±", listOf("±")),
      KeySpec.CharacterKey("(", "("),
      KeySpec.CharacterKey(")", ")"),
      KeySpec.CharacterKey("/", "/"),
    ),
    listOf(
      KeySpec.ActionKey(KeyActionType.SYMBOLS_ALT, label = "=\\<", weight = 1.45f),
      KeySpec.CharacterKey("*", "*"),
      KeySpec.CharacterKey("\"", "\"", "«", listOf("«", "»", "„", "“")),
      KeySpec.CharacterKey("'", "'"),
      KeySpec.CharacterKey(":", ":"),
      KeySpec.CharacterKey(";", ";"),
      KeySpec.CharacterKey("!", "!"),
      KeySpec.CharacterKey("?", "?"),
      KeySpec.ActionKey(KeyActionType.BACKSPACE, weight = 1.45f),
    ),
  )

  private val symbolsSecondaryRows: List<List<KeySpec>> = listOf(
    listOf(
      KeySpec.CharacterKey("~", "~"),
      KeySpec.CharacterKey("`", "`"),
      KeySpec.CharacterKey("|", "|"),
      KeySpec.CharacterKey("•", "•"),
      KeySpec.CharacterKey("√", "√"),
      KeySpec.CharacterKey("π", "π"),
      KeySpec.CharacterKey("÷", "÷"),
      KeySpec.CharacterKey("×", "×"),
      KeySpec.CharacterKey("§", "§"),
      KeySpec.CharacterKey("Δ", "Δ"),
    ),
    listOf(
      KeySpec.CharacterKey("€", "€"),
      KeySpec.CharacterKey("$", "$"),
      KeySpec.CharacterKey("£", "£"),
      KeySpec.CharacterKey("¥", "¥"),
      KeySpec.CharacterKey("^", "^"),
      KeySpec.CharacterKey("°", "°"),
      KeySpec.CharacterKey("=", "=", "≈", listOf("≈", "≠")),
      KeySpec.CharacterKey("{", "{"),
      KeySpec.CharacterKey("}", "}"),
      KeySpec.CharacterKey("\\", "\\"),
    ),
    listOf(
      KeySpec.ActionKey(KeyActionType.SYMBOLS, label = "?123", weight = 1.45f),
      KeySpec.CharacterKey("%", "%"),
      KeySpec.CharacterKey("©", "©"),
      KeySpec.CharacterKey("®", "®"),
      KeySpec.CharacterKey("™", "™"),
      KeySpec.CharacterKey("✓", "✓"),
      KeySpec.CharacterKey("[", "["),
      KeySpec.CharacterKey("]", "]"),
      KeySpec.ActionKey(KeyActionType.BACKSPACE, weight = 1.45f),
    ),
  )

  fun rowsFor(
    language: KeyboardLanguage,
    page: KeyboardPage,
    includeNumberRow: Boolean,
  ): List<List<KeySpec>> {
    val body =
      when (page) {
        KeyboardPage.LETTERS ->
          if (language == KeyboardLanguage.RUSSIAN) russianRows else englishRows
        KeyboardPage.SYMBOLS_PRIMARY -> symbolsPrimaryRows
        KeyboardPage.SYMBOLS_SECONDARY -> symbolsSecondaryRows
      }

    val bottomRow =
      listOf(
        if (page == KeyboardPage.LETTERS) {
          KeySpec.ActionKey(KeyActionType.SYMBOLS, label = "?123", weight = 1.35f)
        } else {
          KeySpec.ActionKey(KeyActionType.LETTERS, label = "АБВ", weight = 1.35f)
        },
        KeySpec.CharacterKey(",", ",", weight = 0.9f),
        KeySpec.ActionKey(KeyActionType.LANGUAGE, weight = 0.95f),
        KeySpec.ActionKey(KeyActionType.SPACE, weight = 3.9f),
        KeySpec.CharacterKey(
          primary = ".",
          shifted = ".",
          longPressOptions = listOf(",", "?", "!", ":", ";", "…", "-", "—"),
          weight = 0.9f,
        ),
        KeySpec.ActionKey(KeyActionType.ENTER, weight = 1.45f),
      )

    val rows = mutableListOf<List<KeySpec>>()
    if (includeNumberRow && page == KeyboardPage.LETTERS) {
      rows.add(numberRow)
    }
    rows.addAll(body)
    rows.add(bottomRow)
    return rows
  }

  val emojiCategories: List<EmojiCategory> = listOf(
    EmojiCategory(
      id = "smileys",
      label = "Эмоции",
      items = listOf(
        "😀", "😄", "😁", "😂", "🤣", "😊", "😇", "🙂",
        "😉", "😍", "🥰", "😘", "😎", "🤩", "🥳", "🤔",
        "🤨", "😐", "😏", "😌", "😴", "😭", "😤", "🤯",
      ),
    ),
    EmojiCategory(
      id = "gestures",
      label = "Жесты",
      items = listOf(
        "👍", "👎", "👏", "🙌", "🤝", "🙏", "✌️", "🤞",
        "👌", "🤌", "💪", "👋", "🫶", "❤️", "🧡", "💛",
        "💚", "💙", "💜", "🖤", "🤍", "✨", "🔥", "⚡",
      ),
    ),
    EmojiCategory(
      id = "objects",
      label = "Знаки",
      items = listOf(
        "🚀", "🎉", "🎨", "🎧", "📱", "💻", "⌨️", "⚙️",
        "📌", "✅", "❌", "⚠️", "💡", "🔔", "📎", "📝",
        "☕", "🌿", "🌙", "☀️", "🌈", "⭐", "💎", "🏆",
      ),
    ),
    EmojiCategory(
      id = "kaomoji",
      label = "Каомодзи",
      items = listOf(
        "(◕‿◕)", "¯\\_(ツ)_/¯", "(╯°□°)╯︵ ┻━┻", "(づ｡◕‿‿◕｡)づ",
        "(¬‿¬)", "ʕ•ᴥ•ʔ", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧", "(╥﹏╥)",
        "ಠ_ಠ", "(•̀ᴗ•́)و", "♡(ŐωŐ人)", "(づ￣ ³￣)づ",
      ),
    ),
  )

  val quickTemplates: List<String> = listOf(
    "Привет! Как дела?",
    "Скоро буду на связи",
    "Спасибо большое!",
    "Отправляю детали чуть позже",
    "Договорились 👍",
  )
}
