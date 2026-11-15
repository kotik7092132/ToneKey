package com.example.ime

import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.model.KeyboardSettings

interface InputSink {
  fun commitText(text: String)
  fun deleteBackward(count: Int = 1)
  fun deleteWordBackward()
  fun moveCursor(delta: Int)
  fun performEnter()
  fun getTextBeforeCursor(length: Int): String
  fun sendHardwareKey(keyCode: Int, metaState: Int = 0) {}
}

class InputConnectionSink(
  private val inputConnectionProvider: () -> InputConnection?,
  private val editorInfoProvider: () -> EditorInfo?,
) : InputSink {

  override fun commitText(text: String) {
    inputConnectionProvider()?.commitText(text, 1)
  }

  override fun deleteBackward(count: Int) {
    val ic = inputConnectionProvider() ?: return
    val selected = ic.getSelectedText(0)
    if (!selected.isNullOrEmpty()) {
      ic.commitText("", 1)
    } else {
      ic.deleteSurroundingText(count, 0)
    }
  }

  override fun deleteWordBackward() {
    val ic = inputConnectionProvider() ?: return
    val before = ic.getTextBeforeCursor(48, 0)?.toString().orEmpty()
    if (before.isEmpty()) return
    var idx = before.length
    while (idx > 0 && before[idx - 1].isWhitespace()) {
      idx--
    }
    while (idx > 0 && !before[idx - 1].isWhitespace()) {
      idx--
    }
    val deleteCount = (before.length - idx).coerceAtLeast(1)
    ic.deleteSurroundingText(deleteCount, 0)
  }

  override fun moveCursor(delta: Int) {
    val ic = inputConnectionProvider() ?: return
    val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
    repeat(kotlin.math.abs(delta)) {
      ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
      ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
  }

  override fun performEnter() {
    val ic = inputConnectionProvider() ?: return
    val info = editorInfoProvider()
    val imeAction = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
    val hasNoEnterAction = ((info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
    if (!hasNoEnterAction &&
      imeAction != EditorInfo.IME_ACTION_NONE &&
      imeAction != EditorInfo.IME_ACTION_UNSPECIFIED
    ) {
      ic.performEditorAction(imeAction)
    } else {
      ic.commitText("\n", 1)
    }
  }

  override fun getTextBeforeCursor(length: Int): String =
    inputConnectionProvider()?.getTextBeforeCursor(length, 0)?.toString().orEmpty()

  override fun sendHardwareKey(keyCode: Int, metaState: Int) {
    val ic = inputConnectionProvider() ?: return
    val now = SystemClock.uptimeMillis()
    ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
    ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState))
  }
}

class TextFieldSandboxSink(
  private val getValue: () -> TextFieldValue,
  private val setValue: (TextFieldValue) -> Unit,
) : InputSink {

  private var internalCopiedText: String = ""

  override fun commitText(text: String) {
    val current = getValue()
    val start = current.selection.min.coerceIn(0, current.text.length)
    val end = current.selection.max.coerceIn(0, current.text.length)
    val updatedText = buildString {
      append(current.text.substring(0, start))
      append(text)
      append(current.text.substring(end))
    }
    val cursor = start + text.length
    setValue(TextFieldValue(text = updatedText, selection = TextRange(cursor)))
  }

  override fun deleteBackward(count: Int) {
    val current = getValue()
    val start = current.selection.min.coerceIn(0, current.text.length)
    val end = current.selection.max.coerceIn(0, current.text.length)
    if (start != end) {
      val updatedText = current.text.removeRange(start, end)
      setValue(TextFieldValue(text = updatedText, selection = TextRange(start)))
      return
    }
    if (start <= 0) return
    val removeFrom = (start - count).coerceAtLeast(0)
    val updatedText = current.text.removeRange(removeFrom, start)
    setValue(TextFieldValue(text = updatedText, selection = TextRange(removeFrom)))
  }

  override fun deleteWordBackward() {
    val current = getValue()
    val start = current.selection.min.coerceIn(0, current.text.length)
    if (start <= 0) return
    var idx = start
    while (idx > 0 && current.text[idx - 1].isWhitespace()) {
      idx--
    }
    while (idx > 0 && !current.text[idx - 1].isWhitespace()) {
      idx--
    }
    val updatedText = current.text.removeRange(idx, start)
    setValue(TextFieldValue(text = updatedText, selection = TextRange(idx)))
  }

  override fun moveCursor(delta: Int) {
    val current = getValue()
    val newPos = (current.selection.end + delta).coerceIn(0, current.text.length)
    setValue(current.copy(selection = TextRange(newPos)))
  }

  override fun performEnter() {
    commitText("\n")
  }

  override fun getTextBeforeCursor(length: Int): String {
    val current = getValue()
    val end = current.selection.min.coerceIn(0, current.text.length)
    val from = (end - length).coerceAtLeast(0)
    return current.text.substring(from, end)
  }

  override fun sendHardwareKey(keyCode: Int, metaState: Int) {
    val hasCtrl = (metaState and KeyEvent.META_CTRL_ON) != 0
    val hasAlt = (metaState and KeyEvent.META_ALT_ON) != 0
    val current = getValue()
    if (hasCtrl && !hasAlt) {
      val start = current.selection.min.coerceIn(0, current.text.length)
      val end = current.selection.max.coerceIn(0, current.text.length)
      when (keyCode) {
        KeyEvent.KEYCODE_A -> {
          setValue(current.copy(selection = TextRange(0, current.text.length)))
          return
        }
        KeyEvent.KEYCODE_C -> {
          if (start != end) {
            internalCopiedText = current.text.substring(start, end)
          }
          return
        }
        KeyEvent.KEYCODE_X -> {
          if (start != end) {
            internalCopiedText = current.text.substring(start, end)
            val updated = current.text.removeRange(start, end)
            setValue(TextFieldValue(text = updated, selection = TextRange(start)))
          }
          return
        }
        KeyEvent.KEYCODE_V -> {
          if (internalCopiedText.isNotEmpty()) {
            commitText(internalCopiedText)
          }
          return
        }
      }
    }

    when (keyCode) {
      KeyEvent.KEYCODE_DPAD_LEFT -> moveCursor(-1)
      KeyEvent.KEYCODE_DPAD_RIGHT -> moveCursor(1)
      KeyEvent.KEYCODE_DPAD_UP,
      KeyEvent.KEYCODE_MOVE_HOME -> setValue(current.copy(selection = TextRange(0)))
      KeyEvent.KEYCODE_DPAD_DOWN,
      KeyEvent.KEYCODE_MOVE_END -> setValue(current.copy(selection = TextRange(current.text.length)))
      KeyEvent.KEYCODE_DEL -> deleteBackward(1)
      KeyEvent.KEYCODE_FORWARD_DEL -> {
        val start = current.selection.min.coerceIn(0, current.text.length)
        val end = current.selection.max.coerceIn(0, current.text.length)
        if (start != end) {
          setValue(TextFieldValue(current.text.removeRange(start, end), TextRange(start)))
        } else if (start < current.text.length) {
          setValue(TextFieldValue(current.text.removeRange(start, start + 1), TextRange(start)))
        }
      }
      KeyEvent.KEYCODE_TAB -> commitText("    ")
      KeyEvent.KEYCODE_ENTER -> performEnter()
      KeyEvent.KEYCODE_SPACE -> commitText(" ")
    }
  }
}

object PcKeyMapper {
  private val charToKeyCodeMap = mapOf(
    'a' to KeyEvent.KEYCODE_A, 'ф' to KeyEvent.KEYCODE_A,
    'b' to KeyEvent.KEYCODE_B, 'и' to KeyEvent.KEYCODE_B,
    'c' to KeyEvent.KEYCODE_C, 'с' to KeyEvent.KEYCODE_C,
    'd' to KeyEvent.KEYCODE_D, 'в' to KeyEvent.KEYCODE_D,
    'e' to KeyEvent.KEYCODE_E, 'у' to KeyEvent.KEYCODE_E,
    'f' to KeyEvent.KEYCODE_F, 'а' to KeyEvent.KEYCODE_F,
    'g' to KeyEvent.KEYCODE_G, 'п' to KeyEvent.KEYCODE_G,
    'h' to KeyEvent.KEYCODE_H, 'р' to KeyEvent.KEYCODE_H,
    'i' to KeyEvent.KEYCODE_I, 'ш' to KeyEvent.KEYCODE_I,
    'j' to KeyEvent.KEYCODE_J, 'о' to KeyEvent.KEYCODE_J,
    'k' to KeyEvent.KEYCODE_K, 'л' to KeyEvent.KEYCODE_K,
    'l' to KeyEvent.KEYCODE_L, 'д' to KeyEvent.KEYCODE_L,
    'm' to KeyEvent.KEYCODE_M, 'ь' to KeyEvent.KEYCODE_M,
    'n' to KeyEvent.KEYCODE_N, 'т' to KeyEvent.KEYCODE_N,
    'o' to KeyEvent.KEYCODE_O, 'щ' to KeyEvent.KEYCODE_O,
    'p' to KeyEvent.KEYCODE_P, 'з' to KeyEvent.KEYCODE_P,
    'q' to KeyEvent.KEYCODE_Q, 'й' to KeyEvent.KEYCODE_Q,
    'r' to KeyEvent.KEYCODE_R, 'к' to KeyEvent.KEYCODE_R,
    's' to KeyEvent.KEYCODE_S, 'ы' to KeyEvent.KEYCODE_S,
    't' to KeyEvent.KEYCODE_T, 'е' to KeyEvent.KEYCODE_T,
    'u' to KeyEvent.KEYCODE_U, 'г' to KeyEvent.KEYCODE_U,
    'v' to KeyEvent.KEYCODE_V, 'м' to KeyEvent.KEYCODE_V,
    'w' to KeyEvent.KEYCODE_W, 'ц' to KeyEvent.KEYCODE_W,
    'x' to KeyEvent.KEYCODE_X, 'ч' to KeyEvent.KEYCODE_X,
    'y' to KeyEvent.KEYCODE_Y, 'н' to KeyEvent.KEYCODE_Y,
    'z' to KeyEvent.KEYCODE_Z, 'я' to KeyEvent.KEYCODE_Z,
    '0' to KeyEvent.KEYCODE_0,
    '1' to KeyEvent.KEYCODE_1,
    '2' to KeyEvent.KEYCODE_2,
    '3' to KeyEvent.KEYCODE_3,
    '4' to KeyEvent.KEYCODE_4,
    '5' to KeyEvent.KEYCODE_5,
    '6' to KeyEvent.KEYCODE_6,
    '7' to KeyEvent.KEYCODE_7,
    '8' to KeyEvent.KEYCODE_8,
    '9' to KeyEvent.KEYCODE_9,
    '-' to KeyEvent.KEYCODE_MINUS,
    '=' to KeyEvent.KEYCODE_EQUALS,
    '[' to KeyEvent.KEYCODE_LEFT_BRACKET, 'х' to KeyEvent.KEYCODE_LEFT_BRACKET,
    ']' to KeyEvent.KEYCODE_RIGHT_BRACKET, 'ъ' to KeyEvent.KEYCODE_RIGHT_BRACKET,
    ';' to KeyEvent.KEYCODE_SEMICOLON, 'ж' to KeyEvent.KEYCODE_SEMICOLON,
    '\'' to KeyEvent.KEYCODE_APOSTROPHE, 'э' to KeyEvent.KEYCODE_APOSTROPHE,
    ',' to KeyEvent.KEYCODE_COMMA, 'б' to KeyEvent.KEYCODE_COMMA,
    '.' to KeyEvent.KEYCODE_PERIOD, 'ю' to KeyEvent.KEYCODE_PERIOD,
    '/' to KeyEvent.KEYCODE_SLASH,
    '\\' to KeyEvent.KEYCODE_BACKSLASH,
    '`' to KeyEvent.KEYCODE_GRAVE,
    ' ' to KeyEvent.KEYCODE_SPACE,
  )

  fun keyCodeForChar(text: String): Int? {
    val ch = text.firstOrNull()?.lowercaseChar() ?: return null
    return charToKeyCodeMap[ch]
  }
}

class ToneKeyInputProcessor(
  private val sink: InputSink,
) {
  private var lastSpaceTapAtMs: Long = 0L

  fun shouldAutoCapitalize(settings: KeyboardSettings): Boolean {
    if (!settings.autoCapitalize) return false
    val before = sink.getTextBeforeCursor(16)
    if (before.isEmpty()) return true
    val trimmed = before.trimEnd(' ')
    if (trimmed.isEmpty()) return true
    val lastChar = trimmed.last()
    val hasTrailingSpace = before.endsWith(" ") || before.endsWith("\n")
    return when {
      before.endsWith("\n") -> true
      hasTrailingSpace && (lastChar == '.' || lastChar == '!' || lastChar == '?') -> true
      else -> false
    }
  }

  fun onCharacterCommitted(
    text: String,
    shiftState: ShiftState,
    settings: KeyboardSettings,
  ): ShiftState {
    sink.commitText(text)
    lastSpaceTapAtMs = 0L
    return when (shiftState) {
      ShiftState.CAPS_LOCK -> ShiftState.CAPS_LOCK
      ShiftState.SINGLE ->
        if (shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
      ShiftState.OFF ->
        if (shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
    }
  }

  fun onSpacePressed(
    shiftState: ShiftState,
    settings: KeyboardSettings,
    nowMs: Long = System.currentTimeMillis(),
  ): ShiftState {
    val before = sink.getTextBeforeCursor(3)
    val isDoubleTap =
      settings.doubleSpacePeriod &&
        (nowMs - lastSpaceTapAtMs) in 1L..450L &&
        before.endsWith(" ") &&
        before.length >= 2 &&
        !before[before.length - 2].isWhitespace() &&
        before[before.length - 2] != '.'

    if (isDoubleTap) {
      sink.deleteBackward(1)
      sink.commitText(". ")
      lastSpaceTapAtMs = 0L
    } else {
      sink.commitText(" ")
      lastSpaceTapAtMs = nowMs
    }

    return when (shiftState) {
      ShiftState.CAPS_LOCK -> ShiftState.CAPS_LOCK
      else -> if (shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
    }
  }

  fun onBackspace(settings: KeyboardSettings): ShiftState {
    sink.deleteBackward(1)
    lastSpaceTapAtMs = 0L
    return if (shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
  }

  fun onEnter(settings: KeyboardSettings): ShiftState {
    sink.performEnter()
    lastSpaceTapAtMs = 0L
    return if (shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
  }
}
