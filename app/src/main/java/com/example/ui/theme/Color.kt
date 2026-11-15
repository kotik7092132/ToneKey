package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.model.PalettePreset

data class PresetSwatch(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color,
)

fun PalettePreset.swatchColors(): PresetSwatch =
  when (this) {
    PalettePreset.DYNAMIC -> PresetSwatch(Color(0xFF4C5FD5), Color(0xFF5D6B8E), Color(0xFF8C5A82))
    PalettePreset.SAGE -> PresetSwatch(Color(0xFF2D6A4F), Color(0xFF52796F), Color(0xFF95A577))
    PalettePreset.TERRACOTTA -> PresetSwatch(Color(0xFFB5543A), Color(0xFF8D6352), Color(0xFFD08C60))
    PalettePreset.INDIGO -> PresetSwatch(Color(0xFF3F51B5), Color(0xFF5C6BC0), Color(0xFF7E57C2))
    PalettePreset.LAVENDER -> PresetSwatch(Color(0xFF6B4FA2), Color(0xFF7C6995), Color(0xFFA95C7E))
    PalettePreset.SAKURA -> PresetSwatch(Color(0xFFAE4368), Color(0xFF875B6B), Color(0xFFD27664))
    PalettePreset.OBSIDIAN -> PresetSwatch(Color(0xFF3A3F47), Color(0xFF5B616A), Color(0xFF7D8590))
  }

