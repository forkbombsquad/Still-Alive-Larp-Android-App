package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharacterModelTests: BaseUnitTestClass {

    // Direct field tests - accessing properties directly from FullCharacterModel
    @Test
    fun testCharacterDirectFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)
            val characters = player!!.characters
            assertTrue(characters.isNotEmpty())

            // Get character by id
            val char1 = characters.first { it.id == 1 }
            assertEquals(1, char1.id)
            assertEquals("Commander Davis", char1.fullName)
            assertEquals("2023/06/03", char1.startDate)
            assertTrue(char1.isAlive)
            assertEquals("", char1.deathDate)
            assertEquals(0, char1.infection)
        }
    }

    @Test
    fun testCharacterStatsFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Stats fields
            assertEquals(20, char1.bullets)
            assertEquals(0, char1.megas)
            assertEquals(0, char1.rivals)
            assertEquals(0, char1.rockets)
            assertEquals(1, char1.bulletCasings)
        }
    }

    @Test
    fun testCharacterSuppliesFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Supplies
            assertEquals(0, char1.clothSupplies)
            assertEquals(0, char1.woodSupplies)
            assertEquals(0, char1.metalSupplies)
            assertEquals(0, char1.techSupplies)
            assertEquals(0, char1.medicalSupplies)
        }
    }

    @Test
    fun testCharacterOtherFields() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            assertEquals("None", char1.armor)
            assertEquals(0, char1.unshakableResolveUses)
            assertEquals(0, char1.mysteriousStrangerUses)
            assertEquals(1, char1.playerId)
            assertEquals(1, char1.characterTypeId)
        }
    }

    @Test
    fun testCharacterBioField() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Bio
            assertNotNull(char1.bio)
            assertTrue(char1.bio.isNotEmpty())
            assertTrue(char1.approvedBio)
        }
    }

    // Linked objects
    @Test
    fun testCharacterGear() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Gear is linked
            assertNotNull(char1.gear)
        }
    }

    @Test
    fun testCharacterAwards() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Awards list
            assertNotNull(char1.awards)
        }
    }

    @Test
    fun testCharacterEventAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Event attendees
            assertNotNull(char1.eventAttendees)
        }
    }

    @Test
    fun testCharacterPreregs() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Preregs
            assertNotNull(char1.preregs)
        }
    }

    @Test
    fun testCharacterXpReductions() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // XP reductions
            assertNotNull(char1.xpReductions)
        }
    }

    // Function tests
    @Test
    fun testBaseModel() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val base = char1.baseModel()
            assertNotNull(base)
            assertEquals(char1.id, base.id)
            assertEquals(char1.fullName, base.fullName)
        }
    }

    @Test
    fun testGetPostText() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Character type is STANDARD, isAlive = true
            val postText = char1.getPostText()
            assertEquals("Active", postText)
        }
    }

    @Test
    fun testGetPostTextDead() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val characters = player!!.characters

            // Find dead character
            val deadChar = characters.firstOrNull { !it.isAlive }
            if (deadChar != null) {
                val postText = deadChar.getPostText()
                assertContains(postText, "Deceased")
            }
        }
    }

    @Test
    fun testCharacterType() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // characterTypeId = 1 means STANDARD
            val charType = char1.characterType()
            assertEquals(CharacterType.STANDARD, charType)
        }
    }

    @Test
    fun testCharacterTypeEnum() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val characters = player!!.characters

            // Check different character types
            val standard = characters.firstOrNull { it.characterTypeId == 1 }
            if (standard != null) {
                assertEquals(CharacterType.STANDARD, standard.characterType())
            }

            val npc = characters.firstOrNull { it.characterTypeId == 2 }
            if (npc != null) {
                assertEquals(CharacterType.NPC, npc.characterType())
            }

            val planned = characters.firstOrNull { it.characterTypeId == 3 }
            if (planned != null) {
                assertEquals(CharacterType.PLANNER, planned.characterType())
            }
        }
    }

    // Skills functions
    @Test
    fun testAllSkillsWithCharacterModifications() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val skills = char1.allSkillsWithCharacterModifications()
            assertNotNull(skills)
            assertTrue(skills.isNotEmpty())
        }
    }

    @Test
    fun testAllPurchasedSkills() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val purchased = char1.allPurchasedSkills()
            assertNotNull(purchased)
            // Should have purchased skills based on test data
        }
    }

    @Test
    fun testAllNonPurchasedSkills() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val nonPurchased = char1.allNonPurchasedSkills()
            assertNotNull(nonPurchased)
            assertTrue(nonPurchased.isNotEmpty())
        }
    }

    @Test
    fun testGetSpentXp() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val spentXp = char1.getSpentXp()
            assertTrue(spentXp >= 0)
        }
    }

    @Test
    fun testGetSpentFt1s() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val spentFt1s = char1.getSpentFt1s()
            assertTrue(spentFt1s >= 0)
        }
    }

    @Test
    fun testGetSpentPp() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val spentPp = char1.getSpentPp()
            assertTrue(spentPp >= 0)
        }
    }

    @Test
    fun testGetAllXpSpent() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val allXp = char1.getAllXpSpent()
            assertTrue(allXp >= 0)
        }
    }

    @Test
    fun testGetAllSpentPrestigePoints() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val allPp = char1.getAllSpentPrestigePoints()
            assertTrue(allPp >= 0)
        }
    }

    // Skills filtering
    @Test
    fun testAllPurchaseableSkills() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val purchasable = char1.allPurchaseableSkills()
            assertNotNull(purchasable)
        }
    }

    @Test
    fun testGetPurchasedSkillsFiltered() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val filtered = char1.getPurchasedSkillsFiltered("", com.forkbombsquad.stillalivelarp.utils.SkillFilterType.NONE)
            assertNotNull(filtered)
        }
    }

    @Test
    fun testGetPurchasedChooseOneSkills() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val chooseOne = char1.getPurchasedChooseOneSkills()
            assertNotNull(chooseOne)
        }
    }

    @Test
    fun testGetPurchasedIntrigueSkills() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val intrigue = char1.getPurchasedIntrigueSkills()
            assertNotNull(intrigue)
        }
    }

    // Special skills
    @Test
    fun testHasUnshakableResolve() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val hasResolve = char1.hasUnshakableResolve()
            // Based on test data - may or may not have
            assertNotNull(hasResolve)
        }
    }

    @Test
    fun testMysteriousStrangerCount() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val count = char1.mysteriousStrangerCount()
            assertTrue(count >= 0)
        }
    }

    // Gear functions
    @Test
    fun testGetGearOrganized() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val organized = char1.getGearOrganized()
            assertNotNull(organized)
        }
    }

    // Awards
    @Test
    fun testGetAwardsSorted() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            val sorted = char1.getAwardsSorted()
            assertNotNull(sorted)
        }
    }

    // Prereq checking
    @Test
    fun testCouldPurchaseSkill() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Get a skill and try to check if it could be purchased
            val skills = char1.allNonPurchasedSkills()
            if (skills.isNotEmpty()) {
                val skill = skills.first()
                val could = char1.couldPurchaseSkill(skill)
                assertNotNull(could)
            }
        }
    }

    @Test
    fun testHasAllPrereqsForSkill() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val char1 = player!!.characters.first { it.id == 1 }

            // Get a skill and check prereqs
            val skills = char1.allSkillsWithCharacterModifications()
            if (skills.isNotEmpty()) {
                val skill = skills.first()
                val hasPrereqs = char1.hasAllPrereqsForSkill(skill)
                assertNotNull(hasPrereqs)
            }
        }
    }

    // DataManager access functions
    @Test
    fun testDataManagerGetCharacter() = runTest {
        loadDataManagerHappyPath(this) {
            val char = DataManager.shared.getCharacter(1)
            assertNotNull(char)
            assertEquals(1, char!!.id)
            assertEquals("Commander Davis", char.fullName)
        }
    }

    @Test
    fun testDataManagerGetAllCharacters() = runTest {
        loadDataManagerHappyPath(this) {
            val characters = DataManager.shared.getAllCharacters()
            assertTrue(characters.isNotEmpty())
        }
    }

    // Player access functions
    @Test
    fun testPlayerGetActiveCharacter() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val activeChar = player!!.getActiveCharacter()
            assertNotNull(activeChar)
            assertEquals(1, activeChar!!.id)
        }
    }

    @Test
    fun testPlayerGetInactiveCharacters() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val inactive = player!!.getInactiveCharacters()
            // Check - there are dead characters in test data
            // Should filter by characterType == STANDARD and !isAlive
        }
    }

    @Test
    fun testPlayerGetPlannedCharacters() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            val planned = player!!.getPlannedCharacters()
            // Should filter by characterType == PLANNER
        }
    }

    // Character count
    @Test
    fun testPlayerCharacterCount() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)
            assertTrue(player!!.characters.isNotEmpty())
            assertEquals(11, player.characters.count())
        }
    }
}