package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.ternary
import java.io.Serializable

enum class EventRegType(val value: String) {
    NOT_PREREGED("NONE"),
    FREE("FREE"),
    BASIC("BASIC"),
    PREMIUM("PREMIUM");

    fun getAttendingText(): String {
        return when (this) {
            NOT_PREREGED -> "Not Attending"
            FREE -> "Free"
            BASIC -> "Basic"
            PREMIUM -> "Premium"
        }
    }

    companion object {

        fun getRegType(value: String): EventRegType {
            return EventRegType.values().firstOrNull { it.value == value } ?: EventRegType.NOT_PREREGED
        }

    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventPreregModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") private val characterId: Int?,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("regType") val regType: String
) : Serializable {

    constructor(id: Int, playerId: Int, characterId: Int?, eventId: Int, regType: EventRegType): this(
        id,
        playerId,
        characterId ?: -1,
        eventId,
        regType.value
    )

    fun eventRegType(): EventRegType {
        return EventRegType.getRegType(regType)
    }

    fun getCharId(): Int? {
        return (characterId == -1 || characterId == 2131231423).ternary(null, characterId)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventPreregCreateModel(
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("characterId") private val characterId: Int?,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("regType") val regType: String
) : Serializable {

    constructor(playerId: Int, characterId: Int?, eventId: Int, regType: EventRegType): this(
        playerId,
        characterId ?: -1,
        eventId,
        regType.value
    )

    fun getCharId(): Int? {
        return (characterId == -1).ternary(null, characterId)
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventPreregListModel(
    @JsonProperty("eventPreregs") val eventPreregs: Array<EventPreregModel>
) : Serializable