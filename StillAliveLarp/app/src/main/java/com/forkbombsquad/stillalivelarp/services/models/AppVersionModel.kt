package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AppVersionModel(
    @JsonProperty("androidVersion") val androidVersion: Int,
    @JsonProperty("iosVersion") val iosVersion: Int,
    @JsonProperty("rulebookVersion") val rulebookVersion: String
)