package com.forkbombsquad.stillalivelarp.services.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract.Data
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable


@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfileImageModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("image") val image: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfileImageCreateModel(
    @JsonProperty("playerId") val playerId: Int,
    @JsonProperty("image") val image: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfileImageListModel(
    @JsonProperty("profileImages") val profileImages: Array<ProfileImageModel>
) : Serializable