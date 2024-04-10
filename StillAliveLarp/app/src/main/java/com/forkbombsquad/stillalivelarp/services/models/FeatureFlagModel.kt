package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureFlagModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("activeAndroid") val activeAndroid: String,
    @JsonProperty("activeIos") val activeIos: String
) : Serializable {
    fun isActiveAndroid(): Boolean {
        return activeAndroid.lowercase() == "false"
    }

    fun isActiveIos(): Boolean {
        return activeIos.lowercase() == "false"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureFlagCreateModel(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("activeAndroid") val activeAndroid: String,
    @JsonProperty("activeIos") val activeIos: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureFlagListModel(
    @JsonProperty("results") val featureFlags: Array<FeatureFlagModel>
) : Serializable