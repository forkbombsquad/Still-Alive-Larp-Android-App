package com.forkbombsquad.stillalivelarp.services.models

import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.views.home.HomeFragment
import com.forkbombsquad.stillalivelarp.views.shared.ViewCharacterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.min

@JsonIgnoreProperties(ignoreUnknown = true)
data class FullPlayerModel(
    val id: Int,
    val username: String,
    val fullName: String,
    val startDate: String,
    val experience: Int,
    val freeTier1Skills: Int,
    val prestigePoints: Int,
    val isCheckedIn: Boolean,
    val isCheckedInAsNpc: Boolean,
    val lastCheckIn: String,
    val numEventsAttended: Int,
    val numNpcEventsAttended: Int,
    val isAdmin: Boolean,
    val characters: List<FullCharacterModel>,
    val awards: List<AwardModel>,
    val eventAttendees: List<EventAttendeeModel>,
    val preregs: List<EventPreregModel>,
    val profileImage: ProfileImageModel?
) : Serializable {
    constructor(player: PlayerModel, characters: List<FullCharacterModel>, awards: List<AwardModel>, eventAttendees: List<EventAttendeeModel>, preregs: List<EventPreregModel>, profileImage: ProfileImageModel?): this(
        player.id,
        player.username,
        player.fullName,
        player.startDate,
        player.experience.toInt(),
        player.freeTier1Skills.toInt(),
        player.prestigePoints.toInt(),
        player.isCheckedIn.toBoolean(),
        player.isCheckedInAsNpc.toBoolean(),
        player.lastCheckIn,
        player.numEventsAttended.toInt(),
        player.numNpcEventsAttended.toInt(),
        player.isAdmin.toBoolean(),
        characters,
        awards,
        eventAttendees,
        preregs,
        profileImage
    )

    fun createCharacter(lifecycleScope: CoroutineScope, name: String, bio: String, completion: (newCharacter: CharacterModel?) -> Unit) {
        val request = CharacterService.CreateCharacter()
        lifecycleScope.launch {
            request.successfulResponse(
                CharacterCreateSP(
                    CharacterCreateModel(
                        fullName = name,
                        startDate = LocalDate.now().yyyyMMddFormatted(),
                        isAlive = "TRUE",
                        deathDate = "",
                        infection = "0",
                        bio = bio,
                        approvedBio = "FALSE",
                        bullets = "20",
                        megas = "0",
                        rivals = "0",
                        rockets = "0",
                        bulletCasings = "0",
                        clothSupplies = "0",
                        woodSupplies = "0",
                        metalSupplies = "0",
                        techSupplies = "0",
                        medicalSupplies = "0",
                        armor = CharacterArmor.NONE.text,
                        unshakableResolveUses = "0",
                        mysteriousStrangerUses = "0",
                        playerId = id,
                        characterTypeId = CharacterType.STANDARD.id
                    )
                )
            ).ifLet({
                completion(it)
            }, {
                completion(null)
            })
        }
    }

    fun determineIfMeetsRequirements(reqs: ViewCharacterActivity.SkillRequirements): ViewCharacterActivity.SkillRequirements {
        val unmetRequirements = ViewCharacterActivity.SkillRequirements(0, 0, 0, 0)
        // Zero if there's enough, otherwise value is equal to how much is needed
        unmetRequirements.xp = abs(min(0, experience - reqs.xp))
        unmetRequirements.ft1s = abs(min(0, freeTier1Skills - reqs.ft1s))
        unmetRequirements.pp = abs(min(0, prestigePoints - reqs.pp))
        if (reqs.inf == 0) {
            unmetRequirements.inf = 0
        } else {
            val char = getActiveCharacter()
            if (char != null) {
                unmetRequirements.inf = abs(min(0, char.infection.toInt() - reqs.inf))
            } else {
                unmetRequirements.inf = abs(min(0, reqs.inf))
            }
        }
        return unmetRequirements
    }

    fun getActiveCharacter(): FullCharacterModel? {
        return characters.firstOrNull { it.characterType() == CharacterType.STANDARD && it.isAlive }
    }

    fun getInactiveCharacters(): List<FullCharacterModel> {
        return characters.filter { it.characterType() == CharacterType.STANDARD && !it.isAlive }
    }

    fun getPlannedCharacters(): List<FullCharacterModel> {
        return characters.filter { it.characterType() == CharacterType.PLANNER }
    }

    fun getAwardsSorted(): List<AwardModel> {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return awards.sortedByDescending { LocalDate.parse(it.date, formatter) }
    }

    fun getCheckInBarcodeModel(useChar: Boolean, event: FullEventModel): CheckInOutBarcodeModel {
        val activeChar = getActiveCharacter()
        return if (useChar && activeChar != null) {
            CheckInOutBarcodeModel(
                playerId = id,
                characterId = activeChar.id,
                eventId = event.id
            )
        } else {
            CheckInOutBarcodeModel(
                playerId = id,
                characterId = null,
                eventId = event.id
            )
        }
    }

    fun getCheckOutBarcodeModel(eventAttendee: EventAttendeeModel): CheckInOutBarcodeModel {
        return if (characters.firstOrNull { it.id == eventAttendee.characterId } != null) {
            CheckInOutBarcodeModel(
                playerId = eventAttendee.playerId,
                characterId = eventAttendee.characterId,
                eventId = eventAttendee.eventId
            )
        } else {
            CheckInOutBarcodeModel(
                playerId = eventAttendee.playerId,
                characterId = null,
                eventId = eventAttendee.eventId
            )
        }
    }

    fun baseModel(): PlayerModel {
        return PlayerModel(this)
    }

    fun baseModelWithModifications(xpChange: Int, ft1sChange: Int, ppChange: Int): PlayerModel {
        return PlayerModel(
            this.id,
            this.username,
            this.fullName,
            this.startDate,
            (this.experience + xpChange).toString(),
            (this.freeTier1Skills + ft1sChange).toString(),
            (this.prestigePoints + ppChange).toString(),
            this.isCheckedIn.toString().uppercase(),
            this.isCheckedInAsNpc.toString().uppercase(),
            this.lastCheckIn,
            this.numEventsAttended.toString(),
            this.numNpcEventsAttended.toString(),
            this.isAdmin.toString().uppercase()
        )
    }

    fun getUniqueCharacterNameRec(name: String, incrementalCount: Int? = null): String {
        val fName = "$name${" ${incrementalCount ?: ""}"}".trim()
        return if (characters.firstOrNull { it.fullName == fName } == null) {
            fName
        } else {
            getUniqueCharacterNameRec(name, (incrementalCount == null).ternary(1, (incrementalCount ?: 0) + 1))
        }
    }

}


