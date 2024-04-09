package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class IntrigueModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("investigatorMessage") val investigatorMessage: String,
    @JsonProperty("interrogatorMessage") val interrogatorMessage: String,
    @JsonProperty("webOfInformantsMessage") val webOfInformantsMessage: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class IntrigueCreateModel(
    @JsonProperty("eventId") val eventId: Int,
    @JsonProperty("investigatorMessage") val investigatorMessage: String,
    @JsonProperty("interrogatorMessage") val interrogatorMessage: String,
    @JsonProperty("webOfInformantsMessage") val webOfInformantsMessage: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class IntrigueListModel(
    @JsonProperty("intrigues") val intrigues: Array<IntrigueModel>
) : Serializable