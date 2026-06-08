package dev.ceccon.pieno.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import dev.ceccon.pieno.core.encodeQrBitmap

// Nessuna crittografia lato client: il QR contiene un token firmato dal server.
@Composable
fun QrImage(
    content: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 640,
    foreground: Color = Color(0xFF15161A),
    background: Color = Color.White,
    contentDescription: String = "Codice QR della tessera carburante",
) {
    val bitmap = remember(content, sizePx, foreground, background) {
        encodeQrBitmap(content, sizePx, foreground.toArgb(), background.toArgb(), margin = 0).asImageBitmap()
    }
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
