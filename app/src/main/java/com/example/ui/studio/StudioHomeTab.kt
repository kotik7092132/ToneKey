package com.example.ui.studio

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ime.InputSink
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.StudioProfile
import com.example.model.StudioProfiles
import com.example.ui.theme.swatchColors
import androidx.compose.ui.text.input.TextFieldValue

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
      modifier = Modifier.fillMaxSize(),
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Black.copy(alpha = 0.15f),
              Color.Black.copy(alpha = 0.72f),
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
  Surface(
    modifier = Modifier
      .width(176.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("profile_${profile.id}"),
    shape = RoundedCornerShape(22.dp),
    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
    border =
      if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
      } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      },
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
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
