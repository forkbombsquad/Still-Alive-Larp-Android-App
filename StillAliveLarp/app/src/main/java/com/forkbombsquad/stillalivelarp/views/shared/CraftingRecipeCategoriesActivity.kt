package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeModel
import com.forkbombsquad.stillalivelarp.services.models.FullCraftingRecipeModel
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueBuildable
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.reflect.KClass

class CraftingRecipeCategoriesActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var categoriesLayout: LinearLayout
    private lateinit var loadingLayout: LoadingLayout

    private val sourceClasses: List<KClass<*>> = listOf(
        com.forkbombsquad.stillalivelarp.views.rules.RulesFragment::class
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crafting_recipe_categories)
        setupView()
    }

    private fun setupView() {
        loadingLayout = findViewById(R.id.loadinglayout)
        title = findViewById(R.id.craftingcategories_title)
        categoriesLayout = findViewById(R.id.craftingcategories_layout)

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
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Crafting Recipe Categories")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(categoriesLayout)) {
            val allRecipes = DataManager.shared.craftingRecipes

            val categories = allRecipes.map { it.craftingRecipe.category }.distinct().sorted()

            categoriesLayout.removeAllViews()

            categories.forEachIndexed { index, category ->
                val filteredRecipes = allRecipes.filter { it.craftingRecipe.category == category }

                val navButton = NavArrowButtonBlackBuildable(this)
                navButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                navButton.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                navButton.textView.text = category
                navButton.setOnClick {
                    DataManager.shared.setPassedData(
                        this::class,
                        DataManagerPassedDataKey.CRAFTING_RECIPE_LIST,
                        filteredRecipes
                    )
                    DataManager.shared.setPassedData(
                        this::class,
                        DataManagerPassedDataKey.CRAFTING_RECIPE_CATEGORY,
                        category
                    )
                    val intent = Intent(this, CraftingRecipesListActivity::class.java)
                    startActivity(intent)
                }
                categoriesLayout.addView(navButton)
            }

            val allRecipesNav = NavArrowButtonBlueBuildable(this)
            allRecipesNav.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            allRecipesNav.setPadding(8, 16, 8, 32)
            allRecipesNav.textView.text = "All Recipes"
            allRecipesNav.setOnClick {
                DataManager.shared.setPassedData(
                    this::class,
                    DataManagerPassedDataKey.CRAFTING_RECIPE_LIST,
                    allRecipes
                )
                DataManager.shared.setPassedData(
                    this::class,
                    DataManagerPassedDataKey.CRAFTING_RECIPE_CATEGORY,
                    "All Recipes"
                )
                val intent = Intent(this, CraftingRecipesListActivity::class.java)
                startActivity(intent)
            }
            categoriesLayout.addView(allRecipesNav)
        }
    }
}
