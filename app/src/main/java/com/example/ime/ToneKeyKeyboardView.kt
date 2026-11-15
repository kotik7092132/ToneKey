package com.example.ime

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnimationPreset
import com.example.model.ClipCategory
import com.example.model.ClipboardItem
import com.example.model.CustomizableActionKey
import com.example.model.KeySurfaceStyle
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.i18n.LocalAppStrings
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

private class KeyBoundsHolder {
  var topLeft: Offset = Offset.Zero
  var size: IntSize = IntSize.Zero
}

private class BoardPositionHolder {
  var positionInRoot: Offset = Offset.Zero
}

@Composable
fun ToneKeyKeyboardView(
  settings: KeyboardSettings,
  sink: InputSink,
  activeLanguage: KeyboardLanguage,
  onLanguageChange: (KeyboardLanguage) -> Unit,
  modifier: Modifier = Modifier,
  imeOptions: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
  inputSessionTick: Int = 0,
  clipboardItems: List<ClipboardItem> = emptyList(),
  smartSuggestionClip: ClipboardItem? = null,
  onTogglePinClip: (String) -> Unit = {},
  onDeleteClip: (String) -> Unit = {},
  onClearUnpinnedClips: () -> Unit = {},
  onDismissSmartSuggestion: (String) -> Unit = {},
  onOpenSettings: (() -> Unit)? = null,
  onOpenImePicker: (() -> Unit)? = null,
  onToggleOneHanded: (() -> Unit)? = null,
  onSwapOneHandedSide: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val systemDark = isSystemInDarkTheme()
  val isHeavyAnim = settings.animationPreset == AnimationPreset.HEAVY

  val targetTokens = remember(settings, systemDark, context) {
    buildKeyboardColorTokens(context, settings, systemDark)
  }
  val tokens = rememberAnimatedKeyboardColorTokens(targetTokens, animate = isHeavyAnim)
  val feedback = remember(context) { KeyboardFeedbackHelper(context) }
  val processor = remember(sink) { ToneKeyInputProcessor(sink) }

  var currentPage by remember { mutableStateOf(KeyboardPage.LETTERS) }
  var currentPanel by remember { mutableStateOf(KeyboardPanel.BOARD) }
  var shiftState by remember { mutableStateOf(ShiftState.SINGLE) }
  var lastShiftTapAt by remember { mutableStateOf(0L) }
  var isPcBarActive by remember { mutableStateOf(false) }
  var ctrlActive by remember { mutableStateOf(false) }
  var altActive by remember { mutableStateOf(false) }
  var fnActive by remember { mutableStateOf(true) }
  var overlay by remember { mutableStateOf<ActiveKeyOverlay?>(null) }
  var lastVisibleOverlay by remember { mutableStateOf<ActiveKeyOverlay?>(null) }
  val boardPosition = remember { BoardPositionHolder() }

  if (overlay != null) {
    lastVisibleOverlay = overlay
  }

  val latestSettings by rememberUpdatedState(settings)
  val latestShift by rememberUpdatedState(shiftState)
  val latestLanguage by rememberUpdatedState(activeLanguage)
  val latestCtrl by rememberUpdatedState(ctrlActive)
  val latestAlt by rememberUpdatedState(altActive)
  val latestFn by rememberUpdatedState(fnActive)
  val latestPcBarActive by rememberUpdatedState(isPcBarActive)

  LaunchedEffect(inputSessionTick, settings.autoCapitalize) {
    currentPage = KeyboardPage.LETTERS
    currentPanel = KeyboardPanel.BOARD
    ctrlActive = false
    altActive = false
    shiftState = if (processor.shouldAutoCapitalize(settings)) ShiftState.SINGLE else ShiftState.OFF
  }

  LaunchedEffect(settings.enablePcMode) {
    if (!settings.enablePcMode) {
      isPcBarActive = false
      ctrlActive = false
      altActive = false
    }
  }

  val buildMetaState: (ShiftState) -> Int = { shift ->
    var meta = 0
    if (latestCtrl) meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
    if (latestAlt) meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
    if (shift != ShiftState.OFF) meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
    if (latestPcBarActive && latestFn) meta = meta or KeyEvent.META_FUNCTION_ON
    meta
  }

  val togglePcBar = {
    feedback.triggerTickFeedback(latestSettings)
    val next = !latestPcBarActive
    isPcBarActive = next
    if (next) {
      fnActive = true
      currentPanel = KeyboardPanel.BOARD
    } else {
      ctrlActive = false
      altActive = false
    }
  }

  val targetBaseRowHeight = (46f * settings.keyboardHeightScale).dp
  val targetNumberRowHeight = (36f * settings.keyboardHeightScale).dp
  val targetBottomPad = settings.bottomPaddingDp.dp
  val targetGapH = settings.keyGapHorizontalDp.dp
  val targetGapV = settings.keyGapVerticalDp.dp
  val targetBoardFraction = if (settings.oneHandedMode == OneHandedMode.OFF) 1f else 0.84f

  val baseRowHeight: Dp
  val numberRowHeight: Dp
  val animatedBottomPadding: Dp
  val animatedGapH: Dp
  val animatedGapV: Dp
  val boardContentWidthFraction: Float

  if (isHeavyAnim) {
    val geometrySpring = spring<Dp>(
      dampingRatio = Spring.DampingRatioLowBouncy,
      stiffness = Spring.StiffnessMediumLow,
    )
    baseRowHeight = animateDpAsState(targetBaseRowHeight, geometrySpring, label = "row_h").value
    numberRowHeight = animateDpAsState(targetNumberRowHeight, geometrySpring, label = "num_h").value
    animatedBottomPadding = animateDpAsState(targetBottomPad, geometrySpring, label = "bot_pad").value
    animatedGapH = animateDpAsState(targetGapH, geometrySpring, label = "gap_h").value
    animatedGapV = animateDpAsState(targetGapV, geometrySpring, label = "gap_v").value
    boardContentWidthFraction = animateFloatAsState(
      targetValue = targetBoardFraction,
      animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f),
      label = "one_hand_frac",
    ).value
  } else {
    baseRowHeight = targetBaseRowHeight
    numberRowHeight = targetNumberRowHeight
    animatedBottomPadding = targetBottomPad
    animatedGapH = targetGapH
    animatedGapV = targetGapV
    boardContentWidthFraction = targetBoardFraction
  }

  val toolbarHeight = 42.dp
  val keyFontFamily = settings.keyFontFamily.toComposeFontFamily()
  val sharedCharKeyShape = remember(settings.keyCornerRadiusDp) {
    RoundedCornerShape(settings.keyCornerRadiusDp.dp)
  }
  val expressivePillShape = remember(settings.keyCornerRadiusDp) {
    RoundedCornerShape((settings.keyCornerRadiusDp + 6f).coerceAtMost(24f).dp)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(tokens.boardBackground)
      .onGloballyPositioned { coords ->
        boardPosition.positionInRoot = coords.positionInRoot()
      }
      .padding(bottom = animatedBottomPadding)
      .testTag("keyboard_surface"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      if (settings.showToolbar) {
        KeyboardToolbar(
          tokens = tokens,
          currentPanel = currentPanel,
          isOneHanded = settings.oneHandedMode != OneHandedMode.OFF,
          isPcModeEnabled = settings.enablePcMode,
          isPcBarActive = isPcBarActive,
          isHeavyAnim = isHeavyAnim,
          toolbarHeight = toolbarHeight,
          smartSuggestionClip = smartSuggestionClip,
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
          onTogglePcMode = togglePcBar,
          onToggleOneHanded = onToggleOneHanded,
          onOpenSettings = onOpenSettings,
          onQuickInsert = { text ->
            feedback.triggerKeyFeedback(latestSettings)
            shiftState = processor.onCharacterCommitted(text, latestShift, latestSettings)
          },
          onPasteSmartSuggestion = { clip ->
            feedback.triggerKeyFeedback(latestSettings)
            sink.commitText(clip.smartExtract ?: clip.text)
            onDismissSmartSuggestion(clip.id)
          },
        )
      } else if (settings.enablePcMode) {
        PcCompactQuickToggleBar(
          tokens = tokens,
          isPcBarActive = isPcBarActive,
          onTogglePcMode = togglePcBar,
          onOpenSettings = onOpenSettings,
        )
      }

      if (settings.enablePcMode && isPcBarActive && currentPanel == KeyboardPanel.BOARD) {
        PcKeysStrip(
          tokens = tokens,
          settings = settings,
          ctrlActive = ctrlActive,
          altActive = altActive,
          fnActive = fnActive,
          gapH = animatedGapH,
          gapV = animatedGapV,
          onToggleCtrl = {
            feedback.triggerTickFeedback(latestSettings)
            ctrlActive = !ctrlActive
          },
          onToggleAlt = {
            feedback.triggerTickFeedback(latestSettings)
            altActive = !altActive
          },
          onToggleFn = {
            feedback.triggerTickFeedback(latestSettings)
            fnActive = !fnActive
          },
          onSendKey = { keyCode ->
            feedback.triggerKeyFeedback(latestSettings, isSpaceOrAction = true)
            sink.sendHardwareKey(keyCode, buildMetaState(latestShift))
            ctrlActive = false
            altActive = false
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
            val renderPanel: @Composable (KeyboardPanel) -> Unit = { panel ->
              when (panel) {
                KeyboardPanel.BOARD -> {
                  val renderPage: @Composable (KeyboardPage) -> Unit = { page ->
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
                                  isHeavyAnim = isHeavyAnim,
                                  keyShape = sharedCharKeyShape,
                                  settings = settings,
                                  tokens = tokens,
                                  fontFamily = keyFontFamily,
                                  boardPosition = boardPosition,
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
                                      val modifierComboActive = latestCtrl || latestAlt
                                      val mappedKeyCode =
                                        if (modifierComboActive) PcKeyMapper.keyCodeForChar(committed) else null
                                      if (modifierComboActive && mappedKeyCode != null) {
                                        sink.sendHardwareKey(mappedKeyCode, buildMetaState(latestShift))
                                        ctrlActive = false
                                        altActive = false
                                      } else {
                                        shiftState = processor.onCharacterCommitted(
                                          committed,
                                          latestShift,
                                          latestSettings,
                                        )
                                      }
                                    }
                                  },
                                )
                              }

                              is KeySpec.ActionKey -> {
                                val actionShape =
                                  if (settings.keySurfaceStyle == KeySurfaceStyle.EXPRESSIVE &&
                                    (keySpec.action == KeyActionType.ENTER || keySpec.action == KeyActionType.SPACE)
                                  ) {
                                    expressivePillShape
                                  } else {
                                    sharedCharKeyShape
                                  }
                                ActionKeyButton(
                                  modifier = Modifier
                                    .weight(keySpec.weight)
                                    .fillMaxHeight(),
                                  keySpec = keySpec,
                                  keyShape = actionShape,
                                  shiftState = shiftState,
                                  activeLanguage = latestLanguage,
                                  imeOptions = imeOptions,
                                  isHeavyAnim = isHeavyAnim,
                                  settings = settings,
                                  tokens = tokens,
                                  fontFamily = keyFontFamily,
                                  onTriggerFeedback = { isSpace ->
                                    feedback.triggerKeyFeedback(latestSettings, isSpace)
                                  },
                                  onAction = { action ->
                                    val modifierComboActive = latestCtrl || latestAlt
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
                                        if (modifierComboActive) {
                                          sink.sendHardwareKey(KeyEvent.KEYCODE_DEL, buildMetaState(latestShift))
                                          ctrlActive = false
                                          altActive = false
                                        } else {
                                          shiftState = processor.onBackspace(latestSettings)
                                        }
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
                                        if (modifierComboActive) {
                                          sink.sendHardwareKey(KeyEvent.KEYCODE_SPACE, buildMetaState(latestShift))
                                          ctrlActive = false
                                          altActive = false
                                        } else {
                                          shiftState = processor.onSpacePressed(latestShift, latestSettings)
                                        }
                                      }

                                      KeyActionType.ENTER -> {
                                        if (modifierComboActive) {
                                          sink.sendHardwareKey(KeyEvent.KEYCODE_ENTER, buildMetaState(latestShift))
                                          ctrlActive = false
                                          altActive = false
                                        } else {
                                          shiftState = processor.onEnter(latestSettings)
                                        }
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

                  if (isHeavyAnim) {
                    AnimatedContent(
                      targetState = currentPage,
                      transitionSpec = {
                        (fadeIn(tween(75)) togetherWith fadeOut(tween(55))) using
                          SizeTransform(clip = false)
                      },
                      label = "keyboard_page",
                    ) { page ->
                      renderPage(page)
                    }
                  } else {
                    renderPage(currentPage)
                  }
                }

                KeyboardPanel.EMOJI -> {
                  EmojiKeyboardPanel(
                    tokens = tokens,
                    settings = settings,
                    isHeavyAnim = isHeavyAnim,
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
                  SmartClipboardPanel(
                    tokens = tokens,
                    settings = settings,
                    clipboardItems = clipboardItems,
                    panelHeight = (baseRowHeight * 4) + (animatedGapV * 4) +
                      (if (settings.showNumberRow) numberRowHeight else 0.dp),
                    onPasteText = { text ->
                      feedback.triggerKeyFeedback(latestSettings)
                      sink.commitText(text)
                      currentPanel = KeyboardPanel.BOARD
                    },
                    onTogglePinClip = { id ->
                      feedback.triggerTickFeedback(latestSettings)
                      onTogglePinClip(id)
                    },
                    onDeleteClip = { id ->
                      feedback.triggerTickFeedback(latestSettings)
                      onDeleteClip(id)
                    },
                    onClearUnpinned = {
                      feedback.triggerTickFeedback(latestSettings)
                      onClearUnpinnedClips()
                    },
                    onBackToLetters = {
                      feedback.triggerTickFeedback(latestSettings)
                      currentPanel = KeyboardPanel.BOARD
                    },
                  )
                }
              }
            }

            if (isHeavyAnim) {
              AnimatedContent(
                targetState = currentPanel,
                transitionSpec = {
                  (fadeIn(tween(150)) +
                    slideInVertically(
                      initialOffsetY = { it / 12 },
                      animationSpec = tween(160),
                    )) togetherWith
                    (fadeOut(tween(100))) using
                    SizeTransform(clip = false)
                },
                label = "keyboard_panel",
              ) { panel ->
                renderPanel(panel)
              }
            } else {
              renderPanel(currentPanel)
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

    if (isHeavyAnim) {
      AnimatedVisibility(
        visible = overlay != null,
        enter = fadeIn(tween(70)) +
          scaleIn(
            initialScale = 0.76f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 750f),
          ) +
          slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 720f),
          ),
        exit = fadeOut(tween(90)) + scaleOut(targetScale = 0.84f),
      ) {
        lastVisibleOverlay?.let { active ->
          KeyOverlayPopup(
            overlay = active,
            tokens = tokens,
            fontFamily = keyFontFamily,
            isHeavyAnim = true,
          )
        }
      }
    } else {
      overlay?.let { active ->
        KeyOverlayPopup(
          overlay = active,
          tokens = tokens,
          fontFamily = keyFontFamily,
          isHeavyAnim = false,
        )
      }
    }
  }
}

@Composable
private fun PcCompactQuickToggleBar(
  tokens: KeyboardColorTokens,
  isPcBarActive: Boolean,
  onTogglePcMode: () -> Unit,
  onOpenSettings: (() -> Unit)?,
) {
  val strings = LocalAppStrings.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .background(tokens.toolbarBackground)
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Surface(
      onClick = onTogglePcMode,
      shape = RoundedCornerShape(50),
      color = if (isPcBarActive) tokens.accentKeyBackground else tokens.toolbarItemBackground,
      modifier = Modifier
        .height(28.dp)
        .testTag("toolbar_pc_toggle"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Terminal,
          contentDescription = strings.cdPcMode,
          tint = if (isPcBarActive) tokens.accentKeyContent else tokens.modifierKeyContent,
          modifier = Modifier.size(15.dp),
        )
        Text(
          text = strings.pcToolbarLabel,
          color = if (isPcBarActive) tokens.accentKeyContent else tokens.keyContent,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    if (onOpenSettings != null) {
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier.size(30.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Tune,
          contentDescription = strings.cdSettings,
          tint = tokens.modifierKeyContent,
          modifier = Modifier.size(17.dp),
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
  isPcModeEnabled: Boolean,
  isPcBarActive: Boolean,
  isHeavyAnim: Boolean,
  toolbarHeight: Dp,
  smartSuggestionClip: ClipboardItem?,
  onToggleEmoji: () -> Unit,
  onToggleClipboard: () -> Unit,
  onTogglePcMode: () -> Unit,
  onToggleOneHanded: (() -> Unit)?,
  onOpenSettings: (() -> Unit)?,
  onQuickInsert: (String) -> Unit,
  onPasteSmartSuggestion: (ClipboardItem) -> Unit,
) {
  val strings = LocalAppStrings.current
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
      contentDescription = strings.cdEmoji,
      selected = currentPanel == KeyboardPanel.EMOJI,
      isHeavyAnim = isHeavyAnim,
      tokens = tokens,
      onClick = onToggleEmoji,
    )
    ToolbarIconPill(
      icon = Icons.Rounded.ContentPaste,
      contentDescription = strings.cdClipboard,
      selected = currentPanel == KeyboardPanel.CLIPBOARD,
      isHeavyAnim = isHeavyAnim,
      tokens = tokens,
      onClick = onToggleClipboard,
    )

    if (isPcModeEnabled) {
      Surface(
        onClick = onTogglePcMode,
        shape = RoundedCornerShape(50),
        color = if (isPcBarActive) tokens.accentKeyBackground else tokens.toolbarItemBackground,
        modifier = Modifier
          .height(32.dp)
          .testTag("toolbar_pc_toggle"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Terminal,
            contentDescription = strings.cdPcMode,
            tint = if (isPcBarActive) tokens.accentKeyContent else tokens.modifierKeyContent,
            modifier = Modifier.size(15.dp),
          )
          Text(
            text = strings.pcToolbarLabel,
            color = if (isPcBarActive) tokens.accentKeyContent else tokens.keyContent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
          )
        }
      }
    }

    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (smartSuggestionClip != null && currentPanel != KeyboardPanel.CLIPBOARD) {
        val pillLabel =
          when {
            smartSuggestionClip.category == ClipCategory.OTP && smartSuggestionClip.smartExtract != null ->
              "${strings.clipSmartPillCodePrefix}${smartSuggestionClip.smartExtract}"
            else -> smartSuggestionClip.smartExtract ?: smartSuggestionClip.text
          }
        Surface(
          onClick = { onPasteSmartSuggestion(smartSuggestionClip) },
          shape = RoundedCornerShape(50),
          color = tokens.colorScheme.primaryContainer,
          modifier = Modifier
            .height(30.dp)
            .widthIn(max = 176.dp)
            .testTag("toolbar_smart_paste_pill"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Icon(
              imageVector = clipCategoryIcon(smartSuggestionClip.category),
              contentDescription = null,
              tint = tokens.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = pillLabel,
              color = tokens.colorScheme.onPrimaryContainer,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

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
        contentDescription = strings.cdOneHanded,
        selected = isOneHanded,
        isHeavyAnim = isHeavyAnim,
        tokens = tokens,
        onClick = onToggleOneHanded,
      )
    }
    if (onOpenSettings != null) {
      ToolbarIconPill(
        icon = Icons.Rounded.Tune,
        contentDescription = strings.cdSettings,
        selected = false,
        isHeavyAnim = isHeavyAnim,
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
  isHeavyAnim: Boolean,
  tokens: KeyboardColorTokens,
  onClick: () -> Unit,
) {
  val bgColor =
    if (isHeavyAnim) {
      animateColorAsState(
        targetValue = if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground,
        animationSpec = tween(140),
        label = "tb_pill_bg",
      ).value
    } else {
      if (selected) tokens.accentKeyBackground else tokens.toolbarItemBackground
    }
  val tint =
    if (selected) tokens.accentKeyContent else tokens.modifierKeyContent

  Box(
    modifier = Modifier
      .size(34.dp)
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
  val strings = LocalAppStrings.current
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
          contentDescription = strings.cdOneHanded,
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
          contentDescription = if (isRightAligned) strings.moveLeft else strings.moveRight,
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
  isHeavyAnim: Boolean,
  keyShape: RoundedCornerShape,
  settings: KeyboardSettings,
  tokens: KeyboardColorTokens,
  fontFamily: FontFamily,
  boardPosition: BoardPositionHolder,
  onPressStart: (Offset, IntSize) -> Unit,
  onLongPressTrigger: (Offset, IntSize) -> Unit,
  onDragOption: (Int) -> Unit,
  onRelease: (String?) -> Unit,
) {
  val bounds = remember { KeyBoundsHolder() }
  val pressAnim = remember { Animatable(0f) }
  var isPressedFast by remember { mutableStateOf(false) }

  val latestDisplay by rememberUpdatedState(displayLabel)
  val latestDelayMs by rememberUpdatedState(settings.longPressDelayMs)
  val latestHeavy by rememberUpdatedState(isHeavyAnim)
  val scope = rememberCoroutineScope()

  val elevation =
    if (settings.showKeyElevationShadow &&
      settings.keyAlpha >= 0.98f &&
      settings.keySurfaceStyle != KeySurfaceStyle.OUTLINED &&
      settings.keySurfaceStyle != KeySurfaceStyle.SOFT_MONOLITH
    ) {
      1.5.dp
    } else {
      0.dp
    }

  Box(
    modifier = modifier
      .graphicsLayer {
        if (latestHeavy) {
          val s = 1f - 0.08f * pressAnim.value
          scaleX = s
          scaleY = s
        }
      }
      .onGloballyPositioned { coords ->
        bounds.topLeft = coords.positionInRoot() - boardPosition.positionInRoot
        bounds.size = coords.size
      }
      .then(if (elevation > 0.dp) Modifier.shadow(elevation, keyShape, clip = false) else Modifier)
      .clip(keyShape)
      .drawBehind {
        val color =
          if (latestHeavy) {
            val p = pressAnim.value
            if (p <= 0.001f) tokens.keyBackground else lerp(tokens.keyBackground, tokens.keyPressedBackground, p)
          } else {
            if (isPressedFast) tokens.keyPressedBackground else tokens.keyBackground
          }
        drawRect(color)
      }
      .then(
        if (tokens.keyBorder != Color.Transparent) {
          Modifier.border(1.dp, tokens.keyBorder, keyShape)
        } else {
          Modifier
        },
      )
      .pointerInput(keySpec.primary, keySpec.longPressOptions) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          if (latestHeavy) {
            scope.launch {
              pressAnim.animateTo(1f, tween(durationMillis = 45))
            }
          } else {
            isPressedFast = true
          }
          onPressStart(bounds.topLeft, bounds.size)

          var longPressOpened = false
          var selectedIdx = 0
          val longPressJob =
            if (keySpec.longPressOptions.isNotEmpty()) {
              scope.launch {
                delay(latestDelayMs.toLong())
                longPressOpened = true
                onLongPressTrigger(bounds.topLeft, bounds.size)
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
          if (latestHeavy) {
            scope.launch {
              pressAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = 720f, dampingRatio = 0.72f),
              )
            }
          } else {
            isPressedFast = false
          }
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
  keyShape: RoundedCornerShape,
  shiftState: ShiftState,
  activeLanguage: KeyboardLanguage,
  imeOptions: Int,
  isHeavyAnim: Boolean,
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
  val strings = LocalAppStrings.current
  val pressAnim = remember { Animatable(0f) }
  var isPressedFast by remember { mutableStateOf(false) }
  var isCursorSwiping by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val latestSpaceSwipe by rememberUpdatedState(settings.spaceCursorControl)
  val latestDelayMs by rememberUpdatedState(settings.longPressDelayMs)
  val latestHeavy by rememberUpdatedState(isHeavyAnim)

  val customKey =
    when (keySpec.action) {
      KeyActionType.ENTER -> CustomizableActionKey.ENTER
      KeyActionType.BACKSPACE -> CustomizableActionKey.BACKSPACE
      KeyActionType.SHIFT -> CustomizableActionKey.SHIFT
      KeyActionType.SYMBOLS,
      KeyActionType.SYMBOLS_ALT,
      KeyActionType.LETTERS -> CustomizableActionKey.SYMBOLS
      KeyActionType.LANGUAGE -> CustomizableActionKey.GLOBE
      else -> null
    }
  val colorOverride = customKey?.let { settings.colorOverrideFor(it) }
  val customBgRaw = colorOverride?.backgroundArgb?.let { Color(it) }
  val customFgRaw = colorOverride?.contentArgb?.let { Color(it) }

  val isAccent =
    keySpec.action == KeyActionType.ENTER ||
      (keySpec.action == KeyActionType.SHIFT && shiftState == ShiftState.CAPS_LOCK)

  val defaultBg =
    when {
      isCursorSwiping -> tokens.accentKeyBackground.copy(alpha = 0.25f)
      isAccent -> tokens.accentKeyBackground
      keySpec.action == KeyActionType.SPACE -> tokens.spacebarBackground
      keySpec.action == KeyActionType.SHIFT && shiftState == ShiftState.SINGLE ->
        tokens.accentKeyBackground.copy(alpha = 0.28f)
      else -> tokens.modifierKeyBackground
    }

  val baseBg =
    when {
      isCursorSwiping -> defaultBg
      customBgRaw != null -> {
        val alphaMultiplier =
          if (keySpec.action == KeyActionType.SHIFT && shiftState == ShiftState.OFF) {
            0.72f
          } else {
            1f
          }
        customBgRaw.copy(alpha = (settings.keyAlpha * alphaMultiplier).coerceIn(0.12f, 1f))
      }
      else -> defaultBg
    }

  val pressedBg =
    if (customBgRaw != null) {
      lerp(
        customBgRaw,
        if (customBgRaw.luminance() > 0.5f) Color.Black else Color.White,
        0.22f,
      ).copy(alpha = (settings.keyAlpha + 0.15f).coerceAtMost(1f))
    } else if (isAccent) {
      tokens.accentKeyPressedBackground
    } else {
      tokens.modifierKeyPressedBackground
    }

  val contentColor =
    when {
      isCursorSwiping -> tokens.keyContent
      customFgRaw != null -> customFgRaw
      customBgRaw != null ->
        if (customBgRaw.luminance() > 0.52f) Color(0xFF16171C) else Color.White
      isAccent -> tokens.accentKeyContent
      keySpec.action == KeyActionType.SPACE -> tokens.spacebarContent
      else -> tokens.modifierKeyContent
    }

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
        if (latestHeavy) {
          val s = 1f - 0.06f * pressAnim.value
          scaleX = s
          scaleY = s
        }
      }
      .clip(keyShape)
      .drawBehind {
        val color =
          if (isCursorSwiping) {
            baseBg
          } else if (latestHeavy) {
            val p = pressAnim.value
            if (p <= 0.001f) baseBg else lerp(baseBg, pressedBg, p)
          } else {
            if (isPressedFast) pressedBg else baseBg
          }
        drawRect(color)
      }
      .then(
        if (tokens.keyBorder != Color.Transparent) {
          Modifier.border(1.dp, tokens.keyBorder, keyShape)
        } else {
          Modifier
        },
      )
      .pointerInput(keySpec.action) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          if (latestHeavy) {
            scope.launch {
              pressAnim.animateTo(1f, tween(durationMillis = 50))
            }
          } else {
            isPressedFast = true
          }
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
          if (latestHeavy) {
            scope.launch {
              pressAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = 680f, dampingRatio = 0.72f),
              )
            }
          } else {
            isPressedFast = false
          }
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
        Icon(
          imageVector = icon,
          contentDescription = strings.cdShift,
          tint = contentColor,
          modifier = Modifier.size(21.dp),
        )
      }

      KeyActionType.BACKSPACE -> {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Backspace,
          contentDescription = strings.cdBackspace,
          tint = contentColor,
          modifier = Modifier.size(20.dp),
        )
      }

      KeyActionType.LANGUAGE -> {
        Icon(
          imageVector = Icons.Rounded.Language,
          contentDescription = strings.cdLanguage,
          tint = contentColor,
          modifier = Modifier.size(18.dp),
        )
      }

      KeyActionType.SPACE -> {
        if (isCursorSwiping) {
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
              text = strings.cursorLabel,
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
          val label = if (activeLanguage == KeyboardLanguage.RUSSIAN) "Русский" else "English"
          Text(
            text = label,
            color = contentColor,
            fontSize = 12.5.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
          )
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
          contentDescription = strings.cdEnter,
          tint = contentColor,
          modifier = Modifier.size(21.dp),
        )
      }

      KeyActionType.SYMBOLS,
      KeyActionType.SYMBOLS_ALT,
      KeyActionType.LETTERS -> {
        val labelText =
          if (keySpec.action == KeyActionType.LETTERS) {
            if (activeLanguage == KeyboardLanguage.ENGLISH) "ABC" else "АБВ"
          } else {
            keySpec.label ?: "?123"
          }
        Text(
          text = labelText,
          color = contentColor,
          fontSize = 13.sp,
          fontFamily = fontFamily,
          fontWeight = FontWeight.SemiBold,
        )
      }

      KeyActionType.EMOJI -> {
        Icon(
          imageVector = Icons.Rounded.EmojiEmotions,
          contentDescription = strings.cdEmoji,
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
  isHeavyAnim: Boolean,
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
  val pillWidthDp =
    if (isHeavyAnim) {
      animateDpAsState(
        targetValue = targetWidthDp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 580f),
        label = "popup_width",
      ).value
    } else {
      targetWidthDp
    }
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
    border = BorderStroke(1.dp, tokens.colorScheme.outlineVariant.copy(alpha = 0.45f)),
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
          val pillBg =
            if (isHeavyAnim) {
              animateColorAsState(
                targetValue = if (selected) tokens.accentKeyBackground else Color.Transparent,
                animationSpec = tween(90),
                label = "opt_bg",
              ).value
            } else {
              if (selected) tokens.accentKeyBackground else Color.Transparent
            }
          val pillScale =
            if (isHeavyAnim) {
              animateFloatAsState(
                targetValue = if (selected) 1.06f else 0.96f,
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 620f),
                label = "opt_scale",
              ).value
            } else {
              1f
            }
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(0.8f)
              .then(
                if (isHeavyAnim) {
                  Modifier.graphicsLayer {
                    scaleX = pillScale
                    scaleY = pillScale
                  }
                } else {
                  Modifier
                },
              )
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
