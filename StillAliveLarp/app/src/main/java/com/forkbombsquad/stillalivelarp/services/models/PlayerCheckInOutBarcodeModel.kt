package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class CheckInOutBarcodeModel(
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") val characterId: Int?,
    @JsonProperty("eventId") val eventId: Int
) : Serializable

// TODO remove all of these LEGACY_ models this once iOS has been updated
@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_PlayerCheckInBarcodeModel(
    @JsonProperty("player") val player: LEGACY_PlayerBarcodeModel,
    @JsonProperty("character") val character: LEGACY_CharacterBarcodeModel?,
    @JsonProperty("event") val event: LEGACY_EventBarcodeModel,
    @JsonProperty("relevantSkills") val relevantSkills: Array<LEGACY_SkillBarcodeModel>,
    @JsonProperty("gear") var gear: GearModel?
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_PlayerCheckOutBarcodeModel(
    @JsonProperty("player") val player: LEGACY_PlayerBarcodeModel,
    @JsonProperty("character") val character: LEGACY_CharacterBarcodeModel?,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("eventAttendeeId") val eventAttendeeId: Int,
    @JsonProperty("relevantSkills") val relevantSkills: Array<LEGACY_SkillBarcodeModel>
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_PlayerBarcodeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("lastCheckIn") val lastCheckIn: String,
    @JsonProperty("numEventsAttended") val numEventsAttended: String,
    @JsonProperty("numNpcEventsAttended") val numNpcEventsAttended: String,
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_CharacterBarcodeModel(
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
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_EventBarcodeModel(
    val id: Int,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isStarted: String,
    val isFinished: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class LEGACY_SkillBarcodeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String
) : Serializable

fun LEGACY_globalGenerateNewBarcodeModelFromOld(old: LEGACY_PlayerCheckInBarcodeModel): CheckInOutBarcodeModel {
    return CheckInOutBarcodeModel(
        playerId = old.player.id,
        characterId = if (old.character?.id != -1) { old.character?.id } else { null },
        eventId = old.event.id
    )
}

fun LEGACY_globalGenerateNewBarcodeModelFromOld(old: LEGACY_PlayerCheckOutBarcodeModel): CheckInOutBarcodeModel {
    return CheckInOutBarcodeModel(
        playerId = old.player.id,
        characterId = if (old.character?.id != -1) { old.character?.id } else { null },
        eventId = old.eventId
    )
}