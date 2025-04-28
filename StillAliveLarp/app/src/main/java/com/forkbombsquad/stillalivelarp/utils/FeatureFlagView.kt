package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel

class FeatureFlagView(context: Context): LinearLayout(context) {

    val name: TextView
    val desc: TextView
    val android: TextView
    val iOS: TextView
    val edit: SmallLoadingButton

    private lateinit var callback: () -> Unit

    init {
        inflate(context, R.layout.featureflagview, this)

        name = findViewById(R.id.featureflagview_name)
        desc = findViewById(R.id.featureflagview_desc)
        android = findViewById(R.id.featureflagview_android)
        iOS = findViewById(R.id.featureflagview_iOS)
        edit = findViewById(R.id.featureflagview_edit)
    }

    fun set(featureFlag: FeatureFlagModel) {
        name.text = featureFlag.name
        desc.text = featureFlag.description

        android.text = featureFlag.isActiveAndroid().ternary("ANDROID: ON", "ANDROID: OFF")
        android.setTextColor(context.getColor(featureFlag.isActiveAndroid().ternary(R.color.green, R.color.mid_red)))

        iOS.text = featureFlag.isActiveIos().ternary("iOS: ON", "iOS: OFF")
        iOS.setTextColor(context.getColor(featureFlag.isActiveIos().ternary(R.color.green, R.color.mid_red)))
    }

    fun setLoading(isLoading: Boolean) {
        edit.progressView.isGone = !isLoading
        edit.textView.isGone = isLoading
        edit.progressView2.isGone = true
        edit.loadingText.isGone = true
    }

    fun setOnClickEdit(callback: () -> Unit) {
        this.callback = callback
        this.edit.setOnClickListener {
            if (edit.progressView.isGone) {
                this.dismissKeyboard()
                this.callback()
            }
        }

    }

}