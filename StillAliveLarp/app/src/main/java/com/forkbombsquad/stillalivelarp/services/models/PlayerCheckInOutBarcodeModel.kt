package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerCheckInBarcodeModel(
    @JsonProperty("player") val player: PlayerBarcodeModel,
    @JsonProperty("character") val character: CharacterBarcodeModel?,
    @JsonProperty("event") val event: EventBarcodeModel,
    @JsonProperty("relevantSkills") val relevantSkills: Array<SkillBarcodeModel>,
    @JsonProperty("primaryWeapon") val primaryWeapon: GearModel?
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlayerCheckOutBarcodeModel(
    @JsonProperty("player") val player: PlayerBarcodeModel,
    @JsonProperty("character") val character: CharacterBarcodeModel?,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("eventAttendeeId") val eventAttendeeId: Int,
    @JsonProperty("relevantSkills") val relevantSkills: Array<SkillBarcodeModel>
) : Serializable