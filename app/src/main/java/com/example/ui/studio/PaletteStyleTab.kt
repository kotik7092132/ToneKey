package com.example.ui.studio

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LensBlur
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationPreset
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardSettings
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.swatchColors
import com.example.ui.theme.toComposeFontFamily
import kotlin.math.roundToInt

@Composable
fun PaletteStyleTab(
  settings: KeyboardSettings,
  onSetDynamicColor: (Boolean) -> Unit,
  onSelectPalette: (PalettePreset) -> Unit,
  onSetThemeMode: (ThemeMode) -> Unit,
  onSetSurfaceStyle: (KeySurfaceStyle) -> Unit,
  onSetKeyAlpha: (Float) -> Unit,
  onSetFontFamily: (KeyFontFamily) -> Unit,
  onSetAnimationPreset: (AnimationPreset) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalAppStrings.current
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      SectionHeader(
        title = strings.dynamicColorHeader,
        subtitle = strings.dynamicColorSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      SettingSwitchRow(
        title = strings.dynamicColorTitle,
        subtitle =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            strings.dynamicColorDescApi31
          } else {
            strings.dynamicColorDescLegacy
          },
        checked = settings.useDynamicColor,
        onCheckedChange = onSetDynamicColor,
        icon = Icons.Rounded.AutoAwesome,
        modifier = Modifier.testTag("switch_dynamic_color"),
      )
    }

    item {
      // 5-token Material You tonal spectrum strip
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TokenSwatchPill(
            label = strings.tokenPrimary,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          TokenSwatchPill(
            label = strings.tokenContainer,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f),
          )
          TokenSwatchPill(
            label = strings.tokenSecondary,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
          )
          TokenSwatchPill(
            label = strings.tokenTertiary,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
          )
          TokenSwatchPill(
            label = strings.tokenSurface,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    item {
      SectionHeader(
        title = strings.palettesHeader,
        subtitle = strings.palettesSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PalettePreset.entries.chunked(2).forEach { rowPresets ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            rowPresets.forEach { preset ->
              val selected = settings.palettePreset == preset
              val swatch = preset.swatchColors()
              val cardBg by animateColorAsState(
                targetValue =
                  if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                  },
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "pal_bg_${preset.name}",
              )
              val cardScale by animateFloatAsState(
                targetValue = if (selected) 1.015f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
                label = "pal_scale_${preset.name}",
              )

              Surface(
                modifier = Modifier
                  .weight(1f)
                  .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                  }
                  .clip(RoundedCornerShape(22.dp))
                  .clickable { onSelectPalette(preset) }
                  .testTag("palette_${preset.name.lowercase()}"),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border =
                  if (selected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                  } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                  },
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    // Overlapping 3-color tonal pill cluster
                    Box(modifier = Modifier.width(54.dp).height(26.dp)) {
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(swatch.primary),
                      )
                      Box(
                        modifier = Modifier
                          .offset(x = 14.dp)
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(swatch.secondary),
                      )
                      Box(
                        modifier = Modifier
                          .offset(x = 28.dp)
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(swatch.tertiary),
                      )
                    }
                    AnimatedVisibility(
                      visible = selected,
                      enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                      exit = scaleOut() + fadeOut(),
                    ) {
                      Box(
                        modifier = Modifier
                          .size(22.dp)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                          imageVector = Icons.Rounded.Check,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.size(14.dp),
                        )
                      }
                    }
                  }
                  Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                      text = strings.paletteTitle(preset),
                      style = MaterialTheme.typography.titleSmall,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text = strings.paletteSubtitle(preset),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }
              }
            }
            if (rowPresets.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }

    item {
      SectionHeader(
        title = strings.themeModeHeader,
        subtitle = strings.themeModeSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
      ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = ThemeMode.entries.size,
            ),
            selected = settings.themeMode == mode,
            onClick = { onSetThemeMode(mode) },
            modifier = Modifier.testTag("theme_${mode.name.lowercase()}"),
          ) {
            Text(
              text = strings.themeModeLabel(mode),
              maxLines = 1,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }

    item {
      SectionHeader(
        title = strings.surfaceStyleHeader,
        subtitle = strings.surfaceStyleSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        KeySurfaceStyle.entries.forEach { style ->
          val selected = settings.keySurfaceStyle == style
          val styleBg by animateColorAsState(
            targetValue =
              if (selected) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
              } else {
                MaterialTheme.colorScheme.surfaceContainerLow
              },
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "style_bg_${style.name}",
          )
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(22.dp))
              .clickable { onSetSurfaceStyle(style) }
              .testTag("surface_style_${style.name.lowercase()}"),
            shape = RoundedCornerShape(22.dp),
            color = styleBg,
            border =
              if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
              },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = strings.surfaceStyleTitle(style),
                    style = MaterialTheme.typography.titleMedium,
                  )
                  if (selected) {
                    Icon(
                      imageVector = Icons.Rounded.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
                Text(
                  text = strings.surfaceStyleDescription(style),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              MiniSurfaceKeyPreview(
                style = style,
                cornerRadiusDp = settings.keyCornerRadiusDp,
                keyAlpha = settings.keyAlpha,
                fontFamily = settings.keyFontFamily,
              )
            }
          }
        }
      }
    }

    item {
      SettingSliderRow(
        title = strings.keyOpacityTitle,
        subtitle = strings.keyOpacitySubtitle,
        valueText = "${(settings.keyAlpha * 100).roundToInt()}%",
        value = settings.keyAlpha,
        valueRange = 0.15f..1.0f,
        onValueChange = onSetKeyAlpha,
        modifier = Modifier.testTag("slider_key_alpha"),
      )
    }

    item {
      SectionHeader(
        title = strings.keyFontHeader,
        subtitle = strings.keyFontSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        KeyFontFamily.entries.forEach { font ->
          val selected = settings.keyFontFamily == font
          val fontBg by animateColorAsState(
            targetValue =
              if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
              } else {
                MaterialTheme.colorScheme.surfaceContainerLow
              },
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "font_bg_${font.name}",
          )
          val fontScale by animateFloatAsState(
            targetValue = if (selected) 1.03f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
            label = "font_scale_${font.name}",
          )
          Surface(
            modifier = Modifier
              .weight(1f)
              .graphicsLayer {
                scaleX = fontScale
                scaleY = fontScale
              }
              .clip(RoundedCornerShape(22.dp))
              .clickable { onSetFontFamily(font) }
              .testTag("font_${font.name.lowercase()}"),
            shape = RoundedCornerShape(22.dp),
            color = fontBg,
            border =
              if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
              },
          ) {
            Column(
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = strings.keyFontSample,
                fontFamily = font.toComposeFontFamily(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = strings.fontFamilyTitle(font),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
              )
            }
          }
        }
      }
    }

    item {
      SectionHeader(
        title = strings.animPresetHeader,
        subtitle = strings.animPresetSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimationPreset.entries.forEach { preset ->
          val selected = settings.animationPreset == preset
          val isHeavy = settings.animationPreset == AnimationPreset.HEAVY
          val cardBg =
            if (isHeavy) {
              animateColorAsState(
                targetValue =
                  if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                  },
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "anim_bg_${preset.name}",
              ).value
            } else {
              if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
              } else {
                MaterialTheme.colorScheme.surfaceContainerLow
              }
            }
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(22.dp))
              .clickable { onSetAnimationPreset(preset) }
              .testTag("anim_preset_${preset.name.lowercase()}"),
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border =
              if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
              },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector =
                    if (preset == AnimationPreset.OPTIMIZED) Icons.Rounded.Bolt else Icons.Rounded.LensBlur,
                  contentDescription = null,
                  tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
              }
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = strings.animationPresetTitle(preset),
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = strings.animationPresetDescription(preset),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (selected) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun TokenSwatchPill(
  label: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(22.dp),
      shape = RoundedCornerShape(11.dp),
      color = color,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
    ) {}
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun MiniSurfaceKeyPreview(
  style: KeySurfaceStyle,
  cornerRadiusDp: Float,
  keyAlpha: Float,
  fontFamily: KeyFontFamily,
) {
  val shapeRadius = cornerRadiusDp.coerceIn(6f, 16f).dp
  val charKeyBg =
    when (style) {
      KeySurfaceStyle.EXPRESSIVE -> MaterialTheme.colorScheme.surfaceContainerLowest
      KeySurfaceStyle.CLASSIC_M3 -> MaterialTheme.colorScheme.surfaceContainerHigh
      KeySurfaceStyle.OUTLINED -> Color.Transparent
      KeySurfaceStyle.SOFT_MONOLITH -> MaterialTheme.colorScheme.surfaceContainer
    }.let { if (it == Color.Transparent) it else it.copy(alpha = keyAlpha) }

  val accentKeyBg =
    when (style) {
      KeySurfaceStyle.EXPRESSIVE -> MaterialTheme.colorScheme.primary
      KeySurfaceStyle.CLASSIC_M3 -> MaterialTheme.colorScheme.primaryContainer
      KeySurfaceStyle.OUTLINED -> Color.Transparent
      KeySurfaceStyle.SOFT_MONOLITH -> MaterialTheme.colorScheme.secondaryContainer
    }.let { if (it == Color.Transparent) it else it.copy(alpha = keyAlpha) }

  val accentContentColor =
    when (style) {
      KeySurfaceStyle.EXPRESSIVE -> MaterialTheme.colorScheme.onPrimary
      KeySurfaceStyle.CLASSIC_M3 -> MaterialTheme.colorScheme.onPrimaryContainer
      KeySurfaceStyle.OUTLINED -> MaterialTheme.colorScheme.primary
      KeySurfaceStyle.SOFT_MONOLITH -> MaterialTheme.colorScheme.onSecondaryContainer
    }

  val charBorder =
    if (style == KeySurfaceStyle.OUTLINED) {
      BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = keyAlpha.coerceAtLeast(0.35f)))
    } else {
      BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    }

  val accentBorder =
    if (style == KeySurfaceStyle.OUTLINED) {
      BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = keyAlpha.coerceAtLeast(0.45f)))
    } else {
      null
    }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
  ) {
    Row(
      modifier = Modifier.padding(5.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        modifier = Modifier.size(width = 30.dp, height = 30.dp),
        shape = RoundedCornerShape(shapeRadius),
        color = charKeyBg,
        border = charBorder,
        shadowElevation = if (style == KeySurfaceStyle.CLASSIC_M3) 1.dp else 0.dp,
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "A",
            fontFamily = fontFamily.toComposeFontFamily(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
      Surface(
        modifier = Modifier.size(width = 36.dp, height = 30.dp),
        shape = RoundedCornerShape(shapeRadius),
        color = accentKeyBg,
        border = accentBorder,
        shadowElevation = if (style == KeySurfaceStyle.EXPRESSIVE) 2.dp else 0.dp,
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardReturn,
            contentDescription = null,
            tint = accentContentColor,
            modifier = Modifier.size(15.dp),
          )
        }
      }
    }
  }
}
