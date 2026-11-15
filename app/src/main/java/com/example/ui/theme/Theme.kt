package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.model.KeyboardSettings

@Composable
fun ToneKeyTheme(
  settings: KeyboardSettings = KeyboardSettings(),
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val systemDark = isSystemInDarkTheme()
  val (colorScheme, _) = resolveColorScheme(context, settings, systemDark)

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}
