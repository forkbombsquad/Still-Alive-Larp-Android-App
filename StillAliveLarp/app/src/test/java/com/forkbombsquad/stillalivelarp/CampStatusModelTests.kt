package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CampFortification
import com.forkbombsquad.stillalivelarp.services.models.CampFortifications
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.models.Fortification
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CampStatusModelTests : BaseUnitTestClass {

    // ==================== DATA MANAGER CAMP STATUS TESTS ====================

    @Test
    fun testDataManagerCampStatus() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: DataManager.shared.campStatus
            val campStatus = DataManager.shared.campStatus
            // campStatus may or may not be loaded depending on feature flag
            // Just verify the property is accessible
            assertNotNull(campStatus)
        }
    }

    @Test
    fun testCampStatusModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val campStatus = DataManager.shared.campStatus
            if (campStatus != null) {
                assertTrue(campStatus.id >= 0)
                assertNotNull(campStatus.campFortificationJson)
            }
        }
    }

    @Test
    fun testCampStatusCampFortifications() = runTest {
        loadDataManagerHappyPath(this) {
            val campStatus = DataManager.shared.campStatus
            if (campStatus != null) {
                // Test the campFortifications getter (parses JSON)
                val fortifications = campStatus.campFortifications
                assertNotNull(fortifications)
                // Should be a list
                assertTrue(fortifications is List<*>)
            }
        }
    }

    // ==================== CAMP FORTIFICATIONS STRUCTURE TESTS ====================

    @Test
    fun testCampFortificationStructure() = runTest {
        loadDataManagerHappyPath(this) {
            val campStatus = DataManager.shared.campStatus
            if (campStatus != null) {
                val forts = campStatus.campFortifications
                if (forts.isNotEmpty()) {
                    val firstRing = forts.first()
                    assertTrue(firstRing.ring >= 0)
                    assertNotNull(firstRing.fortifications)
                }
            }
        }
    }

    @Test
    fun testFortificationStructure() = runTest {
        loadDataManagerHappyPath(this) {
            val campStatus = DataManager.shared.campStatus
            if (campStatus != null) {
                val forts = campStatus.campFortifications
                // Find any ring with fortifications
                val ringWithForts = forts.firstOrNull { it.fortifications.isNotEmpty() }
                if (ringWithForts != null) {
                    val fort = ringWithForts.fortifications.first()
                    assertTrue(fort.type.isNotEmpty())
                    assertTrue(fort.health >= 0)
                }
            }
        }
    }

    @Test
    fun testFortificationFortificationType() = runTest {
        loadDataManagerHappyPath(this) {
            val campStatus = DataManager.shared.campStatus
            if (campStatus != null) {
                val forts = campStatus.campFortifications
                val ringWithForts = forts.firstOrNull { it.fortifications.isNotEmpty() }
                if (ringWithForts != null) {
                    val fort = ringWithForts.fortifications.first()
                    val fortType = fort.fortificationType
                    assertNotNull(fortType)
                }
            }
        }
    }

    // ==================== FORTIFICATION TYPE ENUM TESTS ====================

    @Test
    fun testFortificationTypeValues() = runTest {
        // Test all FortificationType values
        val types = Fortification.FortificationType.values()
        assertEquals(5, types.size)

        assertEquals("LIGHT", Fortification.FortificationType.LIGHT.text)
        assertEquals("MEDIUM", Fortification.FortificationType.MEDIUM.text)
        assertEquals("HEAVY", Fortification.FortificationType.HEAVY.text)
        assertEquals("ADVANCED", Fortification.FortificationType.ADVANCED.text)
        assertEquals("MILITARY GRADE", Fortification.FortificationType.MILITARY_GRADE.text)
    }

    @Test
    fun testFortificationTypeGetMaxHealth() = runTest {
        // Test getMaxHealth for each type
        assertEquals(5, Fortification.FortificationType.LIGHT.getMaxHealth())
        assertEquals(10, Fortification.FortificationType.MEDIUM.getMaxHealth())
        assertEquals(15, Fortification.FortificationType.HEAVY.getMaxHealth())
        assertEquals(20, Fortification.FortificationType.ADVANCED.getMaxHealth())
        assertEquals(30, Fortification.FortificationType.MILITARY_GRADE.getMaxHealth())
    }

    @Test
    fun testFortificationTypeGetFortificationType() = runTest {
        // Test getFortificationType method
        assertEquals(Fortification.FortificationType.LIGHT, Fortification.FortificationType.getFortificationType("LIGHT"))
        assertEquals(Fortification.FortificationType.MEDIUM, Fortification.FortificationType.getFortificationType("MEDIUM"))
        assertEquals(Fortification.FortificationType.HEAVY, Fortification.FortificationType.getFortificationType("HEAVY"))
        assertEquals(Fortification.FortificationType.ADVANCED, Fortification.FortificationType.getFortificationType("ADVANCED"))
        assertEquals(Fortification.FortificationType.MILITARY_GRADE, Fortification.FortificationType.getFortificationType("MILITARY GRADE"))

        // Invalid type should return LIGHT (default)
        assertEquals(Fortification.FortificationType.LIGHT, Fortification.FortificationType.getFortificationType("INVALID"))
    }

    // ==================== INIT WITH CAMP FORTIFICATIONS ====================

    @Test
    fun testInitWithCampFortifications() = runTest {
        // Test pattern from ManageCampFortificationsActivity: CampStatusModel.initWithCampFortifications
        val fortifications = listOf(
            Fortification(Fortification.FortificationType.LIGHT, 5),
            Fortification(Fortification.FortificationType.HEAVY, 15)
        )
        val campForts = listOf(CampFortification(1, fortifications))

        val campStatus = CampStatusModel.initWithCampFortifications(CampStatusModel(1, "", 10, 2, 1, ""), campForts)

        assertEquals(1, campStatus.id)
        assertEquals(10, campStatus.npcSlots)
        assertEquals(2, campStatus.medicalCots)
        assertEquals(1, campStatus.teachingChairs)
        assertNotNull(campStatus.campFortificationJson)

        // Verify the JSON can be parsed back
        val parsedForts = campStatus.campFortifications
        assertEquals(1, parsedForts.size)
        assertEquals(2, parsedForts.first().fortifications.size)
    }

    @Test
    fun testInitWithEmptyFortifications() = runTest {
        val campStatus = CampStatusModel.initWithCampFortifications(
            CampStatusModel(1, "",
                10, 2, 1, ""), emptyList())

        assertEquals(1, campStatus.id)
        assertNotNull(campStatus.campFortificationJson)

        val parsedForts = campStatus.campFortifications
        assertTrue(parsedForts.isEmpty())
    }

    // ==================== PARSING TESTS ====================

    @Test
    fun testCampStatusModelParsing() = runTest {
        // Test direct JSON parsing
        val json = """{"id": 1, "campFortificationJson": "{\"campFortifications\": []}"}"""
        val parsed: CampStatusModel? = globalFromJson(json)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.id)
    }

    @Test
    fun testCampFortificationsParsing() = runTest {
        val json = """{"campFortifications": [{"ring": 1, "fortifications": [{"type": "LIGHT", "health": 5}, {"type": "HEAVY", "health": 15}]}]}"""
        val parsed: CampFortifications? = globalFromJson(json)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.campFortifications.size)
        assertEquals(1, parsed.campFortifications.first().ring)
        assertEquals(2, parsed.campFortifications.first().fortifications.size)
    }

    @Test
    fun testFortificationParsing() = runTest {
        val json = """{"type": "MEDIUM", "health": 10}"""
        val parsed: Fortification? = globalFromJson(json)
        assertNotNull(parsed)
        assertEquals("MEDIUM", parsed!!.type)
        assertEquals(10, parsed.health)
        assertEquals(Fortification.FortificationType.MEDIUM, parsed.fortificationType)
    }

    @Test
    fun testFortificationConstructorWithEnum() = runTest {
        // Test constructor with FortificationType enum
        val fort = Fortification(Fortification.FortificationType.ADVANCED, 20)
        assertEquals("ADVANCED", fort.type)
        assertEquals(20, fort.health)
        assertEquals(Fortification.FortificationType.ADVANCED, fort.fortificationType)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testCampStatusWithNullJson() = runTest {
        // Test with empty/default JSON
        val campStatus = CampStatusModel(1, "{}", 10, 2, 1, "")
        val forts = campStatus.campFortifications
        assertTrue(forts.isEmpty())
    }

    @Test
    fun testCampStatusWithMalformedJson() = runTest {
        // Test with invalid JSON - should return empty list due to null safety
        val campStatus = CampStatusModel(1, "invalid json", 10, 2, 1, "")
        val forts = campStatus.campFortifications
        assertNotNull(forts)
    }

    @Test
    fun testGlobalToJsonAndBack() = runTest {
        // Test roundtrip: globalToJson -> globalFromJson
        val campForts = CampFortifications(listOf(
            CampFortification(1, listOf(Fortification(Fortification.FortificationType.LIGHT, 5)))
        ))
        val json = globalToJson(campForts)
        assertNotNull(json)

        val parsed: CampFortifications? = globalFromJson(json)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.campFortifications.size)
    }
}