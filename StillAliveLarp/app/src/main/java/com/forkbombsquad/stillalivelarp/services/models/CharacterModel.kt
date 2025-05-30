package com.forkbombsquad.stillalivelarp.services.models

import androidx.lifecycle.LifecycleCoroutineScope
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.Serializable

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

    var skills: List<FullCharacterModifiedSkillModel> = listOf()
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
        charSkills.forEach { charSkill ->
            allSkills.firstOrNull { it.id == charSkill.skillId }.ifLet {  baseFullSkill ->
                val xpRed = xpReductions.firstOrNull { it.skillId == baseFullSkill.id }
                fcmSkills.add(FullCharacterModifiedSkillModel(
                    skill = baseFullSkill,
                    charSkillModel = charSkills.firstOrNull { it.skillId == baseFullSkill.id },
                    xpReduction =  xpRed,
                    costOfCombatSkills(),
                    costOfProfessionSkills(),
                    costOfTalentSkills(),
                    costOf50InfectSkills(),
                    costOf75InfectSkills()
                ))
            }
        }
        this.skills = fcmSkills
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
data class OldFullCharacterModel(
    val id: Int,
    val fullName: String,
    val startDate: String,
    val isAlive: String,
    val deathDate: String,
    val infection: String,
    var bio: String,
    val approvedBio: String,
    val bullets: String,
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
    val playerId: Int,
    val characterTypeId: Int,
    var skills: Array<OldFullSkillModel>
) : Serializable {
    constructor(charModel: CharacterModel): this(
        charModel.id,
        charModel.fullName,
        charModel.startDate,
        charModel.isAlive,
        charModel.deathDate,
        charModel.infection,
        charModel.bio,
        charModel.approvedBio,
        charModel.bullets,
        charModel.megas,
        charModel.rivals,
        charModel.rockets,
        charModel.bulletCasings,
        charModel.clothSupplies,
        charModel.woodSupplies,
        charModel.metalSupplies,
        charModel.techSupplies,
        charModel.medicalSupplies,
        charModel.armor,
        charModel.unshakableResolveUses,
        charModel.mysteriousStrangerUses,
        charModel.playerId,
        charModel.characterTypeId,
        arrayOf()
    )

    fun getBaseModel(): CharacterModel {
        return CharacterModel(this)
    }

    fun getBarcodeModel(): CharacterBarcodeModel {
        return CharacterBarcodeModel(this)
    }

    fun getIntrigueSkills(): IntArray {
        val list = mutableListOf<Int>()
        val filteredSkills = skills.filter { sk ->
            sk.id.equalsAnyOf(Constants.SpecificSkillIds.investigatorTypeSkills)
        }
        filteredSkills.forEach {
            list.add(it.id)
        }
        return list.toIntArray()
    }

    fun getChooseOneSkills(): Array<OldFullSkillModel> {
        return skills.filter {
            it.id.equalsAnyOf(Constants.SpecificSkillIds.allSpecalistSkills)
        }.toTypedArray()
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

    fun getRelevantBarcodeSkills(): Array<SkillBarcodeModel> {
        val bskills = mutableListOf<SkillBarcodeModel>()
        skills.forEach {
            if (it.id.equalsAnyOf(Constants.SpecificSkillIds.barcodeRelevantSkills)) {
                bskills.add(SkillBarcodeModel(it))
            }
        }
        return bskills.toTypedArray()
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

    constructor(charModel: OldFullCharacterModel): this(
        charModel.id,
        charModel.fullName,
        charModel.startDate,
        charModel.isAlive,
        charModel.deathDate,
        charModel.infection,
        charModel.bio,
        charModel.approvedBio,
        charModel.bullets,
        charModel.megas,
        charModel.rivals,
        charModel.rockets,
        charModel.bulletCasings,
        charModel.clothSupplies,
        charModel.woodSupplies,
        charModel.metalSupplies,
        charModel.techSupplies,
        charModel.medicalSupplies,
        charModel.armor,
        charModel.unshakableResolveUses,
        charModel.mysteriousStrangerUses,
        charModel.playerId,
        charModel.characterTypeId
    )

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
    constructor(charModel: OldFullCharacterModel): this(
        charModel.id,
        charModel.fullName,
        charModel.infection,
        charModel.bullets,
        charModel.megas,
        charModel.rivals,
        charModel.rockets,
        charModel.bulletCasings,
        charModel.clothSupplies,
        charModel.woodSupplies,
        charModel.metalSupplies,
        charModel.techSupplies,
        charModel.medicalSupplies,
        charModel.armor,
        charModel.unshakableResolveUses,
        charModel.mysteriousStrangerUses,
        charModel.playerId
    )

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