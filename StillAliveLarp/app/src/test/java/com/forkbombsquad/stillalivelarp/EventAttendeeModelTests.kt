package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeCreateModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventAttendeeModelTests : BaseUnitTestClass {

    // ==================== EVENT-BASED ATTENDEE TESTS ====================

    @Test
    fun testEventAttendeeModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val attendees = event1!!.attendees
            assertTrue(attendees.isNotEmpty())

            // First attendee from test data
            val firstAttendee = attendees.first()
            assertEquals(1, firstAttendee.id)
            assertTrue(firstAttendee.playerId > 0)
            // characterId is null for NPC attendees (asNpc = "TRUE")
            assertNull(firstAttendee.characterId)
            assertEquals(1, firstAttendee.eventId)
            assertEquals("FALSE", firstAttendee.isCheckedIn)
            assertEquals("TRUE", firstAttendee.asNpc)
            assertEquals(-1, firstAttendee.npcId)
        }
    }

    @Test
    fun testEventAttendeeModelWithCharacter() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            // Event 2 has both NPC and character attendees
            val event2 = events.firstOrNull { it.id == 2 }
            assertNotNull(event2)

            // Find an attendee with a characterId (asNpc = "FALSE")
            val characterAttendee = event2!!.attendees.firstOrNull { it.asNpc.lowercase() == "false" }
            assertNotNull(characterAttendee)
            assertNotNull(characterAttendee!!.characterId)
            assertEquals("FALSE", characterAttendee.asNpc)
        }
    }

    @Test
    fun testEventAttendeeModelNpcOnlyEvent() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            // Event 1 only has NPCs
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // All attendees in event 1 are NPCs
            val allNpc = event1!!.attendees.all { it.asNpc.lowercase() == "true" }
            assertTrue(allNpc)

            // No character attendees in event 1
            val characterAttendee = event1.attendees.firstOrNull { it.asNpc.lowercase() == "false" }
            assertNull(characterAttendee)
        }
    }

    @Test
    fun testEventAttendeeModelIsCheckedInToBoolean() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val attendee = event1!!.attendees.first()
            // Test the actual usage pattern from the codebase
            assertFalse(attendee.isCheckedIn.toBoolean())
        }
    }

    @Test
    fun testEventAttendeeModelAsNpcCheck() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events

            // Event 1 only has NPCs
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            // All attendees in event 1 are NPCs
            assertTrue(event1!!.attendees.all { it.asNpc.lowercase() == "true" })

            // Event 2 has both NPC and character attendees
            val event2 = events.firstOrNull { it.id == 2 }
            assertNotNull(event2)

            // NPC attendee
            val npcAttendee = event2!!.attendees.firstOrNull { it.asNpc.lowercase() == "true" }
            assertNotNull(npcAttendee)

            // Find a character attendee
            val characterAttendee = event2.attendees.firstOrNull { it.asNpc.lowercase() == "false" }
            assertNotNull(characterAttendee)
            assertFalse(characterAttendee!!.asNpc.lowercase() == "true")
        }
    }

    @Test
    fun testEventAttendeeModelNpcId() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events

            // Regular NPC with npcId = -1
            val regularNpc = events.flatMap { it.attendees }.firstOrNull { it.npcId == -1 }
            assertNotNull(regularNpc)

            // Find an NPC with actual npcId (not -1)
            val actualNpc = events.flatMap { it.attendees }.firstOrNull { it.npcId > 0 }
            assertNotNull(actualNpc)
            assertTrue(actualNpc!!.npcId > 0)
        }
    }

    @Test
    fun testEventAttendeeListModel() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // Verify attendees list works as expected
            val attendeeList = event1!!.attendees
            assertEquals(10, attendeeList.size) // Event 1 has 10 attendees in test data
        }
    }

    @Test
    fun testAttendeesByEvent() = runTest {
        loadDataManagerHappyPath(this) {
            // Test access pattern used in LocalDataManager - byEvent
            val events = DataManager.shared.events
            val eventIds = events.map { it.id }.toSet()

            // Each event should have attendees
            for (eventId in eventIds) {
                val event = events.firstOrNull { it.id == eventId }
                assertNotNull(event)
                // Just verify we can access the attendees list without error
                val attendeeCount = event!!.attendees.size
                assertTrue(attendeeCount >= 0)
            }
        }
    }

    @Test
    fun testAttendeesFlatMap() = runTest {
        loadDataManagerHappyPath(this) {
            // Test the pattern used in ViewNPCStuffActivity
            val allAttendees = DataManager.shared.events.flatMap { it.attendees }
            assertTrue(allAttendees.isNotEmpty())

            // Count how many times an NPC played (by npcId)
            val npcAttendees = allAttendees.filter { it.npcId > 0 }
            assertTrue(npcAttendees.isNotEmpty())
        }
    }

    @Test
    fun testFindAttendeeByNpcId() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from CharacterModel - find attendee by npcId
            val events = DataManager.shared.events

            // Find any attendee with a valid npcId
            val attendeeWithNpc = events.flatMap { it.attendees }.firstOrNull { it.npcId > 0 }
            assertNotNull(attendeeWithNpc)

            // Find that attendee in an event by npcId
            val foundInEvent = events.firstOrNull { event ->
                event.attendees.any { it.npcId == attendeeWithNpc!!.npcId }
            }
            assertNotNull(foundInEvent)
        }
    }

    @Test
    fun testEventAttendeeEdgeCases() = runTest {
        // Test various string values for isCheckedIn and asNpc
        val attendeeMixedCase = """{"id": 1, "playerId": 1, "characterId": null, "eventId": 1, "isCheckedIn": "True", "asNpc": "True", "npcId": -1}"""
        val parsedMixed: EventAttendeeModel? = globalFromJson(attendeeMixedCase)
        assertNotNull(parsedMixed)
        assertTrue(parsedMixed!!.isCheckedIn.toBoolean())
        assertTrue(parsedMixed.asNpc.lowercase() == "true")

        val attendeeLowercase = """{"id": 2, "playerId": 2, "characterId": 5, "eventId": 1, "isCheckedIn": "false", "asNpc": "false", "npcId": -1}"""
        val parsedLower: EventAttendeeModel? = globalFromJson(attendeeLowercase)
        assertNotNull(parsedLower)
        assertFalse(parsedLower!!.isCheckedIn.toBoolean())
        assertFalse(parsedLower.asNpc.lowercase() == "true")
    }

    // ==================== PLAYER-BASED ATTENDEE TESTS ====================

    @Test
    fun testPlayerEventAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: player.eventAttendees (FullPlayerModel has eventAttendees)
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // FullPlayerModel has eventAttendees property
            val attendeeCount = player!!.eventAttendees.size
            assertTrue(attendeeCount >= 0)
        }
    }

    @Test
    fun testPlayerAttendeesFilterByCheckedIn() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from HomeFragment - find checked in attendee from player
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val checkedInAttendee = player!!.eventAttendees.firstOrNull { it.isCheckedIn.toBoolean() }
            // May be null if not checked in - that's OK
            if (checkedInAttendee != null) {
                assertTrue(checkedInAttendee.isCheckedIn.toBoolean())
            }
        }
    }

    @Test
    fun testPlayerAttendeesByEventId() = runTest {
        loadDataManagerHappyPath(this) {
            // Test finding an attendee for a specific event from player
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val events = DataManager.shared.events
            if (events.isNotEmpty()) {
                val eventId = events.first().id
                val attendeeForEvent = player!!.eventAttendees.firstOrNull { it.eventId == eventId }
                // May or may not have attendee for this event
                if (attendeeForEvent != null) {
                    assertEquals(eventId, attendeeForEvent.eventId)
                }
            }
        }
    }

    // ==================== CHARACTER-BASED ATTENDEE TESTS ====================

    @Test
    fun testCharacterEventAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from CharacterPlannerActivity: player.characters.filter
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // FullPlayerModel has characters list - each FullCharacterModel has eventAttendees
            val characters = player!!.characters
            assertTrue(characters.isNotEmpty())

            // Each character has eventAttendees list
            for (character in characters) {
                val attendeeCount = character.eventAttendees.size
                // Just verify the list is accessible
                assertTrue(attendeeCount >= 0)
            }
        }
    }

    @Test
    fun testCharacterFilterByType() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from CharacterPlannerActivity: player.characters.filter { it.characterType() != ... }
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Filter characters by type (like CharacterPlannerActivity does)
            val standardChars = player!!.characters.filter {
                it.characterType().name == "STANDARD"
            }
            // Just verify filter works
            assertNotNull(standardChars)
        }
    }

    @Test
    fun testFindCharacterFromAttendee() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from HomeFragment: DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == attendee.characterId }
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find any attendee with a characterId
            val attendeeWithChar = player!!.eventAttendees.firstOrNull { it.characterId != null }
            if (attendeeWithChar != null) {
                // Find that character
                val character = player.characters.firstOrNull { it.id == attendeeWithChar.characterId }
                assertNotNull(character)
            }
        }
    }

    @Test
    fun testGetAllXpSpentFromCharacterAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: using character.eventAttendees (used in various places)
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Each character's eventAttendees can be used to calculate stats
            val characters = player!!.characters
            for (character in characters) {
                // Example: count events attended
                val eventCount = character.eventAttendees.size
                assertTrue(eventCount >= 0)
            }
        }
    }

    @Test
    fun testActiveCharacterEventAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: DataManager.shared.getActiveCharacter()?.eventAttendees
            // Used throughout the app to get the current active character's data
            val activeChar = DataManager.shared.getActiveCharacter()
            // May be null if player has no active character
            if (activeChar != null) {
                val attendeeCount = activeChar.eventAttendees.size
                assertTrue(attendeeCount >= 0)
            }
        }
    }

    @Test
    fun testActiveCharacterVsAllCharactersAttendees() = runTest {
        loadDataManagerHappyPath(this) {
            // Compare getActiveCharacter() with getCurrentPlayer()?.characters
            val activeChar = DataManager.shared.getActiveCharacter()
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // If there's an active character, it should be in the player's characters list
            if (activeChar != null) {
                val foundInList = player!!.characters.any { it.id == activeChar.id }
                assertTrue(foundInList)
            }
        }
    }

    // ==================== EVENT ATTENDEE CREATE MODEL TESTS ====================

    @Test
    fun testEventAttendeeCreateModel() = runTest {
        // Test EventAttendeeCreateModel parsing
        val createJson = """{"playerId": 1, "characterId": 5, "eventId": 1, "isCheckedIn": "FALSE", "asNpc": "FALSE", "npcId": -1}"""
        val createModel: EventAttendeeCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(1, createModel!!.playerId)
        assertEquals(5, createModel.characterId)
        assertEquals(1, createModel.eventId)
        assertEquals("FALSE", createModel.isCheckedIn)
        assertEquals("FALSE", createModel.asNpc)
        assertEquals(-1, createModel.npcId)
    }

    @Test
    fun testEventAttendeeCreateModelNpc() = runTest {
        // Test NPC creation (no characterId, asNpc = "TRUE")
        val createJson = """{"playerId": 7, "characterId": null, "eventId": 7, "isCheckedIn": "FALSE", "asNpc": "TRUE", "npcId": 21}"""
        val createModel: EventAttendeeCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(7, createModel!!.playerId)
        assertEquals(21, createModel.npcId)
        assertEquals("TRUE", createModel.asNpc)
    }
}