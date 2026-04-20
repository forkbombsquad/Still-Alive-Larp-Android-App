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