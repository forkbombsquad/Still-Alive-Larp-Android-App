package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.containsIgnoreCase
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

    data class MaterialItem(
        val quantity: Int,
        val name: String,
        val recipeId: Int? = null,
        val isRecipeReference: Boolean = false,
        val isFood: Boolean = false
    )

    fun getMaterialsList(): List<MaterialItem> {
        val matList = mutableListOf<MaterialItem>()
        if (wood > 0) matList.add(MaterialItem(wood, "Wood"))
        if (metal > 0) matList.add(MaterialItem(metal, "Metal"))
        if (cloth > 0) matList.add(MaterialItem(cloth, "Cloth"))
        if (tech > 0) matList.add(MaterialItem(tech, "Tech Supplies"))
        if (medical > 0) matList.add(MaterialItem(medical, "Medical Supplies"))
        if (casing > 0) matList.add(MaterialItem(casing, "Casings"))

        // Other recipe items (these should be bolded)
        val otherItems = otherRequiredItemsJsonModel?.otherItemIds
        if (otherItems != null) {
            for (item in otherItems) {
                matList.add(MaterialItem(item.num, "Recipe ${item.id}", recipeId = item.id, isRecipeReference = true))
            }
        }

        // Foods - each food item should be listed separately
        val foods = otherRequiredItemsJsonModel?.getFoodMaterials() ?: listOf()
        foods.forEach {
            matList.add(it)
        }

        return matList
    }

    fun isAlternate(): Boolean {
        return baseRecipeId != null && baseRecipeId != -1
    }

    fun getOtherRecipeIds(): List<Int> {
        return otherRequiredItemsJsonModel?.otherItemIds?.map { it.id } ?: listOf()
    }

}

data class FullCraftingRecipeModel(
    val craftingRecipe: CraftingRecipeModel,
    val requiredSkill: FullSkillModel?,
    private val baseRecipe: FullCraftingRecipeModel?,
    val otherRecipeReferences: List<FullCraftingRecipeModel>
) : Serializable {

    val category = craftingRecipe.category
    val id = craftingRecipe.id
    private val name = craftingRecipe.name
    val desc = craftingRecipe.desc
    private val craftingTime = craftingRecipe.craftingTime
    private val baseRecipeId = baseRecipe?.id

    fun getDisplayName(): String {
        return if (isAlternate() && baseRecipe != null) {
            "${baseRecipe.name} ($name)"
        } else {
            name
        }
    }

    fun getCraftingTimeText(): String {
        return when {
            craftingTime < 0 -> "*see Notes"
            craftingTime < 1 -> "${(craftingTime * 60).toInt()} sec"
            else -> "${craftingTime.toInt()} min"
        }
    }

    fun isAlternate(): Boolean {
        return baseRecipeId != null && baseRecipeId != -1
    }

    fun containedInSearch(searchText: String): Boolean {
        return if (getDisplayName().containsIgnoreCase(searchText)) {
            true
        } else if (category.containsIgnoreCase(searchText)) {
            true
        } else if ((desc ?: "").containsIgnoreCase(searchText)) {
            true
        } else {
            false
        }
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
    @JsonProperty("foods") val foods: List<Map<String, Int>>
) : Serializable {
    fun getFoodMaterials(): List<CraftingRecipeModel.MaterialItem> {
        val items: MutableList<CraftingRecipeModel.MaterialItem> = mutableListOf()
        foods.forEach { map ->
            map.forEach {
                items.add(CraftingRecipeModel.MaterialItem(it.value, it.key, isFood = true))
            }
        }
        return items
    }
}