package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ime.InputSink
import com.example.ime.clipCategoryIcon
import com.example.model.AnimationPreset
import com.example.model.ClipCategory
import com.example.model.ClipboardItem
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.StudioProfile
import com.example.model.StudioProfiles
import com.example.model.ThemeMode
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.swatchColors
import com.example.ui.theme.toComposeFontFamily

@Composable
fun StudioHomeTab(
  settings: KeyboardSettings,
  imeStatus: ImeActivationStatus,
  sandboxText: TextFieldValue,
  previewLanguage: KeyboardLanguage,
  sandboxSink: InputSink,
  clipboardItems: List<ClipboardItem>,
  smartSuggestionClip: ClipboardItem?,
  showInlinePreview: Boolean,
  onOpenImeSettings: () -> Unit,
  onOpenImePicker: () -> Unit,
  onApplyProfile: (StudioProfile) -> Unit,
  onSandboxTextChange: (TextFieldValue) -> Unit,
  onClearSandbox: () -> Unit,
  onLanguageChange: (KeyboardLanguage) -> Unit,
  onToggleOneHanded: () -> Unit,
  onSwapOneHandedSide: () -> Unit,
  onSyncClipboard: () -> Unit,
  onAddClipboardSnippet: (String) -> Unit,
  onTogglePinClip: (String) -> Unit,
  onDeleteClip: (String) -> Unit,
  onClearUnpinnedClips: () -> Unit,
  onDismissSmartSuggestion: (String) -> Unit,
  onNavigateToPalette: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = LocalAppStrings.current
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item {
      HeroStudioBanner(settings = settings)
    }

    item {
      ImeStatusCard(
        status = imeStatus,
        onOpenImeSettings = onOpenImeSettings,
        onOpenImePicker = onOpenImePicker,
      )
    }

    item {
      SectionHeader(
        title = strings.profilesHeader,
        subtitle = strings.profilesSubtitle,
      )
      Spacer(Modifier.height(8.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(StudioProfiles.all, key = { it.id }) { profile ->
          val isSelected =
            settings.palettePreset == profile.settings.palettePreset &&
              settings.keySurfaceStyle == profile.settings.keySurfaceStyle &&
              settings.keyFontFamily == profile.settings.keyFontFamily &&
              settings.themeMode == profile.settings.themeMode
          ProfilePresetCard(
            profile = profile,
            selected = isSelected,
            onClick = { onApplyProfile(profile) },
          )
        }
      }
    }

    if (showInlinePreview) {
      item {
        LiveSandboxPreviewCard(
          settings = settings,
          sandboxText = sandboxText,
          onSandboxTextChange = onSandboxTextChange,
          onClearSandbox = onClearSandbox,
          sink = sandboxSink,
          activeLanguage = previewLanguage,
          onLanguageChange = onLanguageChange,
          onToggleOneHanded = onToggleOneHanded,
          onSwapOneHandedSide = onSwapOneHandedSide,
          showTextInput = true,
          isExpanded = true,
          clipboardItems = clipboardItems,
          smartSuggestionClip = smartSuggestionClip,
          onTogglePinClip = onTogglePinClip,
          onDeleteClip = onDeleteClip,
          onClearUnpinnedClips = onClearUnpinnedClips,
          onDismissSmartSuggestion = onDismissSmartSuggestion,
          onOpenSettingsTab = onNavigateToPalette,
        )
      }
    }

    item {
      SmartClipboardStudioCard(
        clipboardItems = clipboardItems,
        onSyncClipboard = onSyncClipboard,
        onAddSnippet = onAddClipboardSnippet,
        onInsertToSandbox = { text -> sandboxSink.commitText(text) },
        onTogglePin = onTogglePinClip,
        onDelete = onDeleteClip,
        onClearUnpinned = onClearUnpinnedClips,
      )
    }

    item {
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun HeroStudioBanner(settings: KeyboardSettings) {
  val strings = LocalAppStrings.current
  val artworkScale =
    if (settings.animationPreset == AnimationPreset.HEAVY) {
      val infiniteTransition = rememberInfiniteTransition(label = "hero_ambient")
      infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 6500, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
        label = "hero_zoom",
      ).value
    } else {
      1.0f
    }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(172.dp)
      .clip(RoundedCornerShape(32.dp)),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_studio_hero),
      contentDescription = strings.cdHeroBanner,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          scaleX = artworkScale
          scaleY = artworkScale
        },
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.linearGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.25f),
              MaterialTheme.colorScheme.primary.copy(alpha = 0.36f),
              Color.Black.copy(alpha = 0.80f),
            ),
          ),
        ),
    )

    // Floating expressive mini-keycap badges top-right
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val badgeCorner = (settings.keyCornerRadiusDp * 0.65f).coerceIn(4f, 14f).dp
      listOf("T", "K").forEach { letter ->
        Surface(
          shape = RoundedCornerShape(badgeCorner),
          color = Color.White.copy(alpha = 0.22f * settings.keyAlpha + 0.12f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
          modifier = Modifier.size(32.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = letter,
              color = Color.White,
              fontFamily = settings.keyFontFamily.toComposeFontFamily(),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
          }
        }
      }
      Surface(
        shape = RoundedCornerShape(badgeCorner),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
        modifier = Modifier.size(width = 40.dp, height = 32.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "↵",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
          )
        }
      }
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
      ) {
        Text(
          text =
            if (settings.useDynamicColor) {
              strings.heroBadgeDynamic
            } else {
              "${strings.heroBadgePresetPrefix}${strings.paletteTitle(settings.palettePreset)}"
            },
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
      }
      Text(
        text = strings.studioTitle,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
      )
      Text(
        text = strings.studioSubtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.88f),
      )
    }
  }
}

