package com.forkbombsquad.stillalivelarp.views.shared

import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCraftingRecipeModel
import com.forkbombsquad.stillalivelarp.utils.CraftingRecipeCell
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import kotlin.reflect.KClass

class CraftingRecipesListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var hasSkillCheckbox: CheckBox
    private lateinit var hasMaterialsCheckbox: CheckBox
    private lateinit var recipesLayout: LinearLayout

    private lateinit var loadingLayout: LoadingLayout

    private var allRecipes: List<FullCraftingRecipeModel> = listOf()
    private lateinit var categoryName: String

    private val sourceClasses: List<KClass<*>> = listOf(
        CraftingRecipeCategoriesActivity::class,
        com.forkbombsquad.stillalivelarp.views.rules.RulesFragment::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crafting_recipes_list)
        setupView()
    }

    private fun setupView() {
        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.craftingrecipes_title)
        searchBar = findViewById(R.id.craftingrecipes_searchview)
        hasSkillCheckbox = findViewById(R.id.craftingrecipes_hasskill_checkbox)
        hasMaterialsCheckbox = findViewById(R.id.craftingrecipes_hasmaterials_checkbox)
        recipesLayout = findViewById(R.id.craftingrecipes_layout)

        allRecipes = DataManager.shared.getPassedData<List<FullCraftingRecipeModel>>(
            sourceClasses,
            DataManagerPassedDataKey.CRAFTING_RECIPE_LIST
        )!!
        categoryName = DataManager.shared.getPassedData<String>(
            sourceClasses,
            DataManagerPassedDataKey.CRAFTING_RECIPE_CATEGORY
        )!!

        hasSkillCheckbox.setOnCheckedChangeListener { _, isChecked ->
            hasMaterialsCheckbox.isGone = !isChecked
            hasMaterialsCheckbox.isChecked = false
            buildView()
        }
        hasMaterialsCheckbox.setOnCheckedChangeListener { _, _ ->
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
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, categoryName)

        val searchText = searchBar.text.toString().trim().lowercase()
        val filtered = if (searchText.isNotEmpty()) {
            allRecipes.filter { recipe ->
                recipe.containedInSearch(searchText)
            }
        } else {
            allRecipes
        }

        DataManager.shared.getActiveCharacter().ifLet({ char ->
            hasSkillCheckbox.isGone = false
            hasMaterialsCheckbox.isGone = !hasSkillCheckbox.isChecked

            val filteredBySkills = if (hasSkillCheckbox.isChecked) {
                filtered.filter { char.canCraftWithSkills(it) }
            } else {
                filtered
            }

            val filteredByMaterials = if (hasMaterialsCheckbox.isChecked) {
                filteredBySkills.filter { char.canCraftNow(it) }
            } else {
                filteredBySkills
            }

            buildRecipeCells(filteredByMaterials)
        }, {
            hasSkillCheckbox.isGone = true
            hasMaterialsCheckbox.isGone = true
            hasSkillCheckbox.isChecked = false
            hasMaterialsCheckbox.isChecked = false
            buildRecipeCells(filtered)
        })
    }

    private fun buildRecipeCells(recipes: List<FullCraftingRecipeModel>) {
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(searchBar, recipesLayout)) {
            recipesLayout.removeAllViews()
            recipes.sortedBy { it.getDisplayName() }.forEachIndexed { index, recipe ->
                val cell = CraftingRecipeCell(this)
                cell.setup(recipe)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                recipesLayout.addView(cell)
            }
        }
    }
}
