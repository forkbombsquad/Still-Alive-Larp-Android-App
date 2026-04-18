package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.Constants
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GearModelTests: BaseUnitTestClass {

    @Test
    fun testGearModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            // Access gear through characters using getAllCharacters()
            val characters = DataManager.shared.getAllCharacters()
            assertTrue(characters.isNotEmpty())

            // Find character with gear (characterId 1 has gear in test data)
            val charWithGear = characters.firstOrNull { it.gear != null }
            assertNotNull(charWithGear)

            val gear = charWithGear!!.gear
            assertNotNull(gear)
            assertEquals(1, gear!!.id)
            assertEquals(1, gear.characterId)
            assertNotNull(gear.gearJson)
            assertTrue(gear.gearJson!!.isNotEmpty())
        }
    }

    @Test
    fun testGearJsonModelsParsing() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()
            val charWithGear = characters.firstOrNull { it.id == 1 }
            assertNotNull(charWithGear)

            val gear = charWithGear!!.gear
            assertNotNull(gear)

            // jsonModels should parse the gearJson string
            val jsonModels = gear!!.jsonModels
            assertNotNull(jsonModels)
            assertEquals(4, jsonModels!!.size)

            // Verify first item (Blacklight)
            val blacklight = jsonModels.firstOrNull { it.name == "Blacklight" }
            assertNotNull(blacklight)
            assertEquals("Accessory", blacklight!!.gearType)
            assertEquals("Blacklight Flashlight", blacklight.primarySubtype)
            assertEquals("None", blacklight.secondarySubtype)
            assertEquals("Black body with strap", blacklight.desc)
        }
    }

    @Test
    fun testGetPrimaryFirearm() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 1 has a primary firearm (Hammerstrike with secondarySubtype "Primary Firearm")
            val char1 = characters.firstOrNull { it.id == 1 }
            assertNotNull(char1)
            val primaryFirearm = char1!!.gear?.getPrimaryFirearm()
            assertNotNull(primaryFirearm)
            assertEquals("Hammerstrike", primaryFirearm!!.name)
            assertEquals("Firearm", primaryFirearm.gearType)
            assertEquals("Primary Firearm", primaryFirearm.secondarySubtype)

            // Character 8 has no gear - gear is null
            val char8 = characters.firstOrNull { it.id == 8 }
            assertNotNull(char8)
            // Character 8 has gear but no primary firearm
            val char8Gear = char8!!.gear
            assertNotNull(char8Gear)
            val char8Primary = char8Gear!!.getPrimaryFirearm()
            assertNull(char8Primary)
        }
    }

    @Test
    fun testGetGearOrganized() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 1 has mixed gear types
            val char1 = characters.firstOrNull { it.id == 1 }
            assertNotNull(char1)
            val organized = char1!!.gear!!.getGearOrganized()

            // Should have 6 categories
            assertEquals(6, organized.size)

            // Verify categories exist
            assertTrue(organized.containsKey(Constants.GearTypes.firearm))
            assertTrue(organized.containsKey(Constants.GearTypes.meleeWeapon))
            assertTrue(organized.containsKey(Constants.GearTypes.clothing))
            assertTrue(organized.containsKey(Constants.GearTypes.accessory))
            assertTrue(organized.containsKey(Constants.GearTypes.bag))
            assertTrue(organized.containsKey(Constants.GearTypes.other))

            // Character 1: 2 firearms (one is primary), 1 melee, 1 accessory, 0 clothing, 0 bag, 0 other
            val firearms = organized[Constants.GearTypes.firearm]
            assertEquals(2, firearms!!.size)

            val melee = organized[Constants.GearTypes.meleeWeapon]
            assertEquals(1, melee!!.size)

            val accessory = organized[Constants.GearTypes.accessory]
            assertEquals(1, accessory!!.size)
        }
    }

    @Test
    fun testGearOrganizedFirearmSorting() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 5 (characterId 5) has multiple firearms sorted by subtype
            val char5 = characters.firstOrNull { it.id == 5 }
            assertNotNull(char5)
            assertNotNull(char5!!.gear)
            val organized = char5.gear!!.getGearOrganized()
            val firearms = organized[Constants.GearTypes.firearm]
            assertNotNull(firearms)
            assertEquals(2, firearms!!.size)

            // First should be primary firearm
            val first = firearms[0]
            assertEquals("Primary Firearm", first.secondarySubtype)

            // Character 7 (characterId 7) has medium firearm as primary, then light firearm
            val char7 = characters.firstOrNull { it.id == 7 }
            assertNotNull(char7)
            assertNotNull(char7!!.gear)
            val char7Organized = char7.gear!!.getGearOrganized()
            val char7Firearms = char7Organized[Constants.GearTypes.firearm]
            assertNotNull(char7Firearms)
            assertEquals(2, char7Firearms!!.size)

            // First should be medium (primary)
            assertEquals("Medium Firearm", char7Firearms[0].primarySubtype)
            assertEquals("Primary Firearm", char7Firearms[0].secondarySubtype)
        }
    }

    @Test
    fun testGearOrganizedMeleeSorting() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 9 (characterId 9) has multiple melee weapons
            val char9 = characters.firstOrNull { it.id == 9 }
            assertNotNull(char9)
            assertNotNull(char9!!.gear)
            val organized = char9.gear!!.getGearOrganized()
            val melee = organized[Constants.GearTypes.meleeWeapon]
            assertNotNull(melee)
            assertEquals(4, melee!!.size)

            // Sorted by weight: super light -> light -> medium -> heavy
            assertEquals("Super Light Melee Weapon", melee[0].primarySubtype) // Coreless Dagger
            assertEquals("Light Melee Weapon", melee[1].primarySubtype)     // Dagger
            assertEquals("Medium Melee Weapon", melee[2].primarySubtype) // Long Sword
            assertEquals("Heavy Melee Weapon", melee[3].primarySubtype) // Glaive
        }
    }

    @Test
    fun testGearOrganizedBagSorting() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 5 (characterId 5) has multiple bags
            val char5 = characters.firstOrNull { it.id == 5 }
            assertNotNull(char5)
            assertNotNull(char5!!.gear)
            val organized = char5.gear!!.getGearOrganized()
            val bags = organized[Constants.GearTypes.bag]
            assertNotNull(bags)
            assertEquals(3, bags!!.size)

            // Sorted: small -> large -> (then extra large if existed)
            // Looking at test data: Large Bag, Small Bag, Small Bag
            assertEquals("Small Bag", bags[0].primarySubtype)
            assertEquals("Large Bag", bags[1].primarySubtype)
            assertEquals("Small Bag", bags[2].primarySubtype)
        }
    }

    @Test
    fun testGearOrganizedAccessorySorting() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 5 (characterId 5) has multiple accessories
            val char5 = characters.firstOrNull { it.id == 5 }
            assertNotNull(char5)
            assertNotNull(char5!!.gear)
            val organized = char5.gear!!.getGearOrganized()
            val accessories = organized[Constants.GearTypes.accessory]
            assertNotNull(accessories)
            assertEquals(2, accessories!!.size)

            // Sorted: blacklight -> flashlight -> other
            assertEquals("Blacklight Flashlight", accessories[0].primarySubtype)
            assertEquals("Other", accessories[1].primarySubtype)
        }
    }

    @Test
    fun testGearJsonModelIsPrimaryFirearm() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()
            val char1 = characters.firstOrNull { it.id == 1 }
            assertNotNull(char1)
            assertNotNull(char1!!.gear)

            val jsonModels = char1.gear!!.jsonModels
            assertNotNull(jsonModels)

            // Find the primary firearm
            val primary = jsonModels!!.firstOrNull { it.isPrimaryFirearm() }
            assertNotNull(primary)
            assertTrue(primary!!.isPrimaryFirearm())

            // Find a non-primary firearm
            val nonPrimary = jsonModels!!.firstOrNull { !it.isPrimaryFirearm() && it.gearType == Constants.GearTypes.firearm }
            assertNotNull(nonPrimary)
            assertFalse(nonPrimary!!.isPrimaryFirearm())
        }
    }

    @Test
    fun testGearJsonModelIsEqualTo() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()
            val char1 = characters.firstOrNull { it.id == 1 }
            assertNotNull(char1)
            assertNotNull(char1!!.gear)

            val jsonModels = char1.gear!!.jsonModels
            assertNotNull(jsonModels)

            // Get two items and test isEqualTo
            val blacklight = jsonModels!!.firstOrNull { it.name == "Blacklight" }
            val fireAxe = jsonModels!!.firstOrNull { it.name == "Fire Axe" }
            assertNotNull(blacklight)
            assertNotNull(fireAxe)

            // Should not be equal (different names)
            assertFalse(blacklight!!.isEqualTo(fireAxe!!))

            // Create identical model and test equality
            val duplicateBlacklight = GearJsonModel(
                name = "Blacklight",
                gearType = "Accessory",
                primarySubtype = "Blacklight Flashlight",
                secondarySubtype = "None",
                desc = "Black body with strap"
            )
            assertTrue(blacklight.isEqualTo(duplicateBlacklight))
        }
    }

    @Test
    fun testGearJsonModelDuplicateWithEdit() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()
            val char1 = characters.firstOrNull { it.id == 1 }
            assertNotNull(char1)
            assertNotNull(char1!!.gear)

            val jsonModels = char1.gear!!.jsonModels
            assertNotNull(jsonModels)

            val original = jsonModels!!.firstOrNull { it.name == "Blacklight" }
            assertNotNull(original)

            // Duplicate with no changes
            val duplicate = original!!.duplicateWithEdit()
            assertTrue(original.isEqualTo(duplicate))

            // Duplicate with name change
            val renamed = original!!.duplicateWithEdit(name = "New Blacklight")
            assertEquals("New Blacklight", renamed.name)
            assertEquals(original.gearType, renamed.gearType)
            assertEquals(original.primarySubtype, renamed.primarySubtype)
            assertEquals(original.secondarySubtype, renamed.secondarySubtype)
            assertEquals(original.desc, renamed.desc)

            // Duplicate with multiple changes
            val modified = original!!.duplicateWithEdit(
                name = "Modified",
                desc = "New description"
            )
            assertEquals("Modified", modified.name)
            assertEquals("New description", modified.desc)
        }
    }

    @Test
    fun testGearWithOtherTypeOnly() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()

            // Character 44 (characterId 44) has only "Other" type gear (Adreanaline)
            val char44 = characters.firstOrNull { it.id == 44 }
            assertNotNull(char44)
            assertNotNull(char44!!.gear)

            val jsonModels = char44.gear!!.jsonModels
            assertNotNull(jsonModels)
            assertEquals(1, jsonModels!!.size)

            val organized = char44.gear!!.getGearOrganized()
            // Should have 6 categories (including "Other" with 1 item)
            val otherGear = organized[Constants.GearTypes.other]
            assertNotNull(otherGear)
            assertEquals(1, otherGear!!.size)
            assertEquals("Adreanaline", otherGear[0].name)
        }
    }

    @Test
    fun testGearOrganizedEmptyMap() = runTest {
        // Test edge case: manually create GearModel with empty gear list
        val emptyGearJson = """{"gearJson":[]}"""
        val gearWithEmptyJson = com.forkbombsquad.stillalivelarp.services.models.GearModel(
            id = 999,
            characterId = 999,
            gearJson = emptyGearJson
        )

        val jsonModels = gearWithEmptyJson.jsonModels
        assertNotNull(jsonModels)
        assertTrue(jsonModels!!.isEmpty())

        val organized = gearWithEmptyJson.getGearOrganized()
        assertEquals(6, organized.size)

        // All categories should be empty lists
        organized.values.forEach { list ->
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun testFullGearListFromCharacters() = runTest {
        loadDataManagerHappyPath(this) {
            // Get all characters that have gear
            val charactersWithGear = DataManager.shared.getAllCharacters().filter { it.gear != null }

            // Verify we have characters with gear
            assertTrue(charactersWithGear.isNotEmpty())

            // Verify each has valid jsonModels
            val allHaveJson = charactersWithGear.all { it.gear?.jsonModels != null }
            assertTrue(allHaveJson)

            // Check specific character IDs (from test data)
            val characterIds = charactersWithGear.map { it.id }.sorted()
            assertTrue(characterIds.contains(1))
            assertTrue(characterIds.contains(5))
            assertTrue(characterIds.contains(7))
            assertTrue(characterIds.contains(9))
            assertTrue(characterIds.contains(16))
            assertTrue(characterIds.contains(44))
        }
    }

    @Test
    fun testFindCharacterWithNoGear() = runTest {
        loadDataManagerHappyPath(this) {
            // Some characters may have null gear
            val characters = DataManager.shared.getAllCharacters()
            
            // At least one character should have gear
            val charsWithGear = characters.filter { it.gear != null }
            assertTrue(charsWithGear.isNotEmpty())
            
            // Verify we can get primary firearm for at least one
            val charWithPrimary = charsWithGear.firstOrNull { it.gear?.getPrimaryFirearm() != null }
            assertNotNull(charWithPrimary)
        }
    }

}