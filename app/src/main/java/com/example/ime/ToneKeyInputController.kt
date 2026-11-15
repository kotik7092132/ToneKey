package com.example.ime

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings

interface InputSink {
  fun commitText(text: String)
  fun deleteBackward(count: Int = 1)
  fun deleteWordBackward()
  fun moveCursor(delta: Int)
  fun performEnter()
  fun getTextBeforeCursor(length: Int): String
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
}

class TextFieldSandboxSink(
  private val getValue: () -> TextFieldValue,
  private val setValue: (TextFieldValue) -> Unit,
) : InputSink {

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
