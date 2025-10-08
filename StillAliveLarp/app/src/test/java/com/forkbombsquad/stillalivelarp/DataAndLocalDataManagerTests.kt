package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.utils.BaseUnitTestClass
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class DataAndLocalDataManagerTests: BaseUnitTestClass {

    @Test
    fun testDataManagerAndLocalDataManagerWorkInTandem() = runTest {
        var previousLoadingString = ""
        DataManager.shared.load(this, stepFinished = {
            if (previousLoadingString == "") {
                previousLoadingString = DataManager.shared.loadingText
            } else {
                assertTrue(previousLoadingString.length > DataManager.shared.loadingText.length)
            }
        }, finished = {
            val DMT = DataManager.shared
            assertTrue(DMT.announcements.isNotEmpty())
            assertTrue(DMT.contactRequests.isNotEmpty())
            assertTrue(DMT.featureFlags.isNotEmpty())
            assertTrue(DMT.intrigues.isNotEmpty())
            assertTrue(DMT.researchProjects.isNotEmpty())
            assertTrue(DMT.skills.isNotEmpty())
            assertTrue(DMT.events.isNotEmpty())
            assertTrue(DMT.getAllCharacters().isNotEmpty())
            assertTrue(DMT.players.isNotEmpty())
            assertNotNull(DMT.rulebook)
            assertNull(DMT.treatingWounds) // TODO when this is mocked, change it here
            assertNotNull(DMT.campStatus)
        })
    }

    @Test
    fun testDataManagerUtilityFunctions() = runTest {
        loadDataManagerHappyPath(this) {
            val dmt = DataManager.shared

            // Offline Mode
            assertFalse(dmt.offlineMode)
            dmt.setOfflineModeExternally(true)
            assertTrue(dmt.offlineMode)
            dmt.setOfflineModeExternally(false)

            // Player is current player
            assertTrue(dmt.playerIsCurrentPlayer(1))

            // get Skills as FCMSM
            val fcm = dmt.getSkillsAsFCMSM()
            assertTrue(dmt.skills.count() ==  fcm.count())
            for (skill in fcm) {
                assertNotNull(dmt.skills.firstOrNull { it.id == skill.id })
            }

            // get current player
            val p = dmt.getCurrentPlayer()
            assertNotNull(p)
            assertTrue(p!!.id == 1)
            assertEquals(p.fullName, "Smidge Raker")

            // get player for character
            val player = dmt.getPlayerForCharacter(p.characters.first())
            assertEquals(p.id, player.id)

            // Get Active Character
            val char = dmt.getActiveCharacter()
            assertNotNull(char)
            assertTrue(char!!.id == 1)
            assertEquals(char.fullName, "Commander Davis")

            // Get all characters of type
            val standards = dmt.getAllCharacters(CharacterType.STANDARD)
            val npcs = dmt.getAllCharacters(CharacterType.NPC)
            val planner = dmt.getAllCharacters(CharacterType.PLANNER)
            val hidden = dmt.getAllCharacters(CharacterType.HIDDEN)
            val npcAndPlanner = dmt.getAllCharacters(listOf(CharacterType.NPC, CharacterType.PLANNER))
            val allChars = dmt.getAllCharacters()
            assertEquals(allChars.count(), standards.count() + npcs.count() + planner.count() + hidden.count())
            assertEquals(npcAndPlanner.count(), npcs.count() + planner.count())

            // Get Character
            val gchar = dmt.getCharacter(1)
            assertNotNull(gchar)
            assertEquals(gchar!!.fullName, char.fullName)

            // Get ongoing event and get ongoing or today event. Should all be null because no events are started or today
            assertNull(dmt.getOngoingEvent())
            assertNull(dmt.getOngoingOrTodayEvent())

            // Get characters who need bio approval
            assertTrue(dmt.getCharactersWhoNeedBiosApproved().isEmpty())
        }
    }

}