package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("milestones") val milestones: Int,
    @JsonProperty("complete") val complete: String,
    @JsonProperty("milestoneDescs") val milestoneDescs: String
) : Serializable {

    val milestoneJsonModels: List<ResearchProjectMilestoneJsonModel>?
        get() {
            return globalFromJson<ResearchProjectMilestoneJsonListModel>(milestoneDescs)?.milestoneDescs?.toList()
        }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectCreateModel(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("milestones") val milestones: Int,
    @JsonProperty("complete") val complete: String,
    @JsonProperty("milestoneDescs") val milestoneDescs: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResearchProjectListModel(
    @JsonProperty("researchProjects") val researchProjects: Array<ResearchProjectModel>
) : Serializable

data class ResearchProjectMilestoneJsonModel(
    @JsonProperty("id") val id: String,
    @JsonProperty("text") val text: String
) : Serializable

data class ResearchProjectMilestoneJsonListModel(@JsonProperty("milestoneDescs") val milestoneDescs: Array<ResearchProjectMilestoneJsonModel>
) : Serializable