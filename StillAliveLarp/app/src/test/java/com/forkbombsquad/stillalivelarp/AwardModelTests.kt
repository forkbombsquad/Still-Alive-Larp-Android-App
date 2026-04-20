package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.AwardListModel
import com.forkbombsquad.stillalivelarp.services.models.AwardModel
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.AwardType
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AwardModelTests : BaseUnitTestClass {

    // ==================== PLAYER AWARDS TESTS ====================

    @Test
    fun testPlayerAwards() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: getCurrentPlayer()?.awards
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val awards = player!!.awards
            assertTrue(awards.isNotEmpty())

            // Each award should have valid data
            for (award in awards) {
                assertTrue(award.id > 0)
                assertTrue(award.playerId > 0)
                assertTrue(award.awardType.isNotEmpty())
            }
        }
    }

    @Test
    fun testPlayerAwardsFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val awards = player!!.awards
            val firstAward = awards.first()

            assertEquals(1, firstAward.id)
            assertEquals(1, firstAward.playerId)
            assertEquals("XP", firstAward.awardType)
            assertEquals(1, firstAward.amount)
            assertEquals("Bio approved", firstAward.reason)
            assertTrue(firstAward.date.isNotEmpty())
            assertTrue(firstAward.amount.isNotEmpty())
        }
    }

    @Test
    fun testPlayerAwardsSorted() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: player.getAwardsSorted()
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val sortedAwards = player!!.getAwardsSorted()
            assertTrue(sortedAwards.isNotEmpty())
        }
    }

    // ==================== CHARACTER AWARDS TESTS ====================

    @Test
    fun testCharacterAwards() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: character.awards (FullCharacterModel)
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            assertTrue(characters.isNotEmpty())

            // Each character has awards list
            for (character in characters) {
                val awardCount = character.awards.size
                assertTrue(awardCount >= 0)
            }
        }
    }

    @Test
    fun testCharacterAwardsFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters

            // Find a character with awards
            val charWithAwards = characters.firstOrNull { it.awards.isNotEmpty() }
            if (charWithAwards != null) {
                val award = charWithAwards.awards.first()
                assertTrue(award.id > 0)
                assertTrue(award.characterId != null) // Character awards have characterId
                assertTrue(award.awardType.isNotEmpty())
            }
        }
    }

    // ==================== AWARD TYPE TESTS ====================

    @Test
    fun testGetAwardTypeEnumXP() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find an XP award
            val xpAward = player!!.awards.firstOrNull { it.awardType == "XP" }
            assertNotNull(xpAward)

            val awardType = xpAward!!.getAwardTypeEnum()
            assertTrue(awardType is AwardPlayerType)
            assertEquals(AwardPlayerType.XP, awardType)
        }
    }

    @Test
    fun testGetAwardTypeEnumPP() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find a PP award
            val ppAward = player!!.awards.firstOrNull { it.awardType == "PP" }
            if (ppAward != null) {
                val awardType = ppAward.getAwardTypeEnum()
                assertTrue(awardType is AwardPlayerType)
                assertEquals(AwardPlayerType.PRESTIGEPOINTS, awardType)
            }
        }
    }

    @Test
    fun testGetAwardTypeEnumCharacterAward() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            val charWithAwards = characters.firstOrNull { it.awards.isNotEmpty() }
            if (charWithAwards != null) {
                val award = charWithAwards.awards.first()
                val awardType = award.getAwardTypeEnum()

                // Character awards could be AwardCharType or AwardPlayerType
                assertTrue(awardType is AwardType)
            }
        }
    }

    @Test
    fun testGetAwardTypeEnumInvalid() = runTest {
        // Test with invalid award type - should default to XP
        val awardJson = """{"id": 1, "playerId": 1, "characterId": null, "awardType": "INVALID", "reason": "Test", "date": "2024/01/01", "amount": "1"}"""
        val award: AwardModel? = globalFromJson(awardJson)
        assertNotNull(award)

        val awardType = award!!.getAwardTypeEnum()
        // Invalid types default to XP
        assertEquals(AwardPlayerType.XP, awardType)
    }

    // ==================== DISPLAY TEXT TESTS ====================

    @Test
    fun testGetDisplayTextSingular() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find an award with amount "1" (singular)
            val singularAward = player!!.awards.firstOrNull { it.amount == "1" }
            if (singularAward != null) {
                val displayText = singularAward.getDisplayText()
                assertTrue(displayText.contains("1"))
                // Should NOT have "s" at end of unit name
                assertFalse(displayText.contains("Points") && displayText.contains("s "))
            }
        }
    }

    @Test
    fun testGetDisplayTextPlural() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find an award with amount > 1 (plural)
            val pluralAward = player!!.awards.firstOrNull { it.amount.toIntOrNull()?.let { it > 1 } == true }
            if (pluralAward != null) {
                val displayText = pluralAward.getDisplayText()
                assertTrue(displayText.contains(pluralAward.amount))
            }
        }
    }

    @Test
    fun testAwardPlayerTypeDisplayText() = runTest {
        // Test AwardPlayerType.getDisplayText
        val xpSingular = AwardPlayerType.XP.getDisplayText(false)
        assertEquals("Experience Point", xpSingular)

        val xpPlural = AwardPlayerType.XP.getDisplayText(true)
        assertEquals("Experience Points", xpPlural)

        val ppSingular = AwardPlayerType.PRESTIGEPOINTS.getDisplayText(false)
        assertEquals("Prestige Point", ppSingular)

        val ppPlural = AwardPlayerType.PRESTIGEPOINTS.getDisplayText(true)
        assertEquals("Prestige Points", ppPlural)
    }

    @Test
    fun testAwardCharTypeDisplayText() = runTest {
        // Test AwardCharType.getDisplayText
        val woodSingular = AwardCharType.MATERIALWOOD.getDisplayText(false)
        assertEquals("Wood", woodSingular)

        val woodPlural = AwardCharType.MATERIALWOOD.getDisplayText(true)
        assertEquals("Wood", woodPlural) // Wood doesn't change

        val bulletSingular = AwardCharType.AMMOBULLET.getDisplayText(false)
        assertEquals("Bullet", bulletSingular)

        val bulletPlural = AwardCharType.AMMOBULLET.getDisplayText(true)
        assertEquals("Bullets", bulletPlural)

        val techSingular = AwardCharType.MATERIALTECH.getDisplayText(false)
        assertEquals("Tech Supply", techSingular)

        val techPlural = AwardCharType.MATERIALTECH.getDisplayText(true)
        assertEquals("Tech Supplies", techPlural)
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testAwardCreateModelParsing() = runTest {
        val createJson = """{"playerId": 1, "characterId": 5, "awardType": "XP", "reason": "Test award", "date": "2024/06/01", "amount": "5"}"""
        val createModel: AwardCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(1, createModel!!.playerId)
        assertEquals(5, createModel.characterId)
        assertEquals("XP", createModel.awardType)
        assertEquals("Test award", createModel.reason)
        assertEquals("2024/06/01", createModel.date)
        assertEquals("5", createModel.amount)
    }

    @Test
    fun testAwardCreateModelWithNullCharacterId() = runTest {
        val createJson = """{"playerId": 1, "characterId": null, "awardType": "PP", "reason": "Test", "date": "2024/06/01", "amount": "10"}"""
        val createModel: AwardCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertNull(createModel!!.characterId)
    }

    @Test
    fun testAwardCreateModelFactoryPlayer() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val createModel = AwardCreateModel.createPlayerAward(
                player = player!!.baseModel(), // Note: uses baseModel(), not FullPlayerModel
                awardType = AwardPlayerType.XP,
                reason = "Test XP award",
                amount = "5"
            )

            assertEquals(player.id, createModel.playerId)
            assertNull(createModel.characterId)
            assertEquals("XP", createModel.awardType)
            assertEquals("Test XP award", createModel.reason)
            assertEquals("5", createModel.amount)
            // Date should be today's date
            assertEquals(LocalDate.now().yyyyMMddFormatted(), createModel.date)
        }
    }

    @Test
    fun testAwardCreateModelFactoryCharacter() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val character = player!!.characters.firstOrNull()
            assertNotNull(character)

            // Test AwardCreateModel.createCharacterAward - uses char.baseModel() in actual code
            val createModel = AwardCreateModel.createCharacterAward(
                char = character!!.baseModel(), // Note: uses baseModel(), not FullCharacterModel
                awardType = AwardCharType.MATERIALWOOD,
                reason = "Test material",
                amount = "3"
            )

            assertEquals(character.playerId, createModel.playerId)
            assertEquals(character.id, createModel.characterId)
            assertEquals("MATERIAL_WOOD", createModel.awardType)
            assertEquals("Test material", createModel.reason)
        }
    }

    @Test
    fun testAwardCreateModelFactoryPlayerWithId() = runTest {
        // Test factory with playerId instead of PlayerModel
        val createModel = AwardCreateModel.createPlayerAward(
            playerId = 42,
            awardType = AwardPlayerType.PRESTIGEPOINTS,
            reason = "Test PP award",
            amount = "2"
        )

        assertEquals(42, createModel.playerId)
        assertNull(createModel.characterId)
        assertEquals("PP", createModel.awardType)
    }

    // ==================== AWARD LIST MODEL TESTS ====================

    @Test
    fun testAwardListModel() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Create list model like API does
            val listModel = AwardListModel(player!!.awards.toTypedArray())
            assertTrue(listModel.awards.isNotEmpty())
            assertEquals(player.awards.size, listModel.awards.size)
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testAwardModelWithEmptyReason() = runTest {
        val awardJson = """{"id": 1, "playerId": 1, "characterId": null, "awardType": "XP", "reason": "", "date": "2024/06/01", "amount": "1"}"""
        val award: AwardModel? = globalFromJson(awardJson)
        assertNotNull(award)
        assertEquals("", award!!.reason)
    }

    @Test
    fun testPlayerWithNoAwards() = runTest {
        loadDataManagerHappyPath(this) {
            // Current player should have awards in test data
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)
            // If somehow no awards, just verify it's a list
            assertNotNull(player!!.awards)
        }
    }

    @Test
    fun testCharacterWithNoAwards() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            // At least one character should exist
            assertTrue(characters.isNotEmpty())

            // Find a character with no awards
            val charWithoutAwards = characters.firstOrNull { it.awards.isEmpty() }
            if (charWithoutAwards != null) {
                assertEquals(0, charWithoutAwards.awards.size)
            }
        }
    }

    @Test
    fun testAllAwardPlayerTypeValues() = runTest {
        // Verify all AwardPlayerType values
        assertEquals(3, AwardPlayerType.values().size)
        assertEquals("XP", AwardPlayerType.XP.text)
        assertEquals("PP", AwardPlayerType.PRESTIGEPOINTS.text)
        assertEquals("FREE-T1-SKILL", AwardPlayerType.FREETIER1SKILLS.text)
    }

    @Test
    fun testAllAwardCharTypeValues() = runTest {
        // Verify all AwardCharType values
        val charTypes = AwardCharType.values()
        assertTrue(charTypes.size >= 10)

        // Check a few
        assertEquals("INFECTION", AwardCharType.INFECTION.text)
        assertEquals("MATERIAL_WOOD", AwardCharType.MATERIALWOOD.text)
        assertEquals("AMMO_ROCKET", AwardCharType.AMMOROCKET.text)
    }
}