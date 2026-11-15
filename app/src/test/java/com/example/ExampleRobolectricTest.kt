package com.example

import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeyboardPreferencesRepository
import com.example.ime.ToneKeyImeService
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardSettings
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
      useDynamicColor = false,
      palettePreset = PalettePreset.TERRACOTTA,
      themeMode = ThemeMode.AMOLED,
      keySurfaceStyle = KeySurfaceStyle.OUTLINED,
      keyCornerRadiusDp = 22f,
      hapticDurationMs = 25,
    )
    repo.saveSettings(custom)
    val loaded = repo.settingsFlow.first()
    assertEquals(PalettePreset.TERRACOTTA, loaded.palettePreset)
    assertEquals(ThemeMode.AMOLED, loaded.themeMode)
    assertEquals(KeySurfaceStyle.OUTLINED, loaded.keySurfaceStyle)
    assertEquals(22f, loaded.keyCornerRadiusDp)
    assertEquals(25, loaded.hapticDurationMs)

    repo.resetToDefaults()
    val reset = repo.settingsFlow.first()
    assertEquals(PalettePreset.DYNAMIC, reset.palettePreset)
    assertEquals(ThemeMode.SYSTEM, reset.themeMode)
  }
}
