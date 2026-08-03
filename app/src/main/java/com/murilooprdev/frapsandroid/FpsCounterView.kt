package com.murilooprdev.frapsandroid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View

/**
 * Desenha o contador de FPS usando a fonte "fraps" (FontStruct,
 * CC BY-SA 3.0, por Wix) - assets/fonts/fraps.otf.
 * Mantem antialiasing desligado pra ficar fiel ao visual "pixel sharp"
 * original do Fraps.
 */
class FpsCounterView(context: Context) : View(context) {

    var fps: Int = 0
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    private val frapsTypeface: Typeface = Typeface.createFromAsset(
        context.assets, "fonts/fraps.otf"
    )

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FFFF00") // amarelo Fraps
        isAntiAlias = false
        isFilterBitmap = false
        typeface = frapsTypeface
        textSize = 64f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val text = "$fps FPS"
        val metrics = textPaint.fontMetrics
        canvas.drawText(text, 4f, -metrics.top, textPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val text = "$fps FPS"
        val width = textPaint.measureText(text).toInt() + 8
        val metrics = textPaint.fontMetrics
        val height = (metrics.bottom - metrics.top).toInt() + 4
        setMeasuredDimension(width, height)
    }
}
