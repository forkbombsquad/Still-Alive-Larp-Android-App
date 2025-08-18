package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.CharactersListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel

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

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_skill_for_class_xp_reduction)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(CharactersListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.selectskillforxpreduction_title)
        searchView = findViewById(R.id.selectskillforxpreduction_searchview)
        layout = findViewById(R.id.selectskillforxpreduction_layout)

        searchView.addTextChangedListener {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "Select Skill For Xp Reduction For ${character.fullName}"
        layout.removeAllViews()

        getFilteredSkills().forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setupForXpReduction(it) { skill ->
                cell.purchaseButton.setLoading(true)
                val xpRedRequest = AdminService.GiveXpReduction()
                lifecycleScope.launch {
                    xpRedRequest.successfulResponse(TakeClassSP(character.id, skill.id)).ifLet({ _ ->
                        AlertUtils.displayOkMessage(this@SelectSkillForClassXpReductionActivity, "Successfully Added Skill Xp Reduction", "${skill.name} now costs ${max(1, skill.modXpCost() - 1)}xp for ${character.fullName}") { _, _ ->
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
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
    }

    private fun getFilteredSkills(): List<FullCharacterModifiedSkillModel> {
        // Remove all skills that cost 1 or less xp for this character, including those that already have reductions.
        var filteredSkills = character.allNonPurchasedSkills().filter { it.modXpCost() > 1 }
        val text = searchView.text.toString().trim().lowercase()
        if (text.isNotEmpty()) {
            filteredSkills = filteredSkills.filter { it.modXpCost() > 1 && it.includeInFilter(text, SkillFilterType.NONE) }
        }
        return filteredSkills.sortedWith(compareBy { it.name })
    }

}