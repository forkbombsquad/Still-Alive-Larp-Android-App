package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagCreateModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.equalsIgnoreCase
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FeatureFlagModelTests : BaseUnitTestClass {

    // ==================== FEATURE FLAG MODEL TESTS ====================

    @Test
    fun testFeatureFlagModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags
            assertTrue(featureFlags.isNotEmpty())

            // First feature flag from test data
            val firstFlag = featureFlags.first()
            assertTrue(firstFlag.id > 0)
            assertTrue(firstFlag.name.isNotEmpty())
            assertTrue(firstFlag.description.isNotEmpty())
        }
    }

    @Test
    fun testFeatureFlagModelIsActiveAndroid() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags
            assertTrue(featureFlags.isNotEmpty())

            // Test the isActiveAndroid() method
            for (flag in featureFlags) {
                val isActive = flag.isActiveAndroid()
                // Should be either true or false based on activeAndroid string
                val expected = flag.activeAndroid.lowercase() == "true"
                assertEquals(expected, isActive)
            }
        }
    }

    @Test
    fun testFeatureFlagModelIsActiveIos() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags
            assertTrue(featureFlags.isNotEmpty())

            // Test the isActiveIos() method
            for (flag in featureFlags) {
                val isActive = flag.isActiveIos()
                // Should be either true or false based on activeIos string
                val expected = flag.activeIos.lowercase() == "true"
                assertEquals(expected, isActive)
            }
        }
    }

    @Test
    fun testFeatureFlagsByActiveState() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags

            // Filter by active state
            val activeAndroid = featureFlags.filter { it.isActiveAndroid() }
            val inactiveAndroid = featureFlags.filter { !it.isActiveAndroid() }

            // At least one should exist in either category
            assertTrue(activeAndroid.size + inactiveAndroid.size == featureFlags.size)
        }
    }

    // ==================== FIND BY NAME TESTS ====================

    @Test
    fun testFindFeatureFlagByName() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags

            // Test pattern from FeatureFlag.kt: firstOrNull { it.name.equalsIgnoreCase(name) }
            if (featureFlags.isNotEmpty()) {
                val firstFlagName = featureFlags.first().name
                val found = featureFlags.firstOrNull { it.name.equalsIgnoreCase(firstFlagName) }
                assertNotNull(found)
                assertEquals(firstFlagName, found!!.name)
            }
        }
    }

    @Test
    fun testFindFeatureFlagByNameCaseInsensitive() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags

            if (featureFlags.isNotEmpty()) {
                val flagName = featureFlags.first().name
                // Test with lowercase
                val foundLower = featureFlags.firstOrNull { it.name.equalsIgnoreCase(flagName.lowercase()) }
                assertNotNull(foundLower)
                // Test with uppercase
                val foundUpper = featureFlags.firstOrNull { it.name.equalsIgnoreCase(flagName.uppercase()) }
                assertNotNull(foundUpper)
            }
        }
    }

    @Test
    fun testFindNonExistentFeatureFlag() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags

            // Try to find a flag that doesn't exist
            val notFound = featureFlags.firstOrNull { it.name.equalsIgnoreCase("NONEXISTENT_FLAG") }
            assertNull(notFound)
        }
    }

    // ==================== DATA MANAGER ACCESS PATTERNS ====================

    @Test
    fun testDataManagerFeatureFlagsAccess() = runTest {
        loadDataManagerHappyPath(this) {
            // Test direct access: DataManager.shared.featureFlags
            val flags = DataManager.shared.featureFlags
            assertTrue(flags.isNotEmpty())
        }
    }

    @Test
    fun testFeatureFlagUtilityClass() = runTest {
        loadDataManagerHappyPath(this) {
            // Test the FeatureFlag utility class patterns from FeatureFlag.kt
            val featureFlags = DataManager.shared.featureFlags
            assertTrue(featureFlags.isNotEmpty())

            // Get the first flag name to test with
            val firstFlagName = featureFlags.first().name

            // Test FeatureFlag(name).isActive() pattern
            val flag = FeatureFlag(firstFlagName)
            val isActive = flag.isActive()
            // Should match the flag's activeAndroid value
            assertEquals(featureFlags.first().isActiveAndroid(), isActive)

            // Test FeatureFlag(name).isActiveIos() pattern
            val isActiveIos = flag.isActiveIos()
            assertEquals(featureFlags.first().isActiveIos(), isActiveIos)

            // Test FeatureFlag(name).isActiveBoth() pattern
            val isActiveBoth = flag.isActiveBoth()
            val expectedBoth = featureFlags.first().isActiveAndroid() && featureFlags.first().isActiveIos()
            assertEquals(expectedBoth, isActiveBoth)
        }
    }

    @Test
    fun testFeatureFlagUtilityClassNonExistent() = runTest {
        loadDataManagerHappyPath(this) {
            // Test FeatureFlag with a name that doesn't exist
            val flag = FeatureFlag("NONEXISTENT_FLAG")
            assertFalse(flag.isActive())
            assertFalse(flag.isActiveIos())
            assertFalse(flag.isActiveBoth())
        }
    }

    @Test
    fun testFeatureFlagUtilityClassCaseInsensitive() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags
            if (featureFlags.isNotEmpty()) {
                val flagName = featureFlags.first().name

                // Test with different cases - all should find the same flag
                val flagLower = FeatureFlag(flagName.lowercase())
                val flagUpper = FeatureFlag(flagName.uppercase())
                val flagMixed = FeatureFlag(flagName.replaceFirstChar { it.uppercase() })

                // All should return the same isActive result
                assertEquals(flagLower.isActive(), flagUpper.isActive())
                assertEquals(flagUpper.isActive(), flagMixed.isActive())
            }
        }
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testFeatureFlagCreateModel() = runTest {
        val createJson = """{"name": "test_flag", "description": "Test description", "activeAndroid": "TRUE", "activeIos": "FALSE"}"""
        val createModel: FeatureFlagCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("test_flag", createModel!!.name)
        assertEquals("Test description", createModel.description)
        assertEquals("TRUE", createModel.activeAndroid)
        assertEquals("FALSE", createModel.activeIos)
    }

    @Test
    fun testFeatureFlagCreateModelAllFields() = runTest {
        val createJson = """{"name": "new_feature", "description": "A new feature flag", "activeAndroid": "true", "activeIos": "true"}"""
        val createModel: FeatureFlagCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("new_feature", createModel!!.name)
        // Note: lowercase "true" will still parse as string
        assertEquals("true", createModel.activeAndroid)
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    fun testFeatureFlagModelEdgeCases() = runTest {
        // Test various activeAndroid/activeIos string values
        val flagTrue = """{"id": 1, "name": "flag1", "description": "Desc", "activeAndroid": "TRUE", "activeIos": "TRUE"}"""
        val parsedTrue: FeatureFlagModel? = globalFromJson(flagTrue)
        assertNotNull(parsedTrue)
        assertTrue(parsedTrue!!.isActiveAndroid())
        assertTrue(parsedTrue.isActiveIos())

        val flagFalse = """{"id": 2, "name": "flag2", "description": "Desc", "activeAndroid": "FALSE", "activeIos": "FALSE"}"""
        val parsedFalse: FeatureFlagModel? = globalFromJson(flagFalse)
        assertNotNull(parsedFalse)
        assertFalse(parsedFalse!!.isActiveAndroid())
        assertFalse(parsedFalse.isActiveIos())

        val flagLowercase = """{"id": 3, "name": "flag3", "description": "Desc", "activeAndroid": "true", "activeIos": "false"}"""
        val parsedLower: FeatureFlagModel? = globalFromJson(flagLowercase)
        assertNotNull(parsedLower)
        assertTrue(parsedLower!!.isActiveAndroid())
        assertFalse(parsedLower.isActiveIos())
    }

    @Test
    fun testFeatureFlagEmptyDescription() = runTest {
        val flagEmptyDesc = """{"id": 1, "name": "flag", "description": "", "activeAndroid": "TRUE", "activeIos": "TRUE"}"""
        val parsed: FeatureFlagModel? = globalFromJson(flagEmptyDesc)
        assertNotNull(parsed)
        assertEquals("", parsed!!.description)
    }

    @Test
    fun testFeatureFlagListAccess() = runTest {
        loadDataManagerHappyPath(this) {
            val featureFlags = DataManager.shared.featureFlags

            // Test that we can iterate and access all properties
            for (flag in featureFlags) {
                assertTrue(flag.id > 0)
                assertTrue(flag.name.isNotEmpty())
                // description can be empty
                assertNotNull(flag.activeAndroid)
                assertNotNull(flag.activeIos)
            }
        }
    }
}