fun presetLightColorScheme(preset: PalettePreset): ColorScheme =
  when (preset) {
    PalettePreset.DYNAMIC,
    PalettePreset.INDIGO ->
      lightColorScheme(
        primary = Color(0xFF3D53B8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE1FF),
        onPrimaryContainer = Color(0xFF001551),
        secondary = Color(0xFF595D72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDEE1F9),
        onSecondaryContainer = Color(0xFF161B2C),
        tertiary = Color(0xFF755470),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD7F6),
        onTertiaryContainer = Color(0xFF2C122A),
        background = Color(0xFFFAF8FF),
        onBackground = Color(0xFF1A1B21),
        surface = Color(0xFFFAF8FF),
        onSurface = Color(0xFF1A1B21),
        surfaceVariant = Color(0xFFE3E2EC),
        onSurfaceVariant = Color(0xFF45464F),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF4F3FA),
        surfaceContainer = Color(0xFFEEEDF4),
        surfaceContainerHigh = Color(0xFFE8E7EF),
        surfaceContainerHighest = Color(0xFFE2E1E9),
        outline = Color(0xFF767680),
        outlineVariant = Color(0xFFC6C5D0),
      )

    PalettePreset.SAGE ->
      lightColorScheme(
        primary = Color(0xFF2C6A4E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB0F1CC),
        onPrimaryContainer = Color(0xFF002114),
        secondary = Color(0xFF4D6357),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCFE9D8),
        onSecondaryContainer = Color(0xFF0B1F16),
        tertiary = Color(0xFF3E6374),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC1E8FB),
        onTertiaryContainer = Color(0xFF001F2A),
        background = Color(0xFFF6FBF6),
        onBackground = Color(0xFF171D19),
        surface = Color(0xFFF6FBF6),
        onSurface = Color(0xFF171D19),
        surfaceVariant = Color(0xFFDCE5DD),
        onSurfaceVariant = Color(0xFF404943),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF0F5F0),
        surfaceContainer = Color(0xFFEAEFEA),
        surfaceContainerHigh = Color(0xFFE4EAE4),
        surfaceContainerHighest = Color(0xFFDFE4DF),
        outline = Color(0xFF707973),
        outlineVariant = Color(0xFFC0C9C1),
      )

    PalettePreset.TERRACOTTA ->
      lightColorScheme(
        primary = Color(0xFF9F4229),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD1),
        onPrimaryContainer = Color(0xFF3B0900),
        secondary = Color(0xFF77574E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDAD1),
        onSecondaryContainer = Color(0xFF2C150F),
        tertiary = Color(0xFF6D5D2E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF8E1A6),
        onTertiaryContainer = Color(0xFF241A00),
        background = Color(0xFFFFF8F6),
        onBackground = Color(0xFF231917),
        surface = Color(0xFFFFF8F6),
        onSurface = Color(0xFF231917),
        surfaceVariant = Color(0xFFF5DDD8),
        onSurfaceVariant = Color(0xFF53433F),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFFF1ED),
        surfaceContainer = Color(0xFFFAEAE6),
        surfaceContainerHigh = Color(0xFFF5E4E0),
        surfaceContainerHighest = Color(0xFFEFDFDA),
        outline = Color(0xFF85736E),
        outlineVariant = Color(0xFFD8C2BC),
      )

    PalettePreset.LAVENDER ->
      lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE9DDFF),
        onPrimaryContainer = Color(0xFF22005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1E192B),
        tertiary = Color(0xFF7E5260),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD9E3),
        onTertiaryContainer = Color(0xFF31101D),
        background = Color(0xFFFEF7FF),
        onBackground = Color(0xFF1D1B20),
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1B20),
        surfaceVariant = Color(0xFFE7E0EB),
        onSurfaceVariant = Color(0xFF49454E),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF8F1FA),
        surfaceContainer = Color(0xFFF2ECF4),
        surfaceContainerHigh = Color(0xFFECE6EE),
        surfaceContainerHighest = Color(0xFFE6E0E9),
        outline = Color(0xFF7A757F),
        outlineVariant = Color(0xFFCAC4CF),
      )

    PalettePreset.SAKURA ->
      lightColorScheme(
        primary = Color(0xFF9C3E5F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E2),
        onPrimaryContainer = Color(0xFF3E001B),
        secondary = Color(0xFF74565F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E2),
        onSecondaryContainer = Color(0xFF2B151C),
        tertiary = Color(0xFF7C5635),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDCC0),
        onTertiaryContainer = Color(0xFF2E1500),
        background = Color(0xFFFFF8F8),
        onBackground = Color(0xFF22191C),
        surface = Color(0xFFFFF8F8),
        onSurface = Color(0xFF22191C),
        surfaceVariant = Color(0xFFF2DDE2),
        onSurfaceVariant = Color(0xFF514347),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFFF0F3),
        surfaceContainer = Color(0xFFFAEAEE),
        surfaceContainerHigh = Color(0xFFF4E4E8),
        surfaceContainerHighest = Color(0xFFEFDFE3),
        outline = Color(0xFF837377),
        outlineVariant = Color(0xFFD5C2C6),
      )

    PalettePreset.OBSIDIAN ->
      lightColorScheme(
        primary = Color(0xFF3E444D),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8DEE8),
        onPrimaryContainer = Color(0xFF15191E),
        secondary = Color(0xFF575D66),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E4EA),
        onSecondaryContainer = Color(0xFF1B1F24),
        tertiary = Color(0xFF4B5E6B),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD4E5F0),
        onTertiaryContainer = Color(0xFF121E26),
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF191C1E),
        surface = Color(0xFFF8F9FA),
        onSurface = Color(0xFF191C1E),
        surfaceVariant = Color(0xFFE1E3E6),
        onSurfaceVariant = Color(0xFF45484C),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF2F4F6),
        surfaceContainer = Color(0xFFECEEF0),
        surfaceContainerHigh = Color(0xFFE6E8EA),
        surfaceContainerHighest = Color(0xFFE0E2E5),
        outline = Color(0xFF74777C),
        outlineVariant = Color(0xFFC5C7CB),
      )
  }

