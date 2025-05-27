package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.CharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillModel
import com.forkbombsquad.stillalivelarp.services.models.OldFullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.OldFullSkillModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.SkillSortType
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch


class AddPlannedSkillActivity : NoStatusBarActivity() {

    private var currentSort: SkillSortType = SkillSortType.AZ
    private var currentFilter: SkillFilterType = SkillFilterType.NONE

    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var searchBar: EditText

    private lateinit var layout: LinearLayout

    private lateinit var modSkillList: List<CharacterModifiedSkillModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_planned_skill)
        setupView()
    }

    private fun setupView() {

        sortSpinner = findViewById(R.id.addplan_sort)
        filterSpinner = findViewById(R.id.addplan_filter)
        searchBar = findViewById(R.id.addplan_searchview)

        layout = findViewById(R.id.addplan_layout)

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

        modSkillList = getAvailableSkills(OldDataManager.shared.skills, OldDataManager.shared.selectedPlannedCharacter)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.SKILLS), false) {
            modSkillList = getAvailableSkills(OldDataManager.shared.skills, OldDataManager.shared.selectedPlannedCharacter)
            buildView()
        }
        buildView()
    }

    private fun buildView(loading: Boolean = false) {
        val character = OldDataManager.shared.selectedPlannedCharacter

        layout.removeAllViews()

        getFilteredSkills(modSkillList).forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setupForPlannedPurchase(it) { skill ->
                buildView(true)
                // PURCHASE SKILL
                var useFreeSkill: Boolean? = false

                if (skill.canUseFreeSkill()) {
                    AlertUtils.displayChoiceMessage(this, "Plan to use XP or a Free Tier-1 Skill?", arrayOf("XP", "Free Tier-1 Skill")) { choice ->
                        if (choice == -1) {
                            useFreeSkill = null
                        } else if (choice == 1) {
                            useFreeSkill = true
                        }
                        useFreeSkill.ifLet { usedFreeSkill ->
                            // Use free skill is set to null if the player cancels
                            var xpSpent = skill.modXpCost.toInt()
                            var fsSpent = 0
                            var messageString = "${skill.name} \"purchased\" using"
                            if (usedFreeSkill) {
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

                            val charTakeSkillRequest = CharacterSkillService.TakePlannedCharacterSkill()
                            lifecycleScope.launch {
                                charTakeSkillRequest.successfulResponse(CreateModelSP(charSkill)).ifLet({ charSkillModel ->
                                    val skills: MutableList<CharacterSkillModel> = OldDataManager.shared.selectedPlannedCharacterCharSkills?.toMutableList() ?: mutableListOf()
                                    skills.add(charSkillModel)
                                    OldDataManager.shared.selectedPlannedCharacterCharSkills = skills
                                    val fs: MutableList<OldFullSkillModel> = OldDataManager.shared.selectedPlannedCharacter?.skills?.toMutableList() ?: mutableListOf()
                                    fs.add(OldDataManager.shared.skills!!.first { it.id == charSkillModel.skillId })
                                    OldDataManager.shared.selectedPlannedCharacter?.skills = fs.toTypedArray()

                                    AlertUtils.displayOkMessage(this@AddPlannedSkillActivity, "Skill Successfully Planned", messageString) { _, _ -> }
                                    modSkillList = getAvailableSkills(OldDataManager.shared.skills, OldDataManager.shared.selectedPlannedCharacter)
                                    buildView(false)
                                    OldDataManager.shared.unrelaltedUpdateCallback()
                                }, {
                                    buildView(false)
                                })
                            }
                        }
                    }
                } else {
                    useFreeSkill.ifLet { usedFreeSkill ->
                        // Use free skill is set to null if the player cancels
                        var xpSpent = skill.modXpCost.toInt()
                        var fsSpent = 0
                        var messageString = "${skill.name} \"purchased\" using"
                        if (usedFreeSkill) {
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

                        val charTakeSkillRequest = CharacterSkillService.TakePlannedCharacterSkill()
                        lifecycleScope.launch {
                            charTakeSkillRequest.successfulResponse(CreateModelSP(charSkill)).ifLet({ charSkillModel ->
                                val skills: MutableList<CharacterSkillModel> = OldDataManager.shared.selectedPlannedCharacterCharSkills?.toMutableList() ?: mutableListOf()
                                skills.add(charSkillModel)
                                OldDataManager.shared.selectedPlannedCharacterCharSkills = skills
                                val fs: MutableList<OldFullSkillModel> = OldDataManager.shared.selectedPlannedCharacter?.skills?.toMutableList() ?: mutableListOf()
                                fs.add(OldDataManager.shared.skills!!.first { it.id == charSkillModel.skillId })
                                OldDataManager.shared.selectedPlannedCharacter?.skills = fs.toTypedArray()

                                AlertUtils.displayOkMessage(this@AddPlannedSkillActivity, "Skill Successfully Planned", messageString) { _, _ -> }
                                modSkillList = getAvailableSkills(OldDataManager.shared.skills, OldDataManager.shared.selectedPlannedCharacter)
                                buildView(false)
                                OldDataManager.shared.unrelaltedUpdateCallback()
                            }, {
                                buildView(false)
                            })
                        }
                    }
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

    private fun getAvailableSkills(skls: List<OldFullSkillModel>?, character: OldFullCharacterModel?): List<CharacterModifiedSkillModel> {
        val allSkills = skls ?: listOf()
        val charSkills: List<OldFullSkillModel> = character?.skills?.toList() ?: listOf()

        // Remove skills the character already has
        var newSkillList: List<OldFullSkillModel> = allSkills.filter { skillToKeep ->
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

        // Remove Choose One Skills that can't be chosen
        val cskills: List<OldFullSkillModel> = character?.getChooseOneSkills()?.toList() ?: listOf()
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
                        xpReductions = arrayOf()
                    ).toString(),
                    modInfCost = skill.getInfModCost(inf50Mod, inf75Mod).toString()
                )
            )
        }

        return newCharModSkills
    }
}