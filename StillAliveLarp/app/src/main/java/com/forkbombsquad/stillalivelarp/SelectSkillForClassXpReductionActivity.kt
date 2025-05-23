package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.OldFullSkillModel
import com.forkbombsquad.stillalivelarp.services.utils.TakeClassSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch
import kotlin.math.max

class SelectSkillForClassXpReductionActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchView: EditText
    private lateinit var layout: LinearLayout
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_skill_for_class_xp_reduction)
        setupView()
    }

    private fun setupView() {

        title = findViewById(R.id.selectskillforxpreduction_title)
        searchView = findViewById(R.id.selectskillforxpreduction_searchview)
        layout = findViewById(R.id.selectskillforxpreduction_layout)
        loading = findViewById(R.id.selectskillforxpreduction_loading)

        searchView.addTextChangedListener {
            buildView()
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.SELECTED_CHAR_XP_REDUCTIONS), true) {
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.SKILLS), false) {
                buildView()
            }
        }
    }

    private fun buildView() {
        title.text = "Select Skill For Xp Reduction For ${OldDataManager.shared.selectedChar?.fullName ?: "Unknown"}"
        layout.removeAllViews()
        OldDataManager.shared.selectedCharacterXpReductions.ifLet({ xpReds ->
            loading.isGone = true
            searchView.isGone = false
            layout.isGone = false
            getFilteredSkills(OldDataManager.shared.skills ?: listOf()).forEachIndexed { index, it ->
                val cell = SkillCell(this)
                cell.setupForXpReduction(it, xpReds.firstOrNull { red -> red.skillId == it.id }) { skill ->
                    cell.purchaseButton.setLoading(true)
                    val xpRedRequest = AdminService.GiveXpReduction()
                    lifecycleScope.launch {
                        xpRedRequest.successfulResponse(TakeClassSP(OldDataManager.shared.selectedChar?.id ?: -1, skill.id)).ifLet({ xpReduction ->
                            AlertUtils.displayOkMessage(this@SelectSkillForClassXpReductionActivity, "Successfully Added Skill Xp Reduction", "${skill.name} now costs ${max(1, skill.xpCost.toInt() - xpReduction.xpReduction.toInt())}xp for ${OldDataManager.shared.selectedChar?.fullName ?: "Unknown"}") { _, _ ->
                                OldDataManager.shared.activityToClose?.finish()
                                finish()
                            }
                        }, {
                            cell.purchaseButton.setLoading(false)
                        })
                    }
                }
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                layout.addView(cell)
            }
        }, {
            loading.isGone = false
            searchView.isGone = true
            layout.isGone = true
        })
    }

    private fun getFilteredSkills(skills: List<OldFullSkillModel>): List<OldFullSkillModel> {
        var filteredSkills = skills
        val text = searchView.text.toString().trim().lowercase()
        if (text.isNotEmpty()) {
            filteredSkills = skills.filter { it.xpCost.toInt() > 1 && it.includeInFilter(text, SkillFilterType.NONE) }
        }
        return filteredSkills.sortedWith(compareBy { it.name })
    }

}