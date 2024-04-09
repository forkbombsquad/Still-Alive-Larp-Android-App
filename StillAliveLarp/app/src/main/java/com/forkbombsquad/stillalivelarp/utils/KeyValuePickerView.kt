package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.google.android.material.divider.MaterialDivider

class KeyValuePickerView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val keyView: TextView
    val valueTitleView: TextView
    val valuePickerView: Spinner
    val div: MaterialDivider

    init {
        inflate(context, R.layout.keyvaluepickerview, this)

        keyView = findViewById(R.id.keyvalueview_key)
        valueTitleView = findViewById(R.id.keyvalueview_valuetitle)
        valuePickerView = findViewById(R.id.keyvalueview_valuepicker)
        div = findViewById(R.id.keyvalueview_div)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.KeyValueView)
        keyView.text = attributes.getString(R.styleable.KeyValueView_key)
        valueTitleView.text = attributes.getString(R.styleable.KeyValueView_value)
        div.isGone = !attributes.getBoolean(R.styleable.KeyValueView_showDiv, false)
        attributes.recycle()
    }

    fun set(key: String, valueTitle: String, showDiv: Boolean) {
        keyView.text = key
        valueTitleView.text = valueTitle
        div.isGone = !showDiv
    }

    fun set(valueTitle: String, showDiv: Boolean) {
        set(keyView.text.toString(), valueTitle, showDiv)
    }

    fun set(key: String, valueTitle: String) {
        set(key, valueTitle, !div.isGone)
    }

    fun set(valueTitle: String) {
        set(keyView.text.toString(), valueTitle, !div.isGone)
    }

}