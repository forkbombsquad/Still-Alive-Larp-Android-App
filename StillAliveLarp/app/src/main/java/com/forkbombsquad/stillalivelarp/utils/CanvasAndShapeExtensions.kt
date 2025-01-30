package com.forkbombsquad.stillalivelarp.utils

import android.graphics.Rect
import android.graphics.RectF

class Shapes {
    companion object {
        fun rect(x: Int, y: Int, width: Int, height: Int): Rect { return Rect(x, y, x + width, y + height) }
        fun rectf(x: Float, y: Float, width: Float, height: Float): RectF { return RectF(x, y, x + width, y + height) }
    }
}

fun Rect.panned(panX: Int, panY: Int) {

}