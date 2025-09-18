package com.forkbombsquad.stillalivelarp.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AuthPlayerTokenRequest
import com.forkbombsquad.stillalivelarp.services.AuthService
import com.forkbombsquad.stillalivelarp.services.AuthTokenRequest
import com.forkbombsquad.stillalivelarp.services.GetAllAnnouncementsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllAwardsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllCharacterSkillsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllCharactersRequest
import com.forkbombsquad.stillalivelarp.services.GetAllContactRequestsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllEventAttendeesRequest
import com.forkbombsquad.stillalivelarp.services.GetAllEventsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllFeatureFlagsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllFullAnnouncementsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllFullCharactersRequest
import com.forkbombsquad.stillalivelarp.services.GetAllGearRequest
import com.forkbombsquad.stillalivelarp.services.GetAllIntriguesRequest
import com.forkbombsquad.stillalivelarp.services.GetAllPlayersRequest
import com.forkbombsquad.stillalivelarp.services.GetAllPreregsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllProfileImagesRequest
import com.forkbombsquad.stillalivelarp.services.GetAllResearchProjectsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllSkillCategoriesRequest
import com.forkbombsquad.stillalivelarp.services.GetAllSkillPrereqsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllSkillsRequest
import com.forkbombsquad.stillalivelarp.services.GetAllXpReductionsRequest
import com.forkbombsquad.stillalivelarp.services.GetCampStatusRequest
import com.forkbombsquad.stillalivelarp.services.GetUpdateTrackerRequest
import com.forkbombsquad.stillalivelarp.services.PlayerAuthService
import com.forkbombsquad.stillalivelarp.services.SignInPlayerRequest
import com.forkbombsquad.stillalivelarp.services.VersionRequest
import com.forkbombsquad.stillalivelarp.services.VersionService
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import com.forkbombsquad.stillalivelarp.utils.mockdata.MockData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

interface BaseUiTestClass {

    enum class DataLoadType {
        HAPPY_PATH, NONE
    }

    fun beforeEach() {}
    fun afterEach() {}

    @BeforeEach
    fun donotoverride_beforeEachTest() {
        beforeEach_baseUnitTestBehavior()
        beforeEach()
    }

    @AfterEach
    fun donotoverride_afterEachTest() {
        afterEach_baseUnitTestBehavior()
        afterEach()
    }

    fun setDataLoadType(): DataLoadType {
        return DataLoadType.HAPPY_PATH
    }

    fun beforeEach_baseUnitTestBehavior() {
        when (setDataLoadType()) {
            DataLoadType.HAPPY_PATH -> loadHappyPathData()
            DataLoadType.NONE -> MockDataLoader.shared.clearMockData()
        }
    }

    fun afterEach_baseUnitTestBehavior() {
        MockDataLoader.shared.clearMockData()
    }

    fun getLifecycleScope(): CoroutineScope {
        val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
        val lifecycle = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycle
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return lifecycleOwner.lifecycleScope
    }

    private fun loadHappyPathData() {
        val MDL = MockDataLoader.shared

        val empty = EmptyServicePayload()
        // Tokens
        MDL.loadMockData(AuthPlayerTokenRequest::class, empty, MockData.TokenResponses.NEVER_EXPIRE, -1)
        MDL.loadMockData(AuthTokenRequest::class, empty, MockData.TokenResponses.NEVER_EXPIRE, -1)

        // Versions
        MDL.loadMockData(VersionRequest::class, empty, MockData.VersionResponses.ANDROID_23_RULEBOOK_2206)

        // Sign In Admin Player
        MDL.loadMockData(SignInPlayerRequest::class, empty, MockData.SignInResponses.REGULAR)

        // Update Tracker
        MDL.loadMockData(GetUpdateTrackerRequest::class, empty, MockData.UpdateTrackerResponses.ALL_1S, -1)

        // Announcements
        MDL.loadMockData(GetAllFullAnnouncementsRequest::class, empty, MockData.GetAllAnnouncementsResponses.FIVE_ANNOUNCEMENTS)

        // AWARDS
        MDL.loadMockData(GetAllAwardsRequest::class, empty, MockData.GetAllAwardsResponses.STANDARD)

        // Characters
        MDL.loadMockData(GetAllFullCharactersRequest::class, empty, MockData.GetAllCharacterResponses.STANDARD)

        // Gear
        MDL.loadMockData(GetAllGearRequest::class, empty, MockData.GetAllCharacterGearResponses.STANDARD)

        // Char Skills
        MDL.loadMockData(GetAllCharacterSkillsRequest::class, empty, MockData.GetAllCharacterSkillsResponses.STANDARD)

        // Contacts
        MDL.loadMockData(GetAllContactRequestsRequest::class, empty, MockData.GetAllContactRequestResponses.ONE_READ_ONE_UNREAD)

        // Events
        MDL.loadMockData(GetAllEventsRequest::class, empty, MockData.GetAllEventsResponses.STANDARD)

        // Event Attendees
        MDL.loadMockData(GetAllEventAttendeesRequest::class, empty, MockData.GetAllEventAttendeesResponses.STANDARD)

        // Preregs
        MDL.loadMockData(GetAllPreregsRequest::class, empty, MockData.GetAllPreregResponses.STANDARD)

        // Feature Flags
        MDL.loadMockData(GetAllFeatureFlagsRequest::class, empty, MockData.GetFeatureFlagsResponses.ONE_ON_ONE_OFF)

        // Intrigues
        MDL.loadMockData(GetAllIntriguesRequest::class, empty, MockData.GetAllIntrigueResponses.STANDARD)

        // Players
        MDL.loadMockData(GetAllPlayersRequest::class, empty, MockData.GetAllPlayersResponses.STANDARD)

        // Profile Images
        MDL.loadMockData(GetAllProfileImagesRequest::class, empty, MockData.GetAllProfileImageResponses.STANDARD)

        // Research Projects
        MDL.loadMockData(GetAllResearchProjectsRequest::class, empty, MockData.GetAllResearchProjectResponses.STANDARD)

        // Skills
        MDL.loadMockData(GetAllSkillsRequest::class, empty, MockData.GetAllSkillsResponses.STANDARD)

        // Skill Categories
        MDL.loadMockData(GetAllSkillCategoriesRequest::class, empty, MockData.GetAllSkillCategoriesResponses.STANDARD)

        // Skill Prereqs
        MDL.loadMockData(GetAllSkillPrereqsRequest::class, empty, MockData.GetAllSkillPrereqsResponses.STANDARD)

        // XP Reductions
        MDL.loadMockData(GetAllXpReductionsRequest::class, empty, MockData.GetAllXpReductionResponses.STANDARD)

        // Camp Status
        MDL.loadMockData(GetCampStatusRequest::class, empty, MockData.GetCampStatusResponses.STANDARD)

    }

}