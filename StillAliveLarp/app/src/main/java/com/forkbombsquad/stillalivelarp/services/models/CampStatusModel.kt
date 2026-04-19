package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampStatusModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("campFortificationJson") val campFortificationJson: String,
    @JsonProperty("npcSlots") val npcSlots: Int,
    @JsonProperty("medicalCots") val medicalCots: Int,
    @JsonProperty("teachingChairs") val teachingChairs: Int,
    @JsonProperty("otherJson") val otherJson: String = "{}"
) : Serializable {

    val campFortifications: List<CampFortification>
        get() {
            return globalFromJson<CampFortifications>(campFortificationJson)?.campFortifications ?: listOf()
        }

    companion object {
        fun initWithCampFortifications(oldModel: CampStatusModel, campFortifications: List<CampFortification>): CampStatusModel {
            val cf = CampFortifications(campFortifications)
            return CampStatusModel(oldModel.id, globalToJson(cf), oldModel.npcSlots, oldModel.medicalCots, oldModel.teachingChairs, oldModel.otherJson)
        }
    }
}

data class CampFortification(
    @JsonProperty("ring") var ring: Int,
    @JsonProperty("fortifications") var fortifications: List<Fortification>
) : Serializable

data class Fortification(
    @JsonProperty("type") var type: String,
    @JsonProperty("health") var health: Int
) : Serializable {

    constructor(type: FortificationType, health: Int): this(type.text, health)

    val fortificationType: FortificationType
        get() {
            return FortificationType.getFortificationType(type)
        }

    enum class FortificationType(val text: String) {
        LIGHT("LIGHT"),
        MEDIUM("MEDIUM"),
        HEAVY("HEAVY"),
        ADVANCED("ADVANCED"),
        MILITARY_GRADE("MILITARY GRADE");

        fun getMaxHealth(): Int {
            return when (this) {
                LIGHT -> 5
                MEDIUM -> 10
                HEAVY -> 15
                ADVANCED -> 20
                MILITARY_GRADE -> 30
            }
        }

        companion object {

            fun getFortificationType(value: String): FortificationType {
                return FortificationType.values().firstOrNull { it.text == value } ?: LIGHT
            }

        }
    }

}

data class CampFortifications(@JsonProperty("campFortifications") val campFortifications: List<CampFortification>
) : Serializable