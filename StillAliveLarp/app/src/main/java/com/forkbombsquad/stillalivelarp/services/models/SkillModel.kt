package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.addMinOne
import com.forkbombsquad.stillalivelarp.utils.ifLet
import java.io.Serializable
import kotlin.math.max

data class FullCharacterModifiedSkillModel(
    private val skill: FullSkillModel,
    private val charSkillModel: CharacterSkillModel?,
    private val xpReduction: XpReductionModel?,
    private val combatXpMod: Int,
    private val professionXpMod: Int,
    private val talentXpMod: Int,
    private val inf50Mod: Int,
    private val inf75Mod: Int

): Serializable {

    val id = skill.id
    val name = skill.name
    val skillTypeId = skill.skillTypeId
    val description = skill.description

    fun prestigeCost(): Int {
        return skill.prestigeCost
    }

    fun hasXpReduction(): Boolean {
        return xpReduction != null
    }

    fun baseXpCost(): Int {
        return skill.xpCost
    }

    fun baseInfectionCost(): Int {
        return skill.minInfection
    }

    fun getRelevantSpecCostChange(): Int {
        return when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> combatXpMod
            Constants.SkillTypes.profession -> talentXpMod
            Constants.SkillTypes.talent -> professionXpMod
            else -> 0
        }
    }

    fun modXpCost(): Int {
        var baseCost = skill.xpCost
        xpReduction.ifLet {
            baseCost -= it.xpReduction.toInt()
        }
        baseCost += getRelevantSpecCostChange()
        var max = 1
        if (skill.xpCost == 0) {
            max = 0
        }
        return max(max, baseCost)
    }

    fun modInfectionCost(): Int {
        var baseCost = skill.minInfection
        if (baseCost == 50) {
            baseCost -= inf50Mod
        } else if (baseCost == 75) {
            baseCost -= inf75Mod
        }

        return max(0, baseCost)
    }

    fun usesPrestige(): Boolean {
        return prestigeCost() > 0
    }

    fun canUseFreeSkill(): Boolean {
        // Free Skills may not be used on skills that have been reduced to 1 xp, ONLY skills that are naturally 1 xp
        return skill.xpCost == 1
    }

    fun usesInfection(): Boolean {
        return modInfectionCost() > 0
    }

    fun hasModCost(): Boolean {
        return modXpCost() != baseXpCost()
    }

    fun hasModInfCost(): Boolean {
        return modInfectionCost() != baseInfectionCost()
    }

    fun getTypeText(): String {
        when (skill.skillTypeId) {
            Constants.SkillTypes.combat -> return "Combat"
            Constants.SkillTypes.profession -> return "Profession"
            Constants.SkillTypes.talent -> return "Talent"
        }
        return ""
    }

    fun hasPrereqs(): Boolean {
        return skill.prereqs.isNotEmpty()
    }

    fun getPrereqNames(): String {
        var str = ""
        skill.prereqs.forEachIndexed { index, prereq ->
            if (index > 0) {
                str += "\n"
            }
            str += prereq.name
        }
        return str
    }

    fun includeInFilter(seachText: String, filterType: SkillFilterType): Boolean {
        val text = seachText.trim().lowercase()
        if (text.isNotEmpty()) {
            if (!skill.name.lowercase().contains(text) && !getTypeText().lowercase()
                    .contains(text) && !skill.description.lowercase()
                    .contains(text) && !getPrereqNames().lowercase().contains(text)
            ) {
                return false
            }
        }
        return when (filterType) {
            SkillFilterType.NONE -> true
            SkillFilterType.COMBAT -> skill.skillTypeId == Constants.SkillTypes.combat
            SkillFilterType.PROFESSION -> skill.skillTypeId == Constants.SkillTypes.profession
            SkillFilterType.TALENT -> skill.skillTypeId == Constants.SkillTypes.talent
            SkillFilterType.XP0 -> modXpCost() == 0
            SkillFilterType.XP1 -> modXpCost() == 1
            SkillFilterType.XP2 -> modXpCost() == 2
            SkillFilterType.XP3 -> modXpCost() == 3
            SkillFilterType.XP4 -> modXpCost() >= 4 // <-- Also show skills that cost higher than 4
            SkillFilterType.PP -> prestigeCost() > 0
            SkillFilterType.INF -> modInfectionCost() > 0
        }
    }

    fun prereqs(): List<SkillModel> {
        return skill.prereqs
    }

    fun alreadyPurchased(): Boolean {
        return charSkillModel != null
    }

    fun getXpCostText(): String {
        var text = ""
        charSkillModel.ifLet({ cs ->
            // Already Purchased
            text += "Already Purchased With:\n"
            text += "${cs.xpSpent}xp"
            if (cs.fsSpent > 0) {
                text += "\n${cs.fsSpent} Free Tier 1 Skills"
            }
        }, {
            // Not purchased yet
            text += "${modXpCost()}xp"
            if (hasModCost()) {
                text += "(changed from ${baseXpCost()}xp with:"
                if (getRelevantSpecCostChange() != 0) {
                    text += " ${getRelevantSpecCostChange()} from ${getTypeText()} Specialization"
                }
                if (getRelevantSpecCostChange() != 0 && hasXpReduction()) {
                    text += " and"
                }
                if (hasXpReduction()) {
                    text += "  ${xpReduction?.xpReduction?.toInt() ?: 0} from Special Class Xp Reductions"
                }
                text += ")"
            }
        })
        return text
    }

    fun getInfCostText(): String {
        var text = ""
        text += "${modInfectionCost()}% Inf Threshold"
        if (hasModInfCost()) {
            text += " (changed from ${baseInfectionCost()}%)"
        }
        return text
    }

    fun getPrestigeCostText(): String {
        var text = ""
        charSkillModel.ifLet({ cs ->
            text = "${cs.ppSpent}pp"
        }, {
            text = "${prestigeCost()}pp"
        })
        return text
    }

    fun getFullCostText(): String {
        var text = ""
        charSkillModel.ifLet({ cs ->
            // Already Purchased
            text += getXpCostText()
            if (cs.ppSpent > 0) {
                text += "\n"
                text += getPrestigeCostText()
            }
        }, {
            // Not purchased yet
            text += getXpCostText()
            if (baseInfectionCost() > 0) {
                text += "\n"
                text += getInfCostText()
            }
            if (usesPrestige()) {
                text += "\n"
                text += getPrestigeCostText()
            }
        })
        return text
    }

}