@Composable
private fun ProfilePresetCard(
  profile: StudioProfile,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val swatch = profile.settings.palettePreset.swatchColors()
  val cardScale by animateFloatAsState(
    targetValue = if (selected) 1.02f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 480f),
    label = "profile_scale",
  )
  val cardBg by animateColorAsState(
    targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "profile_bg",
  )
  val borderWidth by animateDpAsState(
    targetValue = if (selected) 1.8.dp else 1.dp,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "profile_border",
  )

  val miniBoardBg =
    when (profile.settings.themeMode) {
      ThemeMode.AMOLED -> Color(0xFF060608)
      ThemeMode.DARK -> Color(0xFF1A1B20)
      else -> swatch.primary.copy(alpha = 0.14f)
    }
  val miniKeyBg =
    when {
      profile.settings.keySurfaceStyle == KeySurfaceStyle.OUTLINED -> Color.Transparent
      profile.settings.themeMode == ThemeMode.AMOLED || profile.settings.themeMode == ThemeMode.DARK ->
        Color(0xFF2B2D35)
      else -> Color.White.copy(alpha = 0.92f)
    }
  val miniKeyText =
    if (profile.settings.themeMode == ThemeMode.AMOLED || profile.settings.themeMode == ThemeMode.DARK) {
      Color.White.copy(alpha = 0.9f)
    } else {
      Color(0xFF1A1C1E)
    }
  val miniRadius = (profile.settings.keyCornerRadiusDp * 0.45f).coerceIn(2f, 10f).dp

  Surface(
    modifier = Modifier
      .width(192.dp)
      .graphicsLayer {
        scaleX = cardScale
        scaleY = cardScale
      }
      .clip(RoundedCornerShape(26.dp))
      .clickable(onClick = onClick)
      .testTag("profile_${profile.id}"),
    shape = RoundedCornerShape(26.dp),
    color = cardBg,
    border = BorderStroke(
      borderWidth,
      if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Mini Keyboard Visual Swatch
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(miniBoardBg)
          .padding(8.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            listOf("Q", "W", "E").forEach { key ->
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(22.dp),
                shape = RoundedCornerShape(miniRadius),
                color = miniKeyBg,
                border =
                  if (profile.settings.keySurfaceStyle == KeySurfaceStyle.OUTLINED) {
                    BorderStroke(1.dp, swatch.primary.copy(alpha = 0.7f))
                  } else {
                    null
                  },
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = key,
                    fontSize = 10.sp,
                    fontFamily = profile.settings.keyFontFamily.toComposeFontFamily(),
                    fontWeight = FontWeight.SemiBold,
                    color = miniKeyText,
                  )
                }
              }
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Surface(
              modifier = Modifier
                .weight(2.1f)
                .height(18.dp),
              shape = RoundedCornerShape(miniRadius),
              color = miniKeyBg,
              border =
                if (profile.settings.keySurfaceStyle == KeySurfaceStyle.OUTLINED) {
                  BorderStroke(1.dp, swatch.secondary.copy(alpha = 0.55f))
                } else {
                  null
                },
            ) {}
            Surface(
              modifier = Modifier
                .weight(0.9f)
                .height(18.dp),
              shape = RoundedCornerShape(miniRadius),
              color = swatch.primary,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "↵",
                  fontSize = 10.sp,
                  color = Color.White,
                )
              }
            }
          }
        }

        androidx.compose.animation.AnimatedVisibility(
          visible = selected,
          enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
          exit = scaleOut() + fadeOut(),
          modifier = Modifier.align(Alignment.TopEnd),
        ) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = strings.profileTitle(profile.id),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = strings.profileSubtitle(profile.id),
          style = MaterialTheme.typography.bodyMedium,
          color =
            if (selected) {
              MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SmartClipboardStudioCard(
  clipboardItems: List<ClipboardItem>,
  onSyncClipboard: () -> Unit,
  onAddSnippet: (String) -> Unit,
  onInsertToSandbox: (String) -> Unit,
  onTogglePin: (String) -> Unit,
  onDelete: (String) -> Unit,
  onClearUnpinned: () -> Unit,
) {
  val strings = LocalAppStrings.current
  var draft by remember { mutableStateOf("") }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("studio_smart_clipboard_card"),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = strings.smartClipboardHeader,
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = strings.smartClipboardSubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Action pills row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          onClick = onSyncClipboard,
          shape = RoundedCornerShape(50),
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier = Modifier
            .heightIn(min = 38.dp)
            .testTag("studio_sync_clip_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.ContentPasteGo,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = strings.smartClipboardSyncButton,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }

        if (clipboardItems.any { !it.isPinned }) {
          Surface(
            onClick = onClearUnpinned,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.heightIn(min = 38.dp),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = strings.clipClearUnpinned,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Quick add snippet row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = draft,
          onValueChange = { draft = it },
          placeholder = {
            Text(
              text = strings.smartClipboardAddPlaceholder,
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("studio_clip_input"),
        )
        FilledTonalIconButton(
          onClick = {
            if (draft.isNotBlank()) {
              onAddSnippet(draft)
              draft = ""
            }
          },
          modifier = Modifier
            .size(48.dp)
            .testTag("studio_add_clip_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = strings.smartClipboardAddButton,
          )
        }
      }

      if (clipboardItems.isEmpty()) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Text(
              text = strings.smartClipboardEmptyTitle,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = strings.smartClipboardEmptySubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          clipboardItems.take(5).forEach { item ->
            Surface(
              onClick = { onInsertToSandbox(item.smartExtract ?: item.text) },
              shape = RoundedCornerShape(18.dp),
              color = MaterialTheme.colorScheme.surfaceContainer,
              border =
                if (item.isPinned) {
                  BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                } else {
                  null
                },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                val parsedColor =
                  if (item.category == ClipCategory.COLOR_HEX) {
                    runCatching { Color(android.graphics.Color.parseColor(item.smartExtract ?: item.text)) }.getOrNull()
                  } else {
                    null
                  }

                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                      parsedColor
                        ?: if (item.isSmartEntity) {
                          MaterialTheme.colorScheme.secondaryContainer
                        } else {
                          MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  if (parsedColor == null) {
                    Icon(
                      imageVector = clipCategoryIcon(item.category),
                      contentDescription = null,
                      tint =
                        if (item.isSmartEntity) {
                          MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                  if (item.isSmartEntity) {
                    val detail =
                      if (item.smartExtract != null && item.smartExtract != item.text) {
                        "${strings.clipCategoryLabel(item.category)} • ${item.smartExtract}"
                      } else {
                        strings.clipCategoryLabel(item.category)
                      }
                    Text(
                      text = detail,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }

                IconButton(
                  onClick = { onTogglePin(item.id) },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = if (item.isPinned) strings.clipUnpinAction else strings.clipPinAction,
                    tint =
                      if (item.isPinned) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                    modifier = Modifier.size(18.dp),
                  )
                }

                IconButton(
                  onClick = { onDelete(item.id) },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.clipDeleteAction,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
