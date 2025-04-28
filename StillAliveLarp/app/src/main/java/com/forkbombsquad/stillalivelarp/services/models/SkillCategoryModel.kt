package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillCategoryModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class SkillCategoryListModel(
    @JsonProperty("results") val skillCategories: Array<SkillCategoryModel>
) : Serializable