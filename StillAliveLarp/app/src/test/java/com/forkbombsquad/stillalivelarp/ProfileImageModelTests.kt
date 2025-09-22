package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProfileImageModelTests: BaseUnitTestClass {

    @Test
    fun testProfileImageModelFields()  = runTest {
        loadDataManagerHappyPath(this) {
            val playerWithProfileImage = DataManager.shared.getCurrentPlayer()
            val playerWithoutProfileImage = DataManager.shared.players.firstOrNull { it.id == 2 }

            assertNotNull(playerWithProfileImage)
            assertNotNull(playerWithoutProfileImage)

            assertNull(playerWithoutProfileImage!!.profileImage)
            val pf = playerWithProfileImage!!.profileImage

            assertNotNull(pf)
            assertEquals(pf!!.id, 1)
            assertEquals(pf.playerId, 1)
            assertNotEmpty(pf.image)
        }
    }

}