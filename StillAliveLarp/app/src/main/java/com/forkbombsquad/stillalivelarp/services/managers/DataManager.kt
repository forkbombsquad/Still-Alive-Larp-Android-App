package com.forkbombsquad.stillalivelarp.services.managers

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.services.*
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.CharactersForTypeWithIdSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Rulebook
import com.forkbombsquad.stillalivelarp.utils.RulebookManager
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

enum class DataManagerType {
    PLAYER, CHARACTER, ANNOUNCEMENTS, EVENTS, AWARDS, INTRIGUE, SKILLS, ALL_PLAYERS, ALL_CHARACTERS, CHAR_FOR_SELECTED_PLAYER, CONTACT_REQUESTS, EVENT_ATTENDEES, XP_REDUCTIONS, EVENT_PREREGS, SELECTED_CHAR_XP_REDUCTIONS, INTRIGUE_FOR_SELECTED_EVENT, SELECTED_CHARACTER_GEAR, RULEBOOK, FEATURE_FLAGS, PROFILE_IMAGE, FULL_CHARACTER_FOR_SELECTED_CHARACTER, EVENT_ATTENDEES_FOR_EVENT, SKILL_CATEGORIES, ALL_PLANNED_CHARACTERS, ALL_NPC_CHARACTERS
}

class DataManager private constructor() {

    var checkinBarcodeModel: PlayerCheckInBarcodeModel? = null
    var checkoutBarcodeModel: PlayerCheckOutBarcodeModel? = null
    var unrelaltedUpdateCallback: () -> Unit = {}

    private var loadCountIndex = 0
    private var targetCount: MutableList<Int> = mutableListOf()
    private var countReturned: MutableList<Int> = mutableListOf()
    private var callbacks: MutableList<() -> Unit> = mutableListOf()
    private var callbackSteps: MutableList<() -> Unit> = mutableListOf()

    var announcements: List<AnnouncementSubModel>? = null
    var currentAnnouncement: AnnouncementModel? = null
    var loadingAnnouncements = true

    var player: PlayerModel? = null
    var loadingPlayer = true

    var character: FullCharacterModel? = null
    var loadingCharacter = true

    var events: List<EventModel>? = null
    var currentEvent: EventModel? = null
    var loadingEvents = true

    var awards: List<AwardModel>? = null
    var loadingAwards = true

    var intrigue: IntrigueModel? = null
    var loadingIntrigue = true

    var skills: List<FullSkillModel>? = null
    var loadingSkills = true

    var allPlayers: List<PlayerModel>? = null
    var loadingAllPlayers = true

    var allCharacters: List<CharacterModel>? = null
    var loadingAllCharacters = true

    var selectedPlayer: PlayerModel? = null

    var charForSelectedPlayer: FullCharacterModel? = null
    var loadingCharForSelectedPlayer = true

    var contactRequests: List<ContactRequestModel>? = null
    var loadingContactRequests = true

    var eventAttendeesForPlayer: List<EventAttendeeModel>? = null
    var loadingEventAttendees = true

    var xpReductions: List<XpReductionModel>? = null
    var loadingXpReductions = true

    var eventPreregs: MutableMap<Int, Array<EventPreregModel>> = mutableMapOf()
    var loadingEventPreregs = false

    var selectedEvent: EventModel? = null
    
    var selectedChar: CharacterModel? = null

    var selectedContactRequest: ContactRequestModel? = null

    var playerCheckInModel: PlayerCheckInBarcodeModel? = null
    var playerCheckOutModel: PlayerCheckOutBarcodeModel? = null

    var activityToClose: NoStatusBarActivity? = null

    var selectedCharacterXpReductions: List<XpReductionModel>? = null
    var loadingSelectedCharacterXpReductions = true

    var intrigueForSelectedEvent: IntrigueModel? = null
    var loadingIntrigueForSelectedEvent = true

    var loadingSelectedCharacterGear = true
    var selectedCharacterGear: Array<GearModel>? = null

    var selectedGear: GearModel? = null

    var rulebook: Rulebook? = null
    var loadingRulebook = true

    var featureFlags: Array<FeatureFlagModel>? = null
    var loadingFeatureFlags = true

    var skillCategories: Array<SkillCategoryModel>? = null
    var loadingSkillCategories = true

    var selectedFeatureFlag: FeatureFlagModel? = null

