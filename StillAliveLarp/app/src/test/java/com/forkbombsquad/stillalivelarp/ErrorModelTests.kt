package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ErrorModelTests: BaseUnitTestClass {

    @Test
    fun testErrorModelFields() {
        // Test basic error model parsing
        val json = """{"detail": "Invalid username or password"}"""
        val error: ErrorModel? = globalFromJson(json)
        
        assertNotNull(error)
        assertEquals("Invalid username or password", error!!.detail)
    }

    @Test
    fun testErrorModelEdgeCaseEmptyDetail() {
        // Edge case: empty detail
        val json = """{"detail": ""}"""
        val error: ErrorModel? = globalFromJson(json)
        
        assertNotNull(error)
        assertEquals("", error!!.detail)
    }

    @Test
    fun testErrorModelEdgeCaseMissingDetail() {
        // Edge case: missing detail field (should use default or null)
        // Jackson's @JsonIgnoreProperties(ignoreUnknown = true) will ignore unknown fields
        // but missing required fields will have default values
        val json = """{}"""
        val error: ErrorModel? = globalFromJson(json)
        
        // The detail will be null since it's not nullable and not provided
        // Let's verify the behavior
        assertNotNull(error)
    }

    @Test
    fun testErrorModelLongMessage() {
        // Test with a long error message
        val longMessage = "This is a very long error message that might contain multiple lines\nand describes in detail what went wrong in the system including specific error codes and suggestions for the user to resolve the issue."
        val json = """{"detail": "$longMessage"}"""
        val error: ErrorModel? = globalFromJson(json)
        
        assertNotNull(error)
        assertEquals(longMessage, error!!.detail)
    }

    @Test
    fun testErrorModelSpecialCharacters() {
        // Test with special characters that might appear in error messages
        val json = """{"detail": "Error: Could not connect to server (code: 500) - Please try again later!"}"""
        val error: ErrorModel? = globalFromJson(json)
        
        assertNotNull(error)
        assertTrue(error!!.detail.contains("code: 500"))
    }

}