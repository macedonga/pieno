package dev.ceccon.pieno.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareQrImage(context: Context, payload: String, targa: String) {
    val bitmap = encodeQrBitmap(payload, sizePx = 720, margin = 2)
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val safe = targa.replace(Regex("[^A-Za-z0-9]"), "").ifBlank { "qr" }
    val file = File(dir, "tessera_$safe.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Tessera carburante FVG, targa $targa")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Condividi QR").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
