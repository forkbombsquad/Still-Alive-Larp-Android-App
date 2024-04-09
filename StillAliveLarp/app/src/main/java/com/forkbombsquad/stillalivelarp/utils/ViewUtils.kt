package com.forkbombsquad.stillalivelarp.utils

import android.content.Context

class ViewUtils {
    companion object {
        fun dpToPx(context: Context?, dp: Int): Int {
            val scale = context?.resources?.displayMetrics?.density ?: 1.0f
            return (dp * scale + 0.5f).toInt()
        }
    }
}