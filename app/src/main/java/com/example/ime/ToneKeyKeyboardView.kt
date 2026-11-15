package com.example.ime

import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardCapslock
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.theme.KeyboardColorTokens
import com.example.ui.theme.buildKeyboardColorTokens
import com.example.ui.theme.rememberAnimatedKeyboardColorTokens
import com.example.ui.theme.toComposeFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class ActiveKeyOverlay(
  val label: String,
  val anchorTopLeft: Offset,
  val anchorSize: IntSize,
  val longPressOptions: List<String>,
  val selectedOptionIndex: Int,
  val isLongPressOpen: Boolean,
)

@Composable
fun ToneKeyKeyboardView(
  settings: KeyboardSettings,
  sink: InputSink,
  activeLanguage: KeyboardLanguage,
  onLanguageChange: (KeyboardLanguage) -> Unit,
  modifier: Modifier = Modifier,
  imeOptions: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
  inputSessionTick: Int = 0,
  onOpenSettings: (() -> Unit)? = null,
  onOpenImePicker: (() -> Unit)? = null,
  onToggleOneHanded: (() -> Unit)? = null,
  onSwapOneHandedSide: (() -> Unit)? = null,
  bottomInsetPadding: Dp = 0.dp,
) {
  val context = LocalContext.current
  val systemDark = isSystemInDarkTheme()
  val targetTokens = remember(settings, systemDark, context) {
    buildKeyboardColorTokens(context, settings, systemDark)
  }
  val tokens = rememberAnimatedKeyboardColorTokens(targetTokens)
  val feedback = remember(context) { KeyboardFeedbackHelper(context) }
  val processor = remember(sink) { ToneKeyInputProcessor(sink) }

  var currentPage by remember { mutableStateOf(KeyboardPage.LETTERS) }
  var currentPanel by remember { mutableStateOf(KeyboardPanel.BOARD) }
  var shiftState by remember { mutableStateOf(ShiftState.SINGLE) }
  var lastShiftTapAt by remember { mutableStateOf(0L) }
  var overlay by remember { mutableStateOf<ActiveKeyOverlay?>(null) }
  var lastVisibleOverlay by remember { mutableStateOf<ActiveKeyOverlay?>(null) }
  var rootPositionInRoot by remember { mutableStateOf(Offset.Zero) }

  if (overlay != null) {
    lastVisibleOverlay = overlay
  }

  val latestSettings by rememberUpdatedState(settings)
  val latestShift by rememberUpdatedState(shiftState)
  val latestLanguage by rememberUpdatedState(activeLanguage)

  LaunchedEffect(inputSessionTick, settings.autoCapitalize) {
    currentPage = KeyboardPage.LETTERS
    currentPanel = KeyboardPanel.BOARD
    shiftState = if (processor.shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
  }

  val geometrySpring = spring<Dp>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
  )
  val baseRowHeight by animateDpAsState(
    targetValue = (46f * settings.keyboardHeightScale).dp,
    animationSpec = geometrySpring,
    label = "row_height",
  )
  val numberRowHeight by animateDpAsState(
    targetValue = (36f * settings.keyboardHeightScale).dp,
    animationSpec = geometrySpring,
    label = "num_row_height",
  )
  val animatedBottomPadding by animateDpAsState(
    targetValue = bottomInsetPadding + settings.bottomPaddingDp.dp,
    animationSpec = geometrySpring,
    label = "bottom_pad",
  )
  val animatedGapH by animateDpAsState(
    targetValue = settings.keyGapHorizontalDp.dp,
    animationSpec = geometrySpring,
    label = "gap_h",
  )
  val animatedGapV by animateDpAsState(
    targetValue = settings.keyGapVerticalDp.dp,
    animationSpec = geometrySpring,
    label = "gap_v",
  )
  val boardContentWidthFraction by animateFloatAsState(
    targetValue = if (settings.oneHandedMode == OneHandedMode.OFF) 1f else 0.84f,
    animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
    label = "one_handed_fraction",
  )

  val toolbarHeight = 42.dp
  val keyFontFamily = settings.keyFontFamily.toComposeFontFamily()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(tokens.boardBackground)
      .animateContentSize(
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      )
      .onGloballyPositioned { coords ->
        rootPositionInRoot = coords.positionInRoot()
      }
      .padding(bottom = animatedBottomPadding)
      .testTag("keyboard_surface"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      AnimatedVisibility(
        visible = settings.showToolbar,
        enter = fadeIn(tween(180)) + slideInVertically(
          animationSpec = spring(stiffness = Spring.StiffnessMedium),
          initialOffsetY = { -it / 2 },
        ),
        exit = fadeOut(tween(140)) + slideOutVertically(targetOffsetY = { -it / 2 }),
      ) {
        KeyboardToolbar(
          tokens = tokens,
          currentPanel = currentPanel,
          isOneHanded = settings.oneHandedMode != OneHandedMode.OFF,
          toolbarHeight = toolbarHeight,
          onToggleEmoji = {
            feedback.triggerTickFeedback(latestSettings)
            currentPanel =
              if (currentPanel == KeyboardPanel.EMOJI) KeyboardPanel.BOARD else KeyboardPanel.EMOJI
          },
          onToggleClipboard = {
            feedback.triggerTickFeedback(latestSettings)
            currentPanel =
              if (currentPanel == KeyboardPanel.CLIPBOARD) KeyboardPanel.BOARD else KeyboardPanel.CLIPBOARD
          },
          onToggleOneHanded = onToggleOneHanded,
          onOpenSettings = onOpenSettings,
          onQuickInsert = { text ->
            feedback.triggerKeyFeedback(latestSettings)
            shiftState = processor.onCharacterCommitted(text, latestShift, latestSettings)
          },
        )
      }

      BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val totalWidth = maxWidth
        val boardWidth = totalWidth * boardContentWidthFraction
        val gutterWidth = totalWidth - boardWidth

        Row(modifier = Modifier.fillMaxWidth()) {
          if (settings.oneHandedMode == OneHandedMode.RIGHT && gutterWidth > 24.dp) {
            OneHandedGutter(
              tokens = tokens,
              gutterWidth = gutterWidth,
              isRightAligned = true,
              onSwapSide = onSwapOneHandedSide,
              onExpand = onToggleOneHanded,
            )
          }

          Box(modifier = Modifier.width(boardWidth)) {
            AnimatedContent(
              targetState = currentPanel,
              transitionSpec = {
                (fadeIn(tween(190)) +
                  scaleIn(
                    initialScale = 0.96f,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                  ) +
                  slideInVertically(
                    initialOffsetY = { it / 10 },
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 520f),
                  )) togetherWith
                  (fadeOut(tween(120)) + scaleOut(targetScale = 0.97f)) using
                  SizeTransform(clip = false)
              },
              label = "keyboard_panel",
            ) { panel ->
              when (panel) {
                KeyboardPanel.BOARD -> {
                  AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                      (fadeIn(tween(150)) +
                        scaleIn(
                          initialScale = 0.97f,
                          animationSpec = spring(stiffness = 620f, dampingRatio = 0.85f),
                        )) togetherWith fadeOut(tween(100))
                    },
                    label = "keyboard_page",
                  ) { page ->
                    val rows = remember(activeLanguage, page, settings.showNumberRow) {
                      KeyboardLayouts.rowsFor(
                        language = activeLanguage,
                        page = page,
                        includeNumberRow = settings.showNumberRow,
                      )
                    }
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                      verticalArrangement = Arrangement.spacedBy(animatedGapV),
                    ) {
                      rows.forEachIndexed { rowIdx, rowKeys ->
                        val isNumberRow = settings.showNumberRow &&
                          page == KeyboardPage.LETTERS &&
                          rowIdx == 0
                        val rowHeight = if (isNumberRow) numberRowHeight else baseRowHeight

                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                          horizontalArrangement = Arrangement.spacedBy(animatedGapH),
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          rowKeys.forEach { keySpec ->
                            when (keySpec) {
                              is KeySpec.CharacterKey -> {
                                val displayLabel =
                                  if (page == KeyboardPage.LETTERS && shiftState != ShiftState.OFF) {
                                    keySpec.shifted
                                  } else {
                                    keySpec.primary
                                  }
                                CharacterKeyButton(
                                  modifier = Modifier
                                    .weight(keySpec.weight)
                                    .fillMaxHeight(),
                                  keySpec = keySpec,
                                  displayLabel = displayLabel,
                                  isNumberRow = isNumberRow,
                                  settings = settings,
                                  tokens = tokens,
                                  fontFamily = keyFontFamily,
                                  rootPosition = rootPositionInRoot,
                                  onPressStart = { topLeft, size ->
                                    feedback.triggerKeyFeedback(latestSettings)
                                    if (latestSettings.showKeyPopup) {
                                      overlay = ActiveKeyOverlay(
                                        label = displayLabel,
                                        anchorTopLeft = topLeft,
                                        anchorSize = size,
                                        longPressOptions = keySpec.longPressOptions,
                                        selectedOptionIndex = 0,
                                        isLongPressOpen = false,
                                      )
                                    }
                                  },
                                  onLongPressTrigger = { topLeft, size ->
                                    if (keySpec.longPressOptions.isNotEmpty()) {
                                      feedback.triggerTickFeedback(latestSettings)
                                      overlay = ActiveKeyOverlay(
                                        label = displayLabel,
                                        anchorTopLeft = topLeft,
                                        anchorSize = size,
                                        longPressOptions = keySpec.longPressOptions,
                                        selectedOptionIndex = 0,
                                        isLongPressOpen = true,
                                      )
                                    }
                                  },
                                  onDragOption = { index ->
                                    overlay = overlay?.copy(selectedOptionIndex = index)
                                  },
                                  onRelease = { committed ->
                                    overlay = null
                                    if (committed != null) {
                                      shiftState = processor.onCharacterCommitted(
                                        committed,
                                        latestShift,
                                        latestSettings,
                                      )
                                    }
                                  },
                                )
                              }

                              is KeySpec.ActionKey -> {
                                ActionKeyButton(
                                  modifier = Modifier
                                    .weight(keySpec.weight)
                                    .fillMaxHeight(),
                                  keySpec = keySpec,
                                  shiftState = shiftState,
                                  activeLanguage = latestLanguage,
                                  imeOptions = imeOptions,
                                  settings = settings,
                                  tokens = tokens,
                                  fontFamily = keyFontFamily,
                                  onTriggerFeedback = { isSpace ->
                                    feedback.triggerKeyFeedback(latestSettings, isSpace)
                                  },
                                  onAction = { action ->
                                    when (action) {
                                      KeyActionType.SHIFT -> {
                                        val now = System.currentTimeMillis()
                                        shiftState =
                                          when {
                                            shiftState == ShiftState.CAPS_LOCK -> ShiftState.OFF
                                            shiftState == ShiftState.SINGLE && now - lastShiftTapAt < 350L ->
                                              ShiftState.CAPS_LOCK
                                            shiftState == ShiftState.SINGLE -> ShiftState.OFF
                                            else -> ShiftState.SINGLE
                                          }
                                        lastShiftTapAt = now
                                      }

                                      KeyActionType.BACKSPACE -> {
                                        shiftState = processor.onBackspace(latestSettings)
                                      }

                                      KeyActionType.SYMBOLS -> {
                                        currentPage = KeyboardPage.SYMBOLS_PRIMARY
                                      }

                                      KeyActionType.SYMBOLS_ALT -> {
                                        currentPage = KeyboardPage.SYMBOLS_SECONDARY
                                      }

                                      KeyActionType.LETTERS -> {
                                        currentPage = KeyboardPage.LETTERS
                                      }

                                      KeyActionType.LANGUAGE -> {
                                        val next =
                                          if (latestLanguage == KeyboardLanguage.RUSSIAN) {
                                            KeyboardLanguage.ENGLISH
                                          } else {
                                            KeyboardLanguage.RUSSIAN
                                          }
                                        onLanguageChange(next)
                                      }

                                      KeyActionType.SPACE -> {
                                        shiftState = processor.onSpacePressed(latestShift, latestSettings)
                                      }

                                      KeyActionType.ENTER -> {
                                        shiftState = processor.onEnter(latestSettings)
                                      }

                                      KeyActionType.EMOJI -> {
                                        currentPanel = KeyboardPanel.EMOJI
                                      }
                                    }
                                  },
                                  onRepeatBackspace = {
                                    feedback.triggerTickFeedback(latestSettings)
                                    shiftState = processor.onBackspace(latestSettings)
                                  },
                                  onDeleteWord = {
                                    feedback.triggerKeyFeedback(latestSettings, isSpaceOrAction = true)
                                    sink.deleteWordBackward()
                                  },
                                  onMoveCursor = { delta ->
                                    feedback.triggerTickFeedback(latestSettings)
                                    sink.moveCursor(delta)
                                  },
                                  onLongPressLanguage = onOpenImePicker,
                                )
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }

                KeyboardPanel.EMOJI -> {
                  EmojiKeyboardPanel(
                    tokens = tokens,
                    settings = settings,
                    panelHeight = (baseRowHeight * 4) + (animatedGapV * 4) +
                      (if (settings.showNumberRow) numberRowHeight else 0.dp),
                    onSelectEmoji = { emoji ->
                      feedback.triggerKeyFeedback(latestSettings)
                      sink.commitText(emoji)
                    },
                    onBackspace = {
                      feedback.triggerKeyFeedback(latestSettings)
                      shiftState = processor.onBackspace(latestSettings)
                    },
                    onBackToLetters = {
                      feedback.triggerTickFeedback(latestSettings)
                      currentPanel = KeyboardPanel.BOARD
                    },
                  )
                }

                KeyboardPanel.CLIPBOARD -> {
                  ClipboardSnippetsPanel(
                    tokens = tokens,
                    settings = settings,
                    panelHeight = (baseRowHeight * 4) + (animatedGapV * 4) +
                      (if (settings.showNumberRow) numberRowHeight else 0.dp),
                    onPasteText = { text ->
                      feedback.triggerKeyFeedback(latestSettings)
                      sink.commitText(text)
                      currentPanel = KeyboardPanel.BOARD
                    },
                    onBackToLetters = {
                      feedback.triggerTickFeedback(latestSettings)
                      currentPanel = KeyboardPanel.BOARD
                    },
                  )
                }
              }
            }
          }

          if (settings.oneHandedMode == OneHandedMode.LEFT && gutterWidth > 24.dp) {
            OneHandedGutter(
              tokens = tokens,
              gutterWidth = gutterWidth,
              isRightAligned = false,
              onSwapSide = onSwapOneHandedSide,
              onExpand = onToggleOneHanded,
            )
          }
        }
      }
    }

    AnimatedVisibility(
      visible = overlay != null,
      enter = fadeIn(tween(80)) +
        scaleIn(
          initialScale = 0.72f,
          animationSpec = spring(dampingRatio = 0.62f, stiffness = 700f),
        ) +
        slideInVertically(
          initialOffsetY = { it / 3 },
          animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        ),
      exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.82f),
    ) {
      lastVisibleOverlay?.let { active ->
        KeyOverlayPopup(
          overlay = active,
          tokens = tokens,
          fontFamily = keyFontFamily,
        )
      }
    }
  }
}

