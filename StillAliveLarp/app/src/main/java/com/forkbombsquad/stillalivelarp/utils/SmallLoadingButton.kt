package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R

class SmallLoadingButton(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val textView: TextView
    val progressView: ProgressBar
    val loadingText: TextView
    val progressView2: ProgressBar

    private lateinit var callback: () -> Unit

    init {
        inflate(context, R.layout.smallloadingbutton, this)

        textView = findViewById(R.id.smallloadingbutton_text)
        progressView = findViewById(R.id.smallloadingbutton_progressbar)
        loadingText = findViewById(R.id.smallloadingbutton_loadingText)
        progressView2 = findViewById(R.id.smallloadingbutton_progressbar2)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
        textView.text = attributes.getString(R.styleable.LoadingButton_buttontext)
        progressView.isGone = !attributes.getBoolean(R.styleable.LoadingButton_loading, false)
        textView.isGone = !progressView.isGone
        loadingText.isGone = true
        progressView2.isGone = true
        attributes.recycle()
    }

    fun set(text: String) {
        textView.text = text
    }

    fun setLoading(isLoading: Boolean) {
        progressView.isGone = !isLoading
        textView.isGone = isLoading
        progressView2.isGone = true
        loadingText.isGone = true
    }

    fun setLoadingWithText(text: String) {
        progressView.isGone = false
        textView.isGone = true
        progressView2.isGone = false
        loadingText.isGone = false

        loadingText.text = text
    }

    fun setOnClick(callback: () -> Unit) {
        this.callback = callback
        this.setOnClickListener {
            if (progressView.isGone) {
                this.dismissKeyboard()
                this.callback()
            }
        }

    }

}