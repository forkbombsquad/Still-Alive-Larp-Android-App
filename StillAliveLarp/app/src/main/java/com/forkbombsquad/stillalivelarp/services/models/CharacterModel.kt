package com.forkbombsquad.stillalivelarp.services.models

import android.content.DialogInterface
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.Serializable

enum class CharacterType(val id: Int) {
    STANDARD(1),
    NPC(2),
    PLANNER(3),
    HIDDEN(4);

    companion object {
        private val map = values().associateBy(CharacterType::id)
        fun fromId(id: Int): CharacterType? = map[id]
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FullCharacterModel(
    val id: Int,
    val fullName: String,
    val startDate: String,
    val isAlive: Boolean,
    val deathDate: String,
    val infection: String,
    var bio: String,
    val approvedBio: Boolean,
    val bullets: Int,
    val megas: Int,
    val rivals: Int,
    val rockets: Int,
    val bulletCasings: Int,
    val clothSupplies: Int,
    val woodSupplies: Int,
    val metalSupplies: Int,
    val techSupplies: Int,
    val medicalSupplies: Int,
    val armor: String,
    val unshakableResolveUses: Int,
    val mysteriousStrangerUses: Int,
    val playerId: Int,
    val characterTypeId: Int,
    val gear: GearModel?,
    val awards: List<AwardModel>,
    val eventAttendees: List<EventAttendeeModel>,
    val preregs: List<EventPreregModel>,
    val xpReductions: List<XpReductionModel>
) : Serializable {

    private var skills: List<FullCharacterModifiedSkillModel> = listOf()
        private set

    constructor(charModel: CharacterModel, allSkills: List<FullSkillModel>, charSkills: List<CharacterSkillModel>, gear: GearModel?, awards: List<AwardModel>, eventAttendees: List<EventAttendeeModel>, preregs: List<EventPreregModel>, xpReductions: List<XpReductionModel>): this(
        charModel.id,
        charModel.fullName,
        charModel.startDate,
        charModel.isAlive.toBoolean(),
        charModel.deathDate,
        charModel.infection,
        charModel.bio,
        charModel.approvedBio.toBoolean(),
        charModel.bullets.toInt(),
        charModel.megas.toInt(),
        charModel.rivals.toInt(),
        charModel.rockets.toInt(),
        charModel.bulletCasings.toInt(),
        charModel.clothSupplies.toInt(),
        charModel.woodSupplies.toInt(),
        charModel.metalSupplies.toInt(),
        charModel.techSupplies.toInt(),
        charModel.medicalSupplies.toInt(),
        charModel.armor,
        charModel.unshakableResolveUses.toInt(),
        charModel.mysteriousStrangerUses.toInt(),
        charModel.playerId,
        charModel.characterTypeId,
        gear,
        awards,
        eventAttendees,
        preregs,
        xpReductions
    ) {
        val fcmSkills: MutableList<FullCharacterModifiedSkillModel> = mutableListOf()
        allSkills.forEach { baseFullSkill ->
            val xpRed = xpReductions.firstOrNull { it.skillId == baseFullSkill.id }
            val charSkill = charSkills.first { it.skillId == baseFullSkill.id }
            fcmSkills.add(FullCharacterModifiedSkillModel(
                skill = baseFullSkill,
                charSkillModel = charSkill,
                xpReduction =  xpRed,
                costOfCombatSkills(),
                costOfProfessionSkills(),
                costOfTalentSkills(),
                costOf50InfectSkills(),
                costOf75InfectSkills()
            ))
        }
        this.skills = fcmSkills
    }

    fun getSkill(id: Int): FullCharacterModifiedSkillModel? {
        return skills.firstOrNull { it.id == id }
    }
    fun allSkillsWithCharacterModifications(): List<FullCharacterModifiedSkillModel> {
        return skills
    }
    fun allPurchasedSkills(): List<FullCharacterModifiedSkillModel> {
        return skills.filter { it.isPurchased() }
    }

    fun allNonPurchasedSkills(): List<FullCharacterModifiedSkillModel> {
        return skills.filter { !it.isPurchased() }
    }

    fun attemptToPurchaseSkill(lifecycleScope: LifecycleCoroutineScope, skill: FullCharacterModifiedSkillModel, completion: (successful: Boolean) -> Unit) {
        if (allPurchaseableSkills().firstOrNull { it.id == skill.id } != null) {
            askToPurchase(skill) { cscm ->
                cscm.ifLet({ charSkillCreateModel ->
                    when (characterType()) {
                        CharacterType.STANDARD -> { // Standard Characters
                            val request = CharacterSkillService.TakeCharacterSkill()
                            lifecycleScope.launch {
                                request.successfulResponse(CharacterSkillCreateSP(playerId, charSkillCreateModel)).ifLet({ _ ->
                                    AlertUtils.displayOkMessage(globalGetContext()!!, "Skill Successfully Purchased!", "$fullName now has possesses the skill ${skill.name}.") { _, _ -> }
                                    completion(true)
                                }, {
                                    completion(false)
                                })
                            }
                        }
                        CharacterType.NPC, CharacterType.PLANNER -> { // NPC and Planned Characters
                            val request = CharacterSkillService.TakePlannedCharacterSkill()
                            lifecycleScope.launch {
                                request.successfulResponse(CreateModelSP(charSkillCreateModel)).ifLet({ _ ->
                                    AlertUtils.displayOkMessage(globalGetContext()!!, (characterType() == CharacterType.PLANNER).ternary("Skill Successfully Planned!", "Skill Successfully Added to NPC!"), "$fullName now has possesses the skill ${skill.name}.") { _, _ -> }
                                    completion(true)
                                }, {
                                    completion(false)
                                })
                            }
                        }
                        CharacterType.HIDDEN -> {
                            completion(false)
                        }
                    }
                }, {
                    completion(false)
                })
            }
        } else {
            completion(false)
        }
    }

    private fun askToPurchase(skill: FullCharacterModifiedSkillModel, completion: (char: CharacterSkillCreateModel?) -> Unit) {
        var freeSkillPrompt = ""
        var purchaseTitle = ""
        var purchaseText = ""
        when (characterType()) {
            CharacterType.STANDARD -> {
                freeSkillPrompt = "Use 1 Free Tier-1 Skill?"
                purchaseText = "Purchase ${skill.name}"
                purchaseTitle = "Confirm Purchase?"
            }
            CharacterType.NPC -> {
                freeSkillPrompt = "Use NPC 1 Free Tier-1 Skill?"
                purchaseText = "Purchase ${skill.name} For NPC"
                purchaseTitle = "Confirm NPC Purchase?"
            }
            CharacterType.PLANNER -> {
                freeSkillPrompt = "Plan to use 1 Free Tier-1 Skill?"
                purchaseText = "Plan to purchase ${skill.name}"
                purchaseTitle = "Confirm Planned Purchase?"
            }
            CharacterType.HIDDEN -> {
                completion(null)
                return
            }
        }
        if (skill.canUseFreeSkill()) {
            promptToUseFT1S(freeSkillPrompt) { useFT1S ->
                useFT1S.ifLet({ useFreeSkill ->
                    promptToPurchase(purchaseTitle, purchaseText, useFreeSkill, skill, completion)
                }, {
                    completion(null)
                })
            }
        } else {
            promptToPurchase(purchaseTitle, purchaseText, false, skill, completion)
        }
    }

    private fun promptToUseFT1S(title: String, completion: (useFT1S: Boolean?) -> Unit) {
        AlertUtils.displayChoiceMessage(globalGetContext()!!, title, arrayOf("Use Xp", "Use Free Tier-1 Skill")) { index ->
            if (index == -1) {
                // User hit cancel
                completion(null)
            } else {
                completion(index == 1)
            }

        }
    }

    private fun promptToPurchase(title: String, purchaseText: String, useFreeSkill: Boolean, skill: FullCharacterModifiedSkillModel, completion: (charSkill: CharacterSkillCreateModel?) -> Unit) {
        var message = "$purchaseText using:\n"

        message += if (useFreeSkill) {
            "1 Free Tier-1 Skill point"
        } else {
            "${skill.modXpCost()} Experience Point"
        }

        if (skill.usesPrestige()) {
            message += " and ${skill.prestigeCost()} Prestige Point"
        }
        message += "?"

        AlertUtils.displayOkCancelMessage(globalGetContext()!!, title, message, onClickOk = { _, _ ->
            completion(
                CharacterSkillCreateModel(
                id,
                skill.id,
                useFreeSkill.ternary(0, skill.modXpCost()),
                useFreeSkill.ternary(1, 0),
                skill.prestigeCost()
            ))
        }, onClickCancel = { _, _ ->
            completion(null)
        })
    }

    fun allPurchaseableSkills(): List<FullCharacterModifiedSkillModel> {
        val charSkills = allNonPurchasedSkills()
        val player = DataManager.shared.players.first { it.id == playerId }

        // Remove all skills you don't have prereqs for
        var newSkillList = charSkills.filter { skillToKeep -> hasAllPrereqsForSkill(skillToKeep) }

        // Planned and NPC characters don't require Prestige Points
        if (characterType() != CharacterType.PLANNER && characterType() != CharacterType.NPC) {
            // Filter out pp skills you don't qualify for
            newSkillList = newSkillList.filter { skillToKeep ->
                skillToKeep.prestigeCost() <= player.prestigePoints
            }
        }


        // Remove Choose One Skills that can't be chosen
        val cskills: List<FullCharacterModifiedSkillModel> = getChooseOneSkills()
        if (cskills.isEmpty()) { // Has none
            // Remove all level 2 cskills if the character doesn't have a level 1 cskill.
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allLevel2SpecialistSkills)
            }
        } else if (cskills.count() == 2) { // Has 2
            // Remove all cskills if a character already has taken 2
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(Constants.SpecificSkillIds.allSpecalistSkills)
            }
        } else if (cskills.count() == 1) { // Has 1
            val cskill = cskills.first()
            var idsToRemove: Array<Int> = arrayOf()
            when (cskill.id) {
                Constants.SpecificSkillIds.expertCombat -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertCombat

                Constants.SpecificSkillIds.expertProfession -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertProfession

                Constants.SpecificSkillIds.expertTalent -> idsToRemove =
                    Constants.SpecificSkillIds.allSpecalistsNotUnderExpertTalent
            }
            // Remove cskills not under your expert skill
            newSkillList = newSkillList.filter { skillToKeep ->
                !skillToKeep.id.equalsAnyOf(idsToRemove)
            }
        }

