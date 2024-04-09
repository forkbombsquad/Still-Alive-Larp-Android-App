package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import java.io.Serializable
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("type") val type: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearCreateModel(
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("type") val type: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class GearListModel(
    @JsonProperty("charGear") val charGear: Array<GearModel>
) : Serializable

fun Array<GearModel>.primaryWeapon(): GearModel? {
    for (gear in this) {
        if (gear.type == Constants.Gear.primaryWeapon) {
            return gear
        }
    }
    return null
}

fun Array<GearModel>.removingPrimaryWeapon(): Array<GearModel> {
    var list = mutableListOf<GearModel>()
    for (gear in this) {
        if (gear.type != Constants.Gear.primaryWeapon) {
            list.add(gear)
        }
    }
    return list.toTypedArray()
}