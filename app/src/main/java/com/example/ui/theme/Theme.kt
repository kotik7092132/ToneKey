package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.KeyboardSettings

@Composable
fun ToneKeyTheme(
  settings: KeyboardSettings = KeyboardSettings(),
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val systemDark = isSystemInDarkTheme()
  val (targetScheme, _) = resolveColorScheme(context, settings, systemDark)
  val animatedScheme = rememberAnimatedColorScheme(targetScheme)

  MaterialTheme(
    colorScheme = animatedScheme,
    typography = Typography,
    content = content,
  )
}

@Composable
private fun rememberAnimatedColorScheme(target: ColorScheme): ColorScheme {
  val spec = spring<Color>(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioNoBouncy,
  )
  return target.copy(
    primary = animateColorAsState(target.primary, spec, label = "m3_primary").value,
    onPrimary = animateColorAsState(target.onPrimary, spec, label = "m3_onPrimary").value,
    primaryContainer = animateColorAsState(target.primaryContainer, spec, label = "m3_primaryContainer").value,
    onPrimaryContainer = animateColorAsState(target.onPrimaryContainer, spec, label = "m3_onPrimaryContainer").value,
    secondary = animateColorAsState(target.secondary, spec, label = "m3_secondary").value,
    onSecondary = animateColorAsState(target.onSecondary, spec, label = "m3_onSecondary").value,
    secondaryContainer = animateColorAsState(target.secondaryContainer, spec, label = "m3_secondaryContainer").value,
    onSecondaryContainer = animateColorAsState(target.onSecondaryContainer, spec, label = "m3_onSecondaryContainer").value,
    tertiary = animateColorAsState(target.tertiary, spec, label = "m3_tertiary").value,
    onTertiary = animateColorAsState(target.onTertiary, spec, label = "m3_onTertiary").value,
    tertiaryContainer = animateColorAsState(target.tertiaryContainer, spec, label = "m3_tertiaryContainer").value,
    onTertiaryContainer = animateColorAsState(target.onTertiaryContainer, spec, label = "m3_onTertiaryContainer").value,
    background = animateColorAsState(target.background, spec, label = "m3_background").value,
    onBackground = animateColorAsState(target.onBackground, spec, label = "m3_onBackground").value,
    surface = animateColorAsState(target.surface, spec, label = "m3_surface").value,
    onSurface = animateColorAsState(target.onSurface, spec, label = "m3_onSurface").value,
    surfaceVariant = animateColorAsState(target.surfaceVariant, spec, label = "m3_surfaceVariant").value,
    onSurfaceVariant = animateColorAsState(target.onSurfaceVariant, spec, label = "m3_onSurfaceVariant").value,
    surfaceContainerLowest = animateColorAsState(target.surfaceContainerLowest, spec, label = "m3_scLowest").value,
    surfaceContainerLow = animateColorAsState(target.surfaceContainerLow, spec, label = "m3_scLow").value,
    surfaceContainer = animateColorAsState(target.surfaceContainer, spec, label = "m3_sc").value,
    surfaceContainerHigh = animateColorAsState(target.surfaceContainerHigh, spec, label = "m3_scHigh").value,
    surfaceContainerHighest = animateColorAsState(target.surfaceContainerHighest, spec, label = "m3_scHighest").value,
    outline = animateColorAsState(target.outline, spec, label = "m3_outline").value,
    outlineVariant = animateColorAsState(target.outlineVariant, spec, label = "m3_outlineVariant").value,
  )
}
