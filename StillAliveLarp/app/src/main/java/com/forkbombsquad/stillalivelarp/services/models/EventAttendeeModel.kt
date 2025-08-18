package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventAttendeeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") val characterId: Int?,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("asNpc") val asNpc: String,
    @JsonProperty("npcId") val npcId: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventAttendeeCreateModel(
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") val characterId: Int? = null,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("isCheckedIn") val isCheckedIn: String,
    @JsonProperty("asNpc") val asNpc: String,
    @JsonProperty("npcId") val npcId: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventAttendeeListModel(
    @JsonProperty("eventAttendees") val eventAttendees: Array<EventAttendeeModel>
) : Serializable