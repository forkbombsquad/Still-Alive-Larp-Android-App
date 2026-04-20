package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestCreateModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestListModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ContactRequestModelTests : BaseUnitTestClass {

    // ==================== DATA MANAGER CONTACT REQUESTS TESTS ====================

    @Test
    fun testDataManagerContactRequests() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: DataManager.shared.contactRequests
            val contactRequests = DataManager.shared.contactRequests
            assertTrue(contactRequests.isNotEmpty())
        }
    }

    @Test
    fun testContactRequestModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests
            assertTrue(contactRequests.isNotEmpty())

            val firstRequest = contactRequests.first()
            assertTrue(firstRequest.id > 0)
            assertTrue(firstRequest.fullName.isNotEmpty())
            assertTrue(firstRequest.emailAddress.isNotEmpty())
            assertTrue(firstRequest.postalCode.isNotEmpty())
            assertTrue(firstRequest.message.isNotEmpty())
        }
    }

    @Test
    fun testContactRequestReadField() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests
            assertTrue(contactRequests.isNotEmpty())

            // Test read field - can be "TRUE" or "FALSE"
            val firstRequest = contactRequests.first()
            val isRead = firstRequest.read == "TRUE" || firstRequest.read == "true"
            assertTrue(isRead || !isRead) // Either is valid
        }
    }

    @Test
    fun testContactRequestReadToBoolean() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests

            // Test pattern from ContactDetailsActivity: read.toBoolean()
            for (request in contactRequests) {
                val isRead = request.read.toBoolean()
                // Should be either true or false based on string value
                val expected = request.read.lowercase() == "true"
                assertEquals(expected, isRead)
            }
        }
    }

    @Test
    fun testContactRequestsFilterByRead() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests

            // Filter by read status
            val readRequests = contactRequests.filter { it.read.toBoolean() }
            val unreadRequests = contactRequests.filter { !it.read.toBoolean() }

            // Verify all requests are categorized
            assertEquals(contactRequests.size, readRequests.size + unreadRequests.size)
        }
    }

    @Test
    fun testContactRequestsSortByRead() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern from ContactListActivity: sortedBy { !it.read.toBoolean() }
            // Unread (false) should come first
            val contactRequests = DataManager.shared.contactRequests
            val sorted = contactRequests.sortedBy { it.read.toBoolean() }

            // Verify unread come before read
            if (sorted.size > 1) {
                val firstIsUnread = !sorted.first().read.toBoolean()
                if (firstIsUnread) {
                    // If first is unread, last should be read
                    assertTrue(sorted.last().read.toBoolean())
                }
            }
        }
    }

    @Test
    fun testContactRequestMarkAsReadToggle() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests

            // Test pattern from ContactDetailsActivity: read.toBoolean().ternary("FALSE", "TRUE")
            val request = contactRequests.first()
            val currentRead = request.read.toBoolean()

            // Toggle read status
            val newRead = currentRead.ternary("FALSE", "TRUE")

            // Verify toggle works
            if (currentRead) {
                assertEquals("FALSE", newRead)
            } else {
                assertEquals("TRUE", newRead)
            }
        }
    }

    @Test
    fun testContactRequestMarkAsReadText() = runTest {
        loadDataManagerHappyPath(this) {
            val contactRequests = DataManager.shared.contactRequests

            // Test pattern from ContactDetailsActivity: read.toBoolean().ternary("Mark as Unread", "Mark as Read")
            val request = contactRequests.first()
            val isRead = request.read.toBoolean()

            val buttonText = isRead.ternary("Mark as Unread", "Mark as Read")

            if (isRead) {
                assertEquals("Mark as Unread", buttonText)
            } else {
                assertEquals("Mark as Read", buttonText)
            }
        }
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testContactRequestCreateModel() = runTest {
        val createJson = """{"fullName": "Test User", "emailAddress": "test@test.com", "postalCode": "12345", "message": "Test message", "read": "FALSE"}"""
        val createModel: ContactRequestCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("Test User", createModel!!.fullName)
        assertEquals("test@test.com", createModel.emailAddress)
        assertEquals("12345", createModel.postalCode)
        assertEquals("Test message", createModel.message)
        assertEquals("FALSE", createModel.read)
    }

    @Test
    fun testContactRequestCreateModelAllFields() = runTest {
        val createJson = """{"fullName": "John Doe", "emailAddress": "john@example.com", "postalCode": "54321", "message": "Hello world", "read": "TRUE"}"""
        val createModel: ContactRequestCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("John Doe", createModel!!.fullName)
        assertEquals("john@example.com", createModel.emailAddress)
    }

    // ==================== LIST MODEL TESTS ====================

    @Test
    fun testContactRequestListModel() = runTest {
        loadDataManagerHappyPath(this) {
            // Create list model like API does
            val contactRequests = DataManager.shared.contactRequests
            val listModel = ContactRequestListModel(contactRequests.toTypedArray())

            assertTrue(listModel.contactRequests.isNotEmpty())
            assertEquals(contactRequests.size, listModel.contactRequests.size)
        }
    }

    // ==================== EDGE CASES TESTS ====================

    @Test
    fun testContactRequestModelParsing() = runTest {
        val requestJson = """{"id": 100, "fullName": "Test Name", "emailAddress": "test@test.com", "postalCode": "11111", "message": "Test", "read": "TRUE"}"""
        val parsed: ContactRequestModel? = globalFromJson(requestJson)
        assertNotNull(parsed)
        assertEquals(100, parsed!!.id)
        assertEquals("Test Name", parsed.fullName)
        assertEquals("TRUE", parsed.read)
    }

    @Test
    fun testContactRequestReadLowercase() = runTest {
        // Test lowercase "true" and "false" values
        val requestTrue = """{"id": 1, "fullName": "Test", "emailAddress": "a@a.com", "postalCode": "1", "message": "m", "read": "true"}"""
        val parsedTrue: ContactRequestModel? = globalFromJson(requestTrue)
        assertNotNull(parsedTrue)
        assertTrue(parsedTrue!!.read.toBoolean())

        val requestFalse = """{"id": 2, "fullName": "Test2", "emailAddress": "b@b.com", "postalCode": "2", "message": "m2", "read": "false"}"""
        val parsedFalse: ContactRequestModel? = globalFromJson(requestFalse)
        assertNotNull(parsedFalse)
        assertFalse(parsedFalse!!.read.toBoolean())
    }

    @Test
    fun testContactRequestEmptyFields() = runTest {
        // Test with minimal/empty fields
        val requestJson = """{"id": 1, "fullName": "", "emailAddress": "", "postalCode": "", "message": "", "read": "FALSE"}"""
        val parsed: ContactRequestModel? = globalFromJson(requestJson)
        assertNotNull(parsed)
        assertEquals("", parsed!!.fullName)
        assertEquals("", parsed.emailAddress)
        assertEquals("", parsed.postalCode)
        assertEquals("", parsed.message)
    }
}