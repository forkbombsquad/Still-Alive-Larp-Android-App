package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactRequestModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("emailAddress") val emailAddress: String,
    @JsonProperty("postalCode") val postalCode: String,
    @JsonProperty("message") val message: String,
    @JsonProperty("read") var read: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactRequestCreateModel(
    @JsonProperty("fullName") val fullName: String,
    @JsonProperty("emailAddress") val emailAddress: String,
    @JsonProperty("postalCode") val postalCode: String,
    @JsonProperty("message") val message: String,
    @JsonProperty("read") val read: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactRequestListModel(
    @JsonProperty("contactRequests") val contactRequests: Array<ContactRequestModel>
) : Serializable