data class FullSkillModel(
    val id: Int,
    val xpCost: Int,
    val prestigeCost: Int,
    val name: String,
    val description: String,
    val minInfection: Int,
    val skillTypeId: Int,
    val skillCategoryId: Int,
    val prereqs: List<SkillModel>,
    val postreqs: List<SkillModel>,
    val category: SkillCategoryModel
): Serializable {
    constructor(skillModel: SkillModel, prereqs: List<SkillModel>, postreqs: List<SkillModel>, category: SkillCategoryModel): this(
        skillModel.id,
        skillModel.xpCost.toInt(),
        skillModel.prestigeCost.toInt(),
        skillModel.name,
        skillModel.description,
        skillModel.minInfection.toInt(),
        skillModel.skillTypeId.toInt(),
        skillModel.skillCategoryId.toInt(),
        prereqs,
        postreqs,
        category
    )

    constructor(skillModel: SkillModel): this(
        skillModel.id,
        skillModel.xpCost.toInt(),
        skillModel.prestigeCost.toInt(),
        skillModel.name,
        skillModel.description,
        skillModel.minInfection.toInt(),
        skillModel.skillTypeId.toInt(),
        skillModel.skillCategoryId.toInt(),
        listOf(),
        listOf(),
        SkillCategoryModel(skillModel.skillCategoryId.toInt(), "")
    )

    fun getTypeText(): String {
        when(skillTypeId) {
            Constants.SkillTypes.combat -> return "Combat"
            Constants.SkillTypes.profession -> return "Profession"
            Constants.SkillTypes.talent -> return "Talent"
        }
        return ""
    }

    fun getPrereqNames(): String {
        var str = ""
        prereqs.forEachIndexed{ index, prereq ->
            if (index > 0) {
                str += "\n"
            }
            str += prereq.name
        }
        return str
    }

    fun hasSameCostPrereq(): Boolean {
        prereqs.forEach {
            if (it.xpCost.toInt() == xpCost) { return true }
        }
        return false
    }

    fun includeInFilter(seachText: String, filterType: SkillFilterType): Boolean {
        val text = seachText.trim().lowercase()
        if (text.isNotEmpty()) {
            if (!name.lowercase().contains(text) && !getTypeText().lowercase().contains(text) && !description.lowercase().contains(text) && !getPrereqNames().lowercase().contains(text)) {
                return false
            }
        }
        return when (filterType) {
            SkillFilterType.NONE -> true
            SkillFilterType.COMBAT -> skillTypeId.toInt() == Constants.SkillTypes.combat
            SkillFilterType.PROFESSION -> skillTypeId.toInt() == Constants.SkillTypes.profession
            SkillFilterType.TALENT -> skillTypeId.toInt() == Constants.SkillTypes.talent
            SkillFilterType.XP0 -> xpCost.toInt() == 0
            SkillFilterType.XP1 -> xpCost.toInt() == 1
            SkillFilterType.XP2 -> xpCost.toInt() == 2
            SkillFilterType.XP3 -> xpCost.toInt() == 3
            SkillFilterType.XP4 -> xpCost.toInt() == 4
            SkillFilterType.PP -> prestigeCost.toInt() > 0
            SkillFilterType.INF -> minInfection.toInt() > 0
        }
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("xpCost") val xpCost: String,
    @JsonProperty("prestigeCost") val prestigeCost: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("minInfection") val minInfection: String,
    @JsonProperty("skillTypeId") val skillTypeId: String,
    @JsonProperty("skillCategoryId") val skillCategoryId: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillBarcodeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String
) : Serializable {
    constructor(fullSkillModel: FullSkillModel): this(fullSkillModel.id, fullSkillModel.name)
    constructor(fullSkillModel: FullCharacterModifiedSkillModel): this(fullSkillModel.id, fullSkillModel.name)

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillListModel(
    @JsonProperty("results") val skills: Array<SkillModel>
) : Serializable