package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.models.OAuthTokenModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OAuthTokenModelTests: BaseUnitTestClass {

    @Test
    fun testOAuthTokenModelFields() {
        // Test basic token parsing
        val json = """{"access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"}"""
        val token: OAuthTokenModel? = globalFromJson(json)
        
        assertNotNull(token)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token", token!!.access_token)
    }

    @Test
    fun testOAuthTokenModelEdgeCaseShortToken() {
        // Edge case: short token (minimal valid token)
        val json = """{"access_token": "abc"}"""
        val token: OAuthTokenModel? = globalFromJson(json)
        
        assertNotNull(token)
        assertEquals("abc", token!!.access_token)
    }

    @Test
    fun testOAuthTokenModelEdgeCaseLongToken() {
        // Edge case: very long JWT-style token
        val longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val json = """{"access_token": "$longToken"}"""
        val token: OAuthTokenModel? = globalFromJson(json)
        
        assertNotNull(token)
        assertEquals(longToken, token!!.access_token)
    }

    @Test
    fun testOAuthTokenModelEdgeCaseEmptyToken() {
        // Edge case: empty access token (not ideal but model should handle)
        val json = """{"access_token": ""}"""
        val token: OAuthTokenModel? = globalFromJson(json)
        
        assertNotNull(token)
        assertEquals("", token!!.access_token)
    }

    @Test
    fun testOAuthTokenModelEdgeCaseSpecialCharacters() {
        // Edge case: token with special characters that might appear in real tokens
        val json = """{"access_token": "abc123_+-/=!@#$%^&*()"}"""
        val token: OAuthTokenModel? = globalFromJson(json)
        
        assertNotNull(token)
        assertEquals("abc123_+-/=!@#$%^&*()", token!!.access_token)
    }

    @Test
    fun testOAuthTokenModelUsageInAuth() = runTest {
        loadDataManagerHappyPath(this) {
            // The token is used in AuthService to authenticate requests
            // We can verify that the mock data loads successfully
            
            // Simulate parsing the token response from auth/login
            val tokenJson = """{"access_token": "test_access_token_12345"}"""
            val parsedToken: OAuthTokenModel? = globalFromJson(tokenJson)
            
            assertNotNull(parsedToken)
            assertTrue(parsedToken!!.access_token.isNotEmpty())
            
            // In real usage, this token would be stored and used for subsequent API calls
            // The token format is typically a JWT string
        }
    }

}