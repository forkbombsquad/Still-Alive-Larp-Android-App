package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel

class ResearchProjectCell(context: Context): LinearLayout(context) {

    val rpName: TextView
    val rpCompleted: TextView
    val rpMilestones: TextView
    val rpDesc: TextView
    val progressBar: ProgressBar

    init {
        inflate(context, R.layout.researchprojectcell, this)

        rpName = findViewById(R.id.rp_name)
        rpCompleted = findViewById(R.id.rp_complete)
        rpMilestones = findViewById(R.id.rp_milestones)
        rpDesc = findViewById(R.id.rp_desc)
        progressBar = findViewById(R.id.rp_progressBar)
    }

    fun setup(rp: ResearchProjectModel) {
        rpName.text = rp.name
        rpCompleted.text = rp.complete.toBoolean().ternary("Yes", "No")
        rpMilestones.text = rp.milestones.toString()
        rpDesc.text = rp.description
    }

    fun setOnClick(callback: () -> Unit) {
        this.setOnClickListener {
            callback()
        }
    }

    fun setLoading(loading: Boolean) {
        progressBar.isGone = !loading
    }

}