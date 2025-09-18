package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.AnnouncementService
import com.forkbombsquad.stillalivelarp.services.AuthService
import com.forkbombsquad.stillalivelarp.services.CampStatusService
import com.forkbombsquad.stillalivelarp.services.GetAllIntriguesRequest
import com.forkbombsquad.stillalivelarp.services.GetAllResearchProjectsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllSkillPrereqsRequest
import com.forkbombsquad.stillalivelarp.services.GetCampStatusRequest
import com.forkbombsquad.stillalivelarp.services.IntrigueService
import com.forkbombsquad.stillalivelarp.services.PlayerAuthService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.ResearchProjectService
import com.forkbombsquad.stillalivelarp.services.SkillPrereqService
import com.forkbombsquad.stillalivelarp.services.VersionService
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueListModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqListModel
import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqModel
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import com.forkbombsquad.stillalivelarp.utils.BaseUiTestClass
import com.forkbombsquad.stillalivelarp.utils.MockDataLoader
import com.forkbombsquad.stillalivelarp.utils.globalLastUnitTestPrint
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MockDataLoaderUnitTests: BaseUiTestClass {

    @Test
    fun testNoDataForService() = runTest {
        // Given empty mock data
        MockDataLoader.shared.clearMockData()
        // When a service call is attempted
        val response = PlayerService.GetAllPlayers().successfulResponse()
        // Then response should be null and last printout should contain no data found error
        assertNull(response)
        assertEquals(globalLastUnitTestPrint, "MOCK SERVICE CONTROLLER: Error Model: \nMock Response Error No Data Found")
    }

    @Test
    fun testDataForUnconventionalServices() = runTest {
        // Given preloaded mock happypath data
        // When unconventional services are called
        val token = AuthService().successfulResponse()
        val playerToken = PlayerAuthService().successfulResponse()
        val versions = VersionService().successfulResponse()
        // Then the responses should have correct data
        assertNotNull(token)
        assertNotNull(playerToken)
        assertNotNull(versions)
        assertTrue(token!!.access_token.isNotEmpty())
        assertTrue(playerToken!!.access_token.isNotEmpty())
        assertEquals(versions!!.androidVersion, 23)
        assertEquals(versions.rulebookVersion, "2.2.0.6")
    }

    @Test
    fun testDataForConventionalService() = runTest {
        // Given preloaded mock happypath data
        // When announcements are called
        val announcements = AnnouncementService.GetAllFullAnnouncements().successfulResponse()
        // Then the announcements should have the correct data
        assertNotNull(announcements)
        assertEquals(announcements!!.announcements.count(), 5)

        val first = announcements.announcements.first()
        assertEquals(first.id, 1)
        assertEquals(first.text, "We are pleased to announce that, after a 6-year long hiatus, Still Alive is finally making a comeback and it's going to be better than ever! We are restarting the story from scratch and have updated the system to be cleaner and more efficent! We've secured a new place to play and are currently planning on beginning in Summer of 2024. Keep your eyes open for more announcements and get excited!")
        assertEquals(first.title, "We're Back!")
        assertEquals(first.date, "2023/06/03")
    }

    @Test
    fun testServicesWithDifferentCallNums() = runTest {
        // Given custom mock data with call nums of 1, 2, 10, and infinite
        val empty = EmptyServicePayload()
        val prereqCount = 1
        val intrigueCount = 2
        val researchCount = 10
        val campStatusCount = -1
        MockDataLoader.shared.clearMockData()
        MockDataLoader.shared.loadMockData(GetAllSkillPrereqsRequest::class, empty, SkillPrereqListModel(
            arrayOf(
                SkillPrereqModel(1, 1, 2)
            )
        ))
        MockDataLoader.shared.loadMockData(GetAllIntriguesRequest::class, empty, IntrigueListModel(
            arrayOf(
                IntrigueModel(1, 1, "investigator msg", "interrogator msg", "")
            )
        ), intrigueCount)
        MockDataLoader.shared.loadMockData(GetAllResearchProjectsRequest::class, empty, ResearchProjectListModel(
            arrayOf(
                ResearchProjectModel(1, "The Project", "A desc", 10, "FALSE")
            )
        ), researchCount)
        MockDataLoader.shared.loadMockData(GetCampStatusRequest::class, empty, CampStatusModel(1, "not real json in here during this test"), campStatusCount)

        // When we call those services multiple times, succeeding each time
        // Then they should only succeed until they exceed their service limit count (and infinite should always work. Going to use 20 for proof)

        // Prereq
        var counter = 0
        while (counter <= prereqCount) {
            val response = SkillPrereqService.GetAllSkillPrereqs().successfulResponse()
            if (counter < prereqCount) {
                assertNotNull(response)
                val first = response!!.skillPrereqs.first()
                assertEquals(first.id, 1)
                assertEquals(first.baseSkillId, 1)
                assertEquals(first.prereqSkillId, 2)
            } else {
                // Should be null when counter is equal to the count
                assertNull(response)
            }
            counter += 1
        }

        // Intrigue
        counter = 0
        while (counter <= intrigueCount) {
            val response = IntrigueService.GetAllIntrigues().successfulResponse()
            if (counter < intrigueCount) {
                assertNotNull(response)
                val first = response!!.intrigues.first()
                assertEquals(first.id, 1)
                assertEquals(first.eventId, 1)
                assertEquals(first.investigatorMessage, "investigator msg")
                assertEquals(first.interrogatorMessage, "interrogator msg")
            } else {
                // Should be null when counter is equal to the count
                assertNull(response)
            }
            counter += 1
        }

        // research
        counter = 0
        while (counter <= researchCount) {
            val response = ResearchProjectService.GetAllResearchProjects().successfulResponse()
            if (counter < researchCount) {
                assertNotNull(response)
                val first = response!!.researchProjects.first()
                assertEquals(first.id, 1)
                assertEquals(first.name, "The Project")
                assertEquals(first.description, "A desc")
                assertEquals(first.milestones, 10)
                assertEquals(first.complete, "FALSE")
            } else {
                // Should be null when counter is equal to the count
                assertNull(response)
            }
            counter += 1
        }

        // camp status (should be infinite so just test 20)
        counter = 0
        while (counter < 20) {
            val response = CampStatusService.GetCampStatus().successfulResponse()
            assertNotNull(response)
            assertEquals(response!!.id, 1)
            assertEquals(response.campFortificationJson, "not real json in here during this test")

            counter += 1
        }
    }

    @Test
    fun testServiceWithChangingData() = runTest {
        // Given custom mock data with skill prereqs that change when called after a certain number of times
        val empty = EmptyServicePayload()
        MockDataLoader.shared.clearMockData()
        MockDataLoader.shared.loadMockData(GetAllSkillPrereqsRequest::class, empty, SkillPrereqListModel(
            arrayOf(
                SkillPrereqModel(1, 1, 2)
            )
        ), 2)
        MockDataLoader.shared.loadMockData(GetAllSkillPrereqsRequest::class, empty, SkillPrereqListModel(
            arrayOf(
                SkillPrereqModel(1, 9, 1)
            )
        ), 3)
        MockDataLoader.shared.loadMockData(GetAllSkillPrereqsRequest::class, empty, SkillPrereqListModel(
            arrayOf(
                SkillPrereqModel(1, 18, 7)
            )
        ))
        MockDataLoader.shared.loadMockData(GetAllSkillPrereqsRequest::class, empty, SkillPrereqListModel(
            arrayOf(
                SkillPrereqModel(1, 18, 8)
            )
        ), 4)

        // When called that number of times
        val responses: MutableList<SkillPrereqListModel?> = mutableListOf()
        var counter = 0
        while (counter < 11) {
            responses.add(SkillPrereqService.GetAllSkillPrereqs().successfulResponse())
            counter += 1
        }

        // Those responses should reflect the new data and be null afterwards
        responses.forEachIndexed { index, model ->
            when (index) {
                0, 1 -> {
                    assertNotNull(model)
                    val first = model!!.skillPrereqs.first()
                    assertEquals(first.id, 1)
                    assertEquals(first.baseSkillId, 1)
                    assertEquals(first.prereqSkillId, 2)
                }
                2, 3, 4 -> {
                    assertNotNull(model)
                    val first = model!!.skillPrereqs.first()
                    assertEquals(first.id, 1)
                    assertEquals(first.baseSkillId, 9)
                    assertEquals(first.prereqSkillId, 1)
                }
                5 -> {
                    assertNotNull(model)
                    val first = model!!.skillPrereqs.first()
                    assertEquals(first.id, 1)
                    assertEquals(first.baseSkillId, 18)
                    assertEquals(first.prereqSkillId, 7)
                }
                6, 7, 8, 9 -> {
                    assertNotNull(model)
                    val first = model!!.skillPrereqs.first()
                    assertEquals(first.id, 1)
                    assertEquals(first.baseSkillId, 18)
                    assertEquals(first.prereqSkillId, 8)
                }
                10 -> { assertNull(model) }
                else -> {}
            }
        }
    }

}