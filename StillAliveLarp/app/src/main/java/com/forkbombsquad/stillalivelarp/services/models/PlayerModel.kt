package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

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