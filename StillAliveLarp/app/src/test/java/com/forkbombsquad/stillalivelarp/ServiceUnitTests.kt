package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.mockdata.MockData
import com.forkbombsquad.stillalivelarp.services.GetAllPlayersRequest
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceUnitTests {
    @Test
    fun testService() = runTest {
        setupTests()
        val response = MockService(PlayerService.GetAllPlayers()).successfulResponse()
        assertTrue(response?.players?.count() == 33)
    }

    private fun setupTests() {
        MockDataLoader.shared.loadMockData(GetAllPlayersRequest::class, EmptyServicePayload(), MockData.GET_ALL_PLAYERS_RESPONSE)
    }
}