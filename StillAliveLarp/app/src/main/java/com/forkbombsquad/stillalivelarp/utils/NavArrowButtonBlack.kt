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

class NavArrowButtonBlack(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView
    val notificationBubble: TextView

    init {
        inflate(context, R.layout.navarrowbutton_black, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
        notificationBubble = findViewById(R.id.notificationBubble)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        notificationBubble.isGone = true
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

    fun setNotificationBubble(text: String?) {
        notificationBubble.isGone = text == null
        notificationBubble.text = text
    }

}

class NavArrowButtonBlackBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_black, this)

        textView = findViewById(R.id.navarrow_text)
        progressBar = findViewById(R.id.navarrow_progressbar)
        arrow = findViewById(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonRed(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_red, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonRedBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_red, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonBlue(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_blue, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonBlueBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_blue, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonGreen(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_green, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NavArrowButton)
        textView.text = attributes.getString(R.styleable.NavArrowButton_text)
        progressBar.isGone = !attributes.getBoolean(R.styleable.NavArrowButton_showLoading, false)
        arrow.isGone = attributes.getBoolean(R.styleable.NavArrowButton_showLoading, true)
        attributes.recycle()
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}

class NavArrowButtonGreenBuildable(context: Context): LinearLayout(context) {

    val textView: TextView
    val progressBar: ProgressBar
    val arrow: ImageView

    init {
        inflate(context, R.layout.navarrowbutton_green, this)

        textView = findViewById<TextView>(R.id.navarrow_text)
        progressBar = findViewById<ProgressBar>(R.id.navarrow_progressbar)
        arrow = findViewById<ImageView>(R.id.navarrow_arrow)
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
        arrow.isGone = loading
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            if (progressBar.isGone) {
                callback()
            }
        }
    }

}