package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import kotlin.math.max

class LoadingLayout(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val layout: LinearLayout
    val gettingContentText: TextView
    val loadingText: TextView
    val progressBar: ProgressBar
    init {
        inflate(context, R.layout.loading_layout, this)

        layout = findViewById(R.id.loadingLayout_layout)
        gettingContentText = findViewById(R.id.loadinglayout_gettingContentText)
        loadingText = findViewById(R.id.loadingLayout_loadingText)
        progressBar = findViewById(R.id.loadingLayout_ProgressBar)
    }

    fun setLoading(loading: Boolean) {
        layout.isGone = !loading
    }

    fun setLoadingText(text: String, showGettingContent: Boolean = true) {
        setLoading(true)
        loadingText.text = text
        gettingContentText.isGone = !showGettingContent
    }

}