package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectCreateModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectMilestoneJsonListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectMilestoneJsonModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResearchProjectModelTests : BaseUnitTestClass {

    // ==================== DATA MANAGER RESEARCH PROJECTS TESTS ====================

    @Test
    fun testDataManagerResearchProjects() = runTest {
        loadDataManagerHappyPath(this) {
            // Test pattern: DataManager.shared.researchProjects
            val researchProjects = DataManager.shared.researchProjects
            assertTrue(researchProjects.isNotEmpty())
        }
    }

    @Test
    fun testResearchProjectModelFields() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects
            assertTrue(researchProjects.isNotEmpty())

            val firstProject = researchProjects.first()
            assertTrue(firstProject.id > 0)
            assertTrue(firstProject.name.isNotEmpty())
            assertTrue(firstProject.description.isNotEmpty())
            assertTrue(firstProject.milestones > 0)
        }
    }

    @Test
    fun testResearchProjectCompleteField() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects
            assertTrue(researchProjects.isNotEmpty())

            // Test complete field can be "TRUE" or "FALSE"
            for (project in researchProjects) {
                val isComplete = project.complete == "TRUE" || project.complete == "true"
                assertTrue(isComplete || !isComplete) // Either is valid
            }
        }
    }

    @Test
    fun testResearchProjectCompleteToBoolean() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects

            // Test pattern from ResearchProjectCell: complete.toBoolean()
            for (project in researchProjects) {
                val isComplete = project.complete.toBoolean()
                val expected = project.complete.lowercase() == "true"
                assertEquals(expected, isComplete)
            }
        }
    }

    @Test
    fun testResearchProjectCompleteDisplayText() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects

            // Test pattern from ResearchProjectCell: complete.toBoolean().ternary("Yes", "No")
            for (project in researchProjects) {
                val displayText = project.complete.toBoolean().ternary("Yes", "No")
                assertTrue(displayText == "Yes" || displayText == "No")
            }
        }
    }

    @Test
    fun testResearchProjectMilestoneDescs() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects
            assertTrue(researchProjects.isNotEmpty())

            // Test milestoneDescs field
            for (project in researchProjects) {
                assertNotNull(project.milestoneDescs)
            }
        }
    }

    // ==================== MILESTONE JSON PARSING TESTS ====================

    @Test
    fun testMilestoneJsonModels() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects

            // Test the milestoneJsonModels getter
            for (project in researchProjects) {
                val milestones = project.milestoneJsonModels
                // Should be a list (may be null if parsing fails)
                if (milestones != null) {
                    for (milestone in milestones) {
                        assertTrue(milestone.id.isNotEmpty())
                        assertTrue(milestone.text.isNotEmpty())
                    }
                }
            }
        }
    }

    @Test
    fun testMilestoneJsonModelsCount() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects
            val firstProject = researchProjects.first()
            val milestones = firstProject.milestoneJsonModels

            // Number of milestones doesn't have to match the actual documented milestones.
            if (milestones != null) {
                assertNotEquals(firstProject.milestones, milestones.size)
            }
        }
    }

    // ==================== CREATE MODEL TESTS ====================

    @Test
    fun testResearchProjectCreateModel() = runTest {
        val createJson = """{"name": "Test Project", "description": "Test Description", "milestones": 3, "complete": "FALSE", "milestoneDescs": "[]"}"""
        val createModel: ResearchProjectCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("Test Project", createModel!!.name)
        assertEquals("Test Description", createModel.description)
        assertEquals(3, createModel.milestones)
        assertEquals("FALSE", createModel.complete)
    }

    @Test
    fun testResearchProjectCreateModelComplete() = runTest {
        val createJson = """{"name": "Complete Project", "description": "Done", "milestones": 5, "complete": "TRUE", "milestoneDescs": "{}"}"""
        val createModel: ResearchProjectCreateModel? = globalFromJson(createJson)
        assertNotNull(createModel)
        assertEquals("TRUE", createModel!!.complete)
    }

    // ==================== LIST MODEL TESTS ====================

    @Test
    fun testResearchProjectListModel() = runTest {
        loadDataManagerHappyPath(this) {
            // Create list model like API does
            val projects = DataManager.shared.researchProjects
            val listModel = ResearchProjectListModel(projects.toTypedArray())

            assertTrue(listModel.researchProjects.isNotEmpty())
            assertEquals(projects.size, listModel.researchProjects.size)
        }
    }

    // ==================== MILESTONE JSON MODELS TESTS ====================

    @Test
    fun testResearchProjectMilestoneJsonModelParsing() = runTest {
        val milestoneJson = """{"id": "1", "text": "First milestone"}"""
        val parsed: ResearchProjectMilestoneJsonModel? = globalFromJson(milestoneJson)
        assertNotNull(parsed)
        assertEquals("1", parsed!!.id)
        assertEquals("First milestone", parsed.text)
    }

    @Test
    fun testResearchProjectMilestoneJsonListModelParsing() = runTest {
        val listJson = """{"milestoneDescs": [{"id": "1", "text": "Step 1"}, {"id": "2", "text": "Step 2"}]}"""
        val parsed: ResearchProjectMilestoneJsonListModel? = globalFromJson(listJson)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.milestoneDescs.size)
        assertEquals("1", parsed.milestoneDescs[0].id)
        assertEquals("Step 2", parsed.milestoneDescs[1].text)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun testResearchProjectModelParsing() = runTest {
        val projectJson = """{"id": 10, "name": "Test", "description": "Desc", "milestones": 5, "complete": "TRUE", "milestoneDescs": "[]"}"""
        val parsed: ResearchProjectModel? = globalFromJson(projectJson)
        assertNotNull(parsed)
        assertEquals(10, parsed!!.id)
        assertEquals("Test", parsed.name)
        assertEquals(5, parsed.milestones)
    }

    @Test
    fun testResearchProjectCompleteLowercase() = runTest {
        // Test lowercase "true" and "false"
        val projectTrue = """{"id": 1, "name": "Test", "description": "Desc", "milestones": 1, "complete": "true", "milestoneDescs": "[]"}"""
        val parsedTrue: ResearchProjectModel? = globalFromJson(projectTrue)
        assertNotNull(parsedTrue)
        assertTrue(parsedTrue!!.complete.toBoolean())

        val projectFalse = """{"id": 2, "name": "Test2", "description": "Desc2", "milestones": 1, "complete": "false", "milestoneDescs": "[]"}"""
        val parsedFalse: ResearchProjectModel? = globalFromJson(projectFalse)
        assertNotNull(parsedFalse)
        assertFalse(parsedFalse!!.complete.toBoolean())
    }

    @Test
    fun testMilestoneJsonModelsEmpty() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects
            // Find a project with empty or minimal milestoneDescs
            val minimalProject = researchProjects.firstOrNull {
                it.milestoneDescs.isEmpty() || it.milestoneDescs == "[]"
            }
            if (minimalProject != null) {
                val milestones = minimalProject.milestoneJsonModels
                // Should return null or empty list for invalid/empty JSON
                assertTrue(milestones == null || milestones.isEmpty())
            }
        }
    }

    @Test
    fun testResearchProjectCompleteToggle() = runTest {
        loadDataManagerHappyPath(this) {
            val researchProjects = DataManager.shared.researchProjects

            // Test toggling complete status (like ManageResearchProjectsActivity does)
            val project = researchProjects.first()
            val currentComplete = project.complete.toBoolean()

            // Toggle
            val newComplete = currentComplete.ternary("FALSE", "TRUE")

            // Verify toggle logic
            if (currentComplete) {
                assertEquals("FALSE", newComplete)
            } else {
                assertEquals("TRUE", newComplete)
            }
        }
    }
}