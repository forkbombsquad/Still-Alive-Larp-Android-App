package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.views.account.admin.CreateEditFeatureFlagActivity
import com.forkbombsquad.stillalivelarp.views.community.ViewResearchProjectMilestonesActivity
import kotlin.reflect.KClass

class ResearchProjectCell(context: Context): LinearLayout(context) {

    val rpName: TextView
    val rpCompleted: TextView
    val rpMilestones: TextView
    val rpDesc: TextView
    val progressBar: ProgressBar
    val viewMilestonesButton: NavArrowButtonBlack

    init {
        inflate(context, R.layout.researchprojectcell, this)

        rpName = findViewById(R.id.rp_name)
        rpCompleted = findViewById(R.id.rp_complete)
        rpMilestones = findViewById(R.id.rp_milestones)
        rpDesc = findViewById(R.id.rp_desc)
        progressBar = findViewById(R.id.rp_progressBar)
        viewMilestonesButton = findViewById(R.id.rp_viewMilestones)
    }

    fun setup(rp: ResearchProjectModel, context: Context, kclass: KClass<*>) {
        rpName.text = rp.name
        rpCompleted.text = rp.complete.toBoolean().ternary("Yes", "No")
        rpMilestones.text = rp.milestones.toString()
        rpDesc.text = rp.description
        viewMilestonesButton.isGone = rp.milestoneJsonModels.isNullOrEmpty()

        viewMilestonesButton.setOnClick {
            DataManager.shared.setPassedData(kclass, DataManagerPassedDataKey.RESEARCH_PROJECT, rp)
            val intent = Intent(context, ViewResearchProjectMilestonesActivity::class.java)
            context.startActivity(intent)
        }
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