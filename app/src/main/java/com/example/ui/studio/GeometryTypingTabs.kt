package com.example.ui.studio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ime.KeyboardFeedbackHelper
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import kotlin.math.roundToInt

@Composable
fun GeometryTab(
  settings: KeyboardSettings,
  onSetHeightScale: (Float) -> Unit,
  onSetCornerRadius: (Float) -> Unit,
  onSetHorizontalGap: (Float) -> Unit,
  onSetVerticalGap: (Float) -> Unit,
  onSetFontSize: (Float) -> Unit,
  onSetBottomPadding: (Float) -> Unit,
  onSetOneHandedMode: (OneHandedMode) -> Unit,
  onSetKeyShadow: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SectionHeader(
        title = "Размеры и пропорции",
        subtitle = "Настройте габариты клавиатуры под размер ладони",
      )
    }

    item {
      SettingSliderRow(
        title = "Высота клавиатуры",
        subtitle = "Масштаб рядов от компактного до высокого",
        valueText = "${(settings.keyboardHeightScale * 100).roundToInt()}%",
        value = settings.keyboardHeightScale,
        valueRange = 0.80f..1.25f,
        onValueChange = onSetHeightScale,
        modifier = Modifier.testTag("slider_height"),
      )
    }

    item {
      SettingSliderRow(
        title = "Скругление клавиш",
        subtitle = "От строгих прямоугольников до мягких пилюль",
        valueText = "${settings.keyCornerRadiusDp.roundToInt()} dp",
        value = settings.keyCornerRadiusDp,
        valueRange = 2f..24f,
        onValueChange = onSetCornerRadius,
        modifier = Modifier.testTag("slider_radius"),
      )
    }

    item {
      SettingSliderRow(
        title = "Горизонтальный зазор",
        valueText = "${settings.keyGapHorizontalDp.roundToInt()} dp",
        value = settings.keyGapHorizontalDp,
        valueRange = 1f..8f,
        onValueChange = onSetHorizontalGap,
        modifier = Modifier.testTag("slider_gap_h"),
      )
    }

    item {
      SettingSliderRow(
        title = "Вертикальный зазор",
        valueText = "${settings.keyGapVerticalDp.roundToInt()} dp",
        value = settings.keyGapVerticalDp,
        valueRange = 2f..10f,
        onValueChange = onSetVerticalGap,
        modifier = Modifier.testTag("slider_gap_v"),
      )
    }

    item {
      SettingSliderRow(
        title = "Размер букв на клавишах",
        valueText = "${settings.keyFontSizeSp.roundToInt()} sp",
        value = settings.keyFontSizeSp,
        valueRange = 14f..24f,
        onValueChange = onSetFontSize,
        modifier = Modifier.testTag("slider_font_size"),
      )
    }

    item {
      SettingSliderRow(
        title = "Нижний отступ («подбородок»)",
        subtitle = "Приподнимает клавиши выше от системной панели жестов",
        valueText = "${settings.bottomPaddingDp.roundToInt()} dp",
        value = settings.bottomPaddingDp,
        valueRange = 0f..28f,
        onValueChange = onSetBottomPadding,
        modifier = Modifier.testTag("slider_bottom_padding"),
      )
    }

    item {
      SettingSwitchRow(
        title = "Мягкая тень клавиш",
        subtitle = "Лёгкий физический объём в стилях «Классика» и «Выразительный»",
        checked = settings.showKeyElevationShadow,
        onCheckedChange = onSetKeyShadow,
        icon = Icons.Rounded.Layers,
      )
    }

    item {
      SectionHeader(
        title = "Режим одной руки",
        subtitle = "Сдвигает раскладку к удобному краю экрана",
      )
      Spacer(Modifier.height(6.dp))
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        OneHandedMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = OneHandedMode.entries.size,
            ),
            selected = settings.oneHandedMode == mode,
            onClick = { onSetOneHandedMode(mode) },
            modifier = Modifier.testTag("one_handed_${mode.name.lowercase()}"),
          ) {
            Text(mode.title)
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
fun TypingHapticsTab(
  settings: KeyboardSettings,
  onSetDefaultLanguage: (KeyboardLanguage) -> Unit,
  onSetShowNumberRow: (Boolean) -> Unit,
  onSetShowKeyHints: (Boolean) -> Unit,
  onSetShowKeyPopup: (Boolean) -> Unit,
  onSetShowToolbar: (Boolean) -> Unit,
  onSetHapticEnabled: (Boolean) -> Unit,
  onSetHapticDuration: (Int) -> Unit,
  onSetSoundEnabled: (Boolean) -> Unit,
  onSetSpaceCursorControl: (Boolean) -> Unit,
  onSetAutoCapitalize: (Boolean) -> Unit,
  onSetDoubleSpacePeriod: (Boolean) -> Unit,
  onSetLongPressDelay: (Int) -> Unit,
  onResetDefaults: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val feedbackHelper = remember(context) { KeyboardFeedbackHelper(context) }
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text("Сбросить настройки?") },
      text = {
        Text("Палитра, геометрия и параметры ввода вернутся к исходным значениям ToneKey.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onResetDefaults()
            showResetDialog = false
          },
        ) {
          Text("Сбросить")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text("Отмена")
        }
      },
    )
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SectionHeader(
        title = "Язык по умолчанию",
        subtitle = "Язык при открытии клавиатуры",
      )
      Spacer(Modifier.height(6.dp))
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        KeyboardLanguage.entries.forEachIndexed { index, lang ->
          SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = KeyboardLanguage.entries.size,
            ),
            selected = settings.defaultLanguage == lang,
            onClick = { onSetDefaultLanguage(lang) },
          ) {
            Text("${lang.title} (${lang.layoutLabel})")
          }
        }
      }
    }

    item {
      SectionHeader(
        title = "Элементы раскладки",
      )
    }

    item {
      SettingSwitchRow(
        title = "Цифровой ряд сверху",
        subtitle = "Постоянный ряд 1–0 над буквами",
        checked = settings.showNumberRow,
        onCheckedChange = onSetShowNumberRow,
        icon = Icons.Rounded.Dialpad,
        modifier = Modifier.testTag("switch_number_row"),
      )
    }

    item {
      SettingSwitchRow(
        title = "Подсказки доп. символов",
        subtitle = "Миниатюрные символы в углу клавиш (например, ё и цифры)",
        checked = settings.showKeyHints,
        onCheckedChange = onSetShowKeyHints,
        icon = Icons.Rounded.Info,
      )
    }

    item {
      SettingSwitchRow(
        title = "Всплывающая клавиша при нажатии",
        subtitle = "Увеличенный предпросмотр буквы над пальцем",
        checked = settings.showKeyPopup,
        onCheckedChange = onSetShowKeyPopup,
        icon = Icons.Rounded.Visibility,
      )
    }

    item {
      SettingSwitchRow(
        title = "Верхняя панель инструментов",
        subtitle = "Быстрый доступ к эмодзи, буферу обмена и символам",
        checked = settings.showToolbar,
        onCheckedChange = onSetShowToolbar,
        icon = Icons.Rounded.Build,
      )
    }

    item {
      SectionHeader(
        title = "Тактильность и звук",
      )
    }

    item {
      SettingSwitchRow(
        title = "Виброотклик при печати",
        subtitle = "Короткий чёткий импульс при касании клавиш",
        checked = settings.hapticEnabled,
        onCheckedChange = onSetHapticEnabled,
        icon = Icons.Rounded.Vibration,
      )
    }

    if (settings.hapticEnabled) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SettingSliderRow(
            title = "Длительность виброотклика",
            valueText = "${settings.hapticDurationMs} мс",
            value = settings.hapticDurationMs.toFloat(),
            valueRange = 5f..40f,
            onValueChange = { onSetHapticDuration(it.roundToInt()) },
          )
          FilledTonalButton(
            onClick = { feedbackHelper.triggerKeyFeedback(settings) },
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 48.dp),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Vibration,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Проверить тактильный отклик")
          }
        }
      }
    }

    item {
      SettingSwitchRow(
        title = "Звук нажатия клавиш",
        subtitle = "Системный щелчок при каждом нажатии",
        checked = settings.soundEnabled,
        onCheckedChange = onSetSoundEnabled,
        icon = Icons.Rounded.VolumeUp,
      )
    }

    item {
      SectionHeader(
        title = "Жесты и умный ввод",
      )
    }

    item {
      SettingSwitchRow(
        title = "Курсор свайпом по пробелу",
        subtitle = "Проведите влево или вправо по пробелу для перемещения курсора",
        checked = settings.spaceCursorControl,
        onCheckedChange = onSetSpaceCursorControl,
        icon = Icons.Rounded.Swipe,
      )
    }

    item {
      SettingSwitchRow(
        title = "Автозаглавная буква",
        subtitle = "Заглавная буква в начале предложения и после переноса строки",
        checked = settings.autoCapitalize,
        onCheckedChange = onSetAutoCapitalize,
        icon = Icons.Rounded.AutoFixHigh,
      )
    }

    item {
      SettingSwitchRow(
        title = "Точка по двойному пробелу",
        subtitle = "Быстрый двойной пробел ставит «. » в конце слова",
        checked = settings.doubleSpacePeriod,
        onCheckedChange = onSetDoubleSpacePeriod,
        icon = Icons.Rounded.SpaceBar,
      )
    }

    item {
      SettingSliderRow(
        title = "Задержка долгого нажатия",
        subtitle = "Время удержания для вызова буквы «ё», цифр и повтора удаления",
        valueText = "${settings.longPressDelayMs} мс",
        value = settings.longPressDelayMs.toFloat(),
        valueRange = 200f..600f,
        onValueChange = { onSetLongPressDelay(it.roundToInt()) },
      )
    }

    item {
      Spacer(Modifier.height(4.dp))
      OutlinedButton(
        onClick = { showResetDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 48.dp)
          .testTag("reset_defaults_button"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.error,
        ),
      ) {
        Icon(
          imageVector = Icons.Rounded.RestartAlt,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.reset_defaults))
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}
