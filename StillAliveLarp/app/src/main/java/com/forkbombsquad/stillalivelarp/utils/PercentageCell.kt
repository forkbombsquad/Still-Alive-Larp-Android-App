package com.forkbombsquad.stillalivelarp.utils

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import kotlin.math.abs

class PercentageCell(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val percentTextView: TextView
    val greenBarLayout: LinearLayout
    val whiteBarLayout: LinearLayout

    init {
        inflate(context, R.layout.percentage_cell, this)

        textView = findViewById(R.id.percentagecell_text)
        percentTextView = findViewById(R.id.percentagecell_percentText)
        greenBarLayout = findViewById(R.id.percentagecell_greenSection)
        whiteBarLayout = findViewById(R.id.percentagecell_whiteSection)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PercentageCell)
        textView.text = attributes.getString(R.styleable.PercentageCell_perctext)
        val percent = attributes.getFloat(R.styleable.PercentageCell_percent, 0f)
        setPercent(percent)
        attributes.recycle()
    }

    fun set(title: String, percent: Float) {
        setTitle(title)
        setPercent(percent)
    }
    fun setTitle(title: String) {
        textView.text = title
    }
    fun setPercent(percent: Float) {
        percentTextView.text = "${percent.toInt()}%"
        val grnparams = greenBarLayout.layoutParams as LayoutParams
        grnparams.weight = 100f - percent
        greenBarLayout.layoutParams = grnparams
        val whtparams = whiteBarLayout.layoutParams as LayoutParams
        whtparams.weight = percent
        whiteBarLayout.layoutParams = whtparams
    }

}

class PercentageCellBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val percentTextView: TextView
    val greenBarLayout: LinearLayout
    val whiteBarLayout: LinearLayout

    init {
        inflate(context, R.layout.percentage_cell, this)

        textView = findViewById(R.id.percentagecell_text)
        percentTextView = findViewById(R.id.percentagecell_percentText)
        greenBarLayout = findViewById(R.id.percentagecell_greenSection)
        whiteBarLayout = findViewById(R.id.percentagecell_whiteSection)
    }

    fun set(title: String, percent: Float) {
        setTitle(title)
        setPercent(percent)
    }
    fun setTitle(title: String) {
        textView.text = title
    }
    fun setPercent(percent: Float) {
        percentTextView.text = "${percent.toInt()}%"
        val grnparams = greenBarLayout.layoutParams as LayoutParams
        grnparams.weight = 100f - percent
        greenBarLayout.layoutParams = grnparams
        val whtparams = whiteBarLayout.layoutParams as LayoutParams
        whtparams.weight = percent
        whiteBarLayout.layoutParams = whtparams
    }
}