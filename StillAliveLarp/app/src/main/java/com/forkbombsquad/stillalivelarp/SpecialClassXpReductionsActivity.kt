package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class SpecialClassXpReductionsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var loading: ProgressBar
    private lateinit var noRedsText: TextView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special_class_xp_reductions)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.xpredview_title)
        loading = findViewById(R.id.xpredview_loading)
        noRedsText = findViewById(R.id.xpredview_noredtext)
        layout = findViewById(R.id.xpredview_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.XP_REDUCTIONS), true) {
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS), false) {
                buildView()
            }
        }
    }

    private fun buildView() {
        title.text = "Class Xp Reductions For\n${DataManager.shared.character?.fullName ?: "Unknown"}"
        if (DataManager.shared.loadingXpReductions || DataManager.shared.loadingSkills) {
            loading.isGone = false
            noRedsText.isGone = true
            layout.isGone = true
        } else if (DataManager.shared.xpReductions.isNullOrEmpty()) {
            loading.isGone = true
            noRedsText.isGone = false
            layout.isGone = true
        } else {
            layout.removeAllViews()
            loading.isGone = true
            noRedsText.isGone = true
            layout.isGone = false

            val skills = DataManager.shared.skills ?: listOf()
            val xpReds = DataManager.shared.xpReductions ?: listOf()

            for (xpred in xpReds) {
                getSkill(xpred, skills).ifLet { skill ->
                    val kvView = KeyValueViewBuildable(this)
                    kvView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    kvView.set(skill.name, "-${xpred.xpReduction} (new cost: ${skill.getModCost(0, 0, 0, xpred)})")
                    layout.addView(kvView)
                }
            }
        }
    }

    private fun getSkill(xpRed: XpReductionModel, skills: List<FullSkillModel>): FullSkillModel? {
        return skills.firstOrNull { it.id == xpRed.skillId }
    }
}