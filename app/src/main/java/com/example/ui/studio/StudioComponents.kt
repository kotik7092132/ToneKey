package com.example.ui.studio

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ime.InputSink
import com.example.ime.ToneKeyKeyboardView
import com.example.model.AnimationPreset
import com.example.model.ClipboardItem
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.ui.i18n.LocalAppStrings

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(top = 6.dp, bottom = 2.dp),
    verticalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(16.dp)
          .clip(RoundedCornerShape(50))
          .background(MaterialTheme.colorScheme.primary),
      )
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    if (subtitle != null) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp),
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
  val strings = LocalAppStrings.current
  val containerStart by animateColorAsState(
    targetValue =
      if (status.isDefault) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
      } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
      },
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "ime_card_start",
  )
  val containerEnd by animateColorAsState(
    targetValue =
      if (status.isDefault) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      },
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "ime_card_end",
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMedium)),
    shape = RoundedCornerShape(28.dp),
    border = BorderStroke(
      1.dp,
      if (status.isDefault) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
      },
    ),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.linearGradient(listOf(containerStart, containerEnd)))
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          val badgeBg by animateColorAsState(
            targetValue =
              if (status.isDefault) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.secondaryContainer
              },
            label = "ime_badge_bg",
          )
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(badgeBg),
            contentAlignment = Alignment.Center,
          ) {
            AnimatedContent(
              targetState = status.isDefault,
              transitionSpec = {
                (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                  (scaleOut() + fadeOut())
              },
              label = "ime_badge_icon",
            ) { isDefault ->
              Icon(
                imageVector = if (isDefault) Icons.Rounded.CheckCircle else Icons.Rounded.Keyboard,
                contentDescription = null,
                tint = if (isDefault) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp),
              )
            }
          }
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = strings.setupStatusTitle,
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text =
                when {
                  status.isDefault -> strings.setupReadyBadge
                  status.isEnabled -> strings.setupEnabledBadge
                  else -> strings.setupPendingBadge
                },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Expressive 2-step indicator bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StepProgressDot(step = "1", completed = status.isEnabled)
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          thickness = 2.dp,
          color =
            if (status.isEnabled) {
              MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
        )
        StepProgressDot(step = "2", completed = status.isDefault)
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
          shape = RoundedCornerShape(18.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = if (status.isEnabled) strings.setupStepSettings else strings.setupStepEnable,
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
          shape = RoundedCornerShape(18.dp),
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
            text = strings.setupStepSelect,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun StepProgressDot(
  step: String,
  completed: Boolean,
) {
  Box(
    modifier = Modifier
      .size(24.dp)
      .clip(CircleShape)
      .background(
        if (completed) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.surfaceContainerHighest
        },
      ),
    contentAlignment = Alignment.Center,
  ) {
    if (completed) {
      Icon(
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(14.dp),
      )
    } else {
      Text(
        text = step,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
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
  clipboardItems: List<ClipboardItem> = emptyList(),
  smartSuggestionClip: ClipboardItem? = null,
  onTogglePinClip: (String) -> Unit = {},
  onDeleteClip: (String) -> Unit = {},
  onClearUnpinnedClips: () -> Unit = {},
  onDismissSmartSuggestion: (String) -> Unit = {},
  onToggleExpanded: (() -> Unit)? = null,
  onOpenSettingsTab: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val strings = LocalAppStrings.current
  val isHeavyAnim = settings.animationPreset == AnimationPreset.HEAVY
  val dotScale: Float
  val chevronRotation: Float
  if (isHeavyAnim) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_dot_pulse")
    dotScale = infiniteTransition.animateFloat(
      initialValue = 0.85f,
      targetValue = 1.25f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
      label = "live_dot_scale",
    ).value
    chevronRotation = animateFloatAsState(
      targetValue = if (isExpanded) 180f else 0f,
      animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
      label = "chevron_rot",
    ).value
  } else {
    dotScale = 1f
    chevronRotation = if (isExpanded) 180f else 0f
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .then(
        if (isHeavyAnim) {
          Modifier.animateContentSize(spring(dampingRatio = 0.85f, stiffness = 450f))
        } else {
          Modifier
        },
      ),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    tonalElevation = 3.dp,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
          .then(
            if (onToggleExpanded != null) Modifier.clickable(onClick = onToggleExpanded) else Modifier,
          )
          .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(9.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .graphicsLayer {
                scaleX = dotScale
                scaleY = dotScale
              }
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
          )
          Text(
            text = strings.previewSandboxLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text = activeLanguage.shortLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }

          if (onToggleExpanded != null) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (isExpanded) strings.previewCollapse else strings.previewExpand,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                  .size(18.dp)
                  .graphicsLayer { rotationZ = chevronRotation },
              )
            }
          }
        }
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
          animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        ) + fadeIn(tween(180)),
        exit = shrinkVertically(
          animationSpec = spring(stiffness = 460f),
        ) + fadeOut(tween(140)),
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (showTextInput) {
            OutlinedTextField(
              value = sandboxText,
              onValueChange = onSandboxTextChange,
              placeholder = {
                Text(
                  text = strings.previewSandboxPlaceholder,
                  style = MaterialTheme.typography.bodyMedium,
                )
              },
              trailingIcon = {
                AnimatedVisibility(
                  visible = sandboxText.text.isNotEmpty(),
                  enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                  exit = scaleOut() + fadeOut(),
                ) {
                  IconButton(
                    onClick = onClearSandbox,
                    modifier = Modifier.testTag("clear_sandbox_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Clear,
                      contentDescription = strings.previewClear,
                    )
                  }
                }
              },
              maxLines = 3,
              shape = RoundedCornerShape(18.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
              ),
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("sandbox_textfield"),
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp, topStart = 20.dp, topEnd = 20.dp)),
          ) {
            ToneKeyKeyboardView(
              settings = settings,
              sink = sink,
              activeLanguage = activeLanguage,
              onLanguageChange = onLanguageChange,
              clipboardItems = clipboardItems,
              smartSuggestionClip = smartSuggestionClip,
              onTogglePinClip = onTogglePinClip,
              onDeleteClip = onDeleteClip,
              onClearUnpinnedClips = onClearUnpinnedClips,
              onDismissSmartSuggestion = onDismissSmartSuggestion,
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
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
        Spacer(Modifier.width(8.dp))
        Surface(
          shape = RoundedCornerShape(50),
          color = MaterialTheme.colorScheme.primaryContainer,
        ) {
          AnimatedContent(
            targetState = valueText,
            transitionSpec = {
              fadeIn(tween(110)) togetherWith fadeOut(tween(90))
            },
            label = "slider_badge",
          ) { text ->
            Text(
              text = text,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            )
          }
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
  val iconBg by animateColorAsState(
    targetValue =
      if (checked) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
      },
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "switch_icon_bg",
  )
  val iconScale by animateFloatAsState(
    targetValue = if (checked) 1.06f else 0.96f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
    label = "switch_icon_scale",
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .clickable(enabled = enabled) { onCheckedChange(!checked) },
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 66.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (icon != null) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .graphicsLayer {
              scaleX = iconScale
              scaleY = iconScale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(iconBg),
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
