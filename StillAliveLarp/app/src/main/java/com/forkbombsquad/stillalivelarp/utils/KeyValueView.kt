package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.google.android.material.divider.MaterialDivider

class KeyValueView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val keyView: TextView
    val valueView: TextView
    val div: MaterialDivider

    init {
        inflate(context, R.layout.keyvalueview, this)

        keyView = findViewById(R.id.keyvalueview_key)
        valueView = findViewById(R.id.keyvalueview_value)
        div = findViewById(R.id.keyvalueview_div)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.KeyValueView)
        keyView.text = attributes.getString(R.styleable.KeyValueView_key)
        valueView.text = attributes.getString(R.styleable.KeyValueView_value)
        div.isGone = !attributes.getBoolean(R.styleable.KeyValueView_showDiv, false)
        attributes.recycle()
    }

    fun set(key: String, value: String, showDiv: Boolean) {
        keyView.text = key
        valueView.text = value
        div.isGone = !showDiv
    }

    fun set(value: String, showDiv: Boolean) {
        set(keyView.text.toString(), value, showDiv)
    }

    fun set(key: String, value: String) {
        set(key, value, !div.isGone)
    }

    fun set(value: String) {
        set(keyView.text.toString(), value, !div.isGone)
    }

    fun set(value: Int) {
        set(value.toString())
    }

    fun setAndHideIfEmpty(value: String) {
        set(keyView.text.toString(), value, !div.isGone)
        this.isGone = value.isEmpty()
    }

    fun makeCopyable() {
        this.setOnLongClickListener {
            globalCopyToClipboard(this.context, this)
            true
        }
    }

}


class KeyValueViewBuildable(context: Context): LinearLayout(context) {

    val keyView: TextView
    val valueView: TextView
    val div: MaterialDivider

    init {
        inflate(context, R.layout.keyvalueview, this)

        keyView = findViewById(R.id.keyvalueview_key)
        valueView = findViewById(R.id.keyvalueview_value)
        div = findViewById(R.id.keyvalueview_div)
    }

    fun set(key: String, value: String, showDiv: Boolean) {
        keyView.text = key
        valueView.text = value
        div.isGone = !showDiv
    }

    fun set(value: String, showDiv: Boolean) {
        set(keyView.text.toString(), value, showDiv)
    }

    fun set(key: String, value: String) {
        set(key, value, !div.isGone)
    }

    fun set(value: String) {
        set(keyView.text.toString(), value, !div.isGone)
    }

}