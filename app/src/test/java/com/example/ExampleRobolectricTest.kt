package com.example

import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeyboardPreferencesRepository
import com.example.ime.ToneKeyImeService
import com.example.model.AnimationPreset
import com.example.model.AppLanguage
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardSettings
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import com.example.ui.i18n.resolveAppStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun readAppNameAndImeServiceManifest() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ToneKey", appName)

    val serviceInfo = context.packageManager.getServiceInfo(
      ComponentName(context, ToneKeyImeService::class.java),
      0,
    )
    assertNotNull(serviceInfo)
    assertEquals("android.permission.BIND_INPUT_METHOD", serviceInfo.permission)
  }

  @Test
  fun preferencesRepositorySavesAndResetsSettings() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = KeyboardPreferencesRepository(context)

    val custom = KeyboardSettings(
      appLanguage = AppLanguage.ENGLISH,
      useDynamicColor = false,
      palettePreset = PalettePreset.TERRACOTTA,
      themeMode = ThemeMode.AMOLED,
      keySurfaceStyle = KeySurfaceStyle.OUTLINED,
      keyAlpha = 0.42f,
      animationPreset = AnimationPreset.OPTIMIZED,
      keyCornerRadiusDp = 22f,
      hapticDurationMs = 25,
    )
    repo.saveSettings(custom)
    val loaded = repo.settingsFlow.first()
    assertEquals(AppLanguage.ENGLISH, loaded.appLanguage)
    assertEquals(PalettePreset.TERRACOTTA, loaded.palettePreset)
    assertEquals(ThemeMode.AMOLED, loaded.themeMode)
    assertEquals(KeySurfaceStyle.OUTLINED, loaded.keySurfaceStyle)
    assertEquals(0.42f, loaded.keyAlpha, 0.001f)
    assertEquals(AnimationPreset.OPTIMIZED, loaded.animationPreset)
    assertEquals(22f, loaded.keyCornerRadiusDp)
    assertEquals(25, loaded.hapticDurationMs)

    repo.resetToDefaults()
    val reset = repo.settingsFlow.first()
    assertEquals(AppLanguage.SYSTEM, reset.appLanguage)
    assertEquals(PalettePreset.DYNAMIC, reset.palettePreset)
    assertEquals(ThemeMode.SYSTEM, reset.themeMode)
    assertEquals(1.0f, reset.keyAlpha, 0.001f)
    assertEquals(AnimationPreset.HEAVY, reset.animationPreset)
  }

  @Test
  fun appStringsDetectsSystemLocaleAndRespectsManualOverride() {
    val ruConfig = Configuration().apply { setLocale(Locale.forLanguageTag("ru-RU")) }
    val enConfig = Configuration().apply { setLocale(Locale.US) }

    assertTrue(resolveAppStrings(AppLanguage.SYSTEM, ruConfig).isRussian)
    assertFalse(resolveAppStrings(AppLanguage.SYSTEM, enConfig).isRussian)

    assertFalse(resolveAppStrings(AppLanguage.ENGLISH, ruConfig).isRussian)
    assertEquals("Studio", resolveAppStrings(AppLanguage.ENGLISH, ruConfig).navStudio)

    assertTrue(resolveAppStrings(AppLanguage.RUSSIAN, enConfig).isRussian)
    assertEquals("Студия", resolveAppStrings(AppLanguage.RUSSIAN, enConfig).navStudio)
  }

  @Test
  fun smartClipboardAnalyzerDetectsOtpUrlsEmailsPhonesAndHexColors() {
    val (otpCategory, otpExtract) =
      com.example.model.SmartClipboardAnalyzer.analyze("Код подтверждения для входа: 482910. Никому не сообщайте.")
    assertEquals(com.example.model.ClipCategory.OTP, otpCategory)
    assertEquals("482910", otpExtract)

    val (urlCategory, cleanUrl) =
      com.example.model.SmartClipboardAnalyzer.analyze("https://example.com/shop/item?id=42&utm_source=tg&fbclid=abc123")
    assertEquals(com.example.model.ClipCategory.URL, urlCategory)
    assertEquals("https://example.com/shop/item?id=42", cleanUrl)

    val (emailCategory, emailExtract) =
      com.example.model.SmartClipboardAnalyzer.analyze("hello.tonekey@example.org")
    assertEquals(com.example.model.ClipCategory.EMAIL, emailCategory)
    assertEquals("hello.tonekey@example.org", emailExtract)

    val (phoneCategory, _) =
      com.example.model.SmartClipboardAnalyzer.analyze("+7 (999) 123-45-67")
    assertEquals(com.example.model.ClipCategory.PHONE, phoneCategory)

    val (hexCategory, hexExtract) =
      com.example.model.SmartClipboardAnalyzer.analyze("Accent #4c5fd5")
    assertEquals(com.example.model.ClipCategory.COLOR_HEX, hexCategory)
    assertEquals("#4C5FD5", hexExtract)
  }

  @Test
  fun clipboardRepositoryManagesPinsDeduplicationAndClearUnpinned() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.ClipboardHistoryRepository(context)

    val otpItem = repo.addOrUpdateClip("OTP code: 918273", pinned = false)
    val noteItem = repo.addOrUpdateClip("Привет из ToneKey", pinned = false)
    assertNotNull(otpItem)
    assertNotNull(noteItem)

    repo.togglePin(otpItem!!.id)
    val afterPin = repo.clipsFlow.first()
    assertTrue(afterPin.first().isPinned)
    assertEquals(otpItem.id, afterPin.first().id)

    repo.clearUnpinned()
    val afterClear = repo.clipsFlow.first()
    assertEquals(1, afterClear.size)
    assertEquals(otpItem.id, afterClear.first().id)

    repo.deleteClip(otpItem.id)
    assertTrue(repo.clipsFlow.first().isEmpty())
  }

  @Test
  fun pcModeSettingPersistsAndHardwareShortcutsWork() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = KeyboardPreferencesRepository(context)

    repo.updateSettings { it.copy(enablePcMode = true) }
    assertTrue(repo.settingsFlow.first().enablePcMode)

    repo.resetToDefaults()
    assertFalse(repo.settingsFlow.first().enablePcMode)

    assertEquals(android.view.KeyEvent.KEYCODE_C, com.example.ime.PcKeyMapper.keyCodeForChar("c"))
    assertEquals(android.view.KeyEvent.KEYCODE_C, com.example.ime.PcKeyMapper.keyCodeForChar("с"))
    assertEquals(android.view.KeyEvent.KEYCODE_V, com.example.ime.PcKeyMapper.keyCodeForChar("м"))

    var field = androidx.compose.ui.text.input.TextFieldValue(
      text = "Hello",
      selection = androidx.compose.ui.text.TextRange(5),
    )
    val sink = com.example.ime.TextFieldSandboxSink(
      getValue = { field },
      setValue = { field = it },
    )
    val ctrlMeta = android.view.KeyEvent.META_CTRL_ON
    sink.sendHardwareKey(android.view.KeyEvent.KEYCODE_A, ctrlMeta)
    assertEquals(androidx.compose.ui.text.TextRange(0, 5), field.selection)

    sink.sendHardwareKey(android.view.KeyEvent.KEYCODE_C, ctrlMeta)
    sink.sendHardwareKey(android.view.KeyEvent.KEYCODE_MOVE_END)
    sink.sendHardwareKey(android.view.KeyEvent.KEYCODE_V, ctrlMeta)
    assertEquals("HelloHello", field.text)
  }
}