    var profileImage: ProfileImageModel? = null
    var loadingProfileImage = true

    var fullCharForSelectedChar: FullCharacterModel? = null
    var loadingFullCharForSelectedChar = true

    var eventAttendeesForEvent: List<EventAttendeeModel>? = null
    var loadingEventAttendeesForEvent = true

    var passedBitmap: Bitmap? = null

    var allPlannedCharacters: List<CharacterModel>? = null
    var loadingAllPlannedCharacters = true

    var selectedPlannedCharacter: FullCharacterModel? = null
    var selectedPlannedCharacterCharSkills: List<CharacterSkillModel>? = null

    var allNPCCharacters: List<CharacterModel>? = null
    var loadingAllNPCCharacters = true

    var selectedNPCCharacter: FullCharacterModel? = null

    var gearToEdit: GearJsonModel? = null

    var allOfflineNPCCharacters: List<FullCharacterModel>? = null

    fun load(lifecycleScope: LifecycleCoroutineScope, types: List<DataManagerType>, forceDownloadIfApplicable: Boolean = false, finishedStep: () -> Unit = {}, finished: () -> Unit) {
        val currentLoadCountIndex = loadCountIndex
        loadCountIndex++
        targetCount.add(currentLoadCountIndex, types.count())
        countReturned.add(currentLoadCountIndex, 0)
        callbacks.add(currentLoadCountIndex, finished)
        callbackSteps.add(currentLoadCountIndex, finishedStep)
        if (targetCount[currentLoadCountIndex] == 0) {
            callbacks[currentLoadCountIndex]()
        }

        types.forEach { type ->
            when (type) {
                DataManagerType.PLAYER -> {
                    loadingPlayer = true
                    if (forceDownloadIfApplicable && player?.id != null) {
                        val request = PlayerService.GetPlayer()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(player?.id ?: 0)).ifLet({
                                player = it
                                PlayerManager.shared.updatePlayer(it)
                                loadingPlayer = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                player = PlayerManager.shared.getPlayer()
                                loadingPlayer = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        player = PlayerManager.shared.getPlayer()
                        loadingPlayer = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.CHARACTER -> {
                    loadingCharacter = true
                    CharacterManager.shared.fetchActiveCharacter(lifecycleScope, forceDownloadIfApplicable) {
                        character = it
                        loadingCharacter = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.ANNOUNCEMENTS -> {
                    loadingAnnouncements = true
                    AnnouncementManager.shared.getAnnouncements(lifecycleScope, forceDownloadIfApplicable) { anments ->
                        announcements = anments.reversed().ifEmpty {
                            null
                        }
                        announcements?.firstOrNull().ifLet({ an ->
                            AnnouncementManager.shared.getAnnouncement(lifecycleScope, an.id) { firstAn ->
                                currentAnnouncement = firstAn
                                loadingAnnouncements = false
                                finishedRequest(currentLoadCountIndex)
                            }
                        }, {
                            loadingAnnouncements = false
                            finishedRequest(currentLoadCountIndex)
                        })
                    }
                }
                DataManagerType.EVENTS -> {
                    loadingEvents = true
                    EventManager.shared.getEvents(lifecycleScope, forceDownloadIfApplicable) { eventList ->
                        eventList.ifLet({
                            events = it.reversed()
                            currentEvent = it.reversed().firstOrNull()
                            if (loadingIntrigue) {
                                it.firstOrNull { ev -> ev.isStarted.toBoolean() && !ev.isFinished.toBoolean() }.ifLet({ current ->
                                    currentEvent = current
                                    val intrigueRequest = IntrigueService.GetIntrigueForEvent()
                                    lifecycleScope.launch {
                                        intrigueRequest.successfulResponse(IdSP(current.id)).ifLet({ intr ->
                                            loadingIntrigue = false
                                            intrigue = intr
                                            finishedRequest(currentLoadCountIndex)
                                        }, {
                                            intrigue = null
                                            loadingIntrigue = false
                                            finishedRequest(currentLoadCountIndex)
                                        })
                                    }
                                }, {
                                    loadingIntrigue = false
                                    intrigue = null
                                    finishedRequest(currentLoadCountIndex)
                                })
                            }
                            loadingEvents = false
                            finishedRequest(currentLoadCountIndex)
                        }, {
                            loadingEvents = false
                            finishedRequest(currentLoadCountIndex)
                            if (loadingIntrigue) {
                                loadingIntrigue = false
                                intrigue = null
                                finishedRequest(currentLoadCountIndex)
                            }
                        })
                    }
                }
                DataManagerType.AWARDS -> {
                    loadingAwards = true
                    if (awards == null || forceDownloadIfApplicable) {
                        val awardRequestService = AwardService.GetAllAwardsForPlayer()
                        lifecycleScope.launch {
                            awardRequestService.successfulResponse(IdSP(player?.id ?: 0)).ifLet({
                                loadingAwards = false
                                awards = it.awards.reversed()
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                loadingAwards = false
                                awards = null
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingAwards = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.INTRIGUE -> {
                    loadingIntrigue = true
                    if (events == null && !loadingEvents) {
                        intrigue = null
                        loadingIntrigue = false
                        finishedRequest(currentLoadCountIndex)
                    } else if (events != null) {
                        events.ifLet({
                            it.firstOrNull { ev -> ev.isStarted.toBoolean() && !ev.isFinished.toBoolean() }.ifLet({ current ->
                                val intrigueRequest = IntrigueService.GetIntrigueForEvent()
                                lifecycleScope.launch {
                                    intrigueRequest.successfulResponse(IdSP(current.id)).ifLet({ intr ->
                                        loadingIntrigue = false
                                        intrigue = intr
                                        finishedRequest(currentLoadCountIndex)
                                    }, {
                                        intrigue = null
                                        loadingIntrigue = false
                                        finishedRequest(currentLoadCountIndex)
                                    })
                                }
                            }, {
                                intrigue = null
                                loadingIntrigue = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }, {
                            intrigue = null
                            loadingIntrigue = false
                            finishedRequest(currentLoadCountIndex)
                        })
                    }
                }
                DataManagerType.SKILLS -> {
                    loadingSkills = true
                    SkillManager.shared.getSkills(lifecycleScope, forceDownloadIfApplicable) {
                        skills = it
                        loadingSkills = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.ALL_PLAYERS -> {
                    loadingAllPlayers = true
                    if (allPlayers == null || forceDownloadIfApplicable) {
                        val request = PlayerService.GetAllPlayers()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                allPlayers = it.players.filter { p -> p.username.lowercase() != "googletestaccount@gmail.com" }.toList()
                                loadingAllPlayers = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                allPlayers = null
                                loadingAllPlayers = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingAllPlayers = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.CHAR_FOR_SELECTED_PLAYER -> {
                    loadingCharForSelectedPlayer = true
                    selectedPlayer.ifLet({
                        if (charForSelectedPlayer == null || charForSelectedPlayer?.playerId != it.id || forceDownloadIfApplicable) {
                            CharacterManager.shared.getActiveCharacterForOtherPlayer(lifecycleScope, it.id) { char ->
                                charForSelectedPlayer = char
                                loadingCharForSelectedPlayer = false
                                finishedRequest(currentLoadCountIndex)
                            }
                        } else {
                            loadingCharForSelectedPlayer = false
                            finishedRequest(currentLoadCountIndex)
                        }
                    }, {
                        charForSelectedPlayer = null
                        loadingCharForSelectedPlayer = false
                        finishedRequest(currentLoadCountIndex)
                    })
                }
                DataManagerType.ALL_CHARACTERS -> {
                    loadingAllCharacters = true
                    if (allCharacters == null || forceDownloadIfApplicable) {
                        val request = CharacterService.GetAllCharacters()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                allCharacters = it.characters.filter { c -> c.fullName.lowercase() != "google test" }.toList()
                                loadingAllCharacters = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                allCharacters = null
                                loadingAllCharacters = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingAllCharacters = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.CONTACT_REQUESTS -> {
                    loadingContactRequests = true
                    if (contactRequests == null || forceDownloadIfApplicable) {
                        val request = AdminService.GetAllContactRequests()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                contactRequests = it.contactRequests.toList()
                                loadingContactRequests = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                contactRequests = null
                                loadingContactRequests = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingContactRequests = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.EVENT_ATTENDEES -> {
                    loadingEventAttendees = true
                    if (eventAttendeesForPlayer == null || forceDownloadIfApplicable) {
                        val request = EventAttendeeService.GetEventsForPlayer()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(player?.id ?: 0)).ifLet({
                                eventAttendeesForPlayer = it.eventAttendees.toList()
                                loadingEventAttendees = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                eventAttendeesForPlayer = null
                                loadingEventAttendees = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingEventAttendees = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.XP_REDUCTIONS -> {
                    loadingXpReductions = true
                    if (xpReductions == null || forceDownloadIfApplicable) {
                        val request = SpecialClassXpReductionService.GetXpReductionsForCharacter()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(character?.id ?: 0)).ifLet({
                                xpReductions = it.specialClassXpReductions.toList()
                                loadingXpReductions = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                xpReductions = null
                                loadingXpReductions = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingXpReductions = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.EVENT_PREREGS -> {
                    loadingEventPreregs = true
                    if (hasEventsWithoutPreregs() || (forceDownloadIfApplicable && !events.isNullOrEmpty())) {
                        val request = EventPreregService.GetPreregsForEvent()
                        var count = 0
                        var max = events?.count() ?: 0
                        for (event in events ?: listOf()) {
                            lifecycleScope.launch {
                                request.successfulResponse(IdSP(event.id)).ifLet({
                                    eventPreregs[event.id] = it.eventPreregs
                                    count++
                                    if (count == max) {
                                        loadingEventPreregs = false
                                        finishedRequest(currentLoadCountIndex)
                                    }
                                }, {
                                    eventPreregs.remove(event.id)
                                    count++
                                    if (count == max) {
                                        loadingEventPreregs = false
                                        finishedRequest(currentLoadCountIndex)
                                    }
                                })
                            }
                        }
                    } else {
                        loadingEventPreregs = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.SELECTED_CHAR_XP_REDUCTIONS -> {
                    loadingSelectedCharacterXpReductions = true
                    if (selectedCharacterXpReductions == null || forceDownloadIfApplicable) {
                        val request = SpecialClassXpReductionService.GetXpReductionsForCharacter()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(selectedChar?.id ?: 0)).ifLet({
                                selectedCharacterXpReductions = it.specialClassXpReductions.toList()
                                loadingSelectedCharacterXpReductions = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                selectedCharacterXpReductions = null
                                loadingSelectedCharacterXpReductions = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingSelectedCharacterXpReductions = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.INTRIGUE_FOR_SELECTED_EVENT -> {
                    loadingIntrigueForSelectedEvent = true
                    if (intrigueForSelectedEvent == null || forceDownloadIfApplicable) {
                        val request = IntrigueService.GetIntrigueForEvent()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(selectedEvent?.id ?: 0), true).ifLet({
                                intrigueForSelectedEvent = it
                                loadingIntrigueForSelectedEvent = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                intrigueForSelectedEvent = null
                                loadingIntrigueForSelectedEvent = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingIntrigueForSelectedEvent = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.SELECTED_CHARACTER_GEAR -> {
                    loadingSelectedCharacterGear = true
                    if (selectedCharacterGear == null || forceDownloadIfApplicable || selectedCharacterGear?.firstOrNull()?.characterId != selectedChar?.id) {
                        val request = GearService.GetAllGearForCharacter()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(selectedChar?.id ?: 0), true).ifLet({
                                if (selectedChar?.id == character?.id && character != null) {
                                    // Store gear in shared prefs
                                    SharedPrefsManager.shared.storeGear(it)
                                }

                                selectedCharacterGear = it.charGear
                                loadingSelectedCharacterGear = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                selectedCharacterGear = null
                                loadingSelectedCharacterGear = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingSelectedCharacterGear = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.RULEBOOK -> {
                    loadingRulebook = true
                    if (rulebook == null || forceDownloadIfApplicable) {
                        RulebookManager.shared.getOnlineVersion(lifecycleScope) { rbop ->
                            rbop.ifLet({ rb ->
                                rulebook = rb
                                loadingRulebook = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                loadingRulebook = false
                                rulebook = null
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingRulebook = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.FEATURE_FLAGS -> {
                    loadingFeatureFlags = true
                    if (featureFlags == null || forceDownloadIfApplicable) {
                        val request = FeatureFlagService.GetAllFeatureFlags()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                featureFlags = it.featureFlags
                                loadingFeatureFlags = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                featureFlags = null
                                loadingFeatureFlags = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingFeatureFlags = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.PROFILE_IMAGE -> {
                    loadingProfileImage = true
                    if (profileImage == null || forceDownloadIfApplicable || selectedPlayer?.id != profileImage?.id) {
                        profileImage = null
                        val request = ProfileImageService.GetProfileImage()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(selectedPlayer?.id ?: 0), true).ifLet({
                                profileImage = it
                                loadingProfileImage = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                profileImage = null
                                loadingProfileImage = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingProfileImage = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.FULL_CHARACTER_FOR_SELECTED_CHARACTER -> {
                    loadingFullCharForSelectedChar = true
                    if (fullCharForSelectedChar == null || forceDownloadIfApplicable || fullCharForSelectedChar?.id != selectedChar?.id) {
                        loadingCharForSelectedPlayer = true
                        selectedChar.ifLet({
                            CharacterManager.shared.getActiveCharacterForOtherPlayer(lifecycleScope, it.playerId) { char ->
                                fullCharForSelectedChar = char
                                loadingFullCharForSelectedChar = false
                                finishedRequest(currentLoadCountIndex)
                            }
                        }, {
                            fullCharForSelectedChar = null
                            loadingFullCharForSelectedChar = false
                            finishedRequest(currentLoadCountIndex)
                        })
                    } else {
                        loadingFullCharForSelectedChar = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.EVENT_ATTENDEES_FOR_EVENT -> {
                    loadingEventAttendeesForEvent = true
                    if (eventAttendeesForEvent == null || forceDownloadIfApplicable) {
                        val request = EventAttendeeService.GetAttendeesForEvent()
                        lifecycleScope.launch {
                            request.successfulResponse(IdSP(selectedEvent?.id ?: 0)).ifLet({
                                eventAttendeesForEvent = it.eventAttendees.toList()
                                loadingEventAttendeesForEvent = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                eventAttendeesForEvent = null
                                loadingEventAttendeesForEvent = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingEventAttendeesForEvent = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.SKILL_CATEGORIES -> {
                    loadingSkillCategories = true
                    if (skillCategories == null || forceDownloadIfApplicable) {
                        val request = SkillCategoryService.GetAllSkillCategories()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                skillCategories = it.skillCategories
                                loadingSkillCategories = false
                                finishedRequest(currentLoadCountIndex)
                                SharedPrefsManager.shared.storeSkillCategories(it.skillCategories.toList())
                            }, {
                                skillCategories = null
                                loadingSkillCategories = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingSkillCategories = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.ALL_PLANNED_CHARACTERS -> {
                    loadingAllPlannedCharacters = true
                    if (allPlannedCharacters == null || forceDownloadIfApplicable) {
                        val request = CharacterService.GetAllPlayerCharactersForCharacterType()
                        lifecycleScope.launch {
                            request.successfulResponse(CharactersForTypeWithIdSP(player?.id ?: -1, Constants.CharacterTypes.Planner)).ifLet({
                                allPlannedCharacters = it.characters.toList()
                                loadingAllPlannedCharacters = false
                                finishedRequest(currentLoadCountIndex)
                            }, {
                                allPlannedCharacters = null
                                loadingAllPlannedCharacters = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingAllPlannedCharacters = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
                DataManagerType.ALL_NPC_CHARACTERS -> {
                    loadingAllNPCCharacters = true
                    if (allNPCCharacters == null || forceDownloadIfApplicable) {
                        val request = CharacterService.GetAllNPCCharacters()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                allNPCCharacters = it.characters.toList()
                                loadingAllNPCCharacters = false
                                finishedRequest(currentLoadCountIndex)
                                storeNpcs(lifecycleScope)
                            }, {
                                allNPCCharacters = null
                                loadingAllNPCCharacters = false
                                finishedRequest(currentLoadCountIndex)
                            })
                        }
                    } else {
                        loadingAllNPCCharacters = false
                        finishedRequest(currentLoadCountIndex)
                    }
                }
            }
        }
    }

    private fun hasEventsWithoutPreregs(): Boolean {
        var hasEventWithoutPreregs = false
        for (event in events ?: listOf()) {
            if (event.isInFuture() && eventPreregs[event.id] == null) {
                hasEventWithoutPreregs = true
                break
            }
        }
        return hasEventWithoutPreregs
    }

    private fun finishedRequest(currentLoadCountIndex: Int) {
        countReturned[currentLoadCountIndex]++
//        globalPrint("REQUEST FINISHED ${countReturned[currentLoadCountIndex]} out of ${targetCount[currentLoadCountIndex]}")
        if (targetCount[currentLoadCountIndex] == countReturned[currentLoadCountIndex]) {
            callbacks[currentLoadCountIndex]()
            // Reset values to save memory
            countReturned[currentLoadCountIndex] = 0
            targetCount[currentLoadCountIndex] = 0
            callbacks[currentLoadCountIndex] = {}
            callbackSteps[currentLoadCountIndex] = {}
        } else {
            callbackSteps[currentLoadCountIndex]()
        }
    }
    private fun storeNpcs(lifecycleScope: LifecycleCoroutineScope) {
        val fullNPCs: MutableList<FullCharacterModel> = mutableListOf()
        var counter = 0
        var actuallyStoreThem = {
            SharedPrefsManager.shared.storeNPCs(fullNPCs)
        }
        allNPCCharacters?.forEach {
            CharacterManager.shared.fetchFullCharacter(lifecycleScope, it.id) { fullChar ->
                if (fullChar != null) {
                    fullNPCs.add(fullChar)
                }
                counter += 1
                if (counter == (allNPCCharacters?.size ?: 0)) {
                    actuallyStoreThem()
                }
            }
        }
    }

    fun getGearOrganzied(): Map<String, List<GearJsonModel>> {
        val gear = selectedCharacterGear?.firstOrNull()?.jsonModels
        if (gear != null) {
            var firearms: MutableList<GearJsonModel> = mutableListOf()
            var melee: MutableList<GearJsonModel> = mutableListOf()
            var clothing: MutableList<GearJsonModel> = mutableListOf()
            var accessory: MutableList<GearJsonModel> = mutableListOf()
            var bag: MutableList<GearJsonModel> = mutableListOf()
            var other: MutableList<GearJsonModel> = mutableListOf()
            gear.forEach { jg ->
                when (jg.gearType) {
                    Constants.GearTypes.firearm -> firearms.add(jg)
                    Constants.GearTypes.meleeWeapon -> melee.add(jg)
                    Constants.GearTypes.clothing -> clothing.add(jg)
                    Constants.GearTypes.accessory -> accessory.add(jg)
                    Constants.GearTypes.bag -> bag.add(jg)
                    Constants.GearTypes.other -> other.add(jg)
                }
            }

            // Sorting
            firearms = firearms.sortedWith(
                compareBy(
                    { if (it.isPrimaryFirearm()) 0 else 1 },
                    {
                        when (it.primarySubtype) {
                            Constants.GearPrimarySubtype.lightFirearm -> 0
                            Constants.GearPrimarySubtype.mediumFirearm -> 1
                            Constants.GearPrimarySubtype.heavyFirearm -> 2
                            Constants.GearPrimarySubtype.advancedFirearm -> 3
                            Constants.GearPrimarySubtype.militaryGradeFirearm -> 4
                            else -> Int.MAX_VALUE
                        }
                    }
                )
            ).toMutableList()

            melee = melee.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.superLightMeleeWeapon -> 0
                        Constants.GearPrimarySubtype.lightMeleeWeapon -> 1
                        Constants.GearPrimarySubtype.mediumMeleeWeapon -> 2
                        Constants.GearPrimarySubtype.heavyMeleeWeapon -> 3
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            accessory = accessory.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.blacklightFlashlight -> 0
                        Constants.GearPrimarySubtype.flashlight -> 1
                        Constants.GearPrimarySubtype.other -> 2
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            bag = bag.sortedWith(
                compareBy {
                    when (it.primarySubtype) {
                        Constants.GearPrimarySubtype.smallBag -> 0
                        Constants.GearPrimarySubtype.mediumBag -> 1
                        Constants.GearPrimarySubtype.largeBag -> 2
                        Constants.GearPrimarySubtype.extraLargeBag -> 3
                        else -> Int.MAX_VALUE
                    }
                }
            ).toMutableList()

            // Adding to map
            return mapOf(
                Pair(Constants.GearTypes.firearm, firearms),
                Pair(Constants.GearTypes.meleeWeapon, melee),
                Pair(Constants.GearTypes.clothing, clothing),
                Pair(Constants.GearTypes.accessory, accessory),
                Pair(Constants.GearTypes.bag, bag),
                Pair(Constants.GearTypes.other, other)
            )

        } else {
            return mapOf()
        }
    }

    companion object {
        var shared = DataManager()
        private set

        fun forceReset() {
            shared = DataManager()
        }
    }

}