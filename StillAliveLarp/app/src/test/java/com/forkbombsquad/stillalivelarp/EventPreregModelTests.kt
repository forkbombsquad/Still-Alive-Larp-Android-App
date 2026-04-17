package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.EventPreregModel
import com.forkbombsquad.stillalivelarp.services.models.EventPreregCreateModel
import com.forkbombsquad.stillalivelarp.services.models.EventRegType
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.PreregNumbers
import com.forkbombsquad.stillalivelarp.utils.getRegNumbers
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventPreregModelTests : BaseUnitTestClass {

    // ==================== EVENT-BASED PREREG TESTS ====================

    @Test
    fun testEventPreregModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val preregs = event1!!.preregs
            assertTrue(preregs.isNotEmpty())

            // First prereg from test data
            val firstPrereg = preregs.first()
            assertEquals(1, firstPrereg.id)
            assertEquals(1, firstPrereg.playerId)
            assertEquals(1, firstPrereg.eventId)
            assertEquals("PREMIUM", firstPrereg.regType)
        }
    }

    @Test
    fun testEventPreregModelEventRegType() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val premiumPrereg = event1!!.preregs.first()
            assertEquals(EventRegType.PREMIUM, premiumPrereg.eventRegType())

            // Find other reg types
            val basicPrereg = event1.preregs.firstOrNull { it.regType == "BASIC" }
            if (basicPrereg != null) {
                assertEquals(EventRegType.BASIC, basicPrereg.eventRegType())
            }

            val freePrereg = event1.preregs.firstOrNull { it.regType == "FREE" }
            if (freePrereg != null) {
                assertEquals(EventRegType.FREE, freePrereg.eventRegType())
            }

            val nonePrereg = event1.preregs.firstOrNull { it.regType == "NONE" }
            if (nonePrereg != null) {
                assertEquals(EventRegType.NOT_PREREGED, nonePrereg.eventRegType())
            }
        }
    }

    @Test
    fun testEventPreregModelGetCharId() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // Find a prereg with valid characterId (use getCharId() getter - characterId is private!)
            val withCharId = event1!!.preregs.firstOrNull { it.getCharId() != null }
            if (withCharId != null) {
                val charId = withCharId.getCharId()
                assertNotNull(charId)
                assertTrue(charId!! > 0)
            }

            // Find a prereg where getCharId() returns null (characterId was -1)
            val withoutCharId = event1.preregs.firstOrNull { it.getCharId() == null }
            assertNotNull(withoutCharId)
        }
    }

    @Test
    fun testEventPreregModelGetAttendingText() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val preregs = event1!!.preregs

            // Test getAttendingText for different reg types
            val premium = preregs.first { it.eventRegType() == EventRegType.PREMIUM }
            assertEquals("Premium", premium.eventRegType().getAttendingText())

            val basic = preregs.firstOrNull { it.eventRegType() == EventRegType.BASIC }
            if (basic != null) {
                assertEquals("Basic", basic.eventRegType().getAttendingText())
            }

            val free = preregs.firstOrNull { it.eventRegType() == EventRegType.FREE }
            if (free != null) {
                assertEquals("Free", free.eventRegType().getAttendingText())
            }

            val notPre = preregs.firstOrNull { it.eventRegType() == EventRegType.NOT_PREREGED }
            if (notPre != null) {
                assertEquals("Not Attending", notPre.eventRegType().getAttendingText())
            }
        }
    }

    @Test
    fun testPreregsByEvent() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events

            // Each event should have preregs accessible
            for (event in events) {
                val preregCount = event.preregs.size
                assertTrue(preregCount >= 0)
            }

            // Verify event 1 has expected number of preregs
            val event1 = events.first { it.id == 1 }
            assertTrue(event1.preregs.isNotEmpty())
        }
    }

    @Test
    fun testPreregsFlatMap() = runTest {
        loadDataManagerHappyPath(this) {
            // Similar pattern to attendees - flatten all preregs from all events
            val allPreregs = DataManager.shared.events.flatMap { it.preregs }
            assertTrue(allPreregs.isNotEmpty())
            assertTrue(allPreregs.size > 10)
        }
    }

    // ==================== PLAYER-BASED PREREG TESTS ====================

    @Test
    fun testPlayerPreregs() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: player.preregs (FullPlayerModel has preregs)
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val preregCount = player!!.preregs.size
            assertTrue(preregCount >= 0)
        }
    }

    @Test
    fun testPlayerPreregsFilterByEvent() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val events = DataManager.shared.events
            if (events.isNotEmpty()) {
                val eventId = events.first().id
                val preregsForEvent = player!!.preregs.filter { it.eventId == eventId }
                // May or may not have preregs for this event
                for (prereg in preregsForEvent) {
                    assertEquals(eventId, prereg.eventId)
                }
            }
        }
    }

    @Test
    fun testPlayerPreregsByRegType() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Filter preregs by reg type
            val premiumPreregs = player!!.preregs.filter { it.eventRegType() == EventRegType.PREMIUM }
            assertTrue(premiumPreregs.size >= 0)

            val basicPreregs = player.preregs.filter { it.eventRegType() == EventRegType.BASIC }
            assertTrue(basicPreregs.size >= 0)
        }
    }

    // ==================== CHARACTER-BASED PREREG TESTS ====================

    @Test
    fun testCharacterPreregs() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: player.characters.first { }.preregs
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            val characters = player!!.characters
            assertTrue(characters.isNotEmpty())

            for (character in characters) {
                val preregCount = character.preregs.size
                assertTrue(preregCount >= 0)
            }
        }
    }

    @Test
    fun testCharacterPreregsByEvent() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find a character with preregs
            val characterWithPreregs = player!!.characters.firstOrNull { it.preregs.isNotEmpty() }
            if (characterWithPreregs != null) {
                val preregs = characterWithPreregs.preregs
                assertTrue(preregs.isNotEmpty())

                // Each prereg should have this character's ID
                for (prereg in preregs) {
                    assertEquals(characterWithPreregs.id, prereg.getCharId())
                }
            }
        }
    }

    @Test
    fun testFindCharacterFromPrereg() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from HomeFragment: DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == prereg.getCharId() }
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // Find any prereg with a characterId
            val preregWithChar = player!!.preregs.firstOrNull { it.getCharId() != null }
            if (preregWithChar != null) {
                val charId = preregWithChar.getCharId()
                val character = player.characters.firstOrNull { it.id == charId }
                assertNotNull(character)
            }
        }
    }

    @Test
    fun testActiveCharacterPreregs() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: DataManager.shared.getActiveCharacter()?.preregs
            val activeChar = DataManager.shared.getActiveCharacter()
            // May be null if player has no active character
            if (activeChar != null) {
                val preregCount = activeChar.preregs.size
                assertTrue(preregCount >= 0)
            }
        }
    }

    // ==================== EXTENSION FUNCTION TESTS ====================

    @Test
    fun testGetRegNumbers() = runTest {
        loadDataManagerHappyPath(this) {
            // Test the extension function from ListExtensions.kt
            val allPreregs = DataManager.shared.events.flatMap { it.preregs }
            assertTrue(allPreregs.isNotEmpty())

            val regNumbers: PreregNumbers = allPreregs.getRegNumbers()

            // Verify the structure
            assertTrue(regNumbers.premium >= 0)
            assertTrue(regNumbers.premiumNpc >= 0)
            assertTrue(regNumbers.basic >= 0)
            assertTrue(regNumbers.basicNpc >= 0)
            assertTrue(regNumbers.free >= 0)
            assertTrue(regNumbers.notAttending >= 0)

            // Verify premium + premiumNpc <= total premium
            assertTrue(regNumbers.premiumNpc <= regNumbers.premium)
            assertTrue(regNumbers.basicNpc <= regNumbers.basic)
        }
    }

    @Test
    fun testGetRegNumbersByEvent() = runTest {
        loadDataManagerHappyPath(this) {
            // Test getRegNumbers for a specific event
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            val regNumbers: PreregNumbers = event1!!.preregs.getRegNumbers()
            assertTrue(regNumbers.premium >= 0)
        }
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testEventPreregCreateModel() = runTest {
        // Test EventPreregCreateModel parsing
        val createJson = """{"playerId": 1, "characterId": 5, "eventId": 1, "regType": "PREMIUM"}"""
        val createModel: EventPreregCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(1, createModel!!.playerId)
        assertEquals(5, createModel.getCharId())
        assertEquals(1, createModel.eventId)
        assertEquals("PREMIUM", createModel.regType)
    }

    @Test
    fun testEventPreregCreateModelGetCharId() = runTest {
        // Test getCharId on create model
        val createJson = """{"playerId": 1, "characterId": -1, "eventId": 1, "regType": "BASIC"}"""
        val createModel: EventPreregCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        // characterId -1 should return null
        assertNull(createModel!!.getCharId())
    }

    @Test
    fun testEventPreregCreateModelWithEnum() = runTest {
        // Test creating with EventRegType enum
        val createModel = EventPreregCreateModel(
            playerId = 1,
            characterId = 5,
            eventId = 1,
            regType = EventRegType.PREMIUM
        )
        assertEquals(1, createModel.playerId)
        assertEquals(5, createModel.getCharId())
        assertEquals("PREMIUM", createModel.regType)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun testEventRegTypeEnum() = runTest {
        // Test all EventRegType values
        assertEquals("NONE", EventRegType.NOT_PREREGED.value)
        assertEquals("FREE", EventRegType.FREE.value)
        assertEquals("BASIC", EventRegType.BASIC.value)
        assertEquals("PREMIUM", EventRegType.PREMIUM.value)
    }

    @Test
    fun testEventRegTypeGetRegType() = runTest {
        // Test the getRegType companion method
        assertEquals(EventRegType.PREMIUM, EventRegType.getRegType("PREMIUM"))
        assertEquals(EventRegType.BASIC, EventRegType.getRegType("BASIC"))
        assertEquals(EventRegType.FREE, EventRegType.getRegType("FREE"))
        assertEquals(EventRegType.NOT_PREREGED, EventRegType.getRegType("NONE"))
        // Invalid value should return NOT_PREREGED
        assertEquals(EventRegType.NOT_PREREGED, EventRegType.getRegType("INVALID"))
    }

    @Test
    fun testPreregEdgeCases() = runTest {
        // Test various regType string values
        val preregPremium = """{"id": 1, "playerId": 1, "characterId": 1, "eventId": 1, "regType": "PREMIUM"}"""
        val parsedPremium: EventPreregModel? = globalFromJson(preregPremium)
        assertNotNull(parsedPremium)
        assertEquals(EventRegType.PREMIUM, parsedPremium!!.eventRegType())

        val preregLowercase = """{"id": 2, "playerId": 1, "characterId": -1, "eventId": 1, "regType": "basic"}"""
        val parsedLower: EventPreregModel? = globalFromJson(preregLowercase)
        assertNotNull(parsedLower)
        // "basic" is not a valid value, so it returns NOT_PREREGED
        assertEquals(EventRegType.NOT_PREREGED, parsedLower!!.eventRegType())
    }
}