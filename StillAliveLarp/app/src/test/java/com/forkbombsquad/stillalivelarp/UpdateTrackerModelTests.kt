package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateTrackerModelTests : BaseUnitTestClass {

    // ==================== UPDATE TRACKER MODEL FIELDS ====================

    @Test
    fun testUpdateTrackerModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            // Note: UpdateTracker isn't directly accessible from DataManager in the same way
            // But we can test the model directly via JSON parsing

            // The test data uses ALL_1S which has all values = 1
            // We can verify the parsing works
            val trackerJson = """{"id": 1, "announcements": 5, "awards": 10, "characters": 15, "gear": 20, "characterSkills": 25, "contactRequests": 30, "events": 35, "eventAttendees": 40, "preregs": 45, "featureFlags": 2, "intrigues": 7, "players": 12, "profileImages": 3, "researchProjects": 8, "skills": 100, "skillCategories": 5, "skillPrereqs": 50, "xpReductions": 15, "rulebookVersion": "2206", "treatingWoundsVersion": "1.0", "campStatus": 1, "craftingRecipes": 56}"""

            val tracker: UpdateTrackerModel? = globalFromJson(trackerJson)
            assertNotNull(tracker)

            assertEquals(1, tracker!!.id)
            assertEquals(5, tracker.announcements)
            assertEquals(10, tracker.awards)
            assertEquals(15, tracker.characters)
            assertEquals(56, tracker.craftingRecipes)
            assertEquals("2206", tracker.rulebookVersion)
            assertEquals("1.0", tracker.treatingWoundsVersion)
        }
    }

    // ==================== EMPTY MODEL ====================

    @Test
    fun testUpdateTrackerEmpty() = runTest {
        val empty = UpdateTrackerModel.empty()

        assertEquals(-1, empty.id)
        assertEquals(-1, empty.announcements)
        assertEquals(-1, empty.awards)
        assertEquals(-1, empty.characters)
        assertEquals(-1, empty.events)
        assertEquals(-1, empty.eventAttendees)
        assertEquals(-1, empty.preregs)
        assertEquals("", empty.rulebookVersion)
        assertEquals("", empty.treatingWoundsVersion)
    }

    // ==================== GET DIFFERENCES TESTS ====================

    @Test
    fun testGetDifferencesSingleChange() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 1,
                announcements = 1,  // Changed from -1 to 1
                awards = -1,
                characters = -1,
                gear = -1,
                characterSkills = -1,
                contactRequests = -1,
                events = -1,
                eventAttendees = -1,
                preregs = -1,
                featureFlags = -1,
                intrigues = -1,
                players = -1,
                profileImages = -1,
                researchProjects = -1,
                skills = -1,
                skillCategories = -1,
                skillPrereqs = -1,
                xpReductions = -1,
                campStatus = -1,
                craftingRecipes = -1,
                rulebookVersion = "",
                treatingWoundsVersion = ""
            )

            val differences = current.getDifferences(new)

            assertEquals(1, differences.size)
            assertTrue(differences.contains(DataManagerType.ANNOUNCEMENTS))
        }
    }

    @Test
    fun testGetDifferencesMultipleChanges() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 1,
                announcements = 1,  // Changed
                awards = 5,         // Changed
                characters = -1,
                gear = -1,
                characterSkills = 10, // Changed
                contactRequests = -1,
                events = -1,
                eventAttendees = -1,
                preregs = -1,
                featureFlags = -1,
                intrigues = -1,
                players = -1,
                profileImages = -1,
                researchProjects = -1,
                skills = -1,
                skillCategories = -1,
                skillPrereqs = -1,
                xpReductions = -1,
                campStatus = -1,
                craftingRecipes = -1,
                rulebookVersion = "",
                treatingWoundsVersion = ""
            )

            val differences = current.getDifferences(new)

            assertEquals(3, differences.size)
            assertTrue(differences.contains(DataManagerType.ANNOUNCEMENTS))
            assertTrue(differences.contains(DataManagerType.AWARDS))
            assertTrue(differences.contains(DataManagerType.CHARACTER_SKILLS))
        }
    }

    @Test
    fun testGetDifferencesNoChanges() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val same = UpdateTrackerModel.empty()

            val differences = current.getDifferences(same)

            assertEquals(0, differences.size)
        }
    }

    @Test
    fun testGetDifferencesRulebookVersionChange() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 1,
                announcements = -1,
                awards = -1,
                characters = -1,
                gear = -1,
                characterSkills = -1,
                contactRequests = -1,
                events = -1,
                eventAttendees = -1,
                preregs = -1,
                featureFlags = -1,
                intrigues = -1,
                players = -1,
                profileImages = -1,
                researchProjects = -1,
                skills = -1,
                skillCategories = -1,
                skillPrereqs = -1,
                xpReductions = -1,
                campStatus = -1,
                craftingRecipes = -1,
                rulebookVersion = "2206",  // Changed from "" to "2206"
                treatingWoundsVersion = ""
            )

            val differences = current.getDifferences(new)

            assertEquals(1, differences.size)
            assertTrue(differences.contains(DataManagerType.RULEBOOK))
        }
    }

    // ==================== UPDATE IN PLACE TESTS ====================

    @Test
    fun testUpdateInPlace() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 1,
                announcements = 5,
                awards = 10,
                characters = 15,
                gear = 20,
                characterSkills = 25,
                contactRequests = 30,
                events = 35,
                eventAttendees = 40,
                preregs = 45,
                featureFlags = 2,
                intrigues = 7,
                players = 12,
                profileImages = 3,
                researchProjects = 8,
                skills = 100,
                skillCategories = 5,
                skillPrereqs = 50,
                xpReductions = 15,
                campStatus = 1,
                craftingRecipes = 56,
                rulebookVersion = "2206",
                treatingWoundsVersion = "1.0"
            )

            // Update only specific fields
            val updates = listOf(DataManagerType.ANNOUNCEMENTS, DataManagerType.EVENTS)
            current.updateInPlace(new, updates)

            assertEquals(5, current.announcements)
            assertEquals(35, current.events)

            // Fields not in updates should remain -1
            assertEquals(-1, current.awards)
            assertEquals(-1, current.characters)
        }
    }

    @Test
    fun testUpdateInPlaceAllFields() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 1,
                announcements = 5,
                awards = 10,
                characters = 15,
                gear = 20,
                characterSkills = 25,
                contactRequests = 30,
                events = 35,
                eventAttendees = 40,
                preregs = 45,
                featureFlags = 2,
                intrigues = 7,
                players = 12,
                profileImages = 3,
                researchProjects = 8,
                skills = 100,
                skillCategories = 5,
                skillPrereqs = 50,
                xpReductions = 15,
                campStatus = 1,
                craftingRecipes = 56,
                rulebookVersion = "2206",
                treatingWoundsVersion = "1.0"
            )

            // Get all differences then update
            val differences = UpdateTrackerModel.empty().getDifferences(new)
            current.updateInPlace(new, differences)

            // All fields should now match new
            assertEquals(5, current.announcements)
            assertEquals(10, current.awards)
            assertEquals(15, current.characters)
            assertEquals(56, current.craftingRecipes)
            assertEquals("2206", current.rulebookVersion)
        }
    }

    // ==================== UPDATE TO NEW TESTS ====================

    @Test
    fun testUpdateToNew() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel(
                id = 1,
                announcements = 5,
                awards = 10,
                characters = 15,
                gear = 20,
                characterSkills = 25,
                contactRequests = 30,
                events = 35,
                eventAttendees = 40,
                preregs = 45,
                featureFlags = 2,
                intrigues = 7,
                players = 12,
                profileImages = 3,
                researchProjects = 8,
                skills = 100,
                skillCategories = 5,
                skillPrereqs = 50,
                xpReductions = 15,
                campStatus = 1,
                craftingRecipes = 56,
                rulebookVersion = "2206",
                treatingWoundsVersion = "1.0"
            )

            val new = UpdateTrackerModel(
                id = 2,
                announcements = 6,  // Changed
                awards = 10,
                characters = 15,
                gear = 20,
                characterSkills = 25,
                contactRequests = 30,
                events = 36,  // Changed
                eventAttendees = 40,
                preregs = 45,
                featureFlags = 2,
                intrigues = 7,
                players = 12,
                profileImages = 3,
                researchProjects = 8,
                skills = 100,
                skillCategories = 5,
                skillPrereqs = 50,
                xpReductions = 15,
                campStatus = 1,
                craftingRecipes = 56,
                rulebookVersion = "2206",
                treatingWoundsVersion = "1.0"
            )
            current.updateInPlace(new, listOf(DataManagerType.ANNOUNCEMENTS, DataManagerType.EVENTS))

            assertEquals(6, current.announcements)
            assertEquals(36, current.events)

            // Fields not in differences should be old values
            assertEquals(10, current.awards)
            assertEquals("2206", current.rulebookVersion)
        }
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testGetDifferencesEmptyVsEmpty() = runTest {
        loadDataManagerHappyPath(this) {
            val empty1 = UpdateTrackerModel.empty()
            val empty2 = UpdateTrackerModel.empty()

            val differences = empty1.getDifferences(empty2)
            assertEquals(0, differences.size)
        }
    }

    @Test
    fun testGetDifferencesAllChanged() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val allChanged = UpdateTrackerModel(
                id = 1,
                announcements = 1,
                awards = 2,
                characters = 3,
                gear = 4,
                characterSkills = 5,
                contactRequests = 6,
                events = 7,
                eventAttendees = 8,
                preregs = 9,
                featureFlags = 10,
                intrigues = 11,
                players = 12,
                profileImages = 13,
                researchProjects = 14,
                skills = 15,
                skillCategories = 16,
                skillPrereqs = 17,
                xpReductions = 18,
                campStatus = 19,
                craftingRecipes = 20,
                rulebookVersion = "2206",
                treatingWoundsVersion = "1.0"
            )

            val differences = current.getDifferences(allChanged)

            // Should have all DataManagerType entries except UPDATE_TRACKER
            assertEquals(22, differences.size) // 23 types - 1 for UPDATE_TRACKER
        }
    }

    @Test
    fun testUpdateInPlaceSkipsUpdateTracker() = runTest {
        loadDataManagerHappyPath(this) {
            val current = UpdateTrackerModel.empty()
            val new = UpdateTrackerModel(
                id = 999,
                announcements = 5,
                awards = -1,
                characters = -1,
                gear = -1,
                characterSkills = -1,
                contactRequests = -1,
                events = -1,
                eventAttendees = -1,
                preregs = -1,
                featureFlags = -1,
                intrigues = -1,
                players = -1,
                profileImages = -1,
                researchProjects = -1,
                skills = -1,
                skillCategories = -1,
                skillPrereqs = -1,
                xpReductions = -1,
                campStatus = -1,
                craftingRecipes = -1,
                rulebookVersion = "",
                treatingWoundsVersion = ""
            )

            // Even if we include UPDATE_TRACKER in the list, it should be skipped
            val updates = listOf(DataManagerType.UPDATE_TRACKER, DataManagerType.ANNOUNCEMENTS)
            current.updateInPlace(new, updates)

            // id should NOT be updated (UPDATE_TRACKER is skipped)
            assertEquals(-1, current.id)
            // But announcements should be updated
            assertEquals(5, current.announcements)
        }
    }
}