package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CraftingRecipeModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("baseRecipeId") val baseRecipeId: Int?,
    @JsonProperty("skillId") val skillId: Int?,
    @JsonProperty("numProduced") val numProduced: Int,
    @JsonProperty("category") val category: String,
    @JsonProperty("craftingTime") val craftingTime: Double,
    @JsonProperty("wood") val wood: Int,
    @JsonProperty("metal") val metal: Int,
    @JsonProperty("cloth") val cloth: Int,
    @JsonProperty("tech") val tech: Int,
    @JsonProperty("medical") val medical: Int,
    @JsonProperty("casing") val casing: Int,
    @JsonProperty("otherRequiredItemIds") val otherRequiredItemIds: String?,
    @JsonProperty("desc") val desc: String?
) : Serializable {

    val otherRequiredItemsJsonModel: CraftingRecipeOtherRequiredItemsJsonModel?
        get() {
            return globalFromJson<CraftingRecipeOtherRequiredItemsJsonModel>(otherRequiredItemIds ?: "")
        }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CraftingRecipeListModel(
    @JsonProperty("craftingRecipes") val craftingRecipes: Array<CraftingRecipeModel>
) : Serializable

data class CraftingRecipeOtherRequiredItemJsonModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("num") val num: Int
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class CraftingRecipeOtherRequiredItemsJsonModel(
    @JsonProperty("otherItemIds") val otherItemIds: Array<CraftingRecipeOtherRequiredItemJsonModel>?,
    @JsonProperty("foods") val foods: Any?
) : Serializable