@Composable
private fun KeyboardToolbar(
  tokens: KeyboardColorTokens,
  currentPanel: KeyboardPanel,
  isOneHanded: Boolean,
  toolbarHeight: Dp,
  onToggleEmoji: () -> Unit,
  onToggleClipboard: () -> Unit,
  onToggleOneHanded: (() -> Unit)?,
  onOpenSettings: (() -> Unit)?,
  onQuickInsert: (String) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(toolbarHeight)
      .background(tokens.toolbarBackground)
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    ToolbarIconPill(
      icon = if (currentPanel == KeyboardPanel.EMOJI) Icons.Rounded.Keyboard else Icons.Rounded.EmojiEmotions,
      contentDescription = stringResource(R.string.cd_emoji),
      selected = currentPanel == KeyboardPanel.EMOJI,
      tokens = tokens,
      onClick = onToggleEmoji,
    )
    ToolbarIconPill(
      icon = Icons.Rounded.ContentPaste,
      contentDescription = stringResource(R.string.cd_clipboard),
      selected = currentPanel == KeyboardPanel.CLIPBOARD,
      tokens = tokens,
      onClick = onToggleClipboard,
    )

    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      listOf("—", "«»", "₽", "№", "…", "✓").forEach { symbol ->
        Box(
          modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tokens.toolbarItemBackground)
            .clickable { onQuickInsert(symbol) }
            .padding(horizontal = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = symbol,
            color = tokens.keyContent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }

    if (onToggleOneHanded != null) {
      ToolbarIconPill(
        icon = if (isOneHanded) Icons.Rounded.OpenInFull else Icons.Rounded.CloseFullscreen,
        contentDescription = stringResource(R.string.cd_one_handed),
        selected = isOneHanded,
        tokens = tokens,
        onClick = onToggleOneHanded,
      )
    }
    if (onOpenSettings != null) {
      ToolbarIconPill(
        icon = Icons.Rounded.Tune,
        contentDescription = stringResource(R.string.cd_settings),
        selected = false,
        tokens = tokens,
        onClick = onOpenSettings,
      )
    }
  }
}

