package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ime.InputSink
import com.example.ime.ToneKeyKeyboardView
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
    if (subtitle != null) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun ImeStatusCard(
  status: ImeActivationStatus,
  onOpenImeSettings: () -> Unit,
  onOpenImePicker: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerColor =
    if (status.isDefault) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    } else {
      MaterialTheme.colorScheme.surfaceContainerHigh
    }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                if (status.isDefault) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.secondaryContainer
                },
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (status.isDefault) Icons.Rounded.CheckCircle else Icons.Rounded.Keyboard,
              contentDescription = null,
              tint = if (status.isDefault) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(22.dp),
            )
          }
          Column {
            Text(
              text = stringResource(R.string.setup_status_title),
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text =
                when {
                  status.isDefault -> stringResource(R.string.setup_ready_badge)
                  status.isEnabled -> stringResource(R.string.setup_enabled_badge)
                  else -> stringResource(R.string.setup_pending_badge)
                },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        FilledTonalButton(
          onClick = onOpenImeSettings,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("enable_ime_button"),
          shape = RoundedCornerShape(16.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = if (status.isEnabled) "Настройки IME" else stringResource(R.string.setup_step_enable),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Button(
          onClick = onOpenImePicker,
          modifier = Modifier
            .weight(1f)
            .heightIn(min = 48.dp)
            .testTag("select_ime_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
          ),
        ) {
          Icon(
            imageVector = Icons.Rounded.TouchApp,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.setup_step_select),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
fun LiveSandboxPreviewCard(
  settings: KeyboardSettings,
  sandboxText: TextFieldValue,
  onSandboxTextChange: (TextFieldValue) -> Unit,
  onClearSandbox: () -> Unit,
  sink: InputSink,
  activeLanguage: KeyboardLanguage,
  onLanguageChange: (KeyboardLanguage) -> Unit,
  onToggleOneHanded: () -> Unit,
  onSwapOneHandedSide: () -> Unit,
  showTextInput: Boolean,
  isExpanded: Boolean,
  onToggleExpanded: (() -> Unit)? = null,
  onOpenSettingsTab: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(26.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    tonalElevation = 2.dp,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .then(
            if (onToggleExpanded != null) Modifier.clickable(onClick = onToggleExpanded) else Modifier,
          )
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
          )
          Text(
            text = stringResource(R.string.preview_sandbox_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        if (onToggleExpanded != null) {
          Icon(
            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = if (isExpanded) "Свернуть превью" else "Развернуть превью",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (showTextInput) {
            OutlinedTextField(
              value = sandboxText,
              onValueChange = onSandboxTextChange,
              placeholder = {
                Text(
                  text = stringResource(R.string.preview_sandbox_placeholder),
                  style = MaterialTheme.typography.bodyMedium,
                )
              },
              trailingIcon = {
                if (sandboxText.text.isNotEmpty()) {
                  IconButton(
                    onClick = onClearSandbox,
                    modifier = Modifier.testTag("clear_sandbox_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Clear,
                      contentDescription = stringResource(R.string.preview_clear),
                    )
                  }
                }
              },
              maxLines = 3,
              shape = RoundedCornerShape(18.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 10.dp)
                .testTag("sandbox_textfield"),
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp, topStart = 18.dp, topEnd = 18.dp)),
          ) {
            ToneKeyKeyboardView(
              settings = settings,
              sink = sink,
              activeLanguage = activeLanguage,
              onLanguageChange = onLanguageChange,
              onToggleOneHanded = onToggleOneHanded,
              onSwapOneHandedSide = onSwapOneHandedSide,
              onOpenSettings = onOpenSettingsTab,
            )
          }
        }
      }
    }
  }
}

@Composable
fun SettingSliderRow(
  title: String,
  valueText: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  steps: Int = 0,
  subtitle: String? = null,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text = valueText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
      )
    }
  }
}

@Composable
fun SettingSwitchRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  icon: ImageVector? = null,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable(enabled = enabled) { onCheckedChange(!checked) },
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 64.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (icon != null) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
              if (checked) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
              },
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f),
        )
      }

      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
      )
    }
  }
}
