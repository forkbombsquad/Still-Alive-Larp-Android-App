package com.forkbombsquad.stillalivelarp.tabbar_fragments.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.utils.*
import kotlinx.coroutines.launch

class AddSkillActivity : NoStatusBarActivity() {

    private var currentSort: SkillSortType = SkillSortType.AZ
    private var currentFilter: SkillFilterType = SkillFilterType.NONE

    private lateinit var xp: TextView
    private lateinit var pp: TextView
    private lateinit var ft1s: TextView
    private lateinit var inf: TextView

    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var searchBar: EditText

    private lateinit var layout: LinearLayout

    private lateinit var modSkillList: List<CharacterModifiedSkillModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_skill)
        setupView()
    }

    private fun setupView() {
        xp = findViewById(R.id.addskill_xp)
        pp = findViewById(R.id.addskill_pp)
        ft1s = findViewById(R.id.addskill_ft1s)
        inf = findViewById(R.id.addskill_inf)

        sortSpinner = findViewById(R.id.addskill_sort)
        filterSpinner = findViewById(R.id.addskill_filter)
        searchBar = findViewById(R.id.addskill_searchview)

        layout = findViewById(R.id.addskill_layout)

        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillSortType.getAllStrings())
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = SkillSortType.getTypeForString(sortSpinner.getItemAtPosition(position).toString())
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillFilterType.getAllStrings())
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = SkillFilterType.getTypeForString(filterSpinner.getItemAtPosition(position).toString())
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchBar.addTextChangedListener {
            buildView()
        }

        modSkillList = getAvailableSkills(DataManager.shared.skills, DataManager.shared.player, DataManager.shared.character, DataManager.shared.xpReductions)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.XP_REDUCTIONS), false) {
            modSkillList = getAvailableSkills(DataManager.shared.skills, DataManager.shared.player, DataManager.shared.character, DataManager.shared.xpReductions)
            buildView()
        }
        buildView()
    }

    private fun buildView(loading: Boolean = false) {
        val character = DataManager.shared.character
        val player = DataManager.shared.player

        xp.text = "experience\n${player?.experience ?: "0"}"
        pp.text = "prestige\n${player?.prestigePoints ?: "0"}"
        ft1s.text = "Free T1 Skills\n${player?.freeTier1Skills ?: "0"}"
        inf.text = "Infection\n${character?.infection ?: "0"}%"

        layout.removeAllViews()

        getFilteredSkills(modSkillList).forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setupForPurchase(it, player!!) { skill ->
                buildView(true)
                // PURCHASE SKILL
                var xpSpent = skill.modXpCost.toInt()
                var fsSpent = 0
                var messageString = "${skill.name} purchased using"
                if (skill.canUseFreeSkill() && player.freeTier1Skills.toInt() > 0) {
                    fsSpent = 1
                    xpSpent = 0
                    messageString = "$messageString 1 Free Tier-1 Skill point"
                } else {
                    messageString = "$messageString ${xpSpent}xp"
                }

                if (skill.usesPrestige()) {
                    messageString = "$messageString and ${skill.prestigeCost}pp"
                }

                val charSkill = CharacterSkillCreateModel(
                    characterId = character?.id ?: 0,
                    skillId = skill.id,
                    xpSpent = xpSpent,
                    fsSpent = fsSpent,
                    ppSpent = skill.prestigeCost.toInt()
                )

                val charTakeSkillRequest = CharacterSkillService.TakeCharacterSkill()
                lifecycleScope.launch {
                    charTakeSkillRequest.successfulResponse(CharacterSkillCreateSP(player?.id ?: 0, charSkill)).ifLet({
                        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CHARACTER, DataManagerType.PLAYER), true) {
                            AlertUtils.displayOkMessage(this@AddSkillActivity, "Skill Purchased", messageString) { _, _ -> }
                            modSkillList = getAvailableSkills(DataManager.shared.skills, DataManager.shared.player, DataManager.shared.character, DataManager.shared.xpReductions)
                            buildView(false)
                            DataManager.shared.unrelaltedUpdateCallback()
                        }
                    }, {
                        buildView(false)
                    })
                }
            }
            cell.purchaseButton.setLoading(loading)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            layout.addView(cell)
        }
    }

    private fun getFilteredSkills(skills: List<CharacterModifiedSkillModel>): List<CharacterModifiedSkillModel> {
        var filteredSkills = skills
        val text = searchBar.text.toString().trim().lowercase()
        if (text.isNotEmpty() || currentFilter != SkillFilterType.NONE) {
            filteredSkills = skills.filter { it.includeInFilter(text, currentFilter) }
        }
        return getSortedSkills(filteredSkills)
    }

    private fun getSortedSkills(skills: List<CharacterModifiedSkillModel>): List<CharacterModifiedSkillModel> {
        var sorted = skills
        sorted = when (currentSort) {
            SkillSortType.AZ -> skills.sortedWith(compareBy { it.name })
            SkillSortType.ZA -> skills.sortedWith(compareByDescending { it.name })
            SkillSortType.XPASC -> skills.sortedWith(compareBy({ it.xpCost.toInt() }, { it.name }))
            SkillSortType.XPDESC -> skills.sortedWith(compareByDescending { it.xpCost.toInt() })
            SkillSortType.TYPEASC -> skills.sortedWith(compareBy({ it.getTypeText() }, { it.name }))
            SkillSortType.TYPEDESC -> skills.sortedWith(compareByDescending { it.getTypeText() })
        }
        return sorted
    }

    private fun getAvailableSkills(skls: List<FullSkillModel>?, player: PlayerModel?, character: FullCharacterModel?, xpReductions: List<XpReductionModel>?): List<CharacterModifiedSkillModel> {
        val allSkills = skls ?: listOf()
        val charSkills: List<FullSkillModel> = character?.skills?.toList() ?: listOf()

        // Remove skills the character already has
        var newSkillList: List<FullSkillModel> = allSkills.filter { skillToKeep ->
            charSkills.firstOrNull { charSkill ->
                charSkill.id == skillToKeep.id
            } == null
        }

        // Remove all skills you don't have prereqs for
        newSkillList = newSkillList.filter { skillToKeep ->
            if (skillToKeep.prereqs.isEmpty()) {
                true
            } else {
                var keep = true
                for (prereq in skillToKeep.prereqs) {
                    if (charSkills.firstOrNull { charSkill ->
                            charSkill.id == prereq.id
                        } == null) {
                        keep = false
                        break
                    }
                }
                keep
            }
        }

        // Filter out pp skills you don't qualify for
        newSkillList = newSkillList.filter { skillToKeep ->
            skillToKeep.prestigeCost.toInt() <= (player?.prestigePoints?.toInt() ?: 0)
        }

        // Remove Choose One Skills that can't be chosen
        val cskills: List<FullSkillModel> = character?.getChooseOneSkills()?.toList() ?: listOf()
        if (cskills.isEmpty()) {
            // Remove all level 2 cskills
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allLevel2SpecialistSkills)
            }
        } else if (cskills.count() == 2) {
            // Remove all cskills
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allSpecalistSkills)
            }
        } else if (cskills.firstOrNull() != null) {
            val cskill = cskills.first()
            var idsToRemove: Array<Int> = arrayOf()
            when (cskill.id) {
                Constants.SpecificSkillIds.expertCombat -> idsToRemove = Constants.SpecificSkillIds.allSpecalistsNotUnderExpertCombat
                Constants.SpecificSkillIds.expertProfession -> idsToRemove = Constants.SpecificSkillIds.allSpecalistsNotUnderExpertProfession
                Constants.SpecificSkillIds.expertTalent -> idsToRemove = Constants.SpecificSkillIds.allSpecalistsNotUnderExpertTalent
            }
            // Remove cskills not under your exper skill
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(idsToRemove)
            }
        }

        val combatXpMod = character?.costOfCombatSkills() ?: 0
        val professionXpMod = character?.costOfProfessionSkills() ?: 0
        val talentXpMod = character?.costOfTalentSkills() ?: 0
        val inf50Mod = character?.costOf50InfectSkills() ?: 50
        val inf75Mod = character?.costOf75InfectSkills() ?: 75

        // Convert to new model type
        var newCharModSkills: MutableList<CharacterModifiedSkillModel> = mutableListOf()
        newSkillList.forEach { skill ->
            newCharModSkills.add(
                CharacterModifiedSkillModel(
                    fsm = skill,
                    modXpCost = skill.getModCost(
                        combatMod = combatXpMod,
                        professionMod = professionXpMod,
                        talentMod = talentXpMod,
                        xpReductions = xpReductions?.toTypedArray() ?: arrayOf()
                    ).toString(),
                    modInfCost = skill.getInfModCost(inf50Mod, inf75Mod).toString()
                )
            )
        }

        // Filter out skills that you don't have enough xp, fs, or inf for
        return newCharModSkills.filter { skillToKeep ->
            var keep = true
            keep = if (skillToKeep.modInfCost.toInt() > (character?.infection?.toInt() ?: 0)) {
                false
            } else if (skillToKeep.modXpCost.toInt() > (player?.experience?.toInt() ?: 0)) {
                skillToKeep.canUseFreeSkill() && (player?.freeTier1Skills?.toInt() ?: 0) > 0
            } else {
                true
            }
            keep
        }
    }
}