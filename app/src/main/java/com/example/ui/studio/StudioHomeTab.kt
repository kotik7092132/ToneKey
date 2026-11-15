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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ime.InputSink
import com.example.model.AnimationPreset
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.StudioProfile
import com.example.model.StudioProfiles
import com.example.ui.theme.swatchColors

@Composable
fun StudioHomeTab(
  settings: KeyboardSettings,
  imeStatus: ImeActivationStatus,
  sandboxText: TextFieldValue,
  previewLanguage: KeyboardLanguage,
  sandboxSink: InputSink,
  showInlinePreview: Boolean,
  onOpenImeSettings: () -> Unit,
  onOpenImePicker: () -> Unit,
  onApplyProfile: (StudioProfile) -> Unit,
  onSandboxTextChange: (TextFieldValue) -> Unit,
  onClearSandbox: () -> Unit,
  onLanguageChange: (KeyboardLanguage) -> Unit,
  onToggleOneHanded: () -> Unit,
  onSwapOneHandedSide: () -> Unit,
  onNavigateToPalette: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
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
        title = "Готовые профили стиля",
        subtitle = "Мгновенная настройка палитры, геометрии и шрифта",
      )
      Spacer(Modifier.height(6.dp))
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
          onOpenSettingsTab = onNavigateToPalette,
        )
      }
    }

    item {
      Spacer(Modifier.height(12.dp))
    }
  }
}

@Composable
private fun HeroStudioBanner(settings: KeyboardSettings) {
  val artworkScale =
    if (settings.animationPreset == AnimationPreset.HEAVY) {
      val infiniteTransition = rememberInfiniteTransition(label = "hero_ambient")
      infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
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
      .height(152.dp)
      .clip(RoundedCornerShape(28.dp)),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_studio_hero),
      contentDescription = stringResource(R.string.cd_hero_banner),
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
          Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.12f),
              MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
              Color.Black.copy(alpha = 0.74f),
            ),
          ),
        ),
    )

    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
      ) {
        Text(
          text = if (settings.useDynamicColor) "Material You • Системный цвет" else "Material You • ${settings.palettePreset.title}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
      Text(
        text = "Студия ToneKey",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
      )
      Text(
        text = "Тонкая настройка цвета, геометрии и тактильности",
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

  Surface(
    modifier = Modifier
      .width(176.dp)
      .graphicsLayer {
        scaleX = cardScale
        scaleY = cardScale
      }
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("profile_${profile.id}"),
    shape = RoundedCornerShape(22.dp),
    color = cardBg,
    border = BorderStroke(
      borderWidth,
      if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(swatch.primary),
          )
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(swatch.secondary),
          )
          Box(
            modifier = Modifier
              .size(16.dp)
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
          text = profile.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = profile.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color =
            if (selected) {
              MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          maxLines = 2,
        )
      }
    }
  }
}
