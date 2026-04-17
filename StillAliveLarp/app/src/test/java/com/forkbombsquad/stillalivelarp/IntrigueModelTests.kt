package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueCreateModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IntrigueModelTests : BaseUnitTestClass {

    // ==================== FULL EVENT INTRIGUE TESTS ====================

    @Test
    fun testIntrigueModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // Event 1 has an intrigue
            val intrigue = event1!!.intrigue
            assertNotNull(intrigue)

            assertEquals(1, intrigue!!.id)
            assertEquals(1, intrigue.eventId)
            assertEquals("Several hordes have been seen near the camp", intrigue.interrogatorMessage)
            assertEquals("Rumor has it. A large number of zombies has been spotted in the area", intrigue.investigatorMessage)
            assertEquals("A large number of resources and cashes can be found nearby", intrigue.webOfInformantsMessage)
        }
    }

    @Test
    fun testIntrigueModelOptionalFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            // Find an event where webOfInformantsMessage might be empty
            val event3 = events.firstOrNull { it.id == 3 }
            assertNotNull(event3)

            val intrigue = event3!!.intrigue
            assertNotNull(intrigue)
            // webOfInformantsMessage is empty in event 3
            assertEquals("", intrigue!!.webOfInformantsMessage)
        }
    }

    @Test
    fun testEventWithoutIntrigue() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events
            // Event 8 has no intrigue in test data
            val event8 = events.firstOrNull { it.id == 8 }
            assertNotNull(event8)
            assertNull(event8!!.intrigue)
        }
    }

    @Test
    fun testAllEventsWithIntrigue() = runTest {
        loadDataManagerHappyPath(this) {
            val events = DataManager.shared.events

            // Count events with and without intrigue
            val eventsWithIntrigue = events.filter { it.intrigue != null }
            val eventsWithoutIntrigue = events.filter { it.intrigue == null }

            assertTrue(eventsWithIntrigue.isNotEmpty())
            assertTrue(eventsWithoutIntrigue.isNotEmpty())

            // Verify all events with intrigue have valid data
            for (event in eventsWithIntrigue) {
                assertNotNull(event.intrigue!!.id)
                assertEquals(event.id, event.intrigue!!.eventId)
            }
        }
    }

    @Test
    fun testIntrigueAccessViaGetOngoingOrTodayEvent() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from HomeFragment: DataManager.shared.getOngoingOrTodayEvent()?.intrigue
            val ongoingOrTodayEvent = DataManager.shared.getOngoingOrTodayEvent()
            // May be null depending on current date vs test data dates
            if (ongoingOrTodayEvent != null) {
                // If there's an ongoing/today event, it may or may not have intrigue
                ongoingOrTodayEvent.intrigue?.let { intrigue ->
                    assertNotNull(intrigue.id)
                }
            }
        }
    }

    // ==================== DATA MANAGER INTRIGUES MAP TESTS ====================

    @Test
    fun testDataManagerIntriguesMap() = runTest {
        loadDataManagerHappyPath(this) {
            // Test direct access to DataManager.shared.intrigues (Map<Int, IntrigueModel>)
            val intrigues = DataManager.shared.intrigues
            assertTrue(intrigues.isNotEmpty())

            // Map is keyed by eventId
            val intrigueForEvent1 = intrigues[1]
            assertNotNull(intrigueForEvent1)
            assertEquals(1, intrigueForEvent1!!.eventId)
        }
    }

    @Test
    fun testIntriguesMapLookup() = runTest {
        loadDataManagerHappyPath(this) {
            val intrigues = DataManager.shared.intrigues

            // Test looking up intrigue by eventId
            for (eventId in 1..7) {
                val intrigue = intrigues[eventId]
                if (intrigue != null) {
                    assertEquals(eventId, intrigue.eventId)
                }
            }

            // Event 8 should not have an intrigue
            val noIntrigue = intrigues[8]
            assertNull(noIntrigue)
        }
    }

    @Test
    fun testIntriguesMapValues() = runTest {
        loadDataManagerHappyPath(this) {
            val intrigues = DataManager.shared.intrigues

            // Access all intrigues from the map
            val allIntrigueList = intrigues.values.toList()
            assertTrue(allIntrigueList.isNotEmpty())
            assertEquals(7, allIntrigueList.size) // Events 1-7 have intrigues

            // Each should have valid data
            for (intrigue in allIntrigueList) {
                assertTrue(intrigue.id > 0)
                assertTrue(intrigue.eventId > 0)
            }
        }
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testIntrigueCreateModel() = runTest {
        // Test IntrigueCreateModel parsing
        val createJson = """{"eventId": 1, "investigatorMessage": "Test Investigator", "interrogatorMessage": "Test Interrogator", "webOfInformantsMessage": "Test Web"}"""
        val createModel: IntrigueCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals(1, createModel!!.eventId)
        assertEquals("Test Investigator", createModel.investigatorMessage)
        assertEquals("Test Interrogator", createModel.interrogatorMessage)
        assertEquals("Test Web", createModel.webOfInformantsMessage)
    }

    @Test
    fun testIntrigueCreateModelWithNullWebOfInformants() = runTest {
        // Test with null/empty webOfInformantsMessage
        val createJson = """{"eventId": 1, "investigatorMessage": "Test", "interrogatorMessage": "Test", "webOfInformantsMessage": ""}"""
        val createModel: IntrigueCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("", createModel!!.webOfInformantsMessage)
    }

    // ==================== INTRIGUE LIST MODEL TESTS ====================

    @Test
    fun testIntrigueListModel() = runTest {
        loadDataManagerHappyPath(this) {
            // Test that we can iterate all intrigues from events
            val allIntrigues = DataManager.shared.events.mapNotNull { it.intrigue }
            assertTrue(allIntrigues.isNotEmpty())
            assertTrue(allIntrigues.size >= 7)
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun testIntrigueModelParsing() = runTest {
        // Test parsing intrigue directly from JSON
        val intrigueJson = """{"id": 100, "eventId": 10, "investigatorMessage": "Test message", "interrogatorMessage": "Another message", "webOfInformantsMessage": "Web message"}"""
        val parsed: IntrigueModel? = globalFromJson(intrigueJson)
        assertNotNull(parsed)
        assertEquals(100, parsed!!.id)
        assertEquals(10, parsed.eventId)
        assertEquals("Test message", parsed.investigatorMessage)
        assertEquals("Another message", parsed.interrogatorMessage)
        assertEquals("Web message", parsed.webOfInformantsMessage)
    }

    @Test
    fun testIntrigueWithEmptyMessages() = runTest {
        // Test with empty message fields
        val intrigueJson = """{"id": 101, "eventId": 11, "investigatorMessage": "", "interrogatorMessage": "", "webOfInformantsMessage": ""}"""
        val parsed: IntrigueModel? = globalFromJson(intrigueJson)
        assertNotNull(parsed)
        assertEquals("", parsed!!.investigatorMessage)
        assertEquals("", parsed.interrogatorMessage)
        assertEquals("", parsed.webOfInformantsMessage)
    }
}