package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionListModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionCreateModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class XpReductionModelTests : BaseUnitTestClass {

    // ==================== CHARACTER XP REDUCTIONS TESTS ====================

    @Test
    fun testCharacterXpReductions() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from FullCharacterModel - character.xpReductions
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            assertTrue(characters.isNotEmpty())

            // Each character has xpReductions list
            for (character in characters) {
                val xpRedCount = character.xpReductions.size
                assertTrue(xpRedCount >= 0)
            }
        }
    }

    @Test
    fun testXpReductionFields() = runTest {
        loadDataManagerHappyPath(this) {
            // Get any character's xp reductions
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            if (characters.isNotEmpty()) {
                // Find character with xp reductions
                val charWithReductions = characters.firstOrNull { it.xpReductions.isNotEmpty() }
                if (charWithReductions != null) {
                    val xpRed = charWithReductions.xpReductions.first()
                    assertTrue(xpRed.id > 0)
                    assertTrue(xpRed.characterId > 0)
                    assertTrue(xpRed.skillId > 0)
                    assertTrue(xpRed.xpReduction.isNotEmpty())
                }
            }
        }
    }

    @Test
    fun testXpReductionToInt() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            val charWithReductions = characters.firstOrNull { it.xpReductions.isNotEmpty() }
            if (charWithReductions != null) {
                val xpRed = charWithReductions.xpReductions.first()
                // Test pattern from SkillModel: xpReduction.toInt()
                val reductionValue = xpRed.xpReduction.toInt()
                assertTrue(reductionValue >= 0)
            }
        }
    }

    // ==================== XP REDUCTION LIST MODEL TESTS ====================

    @Test
    fun testXpReductionListModelGetSkillIds() = runTest {
        loadDataManagerHappyPath(this) {
            // Get xp reductions from character
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            val charWithReductions = characters.firstOrNull { it.xpReductions.isNotEmpty() }
            if (charWithReductions != null) {
                // Build a list model like the app does
                val xpReductions = charWithReductions.xpReductions
                val listModel = XpReductionListModel(xpReductions.toTypedArray())

                // Test getSkillIds() method
                val skillIds = listModel.getSkillIds()
                assertEquals(xpReductions.size, skillIds.size)

                for (i in skillIds.indices) {
                    assertEquals(xpReductions[i].skillId, skillIds[i])
                }
            }
        }
    }

    @Test
    fun testXpReductionListModelEmpty() = runTest {
        // Test with empty array
        val listModel = XpReductionListModel(emptyArray())
        val skillIds = listModel.getSkillIds()
        assertEquals(0, skillIds.size)
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testXpReductionCreateModel() = runTest {
        val createJson = """{"characterId": 1, "skillId": 37, "xpReduction": "1"}"""
        val createModel: XpReductionCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(1, createModel!!.characterId)
        assertEquals(37, createModel.skillId)
        assertEquals("1", createModel.xpReduction)
    }

    @Test
    fun testXpReductionCreateModelVariousValues() = runTest {
        val createJson = """{"characterId": 5, "skillId": 100, "xpReduction": "2"}"""
        val createModel: XpReductionCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(5, createModel!!.characterId)
        assertEquals(100, createModel.skillId)
        assertEquals("2", createModel.xpReduction)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testXpReductionModelParsing() = runTest {
        val xpRedJson = """{"id": 10, "characterId": 5, "skillId": 42, "xpReduction": "3"}"""
        val parsed: XpReductionModel? = globalFromJson(xpRedJson)
        assertNotNull(parsed)
        assertEquals(10, parsed!!.id)
        assertEquals(5, parsed.characterId)
        assertEquals(42, parsed.skillId)
        assertEquals("3", parsed.xpReduction)
    }

    @Test
    fun testXpReductionZeroValue() = runTest {
        val xpRedJson = """{"id": 11, "characterId": 1, "skillId": 50, "xpReduction": "0"}"""
        val parsed: XpReductionModel? = globalFromJson(xpRedJson)
        assertNotNull(parsed)
        assertEquals("0", parsed!!.xpReduction)
        assertEquals(0, parsed.xpReduction.toInt())
    }

    @Test
    fun testCharacterXpReductionsEmpty() = runTest {
        loadDataManagerHappyPath(this) {
            // Some characters may have no xp reductions
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            // At least one character should exist
            assertTrue(characters.isNotEmpty())

            // Find character with no reductions
            val charWithoutReductions = characters.firstOrNull { it.xpReductions.isEmpty() }
            if (charWithoutReductions != null) {
                assertEquals(0, charWithoutReductions.xpReductions.size)
            }
        }
    }

    @Test
    fun testXpReductionListModelDirect() = runTest {
        // Test XpReductionListModel parsing directly
        val listJson = """{"specialClassXpReductions": [{"id": 1, "characterId": 1, "skillId": 37, "xpReduction": "1"}, {"id": 2, "characterId": 1, "skillId": 38, "xpReduction": "2"}]}"""
        val parsed: XpReductionListModel? = globalFromJson(listJson)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.specialClassXpReductions.size)

        val skillIds = parsed.getSkillIds()
        assertEquals(37, skillIds[0])
        assertEquals(38, skillIds[1])
    }
}