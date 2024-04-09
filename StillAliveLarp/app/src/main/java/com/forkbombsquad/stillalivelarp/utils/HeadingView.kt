package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.media.Image
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R

class HeadingView(context: Context): LinearLayout(context) {

    val title: TextView
    val texts: LinearLayout
    val subsubheadings: LinearLayout
    val subheadings: LinearLayout

    init {
        inflate(context, R.layout.headingview, this)

        title = findViewById(R.id.heading_title)
        texts = findViewById(R.id.heading_textslayout)
        subsubheadings = findViewById(R.id.heading_subsubheadingslayout)
        subheadings = findViewById(R.id.heading_subheadingslayout)
    }

}

class SubHeadingView(context: Context): LinearLayout(context) {

    val title: TextView
    val texts: LinearLayout
    val subsubheadings: LinearLayout

    init {
        inflate(context, R.layout.subheadingview, this)

        title = findViewById(R.id.subheading_title)
        texts = findViewById(R.id.subheading_textslayout)
        subsubheadings = findViewById(R.id.subheading_subsubheadingslayout)
    }

}

class SubSubHeadingView(context: Context): LinearLayout(context) {

    val title: TextView
    val texts: LinearLayout

    init {
        inflate(context, R.layout.subsubheadingview, this)

        title = findViewById(R.id.subsubheading_title)
        texts = findViewById(R.id.subsubheading_textslayout)
    }

}