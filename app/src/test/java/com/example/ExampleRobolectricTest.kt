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
}
