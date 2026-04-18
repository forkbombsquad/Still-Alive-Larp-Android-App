package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SkillCategoryModelTests : BaseUnitTestClass {

    // ==================== PARSING TESTS ====================

    @Test
    fun testSkillCategoryModelParsing() = runTest {
        val categoryJson = """{"id": 10, "name": "Combat"}"""
        val parsed: SkillCategoryModel? = globalFromJson(categoryJson)
        assertNotNull(parsed)
        assertEquals(10, parsed!!.id)
        assertEquals("Combat", parsed.name)
    }

    @Test
    fun testSkillCategoryModelAllFields() = runTest {
        val categoryJson = """{"id": 1, "name": "Beginner (Free) Skills"}"""
        val parsed: SkillCategoryModel? = globalFromJson(categoryJson)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.id)
        assertEquals("Beginner (Free) Skills", parsed.name)
    }

    @Test
    fun testSkillCategoryMultipleParsing() = runTest {
        // Test parsing multiple category models
        val categoryJson1 = """{"id": 1, "name": "Beginner"}"""
        val parsed1: SkillCategoryModel? = globalFromJson(categoryJson1)
        assertNotNull(parsed1)
        assertEquals(1, parsed1!!.id)
        assertEquals("Beginner", parsed1.name)

        val categoryJson2 = """{"id": 2, "name": "Firearms"}"""
        val parsed2: SkillCategoryModel? = globalFromJson(categoryJson2)
        assertNotNull(parsed2)
        assertEquals(2, parsed2!!.id)
        assertEquals("Firearms", parsed2.name)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testSkillCategoryEmptyName() = runTest {
        val categoryJson = """{"id": 1, "name": ""}"""
        val parsed: SkillCategoryModel? = globalFromJson(categoryJson)
        assertNotNull(parsed)
        assertEquals("", parsed!!.name)
    }

    @Test
    fun testSkillCategoryDifferentIds() = runTest {
        // Test various category IDs
        val ids = listOf(1, 2, 3, 14, 15)
        for (id in ids) {
            val json = """{"id": $id, "name": "Test Category"}"""
            val parsed: SkillCategoryModel? = globalFromJson(json)
            assertNotNull(parsed)
            assertEquals(id, parsed!!.id)
        }
    }
}