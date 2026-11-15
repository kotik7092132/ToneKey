package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AnimationPreset
import com.example.model.ClipboardItem
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.studio.GeometryTab
import com.example.ui.studio.ImeActivationStatus
import com.example.ui.studio.LiveSandboxPreviewCard
import com.example.ui.studio.PaletteStyleTab
import com.example.ui.studio.StudioHomeTab
import com.example.ui.studio.StudioTab
import com.example.ui.studio.StudioViewModel
import com.example.ui.studio.TypingHapticsTab
import com.example.ui.theme.ToneKeyTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ToneKeyStudioRoot()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToneKeyStudioRoot(
  viewModel: StudioViewModel = viewModel(),
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()
  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val previewLanguage by viewModel.previewLanguage.collectAsStateWithLifecycle()
  val sandboxText by viewModel.sandboxText.collectAsStateWithLifecycle()
  val isLivePreviewExpanded by viewModel.isLivePreviewExpanded.collectAsStateWithLifecycle()
  val clipboardItems by viewModel.clipboardItems.collectAsStateWithLifecycle()
  val smartSuggestionClip by viewModel.smartSuggestionClip.collectAsStateWithLifecycle()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.refreshImeStatus()
  }

  BackHandler(enabled = activeTab != StudioTab.STUDIO) {
    viewModel.selectTab(StudioTab.STUDIO)
  }

  val openImeSettings = {
    runCatching {
      context.startActivity(
        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
      )
    }
    Unit
  }

  val openImePicker = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showInputMethodPicker()
    Unit
  }

  val toggleOneHanded = {
    val next =
      if (settings.oneHandedMode == OneHandedMode.OFF) {
        OneHandedMode.RIGHT
      } else {
        OneHandedMode.OFF
      }
    viewModel.setOneHandedMode(next)
  }

  val swapOneHandedSide = {
    val next =
      if (settings.oneHandedMode == OneHandedMode.RIGHT) {
        OneHandedMode.LEFT
      } else {
        OneHandedMode.RIGHT
      }
    viewModel.setOneHandedMode(next)
  }

  ToneKeyTheme(settings = settings) {
    val strings = LocalAppStrings.current
    val primaryWash = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
    val tertiaryWash = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.16f)

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .background(
          Brush.radialGradient(
            colors = listOf(primaryWash, Color.Transparent),
            center = Offset(120f, 80f),
            radius = 950f,
          ),
        )
        .background(
          Brush.radialGradient(
            colors = listOf(tertiaryWash, Color.Transparent),
            center = Offset(980f, 340f),
            radius = 820f,
          ),
        ),
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        if (isWideScreen) {
          Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
          ) { innerPadding ->
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            ) {
              NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
              ) {
                StudioTab.entries.forEach { tab ->
                  val selected = activeTab == tab
                  val label = strings.tabTitle(tab)
                  NavigationRailItem(
                    selected = selected,
                    onClick = { viewModel.selectTab(tab) },
                    icon = {
                      Icon(
                        imageVector = tabIcon(tab, selected),
                        contentDescription = label,
                      )
                    },
                    label = { Text(label) },
                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                  )
                }
              }

              VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
              )

              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                Box(
                  modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight(),
                ) {
                  TabContentHost(
                    activeTab = activeTab,
                    settings = settings,
                    imeStatus = imeStatus,
                    sandboxText = sandboxText,
                    previewLanguage = previewLanguage,
                    clipboardItems = clipboardItems,
                    smartSuggestionClip = smartSuggestionClip,
                    showInlinePreview = false,
                    viewModel = viewModel,
                    onOpenImeSettings = openImeSettings,
                    onOpenImePicker = openImePicker,
                    onToggleOneHanded = toggleOneHanded,
                    onSwapOneHandedSide = swapOneHandedSide,
                  )
                }

                Column(
                  modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                  verticalArrangement = Arrangement.Center,
                ) {
                  LiveSandboxPreviewCard(
                    settings = settings,
                    sandboxText = sandboxText,
                    onSandboxTextChange = viewModel::updateSandboxText,
                    onClearSandbox = viewModel::clearSandbox,
                    sink = viewModel.sandboxSink,
                    activeLanguage = previewLanguage,
                    onLanguageChange = viewModel::setPreviewLanguage,
                    onToggleOneHanded = toggleOneHanded,
                    onSwapOneHandedSide = swapOneHandedSide,
                    showTextInput = true,
                    isExpanded = true,
                    clipboardItems = clipboardItems,
                    smartSuggestionClip = smartSuggestionClip,
                    onTogglePinClip = viewModel::togglePinClip,
                    onDeleteClip = viewModel::deleteClip,
                    onClearUnpinnedClips = viewModel::clearUnpinnedClips,
                    onDismissSmartSuggestion = viewModel::dismissSmartSuggestion,
                    onOpenSettingsTab = { viewModel.selectTab(StudioTab.PALETTE) },
                  )
                }
              }
            }
          }
        } else {
          Scaffold(
            containerColor = Color.Transparent,
            topBar = {
              CenterAlignedTopAppBar(
                title = {
                  AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                      fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                    },
                    label = "topbar_title",
                  ) { tab ->
                    if (tab == StudioTab.STUDIO) {
                      Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                        border = BorderStroke(
                          1.dp,
                          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        ),
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                          Box(
                            modifier = Modifier
                              .size(8.dp)
                              .clip(CircleShape)
                              .background(MaterialTheme.colorScheme.primary),
                          )
                          Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                          )
                        }
                      }
                    } else {
                      Text(
                        text = strings.tabTitle(tab),
                        style = MaterialTheme.typography.titleLarge,
                      )
                    }
                  }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                  containerColor = Color.Transparent,
                ),
              )
            },
            bottomBar = {
              Column {
                HorizontalDivider(
                  thickness = 1.dp,
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )
                NavigationBar(
                  windowInsets = WindowInsets.navigationBars,
                  containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.96f),
                ) {
                  StudioTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    val label = strings.tabTitle(tab)
                    NavigationBarItem(
                      selected = selected,
                      onClick = { viewModel.selectTab(tab) },
                      icon = {
                        Icon(
                          imageVector = tabIcon(tab, selected),
                          contentDescription = label,
                        )
                      },
                      label = { Text(label) },
                      modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                    )
                  }
                }
              }
            },
          ) { innerPadding ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
              contentAlignment = Alignment.TopCenter,
            ) {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .widthIn(max = 600.dp),
              ) {
                if (settings.animationPreset == AnimationPreset.HEAVY) {
                  AnimatedVisibility(
                    visible = activeTab == StudioTab.PALETTE || activeTab == StudioTab.GEOMETRY,
                    enter = expandVertically(spring(dampingRatio = 0.85f, stiffness = 420f)) + fadeIn(),
                    exit = shrinkVertically(spring(stiffness = 450f)) + fadeOut(),
                  ) {
                    LiveSandboxPreviewCard(
                      settings = settings,
                      sandboxText = sandboxText,
                      onSandboxTextChange = viewModel::updateSandboxText,
                      onClearSandbox = viewModel::clearSandbox,
                      sink = viewModel.sandboxSink,
                      activeLanguage = previewLanguage,
                      onLanguageChange = viewModel::setPreviewLanguage,
                      onToggleOneHanded = toggleOneHanded,
                      onSwapOneHandedSide = swapOneHandedSide,
                      showTextInput = false,
                      isExpanded = isLivePreviewExpanded,
                      clipboardItems = clipboardItems,
                      smartSuggestionClip = smartSuggestionClip,
                      onTogglePinClip = viewModel::togglePinClip,
                      onDeleteClip = viewModel::deleteClip,
                      onClearUnpinnedClips = viewModel::clearUnpinnedClips,
                      onDismissSmartSuggestion = viewModel::dismissSmartSuggestion,
                      onToggleExpanded = viewModel::toggleLivePreviewExpanded,
                      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                  }
                } else if (activeTab == StudioTab.PALETTE || activeTab == StudioTab.GEOMETRY) {
                  LiveSandboxPreviewCard(
                    settings = settings,
                    sandboxText = sandboxText,
                    onSandboxTextChange = viewModel::updateSandboxText,
                    onClearSandbox = viewModel::clearSandbox,
                    sink = viewModel.sandboxSink,
                    activeLanguage = previewLanguage,
                    onLanguageChange = viewModel::setPreviewLanguage,
                    onToggleOneHanded = toggleOneHanded,
                    onSwapOneHandedSide = swapOneHandedSide,
                    showTextInput = false,
                    isExpanded = isLivePreviewExpanded,
                    clipboardItems = clipboardItems,
                    smartSuggestionClip = smartSuggestionClip,
                    onTogglePinClip = viewModel::togglePinClip,
                    onDeleteClip = viewModel::deleteClip,
                    onClearUnpinnedClips = viewModel::clearUnpinnedClips,
                    onDismissSmartSuggestion = viewModel::dismissSmartSuggestion,
                    onToggleExpanded = viewModel::toggleLivePreviewExpanded,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                  )
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                  TabContentHost(
                    activeTab = activeTab,
                    settings = settings,
                    imeStatus = imeStatus,
                    sandboxText = sandboxText,
                    previewLanguage = previewLanguage,
                    clipboardItems = clipboardItems,
                    smartSuggestionClip = smartSuggestionClip,
                    showInlinePreview = true,
                    viewModel = viewModel,
                    onOpenImeSettings = openImeSettings,
                    onOpenImePicker = openImePicker,
                    onToggleOneHanded = toggleOneHanded,
                    onSwapOneHandedSide = swapOneHandedSide,
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

@Composable
private fun TabContentHost(
  activeTab: StudioTab,
  settings: KeyboardSettings,
  imeStatus: ImeActivationStatus,
  sandboxText: TextFieldValue,
  previewLanguage: KeyboardLanguage,
  clipboardItems: List<ClipboardItem>,
  smartSuggestionClip: ClipboardItem?,
  showInlinePreview: Boolean,
  viewModel: StudioViewModel,
  onOpenImeSettings: () -> Unit,
  onOpenImePicker: () -> Unit,
  onToggleOneHanded: () -> Unit,
  onSwapOneHandedSide: () -> Unit,
) {
  val renderTab: @Composable (StudioTab) -> Unit = { tab ->
    when (tab) {
      StudioTab.STUDIO ->
        StudioHomeTab(
          settings = settings,
          imeStatus = imeStatus,
          sandboxText = sandboxText,
          previewLanguage = previewLanguage,
          sandboxSink = viewModel.sandboxSink,
          clipboardItems = clipboardItems,
          smartSuggestionClip = smartSuggestionClip,
          showInlinePreview = showInlinePreview,
          onOpenImeSettings = onOpenImeSettings,
          onOpenImePicker = onOpenImePicker,
          onApplyProfile = viewModel::applyProfile,
          onSandboxTextChange = viewModel::updateSandboxText,
          onClearSandbox = viewModel::clearSandbox,
          onLanguageChange = viewModel::setPreviewLanguage,
          onToggleOneHanded = onToggleOneHanded,
          onSwapOneHandedSide = onSwapOneHandedSide,
          onSyncClipboard = viewModel::syncClipboard,
          onAddClipboardSnippet = { viewModel.addClipboardSnippet(it) },
          onTogglePinClip = viewModel::togglePinClip,
          onDeleteClip = viewModel::deleteClip,
          onClearUnpinnedClips = viewModel::clearUnpinnedClips,
          onDismissSmartSuggestion = viewModel::dismissSmartSuggestion,
          onNavigateToPalette = { viewModel.selectTab(StudioTab.PALETTE) },
        )

      StudioTab.PALETTE ->
        PaletteStyleTab(
          settings = settings,
          onSetDynamicColor = viewModel::setUseDynamicColor,
          onSelectPalette = viewModel::selectPalettePreset,
          onSetThemeMode = viewModel::setThemeMode,
          onSetSurfaceStyle = viewModel::setKeySurfaceStyle,
          onSetKeyAlpha = viewModel::setKeyAlpha,
          onSetFontFamily = viewModel::setKeyFontFamily,
          onSetAnimationPreset = viewModel::setAnimationPreset,
        )

      StudioTab.GEOMETRY ->
        GeometryTab(
          settings = settings,
          onSetHeightScale = viewModel::setHeightScale,
          onSetCornerRadius = viewModel::setCornerRadius,
          onSetHorizontalGap = viewModel::setHorizontalGap,
          onSetVerticalGap = viewModel::setVerticalGap,
          onSetFontSize = viewModel::setFontSize,
          onSetBottomPadding = viewModel::setBottomPadding,
          onSetOneHandedMode = viewModel::setOneHandedMode,
          onSetKeyShadow = viewModel::setShowKeyShadow,
        )

      StudioTab.TYPING ->
        TypingHapticsTab(
          settings = settings,
          onSetAppLanguage = viewModel::setAppLanguage,
          onSetDefaultLanguage = viewModel::setDefaultLanguage,
          onSetShowNumberRow = viewModel::setShowNumberRow,
          onSetShowKeyHints = viewModel::setShowKeyHints,
          onSetShowKeyPopup = viewModel::setShowKeyPopup,
          onSetShowToolbar = viewModel::setShowToolbar,
          onSetHapticEnabled = viewModel::setHapticEnabled,
          onSetHapticDuration = viewModel::setHapticDuration,
          onSetSoundEnabled = viewModel::setSoundEnabled,
          onSetSpaceCursorControl = viewModel::setSpaceCursorControl,
          onSetAutoCapitalize = viewModel::setAutoCapitalize,
          onSetDoubleSpacePeriod = viewModel::setDoubleSpacePeriod,
          onSetLongPressDelay = viewModel::setLongPressDelay,
          onResetDefaults = viewModel::resetDefaults,
        )
    }
  }

  if (settings.animationPreset == AnimationPreset.HEAVY) {
    AnimatedContent(
      targetState = activeTab,
      transitionSpec = {
        val forward = targetState.ordinal >= initialState.ordinal
        val slideSpec = spring<androidx.compose.ui.unit.IntOffset>(
          dampingRatio = 0.88f,
          stiffness = 440f,
        )
        (slideInHorizontally(animationSpec = slideSpec) { width ->
          if (forward) width / 5 else -width / 5
        } + fadeIn(tween(200))) togetherWith
          (slideOutHorizontally(animationSpec = slideSpec) { width ->
            if (forward) -width / 6 else width / 6
          } + fadeOut(tween(150)))
      },
      label = "studio_tabs_transition",
    ) { tab ->
      renderTab(tab)
    }
  } else {
    renderTab(activeTab)
  }
}

private fun tabIcon(tab: StudioTab, selected: Boolean): ImageVector =
  when (tab) {
    StudioTab.STUDIO -> if (selected) Icons.Rounded.Dashboard else Icons.Outlined.Dashboard
    StudioTab.PALETTE -> if (selected) Icons.Rounded.Palette else Icons.Outlined.Palette
    StudioTab.GEOMETRY -> if (selected) Icons.Rounded.AspectRatio else Icons.Outlined.AspectRatio
    StudioTab.TYPING -> if (selected) Icons.Rounded.Keyboard else Icons.Outlined.Keyboard
  }
