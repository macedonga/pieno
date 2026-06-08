package dev.ceccon.pieno.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.ceccon.pieno.PienoApp
import dev.ceccon.pieno.data.PienoContainer

@Composable
fun rememberContainer(): PienoContainer {
    val context = LocalContext.current
    return (context.applicationContext as PienoApp).container
}
