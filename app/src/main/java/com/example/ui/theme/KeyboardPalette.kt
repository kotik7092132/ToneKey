package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardSettings
import com.example.model.PalettePreset
import com.example.model.ThemeMode

data class KeyboardColorTokens(
  val colorScheme: ColorScheme,
  val isDark: Boolean,
  val boardBackground: Color,
  val toolbarBackground: Color,
  val toolbarItemBackground: Color,
  val keyBackground: Color,
  val keyPressedBackground: Color,
  val keyContent: Color,
  val keyHintContent: Color,
  val modifierKeyBackground: Color,
  val modifierKeyPressedBackground: Color,
  val modifierKeyContent: Color,
  val accentKeyBackground: Color,
  val accentKeyPressedBackground: Color,
  val accentKeyContent: Color,
  val spacebarBackground: Color,
  val spacebarContent: Color,
  val keyBorder: Color,
  val popupBackground: Color,
  val popupContent: Color,
)

fun resolveColorScheme(
  context: Context,
  settings: KeyboardSettings,
  systemIsDark: Boolean,
): Pair<ColorScheme, Boolean> {
  val isDark =
    when (settings.themeMode) {
      ThemeMode.SYSTEM -> systemIsDark
      ThemeMode.LIGHT -> false
      ThemeMode.DARK,
      ThemeMode.AMOLED -> true
    }
  val isAmoled = settings.themeMode == ThemeMode.AMOLED

  val useDynamic =
    (settings.useDynamicColor || settings.palettePreset == PalettePreset.DYNAMIC) &&
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  val scheme =
    if (useDynamic) {
      val dyn = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      if (isAmoled) {
        dyn.copy(
          background = Color.Black,
          surface = Color.Black,
          surfaceContainerLowest = Color.Black,
          surfaceContainerLow = Color(0xFF0B0B0D),
          surfaceContainer = Color(0xFF131317),
          surfaceContainerHigh = Color(0xFF1B1B20),
          surfaceContainerHighest = Color(0xFF24242A),
        )
      } else {
        dyn
      }
    } else {
      if (isDark) {
        presetDarkColorScheme(settings.palettePreset, amoled = isAmoled)
      } else {
        presetLightColorScheme(settings.palettePreset)
      }
    }

  return scheme to isDark
}

fun buildKeyboardColorTokens(
  context: Context,
  settings: KeyboardSettings,
  systemIsDark: Boolean,
): KeyboardColorTokens {
  val (scheme, isDark) = resolveColorScheme(context, settings, systemIsDark)
  val isAmoled = settings.themeMode == ThemeMode.AMOLED

  val boardBg =
    when {
      isAmoled -> Color.Black
      isDark -> scheme.surfaceContainerLowest
      else -> scheme.surfaceContainer
    }

  val regularKeyBg =
    when (settings.keySurfaceStyle) {
      KeySurfaceStyle.OUTLINED -> Color.Transparent
      KeySurfaceStyle.SOFT_MONOLITH ->
        if (isDark) scheme.surfaceContainer else scheme.surfaceContainerLow
      KeySurfaceStyle.CLASSIC_M3,
      KeySurfaceStyle.EXPRESSIVE ->
        if (isDark) scheme.surfaceContainerHigh else scheme.surfaceContainerLowest
    }

  val regularKeyPressed =
    if (isDark) scheme.primaryContainer else scheme.primaryContainer.copy(alpha = 0.85f)

  val modifierBg =
    when (settings.keySurfaceStyle) {
      KeySurfaceStyle.OUTLINED -> scheme.secondaryContainer.copy(alpha = 0.25f)
      KeySurfaceStyle.EXPRESSIVE -> scheme.secondaryContainer
      KeySurfaceStyle.CLASSIC_M3 ->
        if (isDark) scheme.surfaceContainerHighest else scheme.surfaceContainerHigh
      KeySurfaceStyle.SOFT_MONOLITH ->
        if (isDark) scheme.surfaceContainerHigh else scheme.surfaceContainerHigh
    }

  val modifierContent =
    if (settings.keySurfaceStyle == KeySurfaceStyle.EXPRESSIVE) {
      scheme.onSecondaryContainer
    } else {
      scheme.onSurfaceVariant
    }

  val accentBg =
    when (settings.keySurfaceStyle) {
      KeySurfaceStyle.EXPRESSIVE -> scheme.primary
      KeySurfaceStyle.CLASSIC_M3 -> scheme.primaryContainer
      KeySurfaceStyle.OUTLINED -> scheme.primaryContainer.copy(alpha = 0.45f)
      KeySurfaceStyle.SOFT_MONOLITH -> scheme.primaryContainer
    }

  val accentContent =
    if (settings.keySurfaceStyle == KeySurfaceStyle.EXPRESSIVE) {
      scheme.onPrimary
    } else {
      scheme.onPrimaryContainer
    }

  val spaceBg =
    when (settings.keySurfaceStyle) {
      KeySurfaceStyle.OUTLINED -> Color.Transparent
      KeySurfaceStyle.EXPRESSIVE ->
        if (isDark) scheme.surfaceContainerHighest else scheme.surfaceContainerLowest
      KeySurfaceStyle.CLASSIC_M3,
      KeySurfaceStyle.SOFT_MONOLITH -> regularKeyBg
    }

  val keyBorder =
    when (settings.keySurfaceStyle) {
      KeySurfaceStyle.OUTLINED -> scheme.outline.copy(alpha = if (isDark) 0.55f else 0.45f)
      KeySurfaceStyle.EXPRESSIVE -> Color.Transparent
      KeySurfaceStyle.CLASSIC_M3 -> scheme.outlineVariant.copy(alpha = if (isDark) 0.2f else 0.45f)
      KeySurfaceStyle.SOFT_MONOLITH -> Color.Transparent
    }

  return KeyboardColorTokens(
    colorScheme = scheme,
    isDark = isDark,
    boardBackground = boardBg,
    toolbarBackground = if (isAmoled) Color(0xFF08080A) else boardBg,
    toolbarItemBackground =
      if (isDark) scheme.surfaceContainerHigh else scheme.surfaceContainerHigh.copy(alpha = 0.7f),
    keyBackground = regularKeyBg,
    keyPressedBackground = regularKeyPressed,
    keyContent = scheme.onSurface,
    keyHintContent = scheme.onSurfaceVariant.copy(alpha = 0.72f),
    modifierKeyBackground = modifierBg,
    modifierKeyPressedBackground = scheme.primaryContainer,
    modifierKeyContent = modifierContent,
    accentKeyBackground = accentBg,
    accentKeyPressedBackground = scheme.tertiary,
    accentKeyContent = accentContent,
    spacebarBackground = spaceBg,
    spacebarContent = scheme.onSurfaceVariant,
    keyBorder = keyBorder,
    popupBackground = scheme.primaryContainer,
    popupContent = scheme.onPrimaryContainer,
  )
}

