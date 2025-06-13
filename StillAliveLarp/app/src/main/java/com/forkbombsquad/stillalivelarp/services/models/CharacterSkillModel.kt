package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterSkillModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("skillId") val skillId: Int,
    @JsonProperty("xpSpent") val xpSpent: Int,
    @JsonProperty("fsSpent") val fsSpent: Int,
    @JsonProperty("ppSpent") val ppSpent: Int,
    @JsonProperty("date") val date: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterSkillListModel(
    @JsonProperty("charSkills") val charSkills: Array<CharacterSkillModel>
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CharacterSkillCreateModel(
    @JsonProperty("characterId") val characterId: Int,
    @JsonProperty("skillId") val skillId: Int,
    @JsonProperty("xpSpent") val xpSpent: Int,
    @JsonProperty("fsSpent") val fsSpent: Int,
    @JsonProperty("ppSpent") val ppSpent: Int
) : Serializable