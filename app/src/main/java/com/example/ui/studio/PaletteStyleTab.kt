package com.example.ui.studio

import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyFontFamily
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardSettings
import com.example.model.PalettePreset
import com.example.model.ThemeMode
import com.example.ui.theme.swatchColors
import com.example.ui.theme.toComposeFontFamily

@Composable
fun PaletteStyleTab(
  settings: KeyboardSettings,
  onSetDynamicColor: (Boolean) -> Unit,
  onSelectPalette: (PalettePreset) -> Unit,
  onSetThemeMode: (ThemeMode) -> Unit,
  onSetSurfaceStyle: (KeySurfaceStyle) -> Unit,
  onSetFontFamily: (KeyFontFamily) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      SectionHeader(
        title = "Динамические цвета Material You",
        subtitle = "Адаптация тональной палитры под обои Android",
      )
      Spacer(Modifier.height(6.dp))
      SettingSwitchRow(
        title = "Системная палитра (Dynamic Color)",
        subtitle =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            "Автоматически извлекает оттенки темы вашего устройства"
          } else {
            "Используется встроенная тональная схема Material 3"
          },
        checked = settings.useDynamicColor,
        onCheckedChange = onSetDynamicColor,
        icon = Icons.Rounded.AutoAwesome,
        modifier = Modifier.testTag("switch_dynamic_color"),
      )
    }

    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TokenSwatchDot("Primary", MaterialTheme.colorScheme.primary)
          TokenSwatchDot("Container", MaterialTheme.colorScheme.primaryContainer)
          TokenSwatchDot("Secondary", MaterialTheme.colorScheme.secondary)
          TokenSwatchDot("Tertiary", MaterialTheme.colorScheme.tertiary)
        }
      }
    }

    item {
      SectionHeader(
        title = "Авторские палитры",
        subtitle = "Готовые гармоничные оттенки в духе Pixel",
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
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(18.dp))
                  .clickable { onSelectPalette(preset) }
                  .testTag("palette_${preset.name.lowercase()}"),
                shape = RoundedCornerShape(18.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                border =
                  if (selected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                  } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                  },
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(swatch.primary),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (selected) {
                      Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = preset.title,
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = preset.subtitle,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
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
        title = "Режим темы",
        subtitle = "Включая чистый чёрный для OLED-экранов",
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
              text = mode.label,
              maxLines = 1,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }

    item {
      SectionHeader(
        title = "Стиль поверхности клавиш",
        subtitle = "Характер контраста, контуров и акцентных кнопок",
      )
      Spacer(Modifier.height(6.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KeySurfaceStyle.entries.forEach { style ->
          val selected = settings.keySurfaceStyle == style
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .clickable { onSetSurfaceStyle(style) }
              .testTag("surface_style_${style.name.lowercase()}"),
            shape = RoundedCornerShape(18.dp),
            color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            border =
              if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                null
              },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = style.title,
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = style.description,
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
      SectionHeader(
        title = "Шрифт символов на клавишах",
        subtitle = "Локальные шрифты для максимальной чёткости",
      )
      Spacer(Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        KeyFontFamily.entries.forEach { font ->
          val selected = settings.keyFontFamily == font
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(18.dp))
              .clickable { onSetFontFamily(font) }
              .testTag("font_${font.name.lowercase()}"),
            shape = RoundedCornerShape(18.dp),
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
            border =
              if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
              },
          ) {
            Column(
              modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "Аб",
                fontFamily = font.toComposeFontFamily(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = font.title,
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
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun TokenSwatchDot(
  label: String,
  color: androidx.compose.ui.graphics.Color,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(color),
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
