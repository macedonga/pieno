package dev.ceccon.pieno.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import kotlin.math.acos
import kotlin.math.ceil

// Marker della mappa: una pillola col prezzo (simbolo euro incluso) e una punta
// in basso. I colori arrivano dal tema (primary/onPrimary), cosi' segue chiaro e
// scuro come i bottoni. Contorno sottile per staccare dalle tile.
fun priceMarkerBitmap(
    context: Context,
    text: String,
    fillColor: Int,
    textColor: Int,
    outlineColor: Int,
): Bitmap {
    val d = context.resources.displayMetrics.density
    val textSize = 13f * d
    val padH = 10f * d
    val padV = 6f * d
    val radius = 7f * d
    val pointerW = 13f * d
    val pointerH = 7f * d
    val outlineW = 0.8f * d // sottile
    val margin = 4f * d

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        this.textSize = textSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val fm = textPaint.fontMetrics
    val textH = fm.descent - fm.ascent
    val textW = textPaint.measureText(text)

    val pillW = textW + padH * 2
    val pillH = textH + padV * 2
    val w = ceil(pillW + margin * 2).toInt()
    val h = ceil(pillH + pointerH + margin * 2).toInt()

    val bmp = createBitmap(w, h)
    val canvas = Canvas(bmp)

    val left = margin
    val top = margin
    val right = left + pillW
    val bottom = top + pillH
    val cx = (left + right) / 2f

    val shape = Path().apply {
        addRoundRect(RectF(left, top, right, bottom), radius, radius, Path.Direction.CW)
        moveTo(cx - pointerW / 2f, bottom)
        lineTo(cx, bottom + pointerH)
        lineTo(cx + pointerW / 2f, bottom)
        close()
    }

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
        setShadowLayer(2.5f * d, 0f, 1f * d, 0x33000000)
    }
    canvas.drawPath(shape, shadowPaint)
    val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = outlineColor
        style = Paint.Style.STROKE
        strokeWidth = outlineW * 2f
    }
    canvas.drawPath(shape, outlinePaint)
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }
    canvas.drawPath(shape, fillPaint)

    canvas.drawText(text, cx, top + padV - fm.ascent, textPaint)
    return bmp
}

// Spillo semplice (goccia) col colore del tema: per il dettaglio, dove il prezzo
// e' gia' scritto sotto e il marker a pillola non serve. Un solo contorno continuo
// (testa + lati tangenti alla punta), cosi' il bordo non taglia la testa.
fun pinMarkerBitmap(context: Context, fillColor: Int, outlineColor: Int): Bitmap {
    val d = context.resources.displayMetrics.density
    val r = 8.5f * d
    val length = r * 2.5f // distanza dal centro della testa alla punta
    val outlineW = 1.5f * d
    val margin = 5f * d // spazio per ombra + contorno, cosi' non viene tagliato

    val cx = margin + r
    val cy = margin + r
    val tipY = cy + length
    val w = ceil(2f * r + margin * 2).toInt()
    val h = ceil(tipY + margin).toInt()
    val bmp = createBitmap(w, h)
    val canvas = Canvas(bmp)

    val ang = Math.toDegrees(acos((r / length).toDouble())).toFloat()
    val rect = RectF(cx - r, cy - r, cx + r, cy + r)
    val shape = Path().apply {
        moveTo(cx, tipY)
        arcTo(rect, 90f + ang, 360f - 2f * ang)
        close()
    }

    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
        setShadowLayer(2.5f * d, 0f, 1f * d, 0x33000000)
    }
    canvas.drawPath(shape, shadow)
    val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = outlineColor
        style = Paint.Style.STROKE
        strokeWidth = outlineW * 2f
        strokeJoin = Paint.Join.ROUND
    }
    canvas.drawPath(shape, outline)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor; style = Paint.Style.FILL }
    canvas.drawPath(shape, fill)
    canvas.drawCircle(cx, cy, r * 0.36f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = outlineColor; style = Paint.Style.FILL })
    return bmp
}
