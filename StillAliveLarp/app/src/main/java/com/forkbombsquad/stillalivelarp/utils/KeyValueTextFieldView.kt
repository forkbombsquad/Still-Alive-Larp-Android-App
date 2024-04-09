package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.textfield.TextInputEditText

class KeyValueTextFieldView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val keyView: TextView
    val valueTextField: TextInputEditText
    val div: MaterialDivider

    init {
        inflate(context, R.layout.keyvaluetextfieldview, this)

        keyView = findViewById(R.id.keyvalueview_key)
        valueTextField = findViewById(R.id.keyvalueview_textfield)
        div = findViewById(R.id.keyvalueview_div)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.KeyValueView)
        keyView.text = attributes.getString(R.styleable.KeyValueView_key)
        valueTextField.setText(attributes.getString(R.styleable.KeyValueView_value))
        div.isGone = !attributes.getBoolean(R.styleable.KeyValueView_showDiv, false)
        attributes.recycle()
    }

    fun getValue(): String {
        return valueTextField.text.toString()
    }

    fun set(key: String, value: String, showDiv: Boolean) {
        keyView.text = key
        valueTextField.setText(value)
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

    fun setAndHideIfEmpty(value: String) {
        set(keyView.text.toString(), value, !div.isGone)
        this.isGone = value.isEmpty()
    }

}