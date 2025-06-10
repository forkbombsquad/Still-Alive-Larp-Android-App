package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.ternary
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        player.numEventsAttended.toInt(),
        player.isAdmin.toBoolean(),
        characters,
        awards,
        eventAttendees,
        preregs,
        profileImage
    )

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

    fun getCheckInBarcodeModel(useChar: Boolean, event: FullEventModel): PlayerCheckInBarcodeModel {
        val activeChar = getActiveCharacter()
        return if (useChar && activeChar != null) {
            PlayerCheckInBarcodeModel(
                player = barcodeModel(),
                character = activeChar.barcodeModel(),
                event = event.barcodeModel(),
                relevantSkills = activeChar.getRelevantBarcodeSkills(),
                gear = activeChar.gear
            )
        } else {
            PlayerCheckInBarcodeModel(
                player = barcodeModel(),
                character = null,
                event = event.barcodeModel(),
                relevantSkills = arrayOf(),
                gear = null
            )
        }
    }

    fun getCheckOutBarcodeModel(eventAttendee: EventAttendeeModel): PlayerCheckOutBarcodeModel {
        val char = characters.firstOrNull { it.id == eventAttendee.characterId }
        return if (char != null) {
            PlayerCheckOutBarcodeModel(
                player = barcodeModel(),
                character = char.barcodeModel(),
                eventId = eventAttendee.eventId,
                eventAttendeeId = eventAttendee.id,
                relevantSkills = char.getRelevantBarcodeSkills()
            )
        } else {
            PlayerCheckOutBarcodeModel(
                player = barcodeModel(),
                character = null,
                eventId = eventAttendee.eventId,
                eventAttendeeId = eventAttendee.id,
                relevantSkills = arrayOf()
            )
        }
    }

    private fun barcodeModel(): PlayerBarcodeModel {
        return PlayerBarcodeModel(
            id = id,
            fullName = fullName,
            isCheckedIn = isCheckedIn.toString(),
            lastCheckIn = lastCheckIn,
            numEventsAttended = numEventsAttended.toString(),
            numNpcEventsAttended = numNpcEventsAttended.toString()
        )
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
        val fName = "$name${(incrementalCount == null).ternary("", " ${incrementalCount!!}")}"
        return if (characters.firstOrNull { it.fullName == fName } == null) {
            fName
        } else {
            getUniqueCharacterNameRec(name, (incrementalCount == null).ternary(1, incrementalCount!! + 1))
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

    fun getBarcodeModel(): PlayerBarcodeModel {
        return PlayerBarcodeModel(this)
    }
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerBarcodeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("lastCheckIn") val lastCheckIn: String,
    @JsonProperty("numEventsAttended") val numEventsAttended: String,
    @JsonProperty("numNpcEventsAttended") val numNpcEventsAttended: String,
) : Serializable {
    constructor(playerModel: PlayerModel): this(
        playerModel.id,
        playerModel.fullName,
        playerModel.isCheckedIn,
        playerModel.lastCheckIn,
        playerModel.numEventsAttended,
        playerModel.numNpcEventsAttended
    )
}