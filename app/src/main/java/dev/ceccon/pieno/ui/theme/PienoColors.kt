package dev.ceccon.pieno.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Token di colore "chrome" che cambiano con il tema. Gli elementi brand (card
// verde, pannello QR bianco, logo) restano costanti e NON passano da qui.
@Immutable
data class PienoColors(
    val paper: Color,
    val paperDim: Color,
    val surface: Color,
    val ink: Color,
    val inkSoft: Color,
    val hairline: Color,
    val greenSoft: Color,
    val onGreenSoft: Color,
    val accent: Color,
)

val lightPienoColors = PienoColors(
    paper = Paper,
    paperDim = PaperDim,
    surface = Surface,
    ink = Ink,
    inkSoft = InkSoft,
    hairline = Hairline,
    greenSoft = GreenSoft,
    onGreenSoft = OnGreenSoft,
    accent = Accent,
)

val darkPienoColors = PienoColors(
    paper = PaperDark,
    paperDim = Color(0xFF191C21),
    surface = Color(0xFF23262C),
    ink = OnDark,
    inkSoft = OnDarkSoft,
    hairline = HairlineDark,
    greenSoft = Color(0xFF14392B),
    onGreenSoft = GreenSoft,
    accent = AccentLight,
)

val LocalPienoColors = staticCompositionLocalOf { lightPienoColors }

object Pieno {
    val colors: PienoColors
        @Composable @ReadOnlyComposable
        get() = LocalPienoColors.current
}
