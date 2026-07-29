package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.PercentageCell
import com.forkbombsquad.stillalivelarp.utils.PercentageCellBuildable
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.math.truncate

class SkillFrequencyActivity : NoStatusBarActivity() {

    private lateinit var searchBar: EditText
    private lateinit var includeFreeSkills: CheckBox
    private lateinit var includePlayers: CheckBox
    private lateinit var includeNPCs: CheckBox
    private lateinit var includeDead: CheckBox
    private lateinit var innerLayout: LinearLayout

    private lateinit var allSkills: List<FullCharacterModifiedSkillModel>
    private lateinit var allChars: List<FullCharacterModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skill_frequency)
        setupView()
    }

    private fun setupView() {
        searchBar = findViewById(R.id.skillfreq_searchview)
        includeFreeSkills = findViewById(R.id.skillfreq_includeFreeSkills)
        includePlayers = findViewById(R.id.skillfreq_includePlayers)
        includeNPCs = findViewById(R.id.skillfreq_includeNPCs)
        includeDead = findViewById(R.id.skillfreq_includeDead)
        innerLayout = findViewById(R.id.skillfreq_innerlayout)

        searchBar.addTextChangedListener {
            buildView()
        }

        includeFreeSkills.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includePlayers.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeNPCs.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeDead.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        allSkills = DataManager.shared.getSkillsAsFCMSM()
        allChars = DataManager.shared.getAllCharacters(listOf(CharacterType.STANDARD, CharacterType.NPC))

        buildView()
    }

    private fun buildView() {
        val skills = getSkills()
        val chars = getCharacters()
        val skillCounts = getSkillCounts(chars, skills)
        val totalChars = chars.count()
        innerLayout.removeAllViews()

        skillCounts.entries.sortedWith(compareByDescending<Map.Entry<FullCharacterModifiedSkillModel, Int>> { it.value }.thenBy { it.key.name }).forEachIndexed { index, (skill, count) ->

            val cell = PercentageCellBuildable(this@SkillFrequencyActivity)
            val percent = truncate((count.toFloat() / totalChars.toFloat()) * 100f)
            cell.set("${skill.name}\n($count / $totalChars)", percent)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            innerLayout.addView(cell)
        }

    }

    private fun getSkillCounts(characters: List<FullCharacterModel>, skills: List<FullCharacterModifiedSkillModel>): Map<FullCharacterModifiedSkillModel, Int> {
        val skillAggregate: MutableMap<FullCharacterModifiedSkillModel, Int> = mutableMapOf()
        skills.forEach { skill ->
            skillAggregate[skill] = characters.count { char -> char.allPurchasedSkills().any { it.id == skill.id } }
        }
        return skillAggregate
    }

    private fun getSkills(): List<FullCharacterModifiedSkillModel> {
        return if (includeFreeSkills.isChecked) {
            allSkills.filter { it.includeInFilter(searchBar.text.toString(), SkillFilterType.NONE) }
        } else {
            allSkills.filter { it.baseXpCost() > 0 }.filter { it.includeInFilter(searchBar.text.toString(), SkillFilterType.NONE) }
        }
    }

    private fun getCharacters(): List<FullCharacterModel> {
        return if (includePlayers.isChecked && includeNPCs.isChecked) {
            includeDead.isChecked.ternary(allChars, otherwise = allChars.filter { it.isAlive })
        } else if (includePlayers.isChecked) {
            includeDead.isChecked.ternary(allChars.filter { it.characterType() == CharacterType.STANDARD }, otherwise = allChars.filter { it.characterType() == CharacterType.STANDARD }.filter { it.isAlive })
        } else if (includeNPCs.isChecked) {
            includeDead.isChecked.ternary(allChars.filter { it.characterType() == CharacterType.NPC }, otherwise = allChars.filter { it.characterType() == CharacterType.NPC }.filter { it.isAlive })
        } else {
            listOf()
        }
    }

}