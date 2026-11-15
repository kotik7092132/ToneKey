package com.example.ime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClipCategory
import com.example.model.ClipboardFilter
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.KeyboardColorTokens

@Composable
internal fun EmojiKeyboardPanel(
  tokens: KeyboardColorTokens,
  settings: KeyboardSettings,
  isHeavyAnim: Boolean,
  panelHeight: Dp,
  onSelectEmoji: (String) -> Unit,
  onBackspace: () -> Unit,
  onBackToLetters: () -> Unit,
) {
  val strings = LocalAppStrings.current
  var selectedCategoryIdx by remember { mutableIntStateOf(0) }
  val categories = KeyboardLayouts.emojiCategories

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(panelHeight)
      .padding(horizontal = 6.dp, vertical = 4.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      categories.forEachIndexed { idx, cat ->
        val selected = idx == selectedCategoryIdx
        val chipBg =
          if (isHeavyAnim) {
            animateColorAsState(
              targetValue = if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground,
              animationSpec = spring(stiffness = Spring.StiffnessMedium),
              label = "emoji_tab_bg",
            ).value
          } else {
            if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground
          }
        Box(
          modifier = Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .clickable { selectedCategoryIdx = idx },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings.emojiCategoryLabel(cat),
            color = if (selected) tokens.accentKeyContent else tokens.keyContent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    val renderGrid: @Composable (EmojiCategory) -> Unit = { activeCategory ->
      LazyVerticalGrid(
        columns = GridCells.Fixed(if (activeCategory.id == "kaomoji") 3 else 6),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        items(activeCategory.items) { item ->
          Box(
            modifier = Modifier
              .height(42.dp)
              .clip(RoundedCornerShape(settings.keyCornerRadiusDp.dp))
              .background(tokens.keyBackground.copy(alpha = (tokens.keyBackground.alpha * 0.85f).coerceAtLeast(0.12f)))
              .clickable { onSelectEmoji(item) },
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = item,
              fontSize = if (activeCategory.id == "kaomoji") 13.sp else 22.sp,
              color = tokens.keyContent,
              textAlign = TextAlign.Center,
              maxLines = 1,
            )
          }
        }
      }
    }

    if (isHeavyAnim) {
      AnimatedContent(
        targetState = categories[selectedCategoryIdx],
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        label = "emoji_grid",
      ) { activeCategory ->
        renderGrid(activeCategory)
      }
    } else {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        renderGrid(categories[selectedCategoryIdx])
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        onClick = onBackToLetters,
        shape = RoundedCornerShape(settings.keyCornerRadiusDp.dp),
        color = tokens.modifierKeyBackground,
      ) {
        Text(
          text = strings.lettersKeyLabel,
          color = tokens.modifierKeyContent,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
        )
      }

      Surface(
        onClick = onBackspace,
        shape = RoundedCornerShape(settings.keyCornerRadiusDp.dp),
        color = tokens.modifierKeyBackground,
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Backspace,
            contentDescription = strings.cdBackspace,
            tint = tokens.modifierKeyContent,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun SmartClipboardPanel(
  tokens: KeyboardColorTokens,
  settings: KeyboardSettings,
  clipboardItems: List<ClipboardItem>,
  panelHeight: Dp,
  onPasteText: (String) -> Unit,
  onTogglePinClip: (String) -> Unit,
  onDeleteClip: (String) -> Unit,
  onClearUnpinned: () -> Unit,
  onBackToLetters: () -> Unit,
) {
  val strings = LocalAppStrings.current
  var filter by remember { mutableStateOf(ClipboardFilter.ALL) }

  val filteredClips = remember(clipboardItems, filter) {
    when (filter) {
      ClipboardFilter.ALL -> clipboardItems
      ClipboardFilter.PINNED -> clipboardItems.filter { it.isPinned }
      ClipboardFilter.SMART -> clipboardItems.filter { it.isSmartEntity }
    }
  }
  val hasUnpinned = remember(clipboardItems) { clipboardItems.any { !it.isPinned } }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(panelHeight)
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("smart_clipboard_panel"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Header: Filter pills + Clear unpinned action
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      ClipboardFilter.entries.forEach { tab ->
        val selected = filter == tab
        Surface(
          onClick = { filter = tab },
          shape = RoundedCornerShape(50),
          color = if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground,
          modifier = Modifier
            .height(30.dp)
            .testTag("clip_filter_${tab.name.lowercase()}"),
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = strings.clipFilterLabel(tab),
              color = if (selected) tokens.accentKeyContent else tokens.keyContent,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
            )
          }
        }
      }

      Spacer(Modifier.weight(1f))

      if (hasUnpinned) {
        IconButton(
          onClick = onClearUnpinned,
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(tokens.toolbarItemBackground)
            .testTag("clip_clear_unpinned"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteSweep,
            contentDescription = strings.clipClearUnpinned,
            tint = tokens.modifierKeyContent,
            modifier = Modifier.size(17.dp),
          )
        }
      }
    }

    // Main list or empty state
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (filteredClips.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = tokens.keyBackground.copy(alpha = (tokens.keyBackground.alpha * 0.85f).coerceAtLeast(0.18f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(tokens.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = tokens.colorScheme.onSecondaryContainer,
                  modifier = Modifier.size(18.dp),
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.smartClipboardEmptyTitle,
                  color = tokens.keyContent,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = strings.smartClipboardEmptySubtitle,
                  color = tokens.keyHintContent,
                  fontSize = 11.5.sp,
                  lineHeight = 15.sp,
                )
              }
            }
          }
        }
      } else {
        items(filteredClips, key = { it.id }) { item ->
          SmartClipCard(
            item = item,
            tokens = tokens,
            cornerRadius = settings.keyCornerRadiusDp.dp,
            onPaste = { onPasteText(item.text) },
            onPasteSmart = {
              onPasteText(item.smartExtract ?: item.text)
            },
            onTogglePin = { onTogglePinClip(item.id) },
            onDelete = { onDeleteClip(item.id) },
          )
        }
      }

      // Quick phrases row
      item {
        Column(
          modifier = Modifier.padding(top = 2.dp),
          verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Text(
            text = strings.clipQuickPhrasesTitle,
            color = tokens.keyHintContent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            strings.quickTemplates().forEach { phrase ->
              Surface(
                onClick = { onPasteText(phrase) },
                shape = RoundedCornerShape(12.dp),
                color = tokens.toolbarItemBackground,
              ) {
                Text(
                  text = phrase,
                  color = tokens.keyContent,
                  fontSize = 12.sp,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
              }
            }
          }
        }
      }
    }

    // Bottom back button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Start,
    ) {
      Surface(
        onClick = onBackToLetters,
        shape = RoundedCornerShape(settings.keyCornerRadiusDp.dp),
        color = tokens.modifierKeyBackground,
      ) {
        Text(
          text = strings.backToLetters,
          color = tokens.modifierKeyContent,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun SmartClipCard(
  item: ClipboardItem,
  tokens: KeyboardColorTokens,
  cornerRadius: Dp,
  onPaste: () -> Unit,
  onPasteSmart: () -> Unit,
  onTogglePin: () -> Unit,
  onDelete: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val hasDistinctSmartExtract =
    item.smartExtract != null && item.smartExtract != item.text &&
      (item.category == ClipCategory.OTP || item.category == ClipCategory.URL)

  val border =
    when {
      item.isPinned -> BorderStroke(1.dp, tokens.accentKeyBackground.copy(alpha = 0.55f))
      tokens.keyBorder != Color.Transparent -> BorderStroke(1.dp, tokens.keyBorder)
      else -> null
    }

  Surface(
    onClick = onPaste,
    shape = RoundedCornerShape(cornerRadius.coerceAtLeast(12.dp)),
    color = tokens.keyBackground,
    border = border,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ClipCategoryBadge(
          item = item,
          tokens = tokens,
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.text,
            color = tokens.keyContent,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (item.isSmartEntity) {
            Text(
              text = strings.clipCategoryLabel(item.category),
              color = tokens.keyHintContent,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }

        IconButton(
          onClick = onTogglePin,
          modifier = Modifier.size(30.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.PushPin,
            contentDescription = if (item.isPinned) strings.clipUnpinAction else strings.clipPinAction,
            tint = if (item.isPinned) tokens.accentKeyBackground else tokens.keyHintContent,
            modifier = Modifier.size(16.dp),
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(30.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = strings.clipDeleteAction,
            tint = tokens.keyHintContent,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      if (hasDistinctSmartExtract) {
        val smartLabel =
          when (item.category) {
            ClipCategory.OTP -> "${strings.clipPasteCodePrefix}${item.smartExtract}"
            ClipCategory.URL -> strings.clipCleanUrlAction
            else -> item.smartExtract.orEmpty()
          }
        Surface(
          onClick = onPasteSmart,
          shape = RoundedCornerShape(50),
          color = tokens.colorScheme.primaryContainer,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Icon(
              imageVector = clipCategoryIcon(item.category),
              contentDescription = null,
              tint = tokens.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = smartLabel,
              color = tokens.colorScheme.onPrimaryContainer,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
internal fun ClipCategoryBadge(
  item: ClipboardItem,
  tokens: KeyboardColorTokens,
) {
  val parsedColor =
    if (item.category == ClipCategory.COLOR_HEX) {
      runCatching { Color(android.graphics.Color.parseColor(item.smartExtract ?: item.text)) }.getOrNull()
    } else {
      null
    }

  val bg =
    parsedColor
      ?: if (item.isSmartEntity) tokens.colorScheme.secondaryContainer else tokens.toolbarItemBackground
  val fg =
    if (item.isSmartEntity) tokens.colorScheme.onSecondaryContainer else tokens.modifierKeyContent

  Box(
    modifier = Modifier
      .size(28.dp)
      .clip(CircleShape)
      .background(bg),
    contentAlignment = Alignment.Center,
  ) {
    if (parsedColor == null) {
      Icon(
        imageVector = clipCategoryIcon(item.category),
        contentDescription = null,
        tint = fg,
        modifier = Modifier.size(15.dp),
      )
    }
  }
}

internal fun clipCategoryIcon(category: ClipCategory): ImageVector =
  when (category) {
    ClipCategory.OTP -> Icons.Rounded.Password
    ClipCategory.URL -> Icons.Rounded.Link
    ClipCategory.EMAIL -> Icons.Rounded.AlternateEmail
    ClipCategory.PHONE -> Icons.Rounded.Call
    ClipCategory.COLOR_HEX -> Icons.Rounded.Palette
    ClipCategory.TEXT -> Icons.Rounded.ContentPaste
  }
