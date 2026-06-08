@file:OptIn(ExperimentalTextApi::class)

package dev.ceccon.pieno.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.ceccon.pieno.R

// Font variabili inclusi (offline). Il peso si imposta sull'asse wght via
// FontVariation, altrimenti un font variabile resterebbe sull'istanza di default.

private fun fraunces(weight: Int): FontFamily = FontFamily(
    Font(
        resId = R.font.fraunces,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
)

private fun hanken(weight: Int): FontFamily = FontFamily(
    Font(
        resId = R.font.hanken_grotesk,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
)

// Fraunces per il display (carattere, calore), Hanken Grotesk per testo e UI
// (leggibilita'). Gerarchia netta, vedi design-principles.md.
val PienoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = fraunces(600), fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fraunces(600), fontSize = 26.sp, lineHeight = 31.sp, letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = hanken(600), fontSize = 20.sp, lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = hanken(500), fontSize = 16.sp, lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = hanken(400), fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = hanken(400), fontSize = 14.sp, lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = hanken(600), fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = hanken(500), fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
)
