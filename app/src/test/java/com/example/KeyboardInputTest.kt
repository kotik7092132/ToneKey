package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ime.KeyboardLayouts
import com.example.ime.KeyboardPage
import com.example.ime.ShiftState
import com.example.ime.TextFieldSandboxSink
import com.example.ime.ToneKeyInputProcessor
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardInputTest {

  @Test
  fun russianAndEnglishLayoutsHaveExpectedRows() {
    val ruWithNumbers = KeyboardLayouts.rowsFor(
      language = KeyboardLanguage.RUSSIAN,
      page = KeyboardPage.LETTERS,
      includeNumberRow = true,
    )
    assertEquals(5, ruWithNumbers.size)
    assertEquals(10, ruWithNumbers[0].size)
    assertEquals(12, ruWithNumbers[1].size)

    val enWithoutNumbers = KeyboardLayouts.rowsFor(
      language = KeyboardLanguage.ENGLISH,
      page = KeyboardPage.LETTERS,
      includeNumberRow = false,
    )
    assertEquals(4, enWithoutNumbers.size)
    assertEquals(10, enWithoutNumbers[0].size)
  }

  @Test
  fun doubleSpaceInsertsPeriodAndTriggersAutoCapitalize() {
    var field = TextFieldValue("Привет", TextRange(6))
    val sink = TextFieldSandboxSink(getValue = { field }, setValue = { field = it })
    val processor = ToneKeyInputProcessor(sink)
    val settings = KeyboardSettings(doubleSpacePeriod = true, autoCapitalize = true)

    val afterFirstSpace = processor.onSpacePressed(ShiftState.OFF, settings, nowMs = 1_000L)
    assertEquals("Привет ", field.text)
    assertEquals(ShiftState.OFF, afterFirstSpace)

    val afterSecondSpace = processor.onSpacePressed(afterFirstSpace, settings, nowMs = 1_200L)
    assertEquals("Привет. ", field.text)
    assertEquals(ShiftState.SINGLE, afterSecondSpace)
  }

  @Test
  fun deleteWordAndMoveCursorWorkCorrectly() {
    var field = TextFieldValue("Привет мир", TextRange(10))
    val sink = TextFieldSandboxSink(getValue = { field }, setValue = { field = it })

    sink.moveCursor(-4)
    assertEquals(6, field.selection.end)

    sink.moveCursor(4)
    sink.deleteWordBackward()
    assertEquals("Привет ", field.text)
    assertFalse(field.text.contains("мир"))
    assertTrue(field.text.startsWith("Привет"))
  }

  @Test
  fun asyncStaleInputConnectionDoesNotCorruptCapitalizationAfterFirstLetter() {
    // Simulates Jetpack Compose / Chrome / WebView InputConnection where getTextBeforeCursor()
    // still returns the old snapshot immediately after commitText() / deleteBackward()
    class LaggyAsyncInputSink(initialBefore: String) : com.example.ime.InputSink {
      var staleSnapshot: String = initialBefore
      var actualText: String = initialBefore
      override fun commitText(text: String) {
        actualText += text
        // Deliberately do NOT update staleSnapshot immediately to simulate IPC/recompose lag
      }
      override fun deleteBackward(count: Int) {
        actualText = actualText.dropLast(count)
      }
      override fun deleteWordBackward() {}
      override fun moveCursor(delta: Int) {}
      override fun performEnter() {
        actualText += "\n"
      }
      override fun getTextBeforeCursor(length: Int): String = staleSnapshot.takeLast(length)
    }

    val settings = KeyboardSettings(autoCapitalize = true, doubleSpacePeriod = true)

    // Case 1: Starting with existing "Привет. " where user manually turned Shift OFF to type lowercase
    val sinkAfterPeriod = LaggyAsyncInputSink("Привет. ")
    val processor1 = ToneKeyInputProcessor(sinkAfterPeriod)
    val afterFirstLower = processor1.onCharacterCommitted("п", ShiftState.OFF, settings)
    // Must stay OFF after 1st letter even though getTextBeforeCursor() still returned "Привет. "!
    assertEquals(ShiftState.OFF, afterFirstLower)

    // Case 2: Starting with existing "Привет." -> Space -> 1st letter
    val sinkNeedSpace = LaggyAsyncInputSink("Привет.")
    val processor2 = ToneKeyInputProcessor(sinkNeedSpace)
    val afterSpace = processor2.onSpacePressed(ShiftState.OFF, settings, nowMs = 1_000L)
    // Space immediately arms Shift SINGLE even before IC flushes
    assertEquals(ShiftState.SINGLE, afterSpace)
    sinkNeedSpace.staleSnapshot = "Привет. " // Laggy snapshot finally arrives right as user types 'П'
    val afterCapFirst = processor2.onCharacterCommitted("П", afterSpace, settings)
    // Must immediately drop to OFF so 2nd letter is lowercase!
    assertEquals(ShiftState.OFF, afterCapFirst)

    // Case 3: Empty field, typing 1st letter when getTextBeforeCursor still returns ""
    val sinkEmpty = LaggyAsyncInputSink("")
    val processor3 = ToneKeyInputProcessor(sinkEmpty)
    val afterFirstChar = processor3.onCharacterCommitted("П", ShiftState.SINGLE, settings)
    assertEquals(ShiftState.OFF, afterFirstChar)

    // Case 4: Backspace back to empty with stale snapshot still containing the deleted char
    sinkEmpty.staleSnapshot = "П"
    val afterBackspace = processor3.onBackspace(settings, ShiftState.OFF)
    assertEquals(ShiftState.SINGLE, afterBackspace)

    // Case 5: Password and email fields never auto-capitalize
    val passwordInputType =
      android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    assertFalse(processor3.shouldAutoCapitalizeForText("", settings, passwordInputType))
    val emailInputType =
      android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    assertFalse(processor3.shouldAutoCapitalizeForText("", settings, emailInputType))
  }
}
