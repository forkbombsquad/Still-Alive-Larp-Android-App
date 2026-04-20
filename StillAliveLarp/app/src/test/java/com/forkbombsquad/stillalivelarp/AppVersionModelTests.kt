package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.models.AppVersionModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AppVersionModelTests: BaseUnitTestClass {

    @Test
    fun testAppVersionModelFields() {
        // Test basic version model parsing
        val json = """{"androidVersion": 23, "iosVersion": 15, "rulebookVersion": "2.206"}"""
        val version: AppVersionModel? = globalFromJson(json)
        
        assertNotNull(version)
        assertEquals(23, version!!.androidVersion)
        assertEquals(15, version.iosVersion)
        assertEquals("2.206", version.rulebookVersion)
    }

    @Test
    fun testAppVersionModelFromTestData() = runTest {
        loadDataManagerHappyPath(this) {
            // The version is returned from the VersionService and used in MainActivity
            // We can verify the mock data parsing works properly
            val json = """{"androidVersion": 23, "iosVersion": 15, "rulebookVersion": "2.206"}"""
            val version: AppVersionModel? = globalFromJson(json)
            assertNotNull(version)
            assertTrue(version!!.androidVersion > 0)
            assertTrue(version.iosVersion > 0)
        }
    }

    @Test
    fun testAppVersionModelEdgeCaseMultipleDigits() {
        // Test with multiple digit version numbers
        val json = """{"androidVersion": 123, "iosVersion": 456, "rulebookVersion": "10.999"}"""
        val version: AppVersionModel? = globalFromJson(json)
        
        assertNotNull(version)
        assertEquals(123, version!!.androidVersion)
        assertEquals(456, version.iosVersion)
        assertEquals("10.999", version.rulebookVersion)
    }

    @Test
    fun testAppVersionModelEdgeCaseNewerVersions() {
        // Test with newer version numbers (e.g., future versions)
        val json = """{"androidVersion": 100, "iosVersion": 99, "rulebookVersion": "99.999"}"""
        val version: AppVersionModel? = globalFromJson(json)
        
        assertNotNull(version)
        assertEquals(100, version!!.androidVersion)
        assertEquals(99, version.iosVersion)
        assertEquals("99.999", version.rulebookVersion)
    }

    @Test
    fun testAppVersionModelComparisonInMainActivity() {
        // Simulate the version check logic from MainActivity
        val currentVersion = 22 // Current app version
        val json = """{"androidVersion": 23, "iosVersion": 15, "rulebookVersion": "2.206"}"""
        val version: AppVersionModel? = globalFromJson(json)
        
        assertNotNull(version)
        
        // Check if update is needed (this logic is from MainActivity)
        val needsUpdate = currentVersion < version!!.androidVersion
        assertTrue(needsUpdate) // Since 22 < 23, update is needed
        
        // Test case where current version matches
        val json2 = """{"androidVersion": 22, "iosVersion": 15, "rulebookVersion": "2.206"}"""
        val version2: AppVersionModel? = globalFromJson(json2)
        assertNotNull(version2)
        
        val needsUpdate2 = currentVersion < version2!!.androidVersion
        assertFalse(needsUpdate2) // Since 22 is not < 22, no update needed
    }

    @Test
    fun testAppVersionModelRulebookVersionParsing() {
        // Test various rulebook version formats
        val json1 = """{"androidVersion": 1, "iosVersion": 1, "rulebookVersion": "1.0"}"""
        val v1: AppVersionModel? = globalFromJson(json1)
        assertNotNull(v1)
        assertEquals("1.0", v1!!.rulebookVersion)
        
        val json2 = """{"androidVersion": 1, "iosVersion": 1, "rulebookVersion": "2.206"}"""
        val v2: AppVersionModel? = globalFromJson(json2)
        assertNotNull(v2)
        assertEquals("2.206", v2!!.rulebookVersion)
        
        val json3 = """{"androidVersion": 1, "iosVersion": 1, "rulebookVersion": "2024.01"}"""
        val v3: AppVersionModel? = globalFromJson(json3)
        assertNotNull(v3)
        assertEquals("2024.01", v3!!.rulebookVersion)
    }

}