@Composable
private fun ToolbarIconPill(
  icon: ImageVector,
  contentDescription: String,
  selected: Boolean,
  tokens: KeyboardColorTokens,
  onClick: () -> Unit,
) {
  val scale by animateFloatAsState(
    targetValue = if (selected) 1.08f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
    label = "tb_pill_scale",
  )
  val bgColor by animateColorAsState(
    targetValue = if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "tb_pill_bg",
  )
  val tint by animateColorAsState(
    targetValue = if (selected) tokens.accentKeyContent else tokens.modifierKeyContent,
    animationSpec = spring(stiffness = Spring.StiffnessMedium),
    label = "tb_pill_fg",
  )

  Box(
    modifier = Modifier
      .size(34.dp)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clip(CircleShape)
      .background(bgColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = tint,
      modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
private fun OneHandedGutter(
  tokens: KeyboardColorTokens,
  gutterWidth: Dp,
  isRightAligned: Boolean,
  onSwapSide: (() -> Unit)?,
  onExpand: (() -> Unit)?,
) {
  Column(
    modifier = Modifier
      .width(gutterWidth)
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
  ) {
    if (onExpand != null) {
      IconButton(
        onClick = onExpand,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(tokens.toolbarItemBackground),
      ) {
        Icon(
          imageVector = Icons.Rounded.OpenInFull,
          contentDescription = stringResource(R.string.cd_one_handed),
          tint = tokens.keyContent,
          modifier = Modifier.size(20.dp),
        )
      }
    }
    if (onSwapSide != null) {
      IconButton(
        onClick = onSwapSide,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(tokens.toolbarItemBackground),
      ) {
        Icon(
          imageVector = Icons.Rounded.SwapHoriz,
          contentDescription = if (isRightAligned) "Перенести влево" else "Перенести вправо",
          tint = tokens.keyContent,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun CharacterKeyButton(
  modifier: Modifier,
  keySpec: KeySpec.CharacterKey,
  displayLabel: String,
  isNumberRow: Boolean,
  settings: KeyboardSettings,
  tokens: KeyboardColorTokens,
  fontFamily: FontFamily,
  rootPosition: Offset,
  onPressStart: (Offset, IntSize) -> Unit,
  onLongPressTrigger: (Offset, IntSize) -> Unit,
  onDragOption: (Int) -> Unit,
  onRelease: (String?) -> Unit,
) {
  var isPressed by remember { mutableStateOf(false) }
  var myTopLeft by remember { mutableStateOf(Offset.Zero) }
  var mySize by remember { mutableStateOf(IntSize.Zero) }

  val latestDisplay by rememberUpdatedState(displayLabel)
  val latestDelayMs by rememberUpdatedState(settings.longPressDelayMs)
  val scope = rememberCoroutineScope()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1f,
    animationSpec = spring(stiffness = 780f, dampingRatio = 0.65f),
    label = "key_scale",
  )
  val animatedCornerRadius by animateDpAsState(
    targetValue =
      if (isPressed) {
        (settings.keyCornerRadiusDp + 7f).coerceAtMost(26f).dp
      } else {
        settings.keyCornerRadiusDp.dp
      },
    animationSpec = spring(stiffness = 700f, dampingRatio = 0.68f),
    label = "key_morph_radius",
  )
  val bgColor by animateColorAsState(
    targetValue = if (isPressed) tokens.keyPressedBackground else tokens.keyBackground,
    animationSpec = tween(durationMillis = if (isPressed) 45 else 160),
    label = "key_bg",
  )
  val shape = RoundedCornerShape(animatedCornerRadius)
  val elevation =
    if (settings.showKeyElevationShadow &&
      settings.keySurfaceStyle != KeySurfaceStyle.OUTLINED &&
      settings.keySurfaceStyle != KeySurfaceStyle.SOFT_MONOLITH
    ) {
      if (isPressed) 0.dp else 1.5.dp
    } else {
      0.dp
    }

  Box(
    modifier = modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .onGloballyPositioned { coords ->
        myTopLeft = coords.positionInRoot() - rootPosition
        mySize = coords.size
      }
      .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape, clip = false) else Modifier)
      .clip(shape)
      .background(bgColor)
      .then(
        if (tokens.keyBorder != Color.Transparent) {
          Modifier.border(1.dp, tokens.keyBorder, shape)
        } else {
          Modifier
        },
      )
      .pointerInput(keySpec.primary, keySpec.longPressOptions) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          isPressed = true
          onPressStart(myTopLeft, mySize)

          var longPressOpened = false
          var selectedIdx = 0
          val longPressJob =
            if (keySpec.longPressOptions.isNotEmpty()) {
              scope.launch {
                delay(latestDelayMs.toLong())
                longPressOpened = true
                onLongPressTrigger(myTopLeft, mySize)
              }
            } else {
              null
            }

          while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
              change.consume()
              break
            }
            if (longPressOpened && keySpec.longPressOptions.size > 1) {
              val dx = change.position.x - down.position.x
              val stepWidth = 72f
              val idx = ((dx / stepWidth).roundToInt()).coerceIn(0, keySpec.longPressOptions.lastIndex)
              if (idx != selectedIdx) {
                selectedIdx = idx
                onDragOption(idx)
              }
            }
          }

          longPressJob?.cancel()
          isPressed = false
          val committed =
            if (longPressOpened && keySpec.longPressOptions.isNotEmpty()) {
              keySpec.longPressOptions.getOrElse(selectedIdx) { latestDisplay }
            } else {
              latestDisplay
            }
          onRelease(committed)
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    if (settings.showKeyHints && !keySpec.hint.isNullOrEmpty() && !isNumberRow) {
      Text(
        text = keySpec.hint,
        color = tokens.keyHintContent,
        fontSize = 9.5.sp,
        fontFamily = fontFamily,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 3.dp, end = 4.dp),
      )
    }

    Text(
      text = displayLabel,
      color = tokens.keyContent,
      fontSize = (if (isNumberRow) settings.keyFontSizeSp - 2.5f else settings.keyFontSizeSp).sp,
      fontWeight = FontWeight.Medium,
      fontFamily = fontFamily,
      maxLines = 1,
    )
  }
}

@Composable
private fun ActionKeyButton(
  modifier: Modifier,
  keySpec: KeySpec.ActionKey,
  shiftState: ShiftState,
  activeLanguage: KeyboardLanguage,
  imeOptions: Int,
  settings: KeyboardSettings,
  tokens: KeyboardColorTokens,
  fontFamily: FontFamily,
  onTriggerFeedback: (Boolean) -> Unit,
  onAction: (KeyActionType) -> Unit,
  onRepeatBackspace: () -> Unit,
  onDeleteWord: () -> Unit,
  onMoveCursor: (Int) -> Unit,
  onLongPressLanguage: (() -> Unit)?,
) {
  var isPressed by remember { mutableStateOf(false) }
  var isCursorSwiping by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val latestSpaceSwipe by rememberUpdatedState(settings.spaceCursorControl)
  val latestDelayMs by rememberUpdatedState(settings.longPressDelayMs)

  val isAccent =
    keySpec.action == KeyActionType.ENTER ||
      (keySpec.action == KeyActionType.SHIFT && shiftState == ShiftState.CAPS_LOCK)

  val baseBg =
    when {
      isCursorSwiping -> tokens.accentKeyBackground.copy(alpha = 0.25f)
      isAccent -> tokens.accentKeyBackground
      keySpec.action == KeyActionType.SPACE -> tokens.spacebarBackground
      keySpec.action == KeyActionType.SHIFT && shiftState == ShiftState.SINGLE ->
        tokens.accentKeyBackground.copy(alpha = 0.28f)
      else -> tokens.modifierKeyBackground
    }

  val pressedBg =
    if (isAccent) tokens.accentKeyPressedBackground else tokens.modifierKeyPressedBackground

  val contentColor =
    when {
      isAccent -> tokens.accentKeyContent
      isCursorSwiping -> tokens.keyContent
      keySpec.action == KeyActionType.SPACE -> tokens.spacebarContent
      else -> tokens.modifierKeyContent
    }

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1f,
    animationSpec = spring(stiffness = 700f, dampingRatio = 0.68f),
    label = "action_scale",
  )
  val bgColor by animateColorAsState(
    targetValue = if (isPressed && !isCursorSwiping) pressedBg else baseBg,
    animationSpec = tween(durationMillis = if (isPressed) 50 else 170),
    label = "action_bg",
  )

  val baseRadius =
    if (settings.keySurfaceStyle == KeySurfaceStyle.EXPRESSIVE &&
      (keySpec.action == KeyActionType.ENTER || keySpec.action == KeyActionType.SPACE)
    ) {
      (settings.keyCornerRadiusDp + 6f).coerceAtMost(24f)
    } else {
      settings.keyCornerRadiusDp
    }
  val animatedRadius by animateDpAsState(
    targetValue = if (isPressed) (baseRadius + 6f).coerceAtMost(26f).dp else baseRadius.dp,
    animationSpec = spring(stiffness = 680f, dampingRatio = 0.7f),
    label = "action_radius",
  )
  val shape = RoundedCornerShape(animatedRadius)

  val tag =
    when (keySpec.action) {
      KeyActionType.SPACE -> "key_space"
      KeyActionType.ENTER -> "key_enter"
      KeyActionType.BACKSPACE -> "key_backspace"
      KeyActionType.SHIFT -> "key_shift"
      KeyActionType.LANGUAGE -> "key_language"
      KeyActionType.SYMBOLS,
      KeyActionType.SYMBOLS_ALT,
      KeyActionType.LETTERS -> "key_symbols"
      KeyActionType.EMOJI -> "key_emoji"
    }

  Box(
    modifier = modifier
      .testTag(tag)
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .clip(shape)
      .background(bgColor)
      .then(
        if (tokens.keyBorder != Color.Transparent) {
          Modifier.border(1.dp, tokens.keyBorder, shape)
        } else {
          Modifier
        },
      )
      .pointerInput(keySpec.action) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          isPressed = true
          onTriggerFeedback(keySpec.action == KeyActionType.SPACE || keySpec.action == KeyActionType.ENTER)

          var dragConsumed = false
          var accumulatedX = 0f
          var lastX = down.position.x

          val holdJob =
            when (keySpec.action) {
              KeyActionType.BACKSPACE ->
                scope.launch {
                  delay(latestDelayMs.toLong())
                  dragConsumed = true
                  while (true) {
                    onRepeatBackspace()
                    delay(58L)
                  }
                }
              KeyActionType.LANGUAGE ->
                if (onLongPressLanguage != null) {
                  scope.launch {
                    delay(latestDelayMs.toLong() + 80L)
                    dragConsumed = true
                    onLongPressLanguage()
                  }
                } else null
              else -> null
            }

          while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
              change.consume()
              break
            }
            val dx = change.position.x - lastX
            lastX = change.position.x

            if (keySpec.action == KeyActionType.SPACE && latestSpaceSwipe) {
              accumulatedX += dx
              val stepPx = 22f
              if (kotlin.math.abs(accumulatedX) >= stepPx) {
                dragConsumed = true
                isCursorSwiping = true
                val steps = (accumulatedX / stepPx).toInt()
                accumulatedX -= steps * stepPx
                onMoveCursor(steps)
              }
            } else if (keySpec.action == KeyActionType.BACKSPACE) {
              accumulatedX += dx
              if (accumulatedX < -72f && !dragConsumed) {
                dragConsumed = true
                holdJob?.cancel()
                onDeleteWord()
              }
            }
          }

          holdJob?.cancel()
          isPressed = false
          isCursorSwiping = false
          if (!dragConsumed) {
            onAction(keySpec.action)
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    when (keySpec.action) {
      KeyActionType.SHIFT -> {
        val icon =
          if (shiftState == ShiftState.CAPS_LOCK) {
            Icons.Rounded.KeyboardCapslock
          } else {
            Icons.Rounded.ArrowUpward
          }
        val iconScale by animateFloatAsState(
          targetValue = if (shiftState == ShiftState.CAPS_LOCK) 1.15f else 1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
          label = "shift_icon_scale",
        )
        Icon(
          imageVector = icon,
          contentDescription = stringResource(R.string.cd_shift),
          tint = contentColor,
          modifier = Modifier
            .size(21.dp)
            .graphicsLayer {
              scaleX = iconScale
              scaleY = iconScale
            },
        )
      }

      KeyActionType.BACKSPACE -> {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Backspace,
          contentDescription = stringResource(R.string.cd_backspace),
          tint = contentColor,
          modifier = Modifier.size(20.dp),
        )
      }

      KeyActionType.LANGUAGE -> {
        Icon(
          imageVector = Icons.Rounded.Language,
          contentDescription = stringResource(R.string.cd_language),
          tint = contentColor,
          modifier = Modifier.size(18.dp),
        )
      }

      KeyActionType.SPACE -> {
        AnimatedContent(
          targetState = isCursorSwiping to activeLanguage,
          transitionSpec = {
            (fadeIn(tween(150)) + scaleIn(initialScale = 0.92f)) togetherWith
              (fadeOut(tween(110)) + scaleOut(targetScale = 0.92f))
          },
          label = "space_content",
        ) { (swiping, lang) ->
          if (swiping) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Курсор",
                color = contentColor,
                fontSize = 12.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
              )
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
              )
            }
          } else {
            val label =
              if (lang == KeyboardLanguage.RUSSIAN) {
                stringResource(R.string.key_space_ru)
              } else {
                stringResource(R.string.key_space_en)
              }
            Text(
              text = label,
              color = contentColor,
              fontSize = 12.5.sp,
              fontFamily = fontFamily,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }

      KeyActionType.ENTER -> {
        val imeAction = imeOptions and EditorInfo.IME_MASK_ACTION
        val icon =
          when (imeAction) {
            EditorInfo.IME_ACTION_SEARCH -> Icons.Rounded.Search
            EditorInfo.IME_ACTION_SEND -> Icons.AutoMirrored.Rounded.Send
            EditorInfo.IME_ACTION_DONE -> Icons.Rounded.Check
            else -> Icons.AutoMirrored.Rounded.KeyboardReturn
          }
        Icon(
          imageVector = icon,
          contentDescription = stringResource(R.string.cd_enter),
          tint = contentColor,
          modifier = Modifier.size(21.dp),
        )
      }

      KeyActionType.SYMBOLS,
      KeyActionType.SYMBOLS_ALT,
      KeyActionType.LETTERS -> {
        Text(
          text = keySpec.label ?: "?123",
          color = contentColor,
          fontSize = 13.sp,
          fontFamily = fontFamily,
          fontWeight = FontWeight.SemiBold,
        )
      }

      KeyActionType.EMOJI -> {
        Icon(
          imageVector = Icons.Rounded.EmojiEmotions,
          contentDescription = stringResource(R.string.cd_emoji),
          tint = contentColor,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun KeyOverlayPopup(
  overlay: ActiveKeyOverlay,
  tokens: KeyboardColorTokens,
  fontFamily: FontFamily,
) {
  val density = LocalDensity.current
  val keyWidthPx = overlay.anchorSize.width.coerceAtLeast(60)
  val popupHeightDp = 52.dp
  val popupHeightPx = with(density) { popupHeightDp.toPx() }
  val gapPx = with(density) { 8.dp.toPx() }

  val itemCount =
    if (overlay.isLongPressOpen && overlay.longPressOptions.isNotEmpty()) {
      overlay.longPressOptions.size
    } else {
      1
    }
  val targetWidthDp = if (itemCount == 1) 52.dp else (42 * itemCount + 12).dp
  val pillWidthDp by animateDpAsState(
    targetValue = targetWidthDp,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 580f),
    label = "popup_width",
  )
  val pillWidthPx = with(density) { pillWidthDp.toPx() }

  val targetX = (overlay.anchorTopLeft.x + keyWidthPx / 2f - pillWidthPx / 2f)
    .roundToInt()
    .coerceAtLeast(8)
  val targetY = (overlay.anchorTopLeft.y - popupHeightPx - gapPx)
    .roundToInt()
    .coerceAtLeast(4)

  Surface(
    modifier = Modifier
      .offset { IntOffset(targetX, targetY) }
      .height(popupHeightDp)
      .width(pillWidthDp),
    shape = RoundedCornerShape(18.dp),
    color = tokens.popupBackground,
    contentColor = tokens.popupContent,
    shadowElevation = 10.dp,
    tonalElevation = 6.dp,
  ) {
    if (!overlay.isLongPressOpen || overlay.longPressOptions.isEmpty()) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = overlay.label,
          fontSize = 23.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = fontFamily,
        )
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        overlay.longPressOptions.forEachIndexed { idx, opt ->
          val selected = idx == overlay.selectedOptionIndex
          val pillBg by animateColorAsState(
            targetValue = if (selected) tokens.accentKeyBackground else Color.Transparent,
            animationSpec = tween(90),
            label = "opt_bg",
          )
          val pillScale by animateFloatAsState(
            targetValue = if (selected) 1.06f else 0.96f,
            animationSpec = spring(dampingRatio = 0.68f, stiffness = 620f),
            label = "opt_scale",
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(0.8f)
              .graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
              }
              .clip(RoundedCornerShape(12.dp))
              .background(pillBg),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = opt,
              color = if (selected) tokens.accentKeyContent else tokens.popupContent,
              fontSize = 19.sp,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              fontFamily = fontFamily,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmojiKeyboardPanel(
  tokens: KeyboardColorTokens,
  settings: KeyboardSettings,
  panelHeight: Dp,
  onSelectEmoji: (String) -> Unit,
  onBackspace: () -> Unit,
  onBackToLetters: () -> Unit,
) {
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
        val chipBg by animateColorAsState(
          targetValue = if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground,
          animationSpec = spring(stiffness = Spring.StiffnessMedium),
          label = "emoji_tab_bg",
        )
        Box(
          modifier = Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(chipBg)
            .clickable { selectedCategoryIdx = idx },
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = cat.label,
            color = if (selected) tokens.accentKeyContent else tokens.keyContent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    AnimatedContent(
      targetState = categories[selectedCategoryIdx],
      transitionSpec = {
        fadeIn(tween(140)) togetherWith fadeOut(tween(100))
      },
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      label = "emoji_grid",
    ) { activeCategory ->
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
              .background(tokens.keyBackground.copy(alpha = 0.7f))
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
          text = "АБВ",
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
            contentDescription = stringResource(R.string.cd_backspace),
            tint = tokens.modifierKeyContent,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ClipboardSnippetsPanel(
  tokens: KeyboardColorTokens,
  settings: KeyboardSettings,
  panelHeight: Dp,
  onPasteText: (String) -> Unit,
  onBackToLetters: () -> Unit,
) {
  val context = LocalContext.current
  val currentClipText = remember {
    runCatching {
      val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      cm?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
    }.getOrNull()
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(panelHeight)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = "Буфер обмена и быстрые фразы",
      color = tokens.keyHintContent,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (!currentClipText.isNullOrBlank()) {
        Surface(
          onClick = { onPasteText(currentClipText) },
          shape = RoundedCornerShape(settings.keyCornerRadiusDp.dp),
          color = tokens.accentKeyBackground.copy(alpha = 0.22f),
          border = BorderStroke(1.dp, tokens.accentKeyBackground.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.ContentPaste,
              contentDescription = null,
              tint = tokens.keyContent,
              modifier = Modifier.size(16.dp),
            )
            Text(
              text = currentClipText,
              color = tokens.keyContent,
              fontSize = 13.5.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      KeyboardLayouts.quickTemplates.take(3).forEach { phrase ->
        Surface(
          onClick = { onPasteText(phrase) },
          shape = RoundedCornerShape(settings.keyCornerRadiusDp.dp),
          color = tokens.keyBackground,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = phrase,
            color = tokens.keyContent,
            fontSize = 13.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
          )
        }
      }
    }

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
          text = "Вернуться к буквам",
          color = tokens.modifierKeyContent,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }
  }
}
