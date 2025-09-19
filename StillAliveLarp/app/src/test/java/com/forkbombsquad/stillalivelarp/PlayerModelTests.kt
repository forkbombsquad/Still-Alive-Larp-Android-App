package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerModelTests: BaseUnitTestClass {

    @Test
    fun testPlayerUtilFunctions() = runTest {
        loadDataManagerHappyPath(this) {
            val player = DataManager.shared.getCurrentPlayer()
            assertNotNull(player)

            // get active character
            val activeChar = player!!.getActiveCharacter()
            assertNotNull(activeChar)
            assertEquals(activeChar!!.id, 1)
            assertEquals(activeChar!!.fullName, "Commander Davis")

            // Character groups
            assertEmpty(player.getInactiveCharacters())
            assertEmpty(player.getPlannedCharacters())

            // Awards
            val awards = player.getAwardsSorted()
            assertEquals(awards.count { it.characterId == null }, 6)
            assertEquals(awards.count(), awards.count { it.characterId == null })

            // Barcode Checkin
            val event = DataManager.shared.events.firstOrNull()
            assertNotNull(event)
            val checkInWithCharacter = player.getCheckInBarcodeModel(true, event!!)
            val checkInWithoutCharacter = player.getCheckInBarcodeModel(false, event)

            assertEquals(checkInWithCharacter.playerId, 1)
            assertEquals(checkInWithCharacter.characterId, 1)
            assertEquals(checkInWithCharacter.eventId, 1)

            assertEquals(checkInWithoutCharacter.playerId, 1)
            assertNull(checkInWithoutCharacter.characterId)
            assertEquals(checkInWithoutCharacter.eventId, 1)

            // Barcode Checkout
            val attendeeChar = EventAttendeeModel(1, 1, 1, 1, "TRUE", "FALSE", -1)
            val attendeeNPC = EventAttendeeModel(1, 1, null, 1, "TRUE", "TRUE", 1)

            val checkoutBCChar = player.getCheckOutBarcodeModel(attendeeChar)
            val checkoutBCNPC = player.getCheckOutBarcodeModel(attendeeNPC)

            assertEquals(checkoutBCChar.playerId, 1)
            assertEquals(checkoutBCChar.characterId, 1)
            assertEquals(checkoutBCChar.eventId, 1)

            assertEquals(checkoutBCNPC.playerId, 1)
            assertNull(checkoutBCNPC.characterId)
            assertEquals(checkoutBCNPC.eventId, 1)

            // Base model
            val xpChange = 12
            val ft1sChange = 16
            val ppCHange = 4
            val basePlayer = player.baseModel()
            val basePlayerMods = player.baseModelWithModifications(xpChange, ft1sChange, ppCHange)

            assertEquals(basePlayer.id, player.id)
            assertEquals(basePlayerMods.id, player.id)
            assertEquals(basePlayer.experience.toInt(), player.experience)
            assertEquals(basePlayerMods.experience.toInt(), player.experience + xpChange)
            assertEquals(basePlayer.isAdmin.toBoolean(), player.isAdmin)
            assertEquals(basePlayerMods.isAdmin.toBoolean(), player.isAdmin)
            assertEquals(basePlayer.fullName, player.fullName)
            assertEquals(basePlayerMods.fullName, player.fullName)
            assertEquals(basePlayer.freeTier1Skills.toInt(), player.freeTier1Skills)
            assertEquals(basePlayerMods.freeTier1Skills.toInt(), player.freeTier1Skills + ft1sChange)
            assertEquals(basePlayer.isCheckedIn.toBoolean(), player.isCheckedIn)
            assertEquals(basePlayerMods.isCheckedIn.toBoolean(), player.isCheckedIn)
            assertEquals(basePlayer.isCheckedInAsNpc.toBoolean(), player.isCheckedInAsNpc)
            assertEquals(basePlayerMods.isCheckedInAsNpc.toBoolean(), player.isCheckedInAsNpc)
            assertEquals(basePlayer.lastCheckIn, player.lastCheckIn)
            assertEquals(basePlayerMods.lastCheckIn, player.lastCheckIn)
            assertEquals(basePlayer.numEventsAttended.toInt(), player.numEventsAttended)
            assertEquals(basePlayerMods.numEventsAttended.toInt(), player.numEventsAttended)
            assertEquals(basePlayer.numNpcEventsAttended.toInt(), player.numNpcEventsAttended)
            assertEquals(basePlayerMods.numNpcEventsAttended.toInt(), player.numNpcEventsAttended)
            assertEquals(basePlayer.username, player.username)
            assertEquals(basePlayerMods.username, player.username)
            assertEquals(basePlayer.prestigePoints.toInt(), player.prestigePoints)
            assertEquals(basePlayerMods.prestigePoints.toInt(), player.prestigePoints + ppCHange)
            assertEquals(basePlayer.isCheckedInAsNpc.toBoolean(), player.isCheckedInAsNpc)
            assertEquals(basePlayerMods.isCheckedInAsNpc.toBoolean(), player.isCheckedInAsNpc)

            // Unique Character Names Rec
            val usedName = "Commander Davis"
            val notUsedName = "Lrrr Ruler of the planet omicron persei 8"
            assertEquals(player.getUniqueCharacterNameRec(usedName), "$usedName 1")
            assertEquals(player.getUniqueCharacterNameRec(notUsedName), notUsedName)
        }
    }

}