@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("startDate") val startDate: String,
    @JsonProperty("experience") val experience: String,
    @JsonProperty("freeTier1Skills") val freeTier1Skills: String,
    @JsonProperty("prestigePoints") val prestigePoints: String,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("isCheckedInAsNpc") val isCheckedInAsNpc: String,
    @JsonProperty("lastCheckIn") val lastCheckIn: String,
    @JsonProperty("numEventsAttended") val numEventsAttended: String,
    @JsonProperty("numNpcEventsAttended") val numNpcEventsAttended: String,
    @JsonProperty("isAdmin") val isAdmin: String
) : Serializable {

    constructor(p: FullPlayerModel): this(
        p.id,
        p.username,
        p.fullName,
        p.startDate,
        p.experience.toString(),
        p.freeTier1Skills.toString(),
        p.prestigePoints.toString(),
        p.isCheckedIn.toString().uppercase(),
        p.isCheckedInAsNpc.toString().uppercase(),
        p.lastCheckIn,
        p.numEventsAttended.toString(),
        p.numNpcEventsAttended.toString(),
        p.isAdmin.toString().uppercase()
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerCreateModel(
    @JsonProperty("username") val username: String,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("startDate") val startDate: String,
    @JsonProperty("experience") val experience: String,
    @JsonProperty("freeTier1Skills") val freeTier1Skills: String,
    @JsonProperty("prestigePoints") val prestigePoints: String,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("isCheckedInAsNpc") val isCheckedInAsNpc: String,
    @JsonProperty("lastCheckIn") val lastCheckIn: String,
    @JsonProperty("numEventsAttended") val numEventsAttended: String,
    @JsonProperty("numNpcEventsAttended") val numNpcEventsAttended: String,
    @JsonProperty("isAdmin") val isAdmin: String,
    @JsonProperty("password") val password: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerListModel(
    @JsonProperty("players") val players: Array<PlayerModel>
) : Serializable