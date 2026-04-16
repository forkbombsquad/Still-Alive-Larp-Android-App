package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeModel

class CraftingRecipeCell(context: Context): LinearLayout(context) {

    val nameText: TextView
    val categoryText: TextView
    val producesText: TextView
    val materialsText: TextView
    val timeText: TextView
    val skillText: TextView
    val descText: TextView

    init {
        inflate(context, R.layout.craftingrecipecell, this)

        nameText = findViewById(R.id.craftingrecipecell_name)
        categoryText = findViewById(R.id.craftingrecipecell_category)
        producesText = findViewById(R.id.craftingrecipecell_produces)
        materialsText = findViewById(R.id.craftingrecipecell_materials)
        timeText = findViewById(R.id.craftingrecipecell_time)
        skillText = findViewById(R.id.craftingrecipecell_skill)
        descText = findViewById(R.id.craftingrecipecell_desc)
    }

    fun setup(recipe: CraftingRecipeModel, skillName: String?, allRecipeNames: Map<Int, String>) {
        // Name with alternate indicator
        if (recipe.isAlternate()) {
            val baseName = allRecipeNames[recipe.baseRecipeId] ?: "Unknown"
            nameText.text = "${recipe.name} (Alternate of $baseName)"
            nameText.setTypeface(null, Typeface.ITALIC)
        } else {
            nameText.text = recipe.name
            nameText.setTypeface(null, Typeface.BOLD)
        }

        // Category
        categoryText.text = "[${recipe.category}]"

        // Produces
        producesText.text = "Makes x${recipe.numProduced}"

        // Materials
        val matList = mutableListOf<String>()
        if (recipe.wood > 0) matList.add("${recipe.wood} Wood")
        if (recipe.metal > 0) matList.add("${recipe.metal} Metal")
        if (recipe.cloth > 0) matList.add("${recipe.cloth} Cloth")
        if (recipe.tech > 0) matList.add("${recipe.tech} Tech")
        if (recipe.medical > 0) matList.add("${recipe.medical} Medical")
        if (recipe.casing > 0) matList.add("${recipe.casing} Casings")

        // Other recipe items
        val otherItems = recipe.otherRequiredItemsJsonModel?.otherItemIds
        if (otherItems != null) {
            for (item in otherItems) {
                val itemName = allRecipeNames[item.id] ?: "Recipe ${item.id}"
                matList.add("${item.num} $itemName")
            }
        }

        // Foods
        val foods = recipe.otherRequiredItemsJsonModel?.foods
        if (foods != null) {
            when (foods) {
                is List<*> -> {
                    for (foodItem in foods) {
                        matList.add(foodItem.toString())
                    }
                }
                is Map<*, *> -> {
                    for ((foodName, foodNum) in foods) {
                        matList.add("$foodNum $foodName")
                    }
                }
            }
        }

        materialsText.text = if (matList.isNotEmpty()) matList.joinToString(", ") else "*"

        // Time
        timeText.text = "Time: ${recipe.getCraftingTimeText()}"

        // Skill
        skillText.text = "Requires: ${skillName ?: "No Skill"}"

        // Description
        val desc = recipe.desc
        descText.isVisible = !desc.isNullOrBlank()
        descText.text = desc ?: ""
    }
}