@androidx.compose.runtime.Composable
fun rememberAnimatedKeyboardColorTokens(
  target: KeyboardColorTokens,
  animate: Boolean = true,
): KeyboardColorTokens {
  if (!animate) return target
  val spec = androidx.compose.animation.core.spring<Color>(
    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
  )
  val boardBackground = androidx.compose.animation.animateColorAsState(target.boardBackground, spec, label = "kb_board_bg").value
  val toolbarBackground = androidx.compose.animation.animateColorAsState(target.toolbarBackground, spec, label = "kb_tb_bg").value
  val toolbarItemBackground = androidx.compose.animation.animateColorAsState(target.toolbarItemBackground, spec, label = "kb_tb_item").value
  val keyBackground = androidx.compose.animation.animateColorAsState(target.keyBackground, spec, label = "kb_key_bg").value
  val keyPressedBackground = androidx.compose.animation.animateColorAsState(target.keyPressedBackground, spec, label = "kb_key_press").value
  val keyContent = androidx.compose.animation.animateColorAsState(target.keyContent, spec, label = "kb_key_fg").value
  val keyHintContent = androidx.compose.animation.animateColorAsState(target.keyHintContent, spec, label = "kb_hint_fg").value
  val modifierKeyBackground = androidx.compose.animation.animateColorAsState(target.modifierKeyBackground, spec, label = "kb_mod_bg").value
  val modifierKeyPressedBackground = androidx.compose.animation.animateColorAsState(target.modifierKeyPressedBackground, spec, label = "kb_mod_press").value
  val modifierKeyContent = androidx.compose.animation.animateColorAsState(target.modifierKeyContent, spec, label = "kb_mod_fg").value
  val accentKeyBackground = androidx.compose.animation.animateColorAsState(target.accentKeyBackground, spec, label = "kb_acc_bg").value
  val accentKeyPressedBackground = androidx.compose.animation.animateColorAsState(target.accentKeyPressedBackground, spec, label = "kb_acc_press").value
  val accentKeyContent = androidx.compose.animation.animateColorAsState(target.accentKeyContent, spec, label = "kb_acc_fg").value
  val spacebarBackground = androidx.compose.animation.animateColorAsState(target.spacebarBackground, spec, label = "kb_spc_bg").value
  val spacebarContent = androidx.compose.animation.animateColorAsState(target.spacebarContent, spec, label = "kb_spc_fg").value
  val keyBorder = androidx.compose.animation.animateColorAsState(target.keyBorder, spec, label = "kb_border").value
  val popupBackground = androidx.compose.animation.animateColorAsState(target.popupBackground, spec, label = "kb_pop_bg").value
  val popupContent = androidx.compose.animation.animateColorAsState(target.popupContent, spec, label = "kb_pop_fg").value

  return target.copy(
    boardBackground = boardBackground,
    toolbarBackground = toolbarBackground,
    toolbarItemBackground = toolbarItemBackground,
    keyBackground = keyBackground,
    keyPressedBackground = keyPressedBackground,
    keyContent = keyContent,
    keyHintContent = keyHintContent,
    modifierKeyBackground = modifierKeyBackground,
    modifierKeyPressedBackground = modifierKeyPressedBackground,
    modifierKeyContent = modifierKeyContent,
    accentKeyBackground = accentKeyBackground,
    accentKeyPressedBackground = accentKeyPressedBackground,
    accentKeyContent = accentKeyContent,
    spacebarBackground = spacebarBackground,
    spacebarContent = spacebarContent,
    keyBorder = keyBorder,
    popupBackground = popupBackground,
    popupContent = popupContent,
  )
}
