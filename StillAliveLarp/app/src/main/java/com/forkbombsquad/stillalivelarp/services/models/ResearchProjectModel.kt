package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("milestones") val milestones: Int,
    @JsonProperty("complete") val complete: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectCreateModel(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("milestones") val milestones: Int,
    @JsonProperty("complete") val complete: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectListModel(
    @JsonProperty("researchProjects") val researchProjects: Array<ResearchProjectModel>
) : Serializable