package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.addMinOne
import java.io.Serializable

data class CharacterModifiedSkillModel(
    var id: Int,
    val xpCost: String,
    val prestigeCost: String,
    val name: String,
    val description: String,
    val minInfection: String,
    val skillTypeId: String,
    val skillCategoryId: String,
    var prereqs: Array<OldFullSkillModel>,
    var modXpCost: String,
    var modInfCost: String
) : Serializable {

    constructor(fsm: OldFullSkillModel, modXpCost: String, modInfCost: String): this(
        fsm.id,
        fsm.xpCost,
        fsm.prestigeCost,
        fsm.name,
        fsm.description,
        fsm.minInfection,
        fsm.skillTypeId,
        fsm.skillCategoryId,
        fsm.prereqs,
        modXpCost,
        modInfCost
    )

    fun usesPrestige(): Boolean {
        return prestigeCost.toInt() > 0
    }

    fun canUseFreeSkill(): Boolean {
        return xpCost.toInt() == 1
    }

    fun usesInfection(): Boolean {
        return minInfection.toInt() > 0
    }

    fun hasModCost(): Boolean {
        return xpCost.toInt() != modXpCost.toInt()
    }

    fun hasModInfCost(): Boolean {
        return minInfection.toInt() != modInfCost.toInt()
    }


    fun getModCost(combatMod: Int, professionMod: Int, talentMod: Int, xpReductions: Array<XpReductionModel>): Int {
        var cost = xpCost.toInt()
        when(skillTypeId.toInt()) {
            Constants.SkillTypes.combat -> cost = cost.addMinOne(combatMod)
            Constants.SkillTypes.profession -> cost = cost.addMinOne(professionMod)
            Constants.SkillTypes.talent -> cost = cost.addMinOne(talentMod)
        }
        xpReductions.forEach { reduction ->
            if (reduction.skillId == this.id) {
                cost = cost.addMinOne(-1 * reduction.xpReduction.toInt())
            }
        }
        return cost
    }

    fun getInfModCost(inf50Mod: Int, inf75Mod: Int): Int {
        when(minInfection.toInt()) {
            50 -> return inf50Mod
            75 -> return inf75Mod
        }
        return minInfection.toInt()
    }

    fun getTypeText(): String {
        when(skillTypeId.toInt()) {
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

    fun toFullSkillModel(): OldFullSkillModel {
        return OldFullSkillModel(
            id = id,
            xpCost = xpCost,
            prestigeCost = prestigeCost,
            name = name,
            description = description,
            minInfection = minInfection,
            skillTypeId = skillTypeId,
            skillCategoryId = skillCategoryId,
            prereqs = prereqs,
            arrayOf()
        )
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

    constructor(skillModel: CharacterModifiedSkillModel): this(
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

    fun getModCost(combatMod: Int, professionMod: Int, talentMod: Int, xpReductions: Array<XpReductionModel>): Int {
        var cost = xpCost
        when(skillTypeId) {
            Constants.SkillTypes.combat -> {
                if (cost > 0 || combatMod > 0) {
                    cost = cost.addMinOne(combatMod)
                }
            }
            Constants.SkillTypes.profession -> {
                if (cost > 0 || professionMod > 0) {
                    cost = cost.addMinOne(professionMod)
                }
            }
            Constants.SkillTypes.talent -> {
                if (cost > 0 || talentMod > 0) {
                    cost = cost.addMinOne(talentMod)
                }
            }
        }
        xpReductions.forEach { reduction ->
            if (reduction.skillId == this.id) {
                cost = cost.addMinOne(-1 * reduction.xpReduction.toInt())
            }
        }
        return cost
    }

    fun getModCost(combatMod: Int, professionMod: Int, talentMod: Int, xpReduction: XpReductionModel): Int {
        var cost = xpCost
        when(skillTypeId) {
            Constants.SkillTypes.combat -> {
                if (cost > 0 || combatMod > 0) {
                    cost = cost.addMinOne(combatMod)
                }
            }
            Constants.SkillTypes.profession -> {
                if (cost > 0 || professionMod > 0) {
                    cost = cost.addMinOne(professionMod)
                }
            }
            Constants.SkillTypes.talent -> {
                if (cost > 0 || talentMod > 0) {
                    cost = cost.addMinOne(talentMod)
                }
            }
        }
        if (xpReduction.skillId == this.id) {
            cost = cost.addMinOne(-1 * xpReduction.xpReduction.toInt())
        }
        return cost
    }

    fun getInfModCost(inf50Mod: Int, inf75Mod: Int): Int {
        when(minInfection) {
            50 -> return inf50Mod
            75 -> return inf75Mod
        }
        return minInfection
    }

    fun getTypeText(): String {
        when(skillTypeId) {
            Constants.SkillTypes.combat -> return "Combat"
            Constants.SkillTypes.profession -> return "Profession"
            Constants.SkillTypes.talent -> return "Talent"
        }
        return ""
    }

    fun getFullCostText(purchaseableSkills: List<CharacterModifiedSkillModel>): String {
        var text = ""
        val pskill = purchaseableSkills.firstOrNull { it.id == id }
        if (pskill != null) {
            if (pskill.hasModCost()) {
                text += "${pskill.modXpCost}xp (usual cost: ${xpCost}xp)"
            } else {
                text += "${xpCost}xp"
            }

            if (pskill.hasModInfCost() && minInfection > 0) {
                text += " | ${pskill.modInfCost}% Inf Threshold (usual threshold: ${minInfection}%)"
            } else if(minInfection > 0) {
                text += " | ${minInfection}% Inf Threshold"
            }

            if (prestigeCost > 0) {
                text += " | ${prestigeCost}pp"
            }
        } else {
            text += "${xpCost}xp"
            if (minInfection > 0) {
                text += " | ${minInfection}% Inf Threshold"
            }
            if (prestigeCost > 0) {
                text += " | ${prestigeCost}pp"
            }
        }
        return text
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

data class OldFullSkillModel(
    val id: Int,
    val xpCost: String,
    val prestigeCost: String,
    val name: String,
    val description: String,
    val minInfection: String,
    val skillTypeId: String,
    val skillCategoryId: String,
    var prereqs: Array<OldFullSkillModel>,
    var postreqs: Array<Int>
) : Serializable {

    constructor(skillModel: SkillModel): this(
        skillModel.id,
        skillModel.xpCost,
        skillModel.prestigeCost,
        skillModel.name,
        skillModel.description,
        skillModel.minInfection,
        skillModel.skillTypeId,
        skillModel.skillCategoryId,
        arrayOf(),
        arrayOf()
    )

    constructor(skillModel: CharacterModifiedSkillModel): this(
        skillModel.id,
        skillModel.xpCost,
        skillModel.prestigeCost,
        skillModel.name,
        skillModel.description,
        skillModel.minInfection,
        skillModel.skillTypeId,
        skillModel.skillCategoryId,
        arrayOf(),
        arrayOf()
    )

    fun getModCost(combatMod: Int, professionMod: Int, talentMod: Int, xpReductions: Array<XpReductionModel>): Int {
        var cost = xpCost.toInt()
        when(skillTypeId.toInt()) {
            Constants.SkillTypes.combat -> {
                if (cost > 0 || combatMod > 0) {
                    cost = cost.addMinOne(combatMod)
                }
            }
            Constants.SkillTypes.profession -> {
                if (cost > 0 || professionMod > 0) {
                    cost = cost.addMinOne(professionMod)
                }
            }
            Constants.SkillTypes.talent -> {
                if (cost > 0 || talentMod > 0) {
                    cost = cost.addMinOne(talentMod)
                }
            }
        }
        xpReductions.forEach { reduction ->
            if (reduction.skillId == this.id) {
                cost = cost.addMinOne(-1 * reduction.xpReduction.toInt())
            }
        }
        return cost
    }

    fun getModCost(combatMod: Int, professionMod: Int, talentMod: Int, xpReduction: XpReductionModel): Int {
        var cost = xpCost.toInt()
        when(skillTypeId.toInt()) {
            Constants.SkillTypes.combat -> {
                if (cost > 0 || combatMod > 0) {
                    cost = cost.addMinOne(combatMod)
                }
            }
            Constants.SkillTypes.profession -> {
                if (cost > 0 || professionMod > 0) {
                    cost = cost.addMinOne(professionMod)
                }
            }
            Constants.SkillTypes.talent -> {
                if (cost > 0 || talentMod > 0) {
                    cost = cost.addMinOne(talentMod)
                }
            }
        }
        if (xpReduction.skillId == this.id) {
            cost = cost.addMinOne(-1 * xpReduction.xpReduction.toInt())
        }
        return cost
    }

    fun getInfModCost(inf50Mod: Int, inf75Mod: Int): Int {
        when(minInfection.toInt()) {
            50 -> return inf50Mod
            75 -> return inf75Mod
        }
        return minInfection.toInt()
    }

    fun getTypeText(): String {
        when(skillTypeId.toInt()) {
            Constants.SkillTypes.combat -> return "Combat"
            Constants.SkillTypes.profession -> return "Profession"
            Constants.SkillTypes.talent -> return "Talent"
        }
        return ""
    }

    fun getFullCostText(purchaseableSkills: List<CharacterModifiedSkillModel>): String {
        var text = ""
        val pskill = purchaseableSkills.firstOrNull { it.id == id }
        if (pskill != null) {
            if (pskill.hasModCost()) {
                text += "${pskill.modXpCost}xp (usual cost: ${xpCost}xp)"
            } else {
                text += "${xpCost}xp"
            }

            if (pskill.hasModInfCost() && minInfection.toInt() > 0) {
                text += " | ${pskill.modInfCost}% Inf Threshold (usual threshold: ${minInfection}%)"
            } else if(minInfection.toInt() > 0) {
                text += " | ${minInfection}% Inf Threshold"
            }

            if (prestigeCost.toInt() > 0) {
                text += " | ${prestigeCost}pp"
            }
        } else {
            text += "${xpCost}xp"
            if (minInfection.toInt() > 0) {
                text += " | ${minInfection}% Inf Threshold"
            }
            if (prestigeCost.toInt() > 0) {
                text += " | ${prestigeCost}pp"
            }
        }
        return text
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
            if (it.xpCost == xpCost) { return true }
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
    constructor(oldFullSkillModel: OldFullSkillModel): this(oldFullSkillModel.id, oldFullSkillModel.name)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillListModel(
    @JsonProperty("results") val skills: Array<SkillModel>
) : Serializable