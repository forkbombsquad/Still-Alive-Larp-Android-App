package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CraftingRecipeModelTests: BaseUnitTestClass {

    @Test
    fun testCraftingRecipeModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val recipes = DataManager.shared.craftingRecipes.map { it.craftingRecipe }
            
            // Verify we have 56 recipes from the test data
            assertTrue(recipes.isNotEmpty())
            assertEquals(56, recipes.size)

            // Test first recipe (Rocket - id 4)
            val firstRecipe = recipes.firstOrNull { it.id == 4 }
            assertNotNull(firstRecipe)
            assertEquals("Rocket", firstRecipe!!.name)
            assertEquals(4, firstRecipe.id)
            assertEquals(-1, firstRecipe.baseRecipeId)
            assertEquals(22, firstRecipe.skillId)
            assertEquals(1, firstRecipe.numProduced)
            assertEquals("Ammunition", firstRecipe.category)
            assertEquals(5.0, firstRecipe.craftingTime)
            assertEquals(2, firstRecipe.wood)
            assertEquals(2, firstRecipe.metal)
            assertEquals(2, firstRecipe.cloth)
            assertEquals(2, firstRecipe.tech)
            assertEquals(0, firstRecipe.medical)
            assertEquals(0, firstRecipe.casing)

            // Test last recipe (Metal Reclamation Alternate Skillset - id 64)
            val lastRecipe = recipes.firstOrNull { it.id == 64 }
            assertNotNull(lastRecipe)
            assertEquals("Alternate Skillset", lastRecipe!!.name)
            assertEquals(44, lastRecipe.baseRecipeId) // Not -1, so it's an alternate
            assertEquals(10, lastRecipe.casing)
        }
    }

    @Test
    fun testCraftingRecipeIsAlternate() = runTest {
        loadDataManagerHappyPath(this) {
            val recipes = DataManager.shared.craftingRecipes.map { it.craftingRecipe }
            
            // Test non-alternate recipes (baseRecipeId = -1 or null)
            val rocket = recipes.firstOrNull { it.id == 4 }
            assertNotNull(rocket)
            assertFalse(rocket!!.isAlternate())

            // Test alternate recipes (baseRecipeId != -1)
            val repacked = recipes.firstOrNull { it.id == 7 }
            assertNotNull(repacked)
            assertTrue(repacked!!.isAlternate()) // baseRecipeId = 6
        }
    }

    @Test
    fun testCraftingRecipeGetMaterialsList() = runTest {
        loadDataManagerHappyPath(this) {
            val recipes = DataManager.shared.craftingRecipes.map { it.craftingRecipe }
            
            // Test Rocket (id 4) - has wood, metal, cloth, tech
            val rocket = recipes.firstOrNull { it.id == 4 }
            assertNotNull(rocket)
            val rocketMaterials = rocket!!.getMaterialsList()
            assertEquals(4, rocketMaterials.size)
            assertTrue(rocketMaterials.any { it.name == "Wood" && it.quantity == 2 })
            assertTrue(rocketMaterials.any { it.name == "Metal" && it.quantity == 2 })
            assertTrue(rocketMaterials.any { it.name == "Cloth" && it.quantity == 2 })
            assertTrue(rocketMaterials.any { it.name == "Tech Supplies" && it.quantity == 2 })

            // Test Standard Armor (id 19) - has all materials including medical and casings
            val armor = recipes.firstOrNull { it.id == 19 }
            assertNotNull(armor)
            val armorMaterials = armor!!.getMaterialsList()
            assertTrue(armorMaterials.any { it.name == "Wood" && it.quantity == 3 })
            assertTrue(armorMaterials.any { it.name == "Metal" && it.quantity == 5 })
            assertTrue(armorMaterials.any { it.name == "Cloth" && it.quantity == 1 })
        }
    }

    @Test
    fun testCraftingRecipeWithOtherRequiredItems() = runTest {
        loadDataManagerHappyPath(this) {
            val recipes = DataManager.shared.craftingRecipes.map { it.craftingRecipe }
            
            // Test Rocket Rigged (id 18) - has other required item (id 4, num 1)
            val rocketRigged = recipes.firstOrNull { it.id == 18 }
            assertNotNull(rocketRigged)
            
            val otherIds = rocketRigged!!.getOtherRecipeIds()
            assertEquals(1, otherIds.size)
            assertEquals(4, otherIds[0]) // Requires recipe 4 (Rocket)

            // Test Baked Fish Dinner (id 54) - has two other required recipe items
            val fishDinner = recipes.firstOrNull { it.id == 54 }
            assertNotNull(fishDinner)
            val fishDinnerOtherIds = fishDinner!!.getOtherRecipeIds()
            assertEquals(2, fishDinnerOtherIds.size)
            assertTrue(fishDinnerOtherIds.contains(53)) // Boiled Potatoes
            assertTrue(fishDinnerOtherIds.contains(52)) // Filleted Fish
        }
    }

    @Test
    fun testCraftingRecipeFoodMaterials() = runTest {
        loadDataManagerHappyPath(this) {
            val recipes = DataManager.shared.craftingRecipes.map { it.craftingRecipe }
            
            // Test Flour (id 50) - requires wheat food
            val flour = recipes.firstOrNull { it.id == 50 }
            assertNotNull(flour)
            val flourJson = flour!!.otherRequiredItemsJsonModel
            assertNotNull(flourJson)
            val flourFoods = flourJson!!.getFoodMaterials()
            assertEquals(1, flourFoods.size)
            assertEquals("wheat", flourFoods[0].name)
            assertEquals(2, flourFoods[0].quantity)
            assertTrue(flourFoods[0].isFood)

            // Test Fish Stew (id 60) - requires potato and fish foods
            val fishStew = recipes.firstOrNull { it.id == 60 }
            assertNotNull(fishStew)
            val fishStewJson = fishStew!!.otherRequiredItemsJsonModel
            assertNotNull(fishStewJson)
            val fishStewFoods = fishStewJson!!.getFoodMaterials()
            assertEquals(2, fishStewFoods.size)
            assertTrue(fishStewFoods.any { it.name == "potato" && it.quantity == 1 })
            assertTrue(fishStewFoods.any { it.name == "fish" && it.quantity == 2 })
        }
    }

    @Test
    fun testCraftingRecipeEdgeCaseEmptyOtherRequiredItems() = runTest {
        // Test that model handles empty or null otherRequiredItemIds
        val json = """{"id": 999, "name": "Test", "baseRecipeId": -1, "skillId": 1, "numProduced": 1, "category": "Test", "craftingTime": 5, "wood": 1, "metal": 0, "cloth": 0, "tech": 0, "medical": 0, "casing": 0, "otherRequiredItemIds": null, "desc": ""}"""
        val recipe: CraftingRecipeModel? = globalFromJson(json)
        
        assertNotNull(recipe)
        assertEquals(999, recipe!!.id)
        assertNull(recipe.otherRequiredItemsJsonModel)
        
        // getMaterialsList should still work
        val materials = recipe.getMaterialsList()
        assertEquals(1, materials.size)
        assertEquals("Wood", materials[0].name)
    }

    @Test
    fun testFullCraftingRecipeModel() = runTest {
        loadDataManagerHappyPath(this) {
            val fullRecipes = DataManager.shared.craftingRecipes
            
            // Test FullCraftingRecipeModel fields
            val rocket = fullRecipes.firstOrNull { it.id == 4 }
            assertNotNull(rocket)
            assertEquals("Ammunition", rocket!!.category)
            assertEquals(4, rocket.id)
            
            // Test display name for regular recipe
            assertEquals("Rocket", rocket.getDisplayName())

            // Test alternate recipe display name
            val repacked = fullRecipes.firstOrNull { it.id == 7 }
            assertNotNull(repacked)
            // Should show "Bullets (Repacked)" since baseRecipeId = 6 (Bullets)
            val displayName = repacked!!.getDisplayName()
            assertTrue(displayName.contains("Repacked"))
        }
    }

    @Test
    fun testFullCraftingRecipeGetCraftingTimeText() = runTest {
        loadDataManagerHappyPath(this) {
            val fullRecipes = DataManager.shared.craftingRecipes
            
            // Test regular time (5 min)
            val regular = fullRecipes.firstOrNull { it.id == 4 }
            assertNotNull(regular)
            assertEquals("5 min", regular!!.getCraftingTimeText())

            // Test seconds (1 min = 60 sec)
            val quick = fullRecipes.firstOrNull { it.craftingRecipe.craftingTime == 1.0 }
            assertNotNull(quick)
            assertEquals("1 min", quick!!.getCraftingTimeText())

            // Test special case (-1 = see notes)
            val special = fullRecipes.firstOrNull { it.craftingRecipe.craftingTime < 0 }
            assertNotNull(special)
            assertEquals("*see Notes", special!!.getCraftingTimeText())
        }
    }

    @Test
    fun testFullCraftingRecipeContainedInSearch() = runTest {
        loadDataManagerHappyPath(this) {
            val fullRecipes = DataManager.shared.craftingRecipes
            
            // Search by name
            val byName = fullRecipes.filter { it.containedInSearch("Rocket") }
            assertTrue(byName.isNotEmpty())
            assertTrue(byName.any { it.id == 4 })

            // Search by category
            val byCategory = fullRecipes.filter { it.containedInSearch("Ammunition") }
            assertTrue(byCategory.isNotEmpty())

            // Search by description
            val byDesc = fullRecipes.filter { it.containedInSearch("Blue-Beaded Armor") }
            assertTrue(byDesc.isNotEmpty())
            assertTrue(byDesc.any { it.id == 19 })

            // Case insensitive
            val caseInsensitive = fullRecipes.filter { it.containedInSearch("rocket") }
            assertTrue(caseInsensitive.isNotEmpty())
        }
    }

}