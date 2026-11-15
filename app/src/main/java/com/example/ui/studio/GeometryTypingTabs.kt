package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.Terminal
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
import androidx.compose.ui.unit.dp
import com.example.ime.KeyboardFeedbackHelper
import com.example.model.AppLanguage
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.i18n.LocalAppStrings
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
  val strings = LocalAppStrings.current
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SectionHeader(
        title = strings.geometryHeader,
        subtitle = strings.geometrySubtitle,
      )
    }

    item {
      SettingSliderRow(
        title = strings.heightTitle,
        subtitle = strings.heightSubtitle,
        valueText = "${(settings.keyboardHeightScale * 100).roundToInt()}%",
        value = settings.keyboardHeightScale,
        valueRange = 0.80f..1.25f,
        onValueChange = onSetHeightScale,
        modifier = Modifier.testTag("slider_height"),
      )
    }

    item {
      SettingSliderRow(
        title = strings.radiusTitle,
        subtitle = strings.radiusSubtitle,
        valueText = "${settings.keyCornerRadiusDp.roundToInt()} dp",
        value = settings.keyCornerRadiusDp,
        valueRange = 2f..24f,
        onValueChange = onSetCornerRadius,
        modifier = Modifier.testTag("slider_radius"),
      )
    }

    item {
      SettingSliderRow(
        title = strings.gapHorizontalTitle,
        valueText = "${settings.keyGapHorizontalDp.roundToInt()} dp",
        value = settings.keyGapHorizontalDp,
        valueRange = 1f..8f,
        onValueChange = onSetHorizontalGap,
        modifier = Modifier.testTag("slider_gap_h"),
      )
    }

    item {
      SettingSliderRow(
        title = strings.gapVerticalTitle,
        valueText = "${settings.keyGapVerticalDp.roundToInt()} dp",
        value = settings.keyGapVerticalDp,
        valueRange = 2f..10f,
        onValueChange = onSetVerticalGap,
        modifier = Modifier.testTag("slider_gap_v"),
      )
    }

    item {
      SettingSliderRow(
        title = strings.fontSizeTitle,
        valueText = "${settings.keyFontSizeSp.roundToInt()} sp",
        value = settings.keyFontSizeSp,
        valueRange = 14f..24f,
        onValueChange = onSetFontSize,
        modifier = Modifier.testTag("slider_font_size"),
      )
    }

    item {
      SettingSliderRow(
        title = strings.bottomPaddingTitle,
        subtitle = strings.bottomPaddingSubtitle,
        valueText = "${settings.bottomPaddingDp.roundToInt()} dp",
        value = settings.bottomPaddingDp,
        valueRange = 0f..28f,
        onValueChange = onSetBottomPadding,
        modifier = Modifier.testTag("slider_bottom_padding"),
      )
    }

    item {
      SettingSwitchRow(
        title = strings.keyShadowTitle,
        subtitle = strings.keyShadowSubtitle,
        checked = settings.showKeyElevationShadow,
        onCheckedChange = onSetKeyShadow,
        icon = Icons.Rounded.Layers,
      )
    }

    item {
      SectionHeader(
        title = strings.oneHandedHeader,
        subtitle = strings.oneHandedSubtitle,
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
            Text(strings.oneHandedLabel(mode))
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
  onSetAppLanguage: (AppLanguage) -> Unit,
  onSetDefaultLanguage: (KeyboardLanguage) -> Unit,
  onSetShowNumberRow: (Boolean) -> Unit,
  onSetShowKeyHints: (Boolean) -> Unit,
  onSetShowKeyPopup: (Boolean) -> Unit,
  onSetShowToolbar: (Boolean) -> Unit,
  onSetEnablePcMode: (Boolean) -> Unit,
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
  val strings = LocalAppStrings.current
  val context = LocalContext.current
  val feedbackHelper = remember(context) { KeyboardFeedbackHelper(context) }
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text(strings.resetDialogTitle) },
      text = { Text(strings.resetDialogBody) },
      confirmButton = {
        TextButton(
          onClick = {
            onResetDefaults()
            showResetDialog = false
          },
        ) {
          Text(strings.resetConfirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text(strings.cancel)
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
        title = strings.appLanguageHeader,
        subtitle = strings.appLanguageSubtitle,
      )
      Spacer(Modifier.height(6.dp))
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        AppLanguage.entries.forEachIndexed { index, lang ->
          SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = AppLanguage.entries.size,
            ),
            selected = settings.appLanguage == lang,
            onClick = { onSetAppLanguage(lang) },
            modifier = Modifier.testTag("app_lang_${lang.name.lowercase()}"),
          ) {
            Text(
              text = strings.appLanguageLabel(lang),
              maxLines = 1,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }

    item {
      SectionHeader(
        title = strings.defaultKeyboardLangHeader,
        subtitle = strings.defaultKeyboardLangSubtitle,
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
            Text(
              text = strings.keyboardLanguageLabel(lang),
              maxLines = 1,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }

    item {
      SectionHeader(
        title = strings.layoutElementsHeader,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.numberRowTitle,
        subtitle = strings.numberRowSubtitle,
        checked = settings.showNumberRow,
        onCheckedChange = onSetShowNumberRow,
        icon = Icons.Rounded.Dialpad,
        modifier = Modifier.testTag("switch_number_row"),
      )
    }

    item {
      SettingSwitchRow(
        title = strings.keyHintsTitle,
        subtitle = strings.keyHintsSubtitle,
        checked = settings.showKeyHints,
        onCheckedChange = onSetShowKeyHints,
        icon = Icons.Rounded.Info,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.keyPopupTitle,
        subtitle = strings.keyPopupSubtitle,
        checked = settings.showKeyPopup,
        onCheckedChange = onSetShowKeyPopup,
        icon = Icons.Rounded.Visibility,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.toolbarTitle,
        subtitle = strings.toolbarSubtitle,
        checked = settings.showToolbar,
        onCheckedChange = onSetShowToolbar,
        icon = Icons.Rounded.Build,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.pcModeTitle,
        subtitle = strings.pcModeSubtitle,
        checked = settings.enablePcMode,
        onCheckedChange = onSetEnablePcMode,
        icon = Icons.Rounded.Terminal,
        modifier = Modifier.testTag("switch_pc_mode"),
      )
    }

    item {
      SectionHeader(
        title = strings.hapticsSoundHeader,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.hapticEnabledTitle,
        subtitle = strings.hapticEnabledSubtitle,
        checked = settings.hapticEnabled,
        onCheckedChange = onSetHapticEnabled,
        icon = Icons.Rounded.Vibration,
      )
    }

    item {
      AnimatedVisibility(
        visible = settings.hapticEnabled,
        enter = expandVertically(
          animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 420f),
        ) + fadeIn(),
        exit = shrinkVertically(
          animationSpec = spring(stiffness = 460f),
        ) + fadeOut(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SettingSliderRow(
            title = strings.hapticDurationTitle,
            valueText = "${settings.hapticDurationMs} ${strings.msUnit}",
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
            Text(strings.hapticTestButton)
          }
        }
      }
    }

    item {
      SettingSwitchRow(
        title = strings.soundEnabledTitle,
        subtitle = strings.soundEnabledSubtitle,
        checked = settings.soundEnabled,
        onCheckedChange = onSetSoundEnabled,
        icon = Icons.Rounded.VolumeUp,
      )
    }

    item {
      SectionHeader(
        title = strings.gesturesSmartHeader,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.spaceCursorTitle,
        subtitle = strings.spaceCursorSubtitle,
        checked = settings.spaceCursorControl,
        onCheckedChange = onSetSpaceCursorControl,
        icon = Icons.Rounded.Swipe,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.autoCapTitle,
        subtitle = strings.autoCapSubtitle,
        checked = settings.autoCapitalize,
        onCheckedChange = onSetAutoCapitalize,
        icon = Icons.Rounded.AutoFixHigh,
      )
    }

    item {
      SettingSwitchRow(
        title = strings.doubleSpaceTitle,
        subtitle = strings.doubleSpaceSubtitle,
        checked = settings.doubleSpacePeriod,
        onCheckedChange = onSetDoubleSpacePeriod,
        icon = Icons.Rounded.SpaceBar,
      )
    }

    item {
      SettingSliderRow(
        title = strings.longPressDelayTitle,
        subtitle = strings.longPressDelaySubtitle,
        valueText = "${settings.longPressDelayMs} ${strings.msUnit}",
        value = settings.longPressDelayMs.toFloat(),
        valueRange = 200f..600f,
        onValueChange = { onSetLongPressDelay(it.roundToInt()) },
      )
    }

    item {
      FilledTonalButton(
        onClick = { showResetDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 50.dp)
          .testTag("reset_defaults_button"),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
          contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
      ) {
        Icon(
          imageVector = Icons.Rounded.RestartAlt,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(strings.resetDefaults)
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}
