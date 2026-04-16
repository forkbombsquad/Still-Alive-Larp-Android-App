package com.forkbombsquad.stillalivelarp.views.shared

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.utils.CraftingRecipeCell
import com.forkbombsquad.stillalivelarp.utils.CraftingRecipeFilterType
import com.forkbombsquad.stillalivelarp.utils.CraftingRecipeSortType
import com.forkbombsquad.stillalivelarp.utils.DropdownSpinner
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ternary

class CraftingRecipesListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var filterSortLayout: LinearLayout
    private lateinit var filterDropdown: DropdownSpinner
    private lateinit var sortDropdown: DropdownSpinner
    private lateinit var recipesLayout: LinearLayout

    private lateinit var loadingLayout: LoadingLayout

    private var activeCharacter: FullCharacterModel? = null
    private var allRecipes: List<CraftingRecipeModel> = listOf()

    private var currentFilter: CraftingRecipeFilterType = CraftingRecipeFilterType.NONE
    private var currentSort: CraftingRecipeSortType = CraftingRecipeSortType.AZ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crafting_recipes_list)
        setupView()
    }

    private fun setupView() {
        // Get active character (player's current character)
        activeCharacter = DataManager.shared.getActiveCharacter()

        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.craftingrecipes_title)
        searchBar = findViewById(R.id.craftingrecipes_searchview)
        filterSortLayout = findViewById(R.id.craftingrecipes_filter_sort_layout)
        recipesLayout = findViewById(R.id.craftingrecipes_layout)

        // Create dropdowns programmatically
        filterDropdown = DropdownSpinner(this)
        filterDropdown.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        filterSortLayout.addView(filterDropdown)

        sortDropdown = DropdownSpinner(this)
        sortDropdown.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        filterSortLayout.addView(sortDropdown)

        // Setup filter dropdown
        filterDropdown.setup(this, "Filter:", CraftingRecipeFilterType.getAllStrings()) {
            currentFilter = CraftingRecipeFilterType.getTypeForString(filterDropdown.getSelectedItem())
            buildView()
        }

        // Setup sort dropdown
        sortDropdown.setup(this, "Sort:", CraftingRecipeSortType.getAllStrings()) {
            currentSort = CraftingRecipeSortType.getTypeForString(sortDropdown.getSelectedItem())
            buildView()
        }

        searchBar.addTextChangedListener {
            buildView()
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            activeCharacter = DataManager.shared.getActiveCharacter()
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        // Get all recipes
        allRecipes = DataManager.shared.craftingRecipes

        // Filter by active character if any
        if (activeCharacter != null) {
            filterDropdown.isGone = false
        } else {
            currentFilter = CraftingRecipeFilterType.NONE
            filterDropdown.isGone = true
        }

        // Apply search filter
        val searchText = searchBar.text.toString().trim().lowercase()
        var filtered = if (searchText.isNotEmpty()) {
            allRecipes.filter { recipe ->
                recipe.name.lowercase().contains(searchText) ||
                recipe.category.lowercase().contains(searchText) ||
                (recipe.desc ?: "").lowercase().contains(searchText)
            }
        } else {
            allRecipes
        }

        // Apply filter based on character
        if (activeCharacter != null) {
            val purchasedSkills = activeCharacter!!.allPurchasedSkills()
            val char = activeCharacter!!

            filtered = when (currentFilter) {
                CraftingRecipeFilterType.CAN_POTENTIALLY_MAKE -> {
                    filtered.filter { it.canMakeWithSkills(purchasedSkills) }
                }
                CraftingRecipeFilterType.CAN_MAKE_NOW -> {
                    filtered.filter { recipe ->
                        recipe.canMakeNow(
                            purchasedSkills,
                            char.woodSupplies,
                            char.metalSupplies,
                            char.clothSupplies,
                            char.techSupplies,
                            char.medicalSupplies,
                            char.bulletCasings
                        )
                    }
                }
                else -> filtered
            }
        }

        // Build skill name lookup
        val skills = DataManager.shared.skills
        val skillNameMap = skills.associate { it.id to it.name }
        val recipeNameMap = allRecipes.associate { it.id to it.name }

        // Sort recipes
        val sorted = when (currentSort) {
            CraftingRecipeSortType.AZ -> filtered.sortedBy { it.name }
            CraftingRecipeSortType.ZA -> filtered.sortedByDescending { it.name }
            CraftingRecipeSortType.CATEGORY_ASC -> filtered.sortedBy { it.category }
            CraftingRecipeSortType.CATEGORY_DESC -> filtered.sortedByDescending { it.category }
            CraftingRecipeSortType.SKILL_ASC -> filtered.sortedBy { skillNameMap[it.skillId] ?: "Unknown" }
            CraftingRecipeSortType.SKILL_DESC -> filtered.sortedByDescending { skillNameMap[it.skillId] ?: "Unknown" }
            CraftingRecipeSortType.TIME_ASC -> filtered.sortedBy { it.craftingTime }
            CraftingRecipeSortType.TIME_DESC -> filtered.sortedByDescending { it.craftingTime }
        }

        // Set title
        val titleText = if (activeCharacter != null) {
            "${activeCharacter!!.fullName}'s Crafting Recipes"
        } else {
            "All Crafting Recipes"
        }
        DataManager.shared.setTitleTextPotentiallyOffline(title, titleText)

        // Build recipe cells
        recipesLayout.removeAllViews()
        sorted.forEachIndexed { index, recipe ->
            val cell = CraftingRecipeCell(this)
            cell.setup(recipe, skillNameMap[recipe.skillId], recipeNameMap)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            recipesLayout.addView(cell)
        }
    }
}