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
}