        // Planned and NPC characters don't require xp, free skills, or infection
        if (characterType() != CharacterType.PLANNER && characterType() != CharacterType.NPC) {
            // Filter out skills that you don't have enough xp, fs, or inf for
            newSkillList = newSkillList.filter { skillToKeep ->
                val keep = if (skillToKeep.modInfectionCost() > infection.toInt()) {
                    false
                } else if (skillToKeep.canUseFreeSkill() && player.freeTier1Skills > 0) {
                    true
                } else if (skillToKeep.modXpCost() > player.experience) {
                    false
                } else {
                    false
                }
                keep
            }
        }
        return newSkillList
    }
    fun characterType(): CharacterType {
        return CharacterType.fromId(characterTypeId) ?: CharacterType.STANDARD
    }

    fun hasAllPrereqsForSkill(skill: FullCharacterModifiedSkillModel): Boolean {
        var hasAll = true
        skill.prereqs().forEach { skillModel ->
            if (skills.firstOrNull { it.id == skillModel.id }?.isPurchased() == false) {
                hasAll = false
            }
        }
        return hasAll
    }

    fun getIntrigueSkills(): List<Int> {
        val list = mutableListOf<Int>()
        val filteredSkills = skills.filter { sk ->
            sk.id.equalsAnyOf(Constants.SpecificSkillIds.investigatorTypeSkills)
        }
        filteredSkills.forEach {
            list.add(it.id)
        }
        return list
    }

    fun getChooseOneSkills(): List<FullCharacterModifiedSkillModel> {
        return skills.filter {
            it.id.equalsAnyOf(Constants.SpecificSkillIds.allSpecalistSkills)
        }
    }

    fun costOfCombatSkills(): Int {
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allCombatReducingSkills)) {
                return -1
            }
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allCombatIncreasingSkills)) {
                return 1
            }
        }
        return 0
    }

    fun costOfProfessionSkills(): Int {
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allProfessionReducingSkills)) {
                return -1
            }
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allProfessionIncreasingSkills)) {
                return 1
            }
        }
        return 0
    }

    fun costOfTalentSkills(): Int {
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allTalentReducingSkills)) {
                return -1
            }
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.allTalentIncreasingSkills)) {
                return 1
            }
        }
        return 0
    }

    fun costOf50InfectSkills(): Int {
        skills.forEach {
            if (it.id == Constants.SpecificSkillIds.adaptable) {
                return 25
            }
        }
        return 50
    }

    fun costOf75InfectSkills(): Int {
        skills.forEach {
            if (it.id == Constants.SpecificSkillIds.extremelyAdaptable) {
                return 50
            }
        }
        return 75
    }

    fun hasUnshakableResolve(): Boolean {
        skills.forEach {
            if (it.id == Constants.SpecificSkillIds.unshakableResolve) {
                return true
            }
        }
        return false
    }

    fun mysteriousStrangerCount(): Int {
        var count = 0
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.mysteriousStrangerTypeSkills)) {
                count++
            }
        }
        return count
    }

    fun barcodeModel(): CharacterBarcodeModel {
        return CharacterBarcodeModel(this)
    }

    fun getRelevantBarcodeSkills(): Array<SkillBarcodeModel> {
        val bskills = mutableListOf<SkillBarcodeModel>()
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.barcodeRelevantSkills)) {
                bskills.add(SkillBarcodeModel(it))
            }
        }
        return bskills.toTypedArray()
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("startDate") val startDate: String,
    @JsonProperty("isAlive") var isAlive: String,
    @JsonProperty("deathDate") val deathDate: String,
    @JsonProperty("infection") var infection: String,
    @JsonProperty("bio") var bio: String,
    @JsonProperty("approvedBio") var approvedBio: String,
    @JsonProperty("bullets") var bullets: String,
    @JsonProperty("megas") val megas: String,
    @JsonProperty("rivals") val rivals: String,
    @JsonProperty("rockets") val rockets: String,
    @JsonProperty("bulletCasings") val bulletCasings: String,
    @JsonProperty("clothSupplies") val clothSupplies: String,
    @JsonProperty("woodSupplies") val woodSupplies: String,
    @JsonProperty("metalSupplies") val metalSupplies: String,
    @JsonProperty("techSupplies") val techSupplies: String,
    @JsonProperty("medicalSupplies") val medicalSupplies: String,
    @JsonProperty("armor") val armor: String,
    @JsonProperty("unshakableResolveUses") val unshakableResolveUses: String,
    @JsonProperty("mysteriousStrangerUses") val mysteriousStrangerUses: String,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterTypeId") val characterTypeId: Int
) : Serializable {

    fun getAllXpSpent(lifecycleScope: LifecycleCoroutineScope, callback: (xp: Int) -> Unit) {
        val charSkillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
        lifecycleScope.launch {
            charSkillRequest.successfulResponse(IdSP(id)).ifLet({
                var cost = 0
                it.charSkills.forEach { skl ->
                    cost += skl.xpSpent
                }
                callback(cost)
            }, {
                callback(0)
            })
        }
    }

    fun getAllPrestigePointsSpent(lifecycleScope: LifecycleCoroutineScope, callback: (xp: Int) -> Unit) {
        val charSkillRequest = CharacterSkillService.GetAllCharacterSkillsForCharacter()
        lifecycleScope.launch {
            charSkillRequest.successfulResponse(IdSP(id)).ifLet({
                var cost = 0
                it.charSkills.forEach { skl ->
                    cost += skl.ppSpent
                }
                callback(cost)
            }, {
                callback(0)
            })
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterBarcodeModel(
    val id: Int,
    val fullName: String,
    val infection: String,
    var bullets: String,
    val megas: String,
    val rivals: String,
    val rockets: String,
    val bulletCasings: String,
    val clothSupplies: String,
    val woodSupplies: String,
    val metalSupplies: String,
    val techSupplies: String,
    val medicalSupplies: String,
    val armor: String,
    val unshakableResolveUses: String,
    val mysteriousStrangerUses: String,
    val playerId: Int
) : Serializable {

    constructor(charModel: FullCharacterModel): this(
        charModel.id,
        charModel.fullName,
        charModel.infection,
        charModel.bullets.toString(),
        charModel.megas.toString(),
        charModel.rivals.toString(),
        charModel.rockets.toString(),
        charModel.bulletCasings.toString(),
        charModel.clothSupplies.toString(),
        charModel.woodSupplies.toString(),
        charModel.metalSupplies.toString(),
        charModel.techSupplies.toString(),
        charModel.medicalSupplies.toString(),
        charModel.armor,
        charModel.unshakableResolveUses.toString(),
        charModel.mysteriousStrangerUses.toString(),
        charModel.playerId
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterCreateModel(
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("startDate") val startDate: String,
    @JsonProperty("isAlive") val isAlive: String,
    @JsonProperty("deathDate") val deathDate: String,
    @JsonProperty("infection") val infection: String,
    @JsonProperty("bio") val bio: String,
    @JsonProperty("approvedBio") val approvedBio: String,
    @JsonProperty("bullets") val bullets: String,
    @JsonProperty("megas") val megas: String,
    @JsonProperty("rivals") val rivals: String,
    @JsonProperty("rockets") val rockets: String,
    @JsonProperty("bulletCasings") val bulletCasings: String,
    @JsonProperty("clothSupplies") val clothSupplies: String,
    @JsonProperty("woodSupplies") val woodSupplies: String,
    @JsonProperty("metalSupplies") val metalSupplies: String,
    @JsonProperty("techSupplies") val techSupplies: String,
    @JsonProperty("medicalSupplies") val medicalSupplies: String,
    @JsonProperty("armor") val armor: String,
    @JsonProperty("unshakableResolveUses") val unshakableResolveUses: String,
    @JsonProperty("mysteriousStrangerUses") val mysteriousStrangerUses: String,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterTypeId") val characterTypeId: Int

) : Serializable {

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterSubModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("isAlive") val isAlive: String,
    @JsonProperty("characterTypeId") val characterTypeId: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterListModel(
    @JsonProperty("characters") val characters: Array<CharacterSubModel>
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterListFullModel(
    @JsonProperty("characters") val characters: Array<CharacterModel>
) : Serializable