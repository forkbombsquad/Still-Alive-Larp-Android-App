package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import java.io.Serializable
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel

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

    // Check if character has the required skill
    fun canMakeWithSkills(purchasedSkills: List<FullCharacterModifiedSkillModel>): Boolean {
        if (skillId == null || skillId == -1) {
            return true // No skill required
        }
        return purchasedSkills.any { it.id == skillId }
    }

    // Check if character has skill AND materials
    fun canMakeNow(
        purchasedSkills: List<FullCharacterModifiedSkillModel>,
        woodSupplies: Int,
        metalSupplies: Int,
        clothSupplies: Int,
        techSupplies: Int,
        medicalSupplies: Int,
        casing: Int
    ): Boolean {
        if (!canMakeWithSkills(purchasedSkills)) {
            return false
        }
        if (wood > woodSupplies || metal > metalSupplies || cloth > clothSupplies ||
            tech > techSupplies || medical > medicalSupplies || casing > casing) {
            return false
        }
        return true
    }

    // Get crafting time display string
    fun getCraftingTimeText(): String {
        return when {
            craftingTime < 0 -> "*see Notes"
            craftingTime < 1 -> "${(craftingTime * 60).toInt()} sec"
            else -> "${craftingTime.toInt()} min"
        }
    }

    // Get base recipe name if this is an alternate
    fun isAlternate(): Boolean {
        return baseRecipeId != null && baseRecipeId != -1
    }

    // Get other recipe items referenced
    fun getOtherRecipeIds(): List<Int> {
        return otherRequiredItemsJsonModel?.otherItemIds?.map { it.id } ?: listOf()
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