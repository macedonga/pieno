package dev.ceccon.pieno.core

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

fun encodeQrBitmap(
    content: String,
    sizePx: Int = 720,
    foreground: Int = 0xFF15161A.toInt(),
    background: Int = 0xFFFFFFFF.toInt(),
    margin: Int = 0,
): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to margin)
    val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
    val w = matrix.width
    val h = matrix.height
    val pixels = IntArray(w * h)
    for (y in 0 until h) {
        val offset = y * w
        for (x in 0 until w) {
            pixels[offset + x] = if (matrix.get(x, y)) foreground else background
        }
    }
    return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, w, 0, 0, w, h)
    }
}
