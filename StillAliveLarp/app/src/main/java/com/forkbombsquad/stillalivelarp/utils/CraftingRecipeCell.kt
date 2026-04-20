package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FullCraftingRecipeModel
import com.google.android.material.divider.MaterialDivider

class CraftingRecipeCell(context: Context): LinearLayout(context) {

    val nameText: TextView
    val alternateText: TextView
    val producesText: TextView
    val timeText: TextView
    val skillText: TextView
    val matCol1: TextView
    val matCol2: TextView
    val matCol3: TextView
    val descText: TextView
    val descHeader: TextView
    val descDivider: MaterialDivider

    init {
        inflate(context, R.layout.craftingrecipecell, this)

        nameText = findViewById(R.id.craftingrecipecell_name)
        alternateText = findViewById(R.id.craftingrecipecell_alternate)
        producesText = findViewById(R.id.craftingrecipecell_produces)
        timeText = findViewById(R.id.craftingrecipecell_time)
        skillText = findViewById(R.id.craftingrecipecell_skill)
        matCol1 = findViewById(R.id.craftingrecipecell_mat_col1)
        matCol2 = findViewById(R.id.craftingrecipecell_mat_col2)
        matCol3 = findViewById(R.id.craftingrecipecell_mat_col3)
        descText = findViewById(R.id.craftingrecipecell_desc)
        descHeader = findViewById(R.id.craftingrecipecell_desc_header)
        descDivider = findViewById(R.id.craftingrecipecell_desc_divider)
    }

    fun setup(recipe: FullCraftingRecipeModel) {
        nameText.text = recipe.getDisplayName()

        // Alternate subtitle
        alternateText.isVisible = recipe.isAlternate()

        // Produces
        producesText.text = "x${recipe.craftingRecipe.numProduced}"

        // Time
        timeText.text = recipe.getCraftingTimeText()

        // Skill - with type-based coloring
        skillText.text = recipe.requiredSkill?.name ?: "None"
        if (recipe.requiredSkill != null) {
            skillText.setTextColor(
                when (recipe.requiredSkill.skillTypeId) {
                    Constants.SkillTypes.combat -> context.getColor(R.color.bright_red)
                    Constants.SkillTypes.profession -> context.getColor(R.color.green)
                    Constants.SkillTypes.talent -> context.getColor(R.color.blue)
                    else -> context.getColor(R.color.black)
                }
            )
        } else {
            skillText.setTextColor(context.getColor(R.color.black))
        }

        // Materials - distribute across 3 columns with centering
        val matList = recipe.craftingRecipe.getMaterialsList()
        if (matList.isEmpty()) {
            matCol1.text = "*"
            matCol2.text = ""
            matCol3.text = ""
            matCol1.isVisible = true
            matCol2.isVisible = false
            matCol3.isVisible = false
        } else {
            // Resolve recipe references with actual names from full model
            val resolvedMats = matList.map { mat ->
                if (mat.isRecipeReference && mat.recipeId != null) {
                    val fullRef = recipe.otherRecipeReferences.find { it.craftingRecipe.id == mat.recipeId }
                    if (fullRef != null) {
                        mat.copy(name = fullRef.getDisplayName())
                    } else {
                        mat
                    }
                } else {
                    mat
                }
            }

            val col1 = mutableListOf<CharSequence>()
            val col2 = mutableListOf<CharSequence>()
            val col3 = mutableListOf<CharSequence>()

            var column = 2
            resolvedMats.forEach { mat ->
                val matText = "${mat.quantity} ${mat.name}"
                val displayText = if (mat.isRecipeReference) {
                    makeBold(matText)
                } else {
                    matText
                }
                when (column) {
                    1 -> col1.add(displayText)
                    2 -> col2.add(displayText)
                    3 -> col3.add(displayText)
                }
                column -= 1
                if (column == 0) {
                    column = 3
                }
            }

            // Hide empty columns
            matCol1.isVisible = col1.isNotEmpty()
            matCol2.isVisible = col2.isNotEmpty()
            matCol3.isVisible = col3.isNotEmpty()

            // Set text preserving formatting
            matCol1.setText(buildSpannableText(col1), TextView.BufferType.SPANNABLE)
            matCol2.setText(buildSpannableText(col2), TextView.BufferType.SPANNABLE)
            matCol3.setText(buildSpannableText(col3), TextView.BufferType.SPANNABLE)
        }

        // Description with header and divider
        val desc = recipe.desc
        val hasDesc = !desc.isNullOrBlank()
        descHeader.isVisible = hasDesc
        descDivider.isVisible = hasDesc
        descText.isVisible = hasDesc
        descText.text = desc ?: ""
    }

    // Helper to make text bold using Spannable
    private fun makeBold(text: String): CharSequence {
        val spannable = SpannableStringBuilder(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    // Build spannable text from list, joining with newlines
    private fun buildSpannableText(items: List<CharSequence>): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        items.forEachIndexed { index, item ->
            result.append(item)
            if (index < items.size - 1) {
                result.append("\n\n")
            }
        }
        return result
    }
}
