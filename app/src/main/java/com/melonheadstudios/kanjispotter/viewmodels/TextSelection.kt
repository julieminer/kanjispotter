package com.melonheadstudios.kanjispotter.viewmodels

import android.graphics.Paint
import android.graphics.Rect

/**
 * kanjispotter
 * Created by jake on 2017-04-30, 8:24 PM
 */
class TextSelection(var text: String, var rect: Rect = Rect(), var highlightRect: Rect = Rect(), var selected: Boolean = false) {
    fun calculateRect(x: Float, y: Float, textPaint: Paint) {
        rect = Rect(
                x.toInt(),
                (y - textPaint.textSize).toInt(),
                (x + textPaint.measureText(text)).toInt(),
                (y + (textPaint.textSize / 2)).toInt())
        highlightRect = rect
        highlightRect.left -= 12
        highlightRect.right += 12
    }
}
