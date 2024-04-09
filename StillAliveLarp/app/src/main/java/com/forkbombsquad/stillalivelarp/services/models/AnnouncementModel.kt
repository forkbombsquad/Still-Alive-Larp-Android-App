package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnouncementModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("text") val text: String,
    @JsonProperty("date") val date: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnouncementCreateModel(
    @JsonProperty("title") val title: String,
    @JsonProperty("text") val text: String,
    @JsonProperty("date") val date: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnouncementSubModel(
    @JsonProperty("id") val id: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnouncementListModel(
    @JsonProperty("announcements") val announcements: Array<AnnouncementSubModel>
) : Serializable
