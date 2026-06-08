package dev.ceccon.pieno.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenSoft,
    onPrimaryContainer = OnGreenSoft,
    secondary = Accent,
    onSecondary = Color.White,
    secondaryContainer = AccentSoft,
    onSecondaryContainer = OnAccentSoft,
    tertiary = Accent,
    onTertiary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkSoft,
    outline = Hairline,
    outlineVariant = Hairline,
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color(0xFF06251B),
    primaryContainer = Color(0xFF133A2D),
    onPrimaryContainer = GreenSoft,
    secondary = AccentLight,
    onSecondary = Color(0xFF3A0F03),
    secondaryContainer = Color(0xFF50281B),
    onSecondaryContainer = AccentSoft,
    tertiary = AccentLight,
    onTertiary = Color(0xFF3A0F03),
    background = PaperDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = Color(0xFF24272D),
    onSurfaceVariant = OnDarkSoft,
    outline = HairlineDark,
    outlineVariant = HairlineDark,
)

@Composable
fun PienoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val pienoColors = if (darkTheme) darkPienoColors else lightPienoColors
    androidx.compose.runtime.CompositionLocalProvider(LocalPienoColors provides pienoColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = PienoTypography,
            shapes = PienoShapes,
            content = content,
        )
    }
}
