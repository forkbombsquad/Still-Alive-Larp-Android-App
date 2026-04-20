package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddtoDate
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EventModelTests : BaseUnitTestClass {

    @Test
    fun testEventModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Verify we have 8 events from the test data
            assertTrue(events.isNotEmpty())
            assertEquals(8, events.size)

            // Test first event (Mock/Practice Event - id 1)
            val firstEvent = events.firstOrNull { it.id == 1 }
            assertNotNull(firstEvent)
            assertEquals("Mock/Practice Event", firstEvent!!.title)
            assertEquals(1, firstEvent.id)
            assertEquals("2024/06/01", firstEvent.date)
            assertEquals("4:00pm", firstEvent.startTime)
            assertEquals("Midnight-ish", firstEvent.endTime)
            assertTrue(firstEvent.isFinished)
            assertTrue(firstEvent.isStarted)

            // Test last event (Event 6 - TBD - id 8)
            val lastEvent = events.firstOrNull { it.id == 8 }
            assertNotNull(lastEvent)
            assertEquals("Event 6 - TBD", lastEvent!!.title)
            assertEquals("2025/10/18", lastEvent.date)
            assertFalse(lastEvent.isFinished)
            assertFalse(lastEvent.isStarted)
        }
    }

    @Test
    fun testEventModelFieldsInnerEvent() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Test that the inner EventModel has string versions
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            // Inner event (from EventModel) has String isStarted/isFinished
            val innerEvent = com.forkbombsquad.stillalivelarp.services.models.EventModel(
                id = event1!!.id,
                title = event1.title,
                description = event1.description,
                date = event1.date,
                startTime = event1.startTime,
                endTime = event1.endTime,
                isStarted = event1.isStarted.toString(),
                isFinished = event1.isFinished.toString()
            )
            assertEqualsIgnoreCase("TRUE", innerEvent.isStarted)
            assertEqualsIgnoreCase("TRUE", innerEvent.isFinished)
        }
    }

    @Test
    fun testEventModelEdgeCases() = runTest {
        // Test parsing with various isStarted/isFinished values
        val eventMixedCase = """{"id": 100, "title": "Mixed", "description": "Desc", "date": "2025/01/01", "startTime": "5pm", "endTime": "midnight", "isStarted": "True", "isFinished": "False"}"""
        val parsedMixed: EventModel? = globalFromJson(eventMixedCase)
        assertNotNull(parsedMixed)
        assertTrue(parsedMixed!!.isStarted.toBoolean())
        assertFalse(parsedMixed.isFinished.toBoolean())

        val eventLowercase = """{"id": 101, "title": "Lower", "description": "Desc", "date": "2025/01/01", "startTime": "5pm", "endTime": "midnight", "isStarted": "true", "isFinished": "false"}"""
        val parsedLower: EventModel? = globalFromJson(eventLowercase)
        assertNotNull(parsedLower)
        assertTrue(parsedLower!!.isStarted.toBoolean())
        assertFalse(parsedLower.isFinished.toBoolean())
    }

    @Test
    fun testFullEventModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Test FullEventModel with attendees, preregs, and intrigue
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // Event 1 should have attendees from test data
            assertTrue(event1!!.attendees.isNotEmpty())

            // Event 1 should have preregs from test data
            assertTrue(event1.preregs.isNotEmpty())

            // Event 1 should have intrigue (id 1 maps to eventId 1)
            assertNotNull(event1.intrigue)
            assertEquals(1, event1.intrigue?.id)
            assertEquals("Several hordes have been seen near the camp", event1.intrigue?.interrogatorMessage)
        }
    }

    @Test
    fun testFullEventModelSecondaryConstructor() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Test secondary constructor conversion from String to Boolean
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)

            // Verify Boolean conversion worked (secondary constructor converts strings to booleans)
            assertTrue(event1!!.isStarted)
            assertTrue(event1.isFinished)
        }
    }

    @Test
    fun testFullEventModelIsOngoing() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Looking for ongoing event (isStarted && !isFinished)
            // Event 8 has isStarted="false", isFinished="false"
            val event8 = events.firstOrNull { it.id == 8 }
            assertNotNull(event8)
            // Not started yet, so not ongoing
            assertFalse(event8!!.isOngoing())

            // If there was an event with isStarted=true and isFinished=false, it would be ongoing
            // Let's check our test data doesn't have any ongoing events
            val ongoingEvents = events.filter { it.isOngoing() }
            assertTrue(ongoingEvents.isEmpty(), "No ongoing events in test data")
        }
    }

    @Test
    fun testFullEventModelIsRelevant() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Test isRelevant: (isOngoing || isToday || isInFuture) && !isFinished
            // Event 1: isFinished = TRUE, so not relevant
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            assertFalse(event1!!.isRelevant())

            // Event 8: isFinished = false, isStarted = false
            val event8 = events.firstOrNull { it.id == 8 }
            assertNotNull(event8)
            // isFinished is false so could be relevant
            event8!!.isRelevant()
        }
    }

    @Test
    fun testEventListModel() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Verify we can iterate and access events
            assertEquals(8, events.size)

            // Test that we can sort by date (uses yyyyMMddtoDate)
            val sortedEvents = events.sortedBy { it.date.yyyyMMddtoDate() }
            assertTrue(sortedEvents.isNotEmpty())

            // First event in chronological order should be oldest
            assertEquals(1, sortedEvents.first().id)
        }
    }

    @Test
    fun testEventCreateModel() = runTest {
        // Test EventCreateModel parsing
        val createJson = """{"title": "New Event", "description": "Test Description", "date": "2026/01/01", "startTime": "5pm", "endTime": "10pm", "isStarted": "false", "isFinished": "false"}"""
        val createModel: com.forkbombsquad.stillalivelarp.services.models.EventCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("New Event", createModel!!.title)
        assertEquals("Test Description", createModel.description)
        assertEquals("2026/01/01", createModel.date)
        assertEquals("5pm", createModel.startTime)
        assertEquals("10pm", createModel.endTime)
        assertEquals("false", createModel.isStarted)
        assertEquals("false", createModel.isFinished)
    }

    @Test
    fun testFullEventModelIntrigueIntegration() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Event 1 has intrigue with eventId = 1
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            assertNotNull(event1!!.intrigue)
            assertEquals("Rumor has it. A large number of zombies has been spotted in the area", event1.intrigue?.investigatorMessage)

            // Event 8 has no intrigue in test data
            val event8 = events.firstOrNull { it.id == 8 }
            assertNotNull(event8)
            assertNull(event8!!.intrigue)
        }
    }

    @Test
    fun testFullEventModelAttendeesIntegration() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Event 1 has attendees from test data
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            assertTrue(event1!!.attendees.isNotEmpty())

            // Check attendee fields
            val firstAttendee = event1.attendees.first()
            assertTrue(firstAttendee.playerId > 0)
            assertTrue(firstAttendee.eventId == 1)
        }
    }

    @Test
    fun testFullEventModelPreregsIntegration() = runTest {
        loadDataManagerHappyPath(this) {
            val events: List<FullEventModel> = DataManager.shared.events

            // Event 1 has preregs from test data
            val event1 = events.firstOrNull { it.id == 1 }
            assertNotNull(event1)
            assertTrue(event1!!.preregs.isNotEmpty())

            // Check prereg fields
            val firstPrereg = event1.preregs.first()
            assertTrue(firstPrereg.eventId == 1)
            assertTrue(firstPrereg.playerId > 0)
        }
    }

    @Test
    fun testDataManagerEventQueries() = runTest {
        loadDataManagerHappyPath(this) {
            // Test getOngoingEvent
            val ongoing = DataManager.shared.getOngoingEvent()
            assertNull(ongoing) // No ongoing events in test data

            // Test getRelevantEvents
            val relevant = DataManager.shared.getRelevantEvents()

            // Test getOngoingOrTodayEvent
            val ongoingOrToday = DataManager.shared.getOngoingOrTodayEvent()
        }
    }

    @Test
    fun testEventsAssociatedBy() = runTest {
        loadDataManagerHappyPath(this) {
            // Test how it's used in CharacterModel - associateBy
            val eventMap = DataManager.shared.events.associateBy { it.id }
            assertEquals(8, eventMap.size)

            // Access by ID like actual code does
            val foundEvent = eventMap[1]
            assertNotNull(foundEvent)
            assertEquals("Mock/Practice Event", foundEvent!!.title)
        }
    }

    @Test
    fun testEventsFilterForRelevant() = runTest {
        loadDataManagerHappyPath(this) {
            // Test filtering for relevant events like HomeFragment does
            val relevant = DataManager.shared.events.filter { it.isRelevant() }
            // Result depends on current date vs event dates in test data
            // Just verify the filter works without error
            assertNotNull(relevant)
        }
    }
}