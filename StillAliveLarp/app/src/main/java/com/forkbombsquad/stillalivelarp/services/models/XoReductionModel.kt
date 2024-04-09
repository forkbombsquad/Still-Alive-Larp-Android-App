package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class XpReductionModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("skillId") val skillId: Int,
    @JsonProperty("xpReduction") val xpReduction: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class XpReductionListModel(
    @JsonProperty("specialClassXpReductions") val specialClassXpReductions: Array<XpReductionModel>
) : Serializable {
    fun getSkillIds(): IntArray {
        var intArray = IntArray(specialClassXpReductions.size)
        specialClassXpReductions.forEachIndexed { index, element ->
            intArray[index] = element.skillId
        }
        return intArray
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class XpReductionCreateModel(
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("skillId") val skillId: Int,
    @JsonProperty("xpReduction") val xpReduction: String
) : Serializable
