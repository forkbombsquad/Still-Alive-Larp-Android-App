package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AnnouncementModelTests: BaseUnitTestClass {

    @Test
    fun testAnnouncementModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val announcements = DataManager.shared.announcements

            // Verify we have 5 announcements from the test data
            assertTrue(announcements.isNotEmpty())
            assertEquals(5, announcements.size)

            // Test the first announcement (appears last when reversed)
            val firstAnnouncement = announcements.first()
            assertEquals(1, firstAnnouncement.id)
            assertEquals("We're Back!", firstAnnouncement.title)
            assertTrue(firstAnnouncement.text.startsWith("We are pleased to announce"))
            assertEquals("2023/06/03", firstAnnouncement.date)

            // Test a middle announcement
            val middleAnnouncement = announcements[2]
            assertEquals(3, middleAnnouncement.id)
            assertEquals("Events For The 2025 Season Have Been Scheduled!", middleAnnouncement.title)
            assertEquals("2024/10/04", middleAnnouncement.date)

            // Test the last announcement (appears first when reversed)
            val lastAnnouncement = announcements.last()
            assertEquals(5, lastAnnouncement.id)
            assertEquals("Rescheduling Event 6 And August Workday", lastAnnouncement.title)
            assertTrue(lastAnnouncement.text.contains("August 2nd, 2025"))
            assertEquals("2025/07/25", lastAnnouncement.date)
        }
    }

    @Test
    fun testAnnouncementsInDataManager() = runTest {
        loadDataManagerHappyPath(this) {
            // Test that announcements are properly stored in DataManager
            val dmt = DataManager.shared

            // Verify announcements are accessible
            assertNotNull(dmt.announcements)
            assertTrue(dmt.announcements.isNotEmpty())

            // Verify the flow also works
            val announcementsFlow = dmt.announcementsFlow
            assertNotNull(announcementsFlow)
            assertTrue(announcementsFlow.value.isNotEmpty())

            // Verify they're the same
            assertEquals(dmt.announcements, announcementsFlow.value)
        }
    }

    @Test
    fun testAnnouncementReversedOrder() = runTest {
        loadDataManagerHappyPath(this) {
            // HomeFragment displays announcements in reversed order (newest first)
            // Using sortedByDescending on id to avoid reversed() issues
            val originalList = DataManager.shared.announcements
            val announcements = originalList.sortedByDescending { it.id }

            // First (most recent) should be id 5
            val mostRecent = announcements.first()
            assertEquals(5, mostRecent.id)
            assertEquals("Rescheduling Event 6 And August Workday", mostRecent.title)

            // Last (oldest) should be id 1
            val oldest = announcements.last()
            assertEquals(1, oldest.id)
            assertEquals("We're Back!", oldest.title)
        }
    }

    @Test
    fun testAnnouncementDateFormatting() = runTest {
        loadDataManagerHappyPath(this) {
            val announcement = DataManager.shared.announcements.first()

            // Verify the date string format is yyyy/MM/dd (used in the JSON)
            val date = announcement.date
            assertEquals(10, date.length) // "2023/06/03" is 10 chars
            assertTrue(date.matches(Regex("\\d{4}/\\d{2}/\\d{2}")))

            // Verify parts
            val parts = date.split("/")
            assertEquals(3, parts.size)
            assertEquals("2023", parts[0]) // year
            assertEquals("06", parts[1])   // month
            assertEquals("03", parts[2])   // day
        }
    }

    @Test
    fun testAnnouncementEdgeCaseEmptyText() = runTest {
        // Test that the model can handle edge cases like empty text
        // This is more of a model structure test - in real usage, we'd expect
        // announcements to have content, but the model should still deserialize correctly
        val json = """{"id": 999, "title": "Test", "text": "", "date": "2025/01/01"}"""
        val announcement: com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel? = globalFromJson(json)

        assertNotNull(announcement)
        assertEquals(999, announcement!!.id)
        assertEquals("Test", announcement.title)
        assertEquals("", announcement.text)
        assertEquals("2025/01/01", announcement.date)
    }

    @Test
    fun testAnnouncementEdgeCaseLongContent() = runTest {
        // Test handling of long announcement text (like the work day announcement)
        val announcements = DataManager.shared.announcements
        val longTextAnnouncement = announcements.firstOrNull { it.id == 5 }

        assertNotNull(longTextAnnouncement)
        // This announcement has multiple paragraphs and is quite long
        assertTrue(longTextAnnouncement!!.text.length > 200)
        assertTrue(longTextAnnouncement.text.contains("\n")) // Contains newlines
    }

    @Test
    fun testAnnouncementListModelParsing() = runTest {
        // Test that AnnouncementFullListModel correctly parses the JSON
        val json = """{
            "announcements": [
                {"id": 1, "title": "Test 1", "text": "Text 1", "date": "2025/01/01"},
                {"id": 2, "title": "Test 2", "text": "Text 2", "date": "2025/01/02"}
            ]
        }"""

        val listModel: com.forkbombsquad.stillalivelarp.services.models.AnnouncementFullListModel? = globalFromJson(json)

        assertNotNull(listModel)
        assertEquals(2, listModel!!.announcements.size)
        assertEquals("Test 1", listModel.announcements[0].title)
        assertEquals("Test 2", listModel.announcements[1].title)
    }

}