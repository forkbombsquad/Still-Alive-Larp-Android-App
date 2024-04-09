package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillPrereqModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("baseSkillId") val baseSkillId: Int,
    @JsonProperty("prereqSkillId") val prereqSkillId: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillPrereqListModel(
    @JsonProperty("skillPrereqs") val skillPrereqs: Array<SkillPrereqModel>
) : Serializable