fun presetDarkColorScheme(preset: PalettePreset, amoled: Boolean = false): ColorScheme {
  val base =
    when (preset) {
      PalettePreset.DYNAMIC,
      PalettePreset.INDIGO ->
        darkColorScheme(
          primary = Color(0xFFB7C4FF),
          onPrimary = Color(0xFF002780),
          primaryContainer = Color(0xFF233A9F),
          onPrimaryContainer = Color(0xFFDCE1FF),
          secondary = Color(0xFFC2C5DD),
          onSecondary = Color(0xFF2B3042),
          secondaryContainer = Color(0xFF424659),
          onSecondaryContainer = Color(0xFFDEE1F9),
          tertiary = Color(0xFFE3BADA),
          onTertiary = Color(0xFF432740),
          tertiaryContainer = Color(0xFF5B3D57),
          onTertiaryContainer = Color(0xFFFFD7F6),
          background = Color(0xFF121318),
          onBackground = Color(0xFFE3E1E9),
          surface = Color(0xFF121318),
          onSurface = Color(0xFFE3E1E9),
          surfaceVariant = Color(0xFF45464F),
          onSurfaceVariant = Color(0xFFC6C5D0),
          surfaceContainerLowest = Color(0xFF0D0E13),
          surfaceContainerLow = Color(0xFF1A1B21),
          surfaceContainer = Color(0xFF1E1F25),
          surfaceContainerHigh = Color(0xFF292A2F),
          surfaceContainerHighest = Color(0xFF34343A),
          outline = Color(0xFF90909A),
          outlineVariant = Color(0xFF45464F),
        )

      PalettePreset.SAGE ->
        darkColorScheme(
          primary = Color(0xFF94D5B1),
          onPrimary = Color(0xFF003824),
          primaryContainer = Color(0xFF0E5137),
          onPrimaryContainer = Color(0xFFB0F1CC),
          secondary = Color(0xFFB4CCBD),
          onSecondary = Color(0xFF20352A),
          secondaryContainer = Color(0xFF364B3F),
          onSecondaryContainer = Color(0xFFCFE9D8),
          tertiary = Color(0xFFA5CCDE),
          onTertiary = Color(0xFF083544),
          tertiaryContainer = Color(0xFF254B5B),
          onTertiaryContainer = Color(0xFFC1E8FB),
          background = Color(0xFF0F1511),
          onBackground = Color(0xFFDFE4DF),
          surface = Color(0xFF0F1511),
          onSurface = Color(0xFFDFE4DF),
          surfaceVariant = Color(0xFF404943),
          onSurfaceVariant = Color(0xFFC0C9C1),
          surfaceContainerLowest = Color(0xFF0A0F0C),
          surfaceContainerLow = Color(0xFF171D19),
          surfaceContainer = Color(0xFF1B211D),
          surfaceContainerHigh = Color(0xFF262B27),
          surfaceContainerHighest = Color(0xFF303632),
          outline = Color(0xFF8A938C),
          outlineVariant = Color(0xFF404943),
        )

      PalettePreset.TERRACOTTA ->
        darkColorScheme(
          primary = Color(0xFFFFB5A0),
          onPrimary = Color(0xFF601402),
          primaryContainer = Color(0xFF802A15),
          onPrimaryContainer = Color(0xFFFFDAD1),
          secondary = Color(0xFFE7BDB2),
          onSecondary = Color(0xFF442A23),
          secondaryContainer = Color(0xFF5D4038),
          onSecondaryContainer = Color(0xFFFFDAD1),
          tertiary = Color(0xFFDBC58D),
          onTertiary = Color(0xFF3C2F05),
          tertiaryContainer = Color(0xFF544519),
          onTertiaryContainer = Color(0xFFF8E1A6),
          background = Color(0xFF1A110F),
          onBackground = Color(0xFFEFDFDA),
          surface = Color(0xFF1A110F),
          onSurface = Color(0xFFEFDFDA),
          surfaceVariant = Color(0xFF53433F),
          onSurfaceVariant = Color(0xFFD8C2BC),
          surfaceContainerLowest = Color(0xFF140C0A),
          surfaceContainerLow = Color(0xFF231917),
          surfaceContainer = Color(0xFF271D1B),
          surfaceContainerHigh = Color(0xFF322825),
          surfaceContainerHighest = Color(0xFF3D322F),
          outline = Color(0xFFA08C87),
          outlineVariant = Color(0xFF53433F),
        )

      PalettePreset.LAVENDER ->
        darkColorScheme(
          primary = Color(0xFFCFBCFF),
          onPrimary = Color(0xFF381E72),
          primaryContainer = Color(0xFF4F378B),
          onPrimaryContainer = Color(0xFFE9DDFF),
          secondary = Color(0xFFCBC2DB),
          onSecondary = Color(0xFF332D41),
          secondaryContainer = Color(0xFF4A4458),
          onSecondaryContainer = Color(0xFFE8DEF8),
          tertiary = Color(0xFFEFB8C8),
          onTertiary = Color(0xFF4A2532),
          tertiaryContainer = Color(0xFF633B48),
          onTertiaryContainer = Color(0xFFFFD9E3),
          background = Color(0xFF141218),
          onBackground = Color(0xFFE6E0E9),
          surface = Color(0xFF141218),
          onSurface = Color(0xFFE6E0E9),
          surfaceVariant = Color(0xFF49454E),
          onSurfaceVariant = Color(0xFFCAC4CF),
          surfaceContainerLowest = Color(0xFF0F0D13),
          surfaceContainerLow = Color(0xFF1D1B20),
          surfaceContainer = Color(0xFF211F26),
          surfaceContainerHigh = Color(0xFF2B2930),
          surfaceContainerHighest = Color(0xFF36343B),
          outline = Color(0xFF948F99),
          outlineVariant = Color(0xFF49454E),
        )

      PalettePreset.SAKURA ->
        darkColorScheme(
          primary = Color(0xFFFFB1C6),
          onPrimary = Color(0xFF600C30),
          primaryContainer = Color(0xFF7E2647),
          onPrimaryContainer = Color(0xFFFFD9E2),
          secondary = Color(0xFFE3BDC6),
          onSecondary = Color(0xFF422931),
          secondaryContainer = Color(0xFF5B3F47),
          onSecondaryContainer = Color(0xFFFFD9E2),
          tertiary = Color(0xFFEFBD94),
          onTertiary = Color(0xFF48290C),
          tertiaryContainer = Color(0xFF623F20),
          onTertiaryContainer = Color(0xFFFFDCC0),
          background = Color(0xFF191113),
          onBackground = Color(0xFFEFDFE3),
          surface = Color(0xFF191113),
          onSurface = Color(0xFFEFDFE3),
          surfaceVariant = Color(0xFF514347),
          onSurfaceVariant = Color(0xFFD5C2C6),
          surfaceContainerLowest = Color(0xFF140C0E),
          surfaceContainerLow = Color(0xFF22191C),
          surfaceContainer = Color(0xFF261D20),
          surfaceContainerHigh = Color(0xFF31282A),
          surfaceContainerHighest = Color(0xFF3D3235),
          outline = Color(0xFF9E8C90),
          outlineVariant = Color(0xFF514347),
        )

      PalettePreset.OBSIDIAN ->
        darkColorScheme(
          primary = Color(0xFFC2C8D1),
          onPrimary = Color(0xFF2A2E35),
          primaryContainer = Color(0xFF40464E),
          onPrimaryContainer = Color(0xFFE4E8EE),
          secondary = Color(0xFFAEB4BD),
          onSecondary = Color(0xFF25292F),
          secondaryContainer = Color(0xFF3B4047),
          onSecondaryContainer = Color(0xFFE0E4EA),
          tertiary = Color(0xFFB4C9D6),
          onTertiary = Color(0xFF1F323D),
          tertiaryContainer = Color(0xFF364955),
          onTertiaryContainer = Color(0xFFD4E5F0),
          background = Color(0xFF111315),
          onBackground = Color(0xFFE2E4E7),
          surface = Color(0xFF111315),
          onSurface = Color(0xFFE2E4E7),
          surfaceVariant = Color(0xFF43464B),
          onSurfaceVariant = Color(0xFFC4C7CC),
          surfaceContainerLowest = Color(0xFF0B0C0E),
          surfaceContainerLow = Color(0xFF181A1D),
          surfaceContainer = Color(0xFF1E2023),
          surfaceContainerHigh = Color(0xFF282A2E),
          surfaceContainerHighest = Color(0xFF33363A),
          outline = Color(0xFF8E9196),
          outlineVariant = Color(0xFF43464B),
        )
    }

  return if (!amoled) {
    base
  } else {
    base.copy(
      background = Color.Black,
      surface = Color.Black,
      surfaceContainerLowest = Color.Black,
      surfaceContainerLow = Color(0xFF0A0A0C),
      surfaceContainer = Color(0xFF121215),
      surfaceContainerHigh = Color(0xFF19191D),
      surfaceContainerHighest = Color(0xFF232328),
    )
  }
}
