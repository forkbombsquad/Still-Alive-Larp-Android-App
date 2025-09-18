package com.forkbombsquad.stillalivelarp.services.managers

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.AnnouncementService
import com.forkbombsquad.stillalivelarp.services.AwardService
import com.forkbombsquad.stillalivelarp.services.CampStatusService
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.ContactRequestService
import com.forkbombsquad.stillalivelarp.services.EventAttendeeService
import com.forkbombsquad.stillalivelarp.services.EventPreregService
import com.forkbombsquad.stillalivelarp.services.EventService
import com.forkbombsquad.stillalivelarp.services.FeatureFlagService
import com.forkbombsquad.stillalivelarp.services.GearService
import com.forkbombsquad.stillalivelarp.services.IntrigueService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.ProfileImageService
import com.forkbombsquad.stillalivelarp.services.ResearchProjectService
import com.forkbombsquad.stillalivelarp.services.SkillCategoryService
import com.forkbombsquad.stillalivelarp.services.SkillPrereqService
import com.forkbombsquad.stillalivelarp.services.SkillService
import com.forkbombsquad.stillalivelarp.services.SpecialClassXpReductionService
import com.forkbombsquad.stillalivelarp.services.UpdateTrackerService
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.models.CampFortification
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.Rulebook
import com.forkbombsquad.stillalivelarp.utils.capitalizeOnlyFirstLetterOfEachWord
import com.forkbombsquad.stillalivelarp.utils.getFragmentOrActivityName
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.isUnitTesting
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KClass

enum class DataManagerPassedDataKey {
    BARCODE,
    SELECTED_EVENT,
    SELECTED_PLAYER,
    SELECTED_CHARACTER,
    AWARDS_LIST,
    CHARACTER_LIST,
    DESTINATION_CLASS,
    VIEW_TITLE,
    PLAYER_LIST,
    EVENT_LIST,
    ADDITIONAL_DESTINATION_CLASS,
    CONTACT_REQUEST_LIST,
    SELECTED_CONTACT_REQUEST,
    FEATURE_FLAG_LIST,
    SELECTED_FEATURE_FLAG,
    RESEARCH_PROJECT_LIST,
    SKILL_LIST,
    ACTION,
    RULEBOOK,
    IMAGE,
    CAMP_STATUS
}

enum class DataManagerType(val localDataKey: String) {
    UPDATE_TRACKER("updateTracker_dm_sp_key"),
    ANNOUNCEMENTS("announcements_dm_sp_key"),
    AWARDS("awards_dm_sp_key"),
    CHARACTERS("characters_dm_sp_key"),
    GEAR("gear_dm_sp_key"),
    CHARACTER_SKILLS("characterSkills_dm_sp_key"),
    CONTACT_REQUESTS("contactRequests_dm_sp_key"),
    EVENTS("events_dm_sp_key"),
    EVENT_ATTENDEES("eventAttendees_dm_sp_key"),
    PREREGS("preregs_dm_sp_key"),
    FEATURE_FLAGS("featureFlags_dm_sp_key"),
    INTRIGUES("intrigues_dm_sp_key"),
    PLAYERS("players_dm_sp_key"),
    PROFILE_IMAGES("profileImages_dm_sp_key"),
    RESEARCH_PROJECTS("researchProjects_dm_sp_key"),
    SKILLS("skills_dm_sp_key"),
    SKILL_CATEGORIES("skillCategories_dm_sp_key"),
    SKILL_PREREQS("skillPrereqs_dm_sp_key"),
    XP_REDUCTIONS("xpReductions_dm_sp_key"),
    RULEBOOK("rulebook_dm_sp_key"),
    TREATING_WOUNDS("treatingwounds_dm_sp_key"),
    CAMP_STATUS("campStatus_dm_sp_key");
}

enum class DataManagerLoadType {
    OFFLINE, DOWNLOAD_IF_NECESSARY, FORCE_DOWNLOAD_ALL
}

class DataManager private constructor() {
    // Global Settings
    var offlineMode: Boolean = false
    private val _offlineMode = MutableStateFlow(false)
    val offlineModeFlow: StateFlow<Boolean> = _offlineMode
    private fun _updateOfflineMode(new: Boolean) {
        offlineMode = new
        _offlineMode.value = new
    }

    var currentPlayerId: Int = -1
    private val _currentPlayerId = MutableStateFlow(-1)
    val currentPlayerIdFlow: StateFlow<Int> = _currentPlayerId
    private fun _updateCurrentPlayerId(newPlayerId: Int) {
        currentPlayerId = newPlayerId
        _currentPlayerId.value = newPlayerId
    }

    var updateCallbacks: MutableMap<String, () -> Unit> = mutableMapOf()
        private set

    var passedData: MutableMap<String, Any> = mutableMapOf()

    // Unchanged From Server
    var announcements: List<AnnouncementModel> = listOf()
    private val _announcements = MutableStateFlow<List<AnnouncementModel>>(listOf())
    val announcementsFlow: StateFlow<List<AnnouncementModel>> = _announcements
    private fun _updateAnnouncements(new: List<AnnouncementModel>) {
        announcements = new
        _announcements.value = new
    }

    var contactRequests: List<ContactRequestModel> = listOf()
    private val _contactRequests = MutableStateFlow<List<ContactRequestModel>>(listOf())
    val contactRequestsFlow: StateFlow<List<ContactRequestModel>> = _contactRequests
    private fun _updateContactRequests(new: List<ContactRequestModel>) {
        contactRequests = new
        _contactRequests.value = new
    }

    var featureFlags: List<FeatureFlagModel> = listOf()
    private val _featureFlags = MutableStateFlow<List<FeatureFlagModel>>(listOf())
    val featureFlagsFlow: StateFlow<List<FeatureFlagModel>> = _featureFlags
    private fun _updateFeatureFlags(new: List<FeatureFlagModel>) {
        featureFlags = new
        _featureFlags.value = new
    }

    var intrigues: Map<Int, IntrigueModel> = mapOf()
    private val _intrigues = MutableStateFlow<Map<Int, IntrigueModel>>(mapOf())
    val intriguesFlow: StateFlow<Map<Int, IntrigueModel>> = _intrigues
    private fun _updateIntrigues(new: Map<Int, IntrigueModel>) {
        intrigues = new
        _intrigues.value = new
    }

    var researchProjects: List<ResearchProjectModel> = listOf()
    private val _researchProjects = MutableStateFlow<List<ResearchProjectModel>>(listOf())
    val researchProjectsFlow: StateFlow<List<ResearchProjectModel>> = _researchProjects
    private fun _updateResearchProjects(new: List<ResearchProjectModel>) {
        researchProjects = new
        _researchProjects.value = new
    }

    // Built Models
    var skills: List<FullSkillModel> = listOf()
    private val _skills = MutableStateFlow<List<FullSkillModel>>(listOf())
    val skillsFlow: StateFlow<List<FullSkillModel>> = _skills
    private fun _updateSkills(new: List<FullSkillModel>) {
        skills = new
        _skills.value = new
    }

    var events: List<FullEventModel> = listOf()
    private val _events = MutableStateFlow<List<FullEventModel>>(listOf())
    val eventsFlow: StateFlow<List<FullEventModel>> = _events
    private fun _updateEvents(new: List<FullEventModel>) {
        events = new
        _events.value = new
    }

    private var characters: List<FullCharacterModel> = listOf()
    private val _characters = MutableStateFlow<List<FullCharacterModel>>(listOf())
    val charactersFlow: StateFlow<List<FullCharacterModel>> = _characters
    private fun _updateCharacters(new: List<FullCharacterModel>) {
        characters = new
        _characters.value = new
    }

    var players: List<FullPlayerModel> = listOf()
    private val _players = MutableStateFlow<List<FullPlayerModel>>(listOf())
    val playersFlow: StateFlow<List<FullPlayerModel>> = _players
    private fun _updatePlayers(new: List<FullPlayerModel>) {
        players = new
        _players.value = new
    }

    var rulebook: Rulebook? = null
    private val _rulebook = MutableStateFlow<Rulebook?>(null)
    val rulebookFlow: StateFlow<Rulebook?> = _rulebook
    private fun _updateRulebook(new: Rulebook?) {
        rulebook = new
        _rulebook.value = new
    }

    var treatingWounds: Bitmap? = null
    private val _treatingWounds = MutableStateFlow<Bitmap?>(null)
    val treatingWoundsFlow: StateFlow<Bitmap?> = _treatingWounds
    private fun _updateTreatingWounds(new: Bitmap?) {
        treatingWounds = new
        _treatingWounds.value = new
    }

    var campStatus: CampStatusModel? = null
    private val _campStatus = MutableStateFlow<CampStatusModel?>(null)
    val campStatusFlow: StateFlow<CampStatusModel?> = _campStatus
    private fun _updateCampStatus(new: CampStatusModel?) {
        campStatus = new
        _campStatus.value = new
    }

    var loading: Boolean = false
    private val _loading = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loading
    private fun _updateLoading(new: Boolean) {
        loading = new
        _loading.value = new
    }

    var loadingText = ""
    private val _loadingText = MutableStateFlow("")
    val loadingTextFlow: StateFlow<String> = _loadingText
    private fun _updateLoadingText(new: String) {
        loadingText = new
        _loadingText.value = new
    }


    // DM variables
    private var firstLoad: Boolean = true
    private var callbacks: MutableList<() -> Unit> = mutableListOf()
    private var stepCallbacks: MutableList<() -> Unit> = mutableListOf()
    private var currentUpdateTracker: UpdateTrackerModel = UpdateTrackerModel.empty()
    private var updatesNeeded: MutableList<DataManagerType> = mutableListOf()
    private var updatesCompleted: MutableList<DataManagerType> = mutableListOf()
    private var finishedCount = 0

    private val mutexThreadLocker = Mutex()
    private val finishedCountMutexThreadLocker = Mutex()

    private var activitiesToClose: MutableList<Activity> = mutableListOf()

    //
    // Editable in place Variables
    //

    var characterToEdit: FullCharacterModel? = null
    var gearToEdit: GearJsonModel? = null
    var fortificationToEdit: CampFortification? = null

    fun load(lifecycleScope: CoroutineScope, loadType: DataManagerLoadType = DataManagerLoadType.DOWNLOAD_IF_NECESSARY, stepFinished: () -> Unit = {}, finished: () -> Unit) {
        var modLoadType = loadType
        if (offlineMode) {
            modLoadType = DataManagerLoadType.OFFLINE
        }
        lifecycleScope.launch {
            var previousLoading: Boolean
            mutexThreadLocker.withLock {
                previousLoading = loading
                stepCallbacks.add(stepFinished)
                callbacks.add(finished)
                _updateLoading(true)
            }
            if (!previousLoading) {
                when (modLoadType) {
                    DataManagerLoadType.OFFLINE -> loadOffline(lifecycleScope)
                    DataManagerLoadType.DOWNLOAD_IF_NECESSARY -> loadDownloadIfNecessary(lifecycleScope)
                    DataManagerLoadType.FORCE_DOWNLOAD_ALL -> loadForceDownloadAll(lifecycleScope)
                }
            }
        }
    }

    private fun loadOffline(lifecycleScope: CoroutineScope) {
        populateLocalData(lifecycleScope, false)
    }

    private fun loadDownloadIfNecessary(lifecycleScope: CoroutineScope) {
        val updateRequest = UpdateTrackerService.GetUpdateTracker()
        lifecycleScope.launch {
            updateRequest.successfulResponse().ifLet({ updateTrackerModel ->
                this@DataManager.handleUpdates(lifecycleScope, updateTrackerModel)
            }, {
                lifecycleScope.launch {
                    loadOffline(lifecycleScope)
                }
            })
        }
    }

    private fun loadForceDownloadAll(lifecycleScope: CoroutineScope) {
        lifecycleScope.launch {
            mutexThreadLocker.withLock {
                _updateLoadingText("Force Clearing Data...")
                firstLoad = true
                LocalDataManager.shared.storeUpdateTracker(UpdateTrackerModel.empty())
            }
            loadDownloadIfNecessary(lifecycleScope)
        }
    }

    private fun handleUpdates(lifecycleScope: CoroutineScope, updateTracker: UpdateTrackerModel) {
        updatesNeeded = LocalDataManager.shared.determineWhichTypesNeedUpdates(updateTracker).toMutableList()
        updatesCompleted = mutableListOf()
        currentUpdateTracker = updateTracker
        finishedCount = 0
        if (updatesNeeded.isEmpty()) {
            populateLocalData(lifecycleScope, false)
        } else {
            lifecycleScope.launch {
                mutexThreadLocker.withLock {
                    _updateLoadingText(generateLoadingText())
                    stepCallbacks.forEach { it() }
                }
            }
            val updatesNeededCopy = updatesNeeded.toMutableList()
            updatesNeededCopy.forEach { updateType ->
                when (updateType) {
                    DataManagerType.UPDATE_TRACKER -> {}
                    DataManagerType.ANNOUNCEMENTS -> {
                        val request = AnnouncementService.GetAllFullAnnouncements()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeAnnouncements(it.announcements)
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.AWARDS -> {
                        val request = AwardService.GetAllAwards()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeAwards(it.awards.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.CHARACTERS -> {
                        val request = CharacterService.GetAllFullCharacters()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeCharacters(it.characters.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.GEAR -> {
                        val request = GearService.GetAllGear()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeGear(it.charGear.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.CHARACTER_SKILLS -> {
                        val request = CharacterSkillService.GetAllCharacterSkills()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeCharacterSkills(it.charSkills.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.CONTACT_REQUESTS -> {
                        val request = ContactRequestService.GetAllContactRequests()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeContactRequests(it.contactRequests.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.EVENTS -> {
                        val request = EventService.GetAllEvents()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeEvents(it.events.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.EVENT_ATTENDEES -> {
                        val request = EventAttendeeService.GetAllEventAttendees()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeEventAttendees(it.eventAttendees.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.PREREGS -> {
                        val request = EventPreregService.GetAllPreregs()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storePreregs(it.eventPreregs.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.FEATURE_FLAGS -> {
                        val request = FeatureFlagService.GetAllFeatureFlags()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeFeatureFlags(it.featureFlags.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.INTRIGUES -> {
                        val request = IntrigueService.GetAllIntrigues()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeIntrigues(it.intrigues.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.PLAYERS -> {
                        val request = PlayerService.GetAllPlayers()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storePlayers(it.players.toList().filter { p -> p.username.lowercase() != "googletestaccount@gmail.com" })
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.PROFILE_IMAGES -> {
                        val request = ProfileImageService.GetAllProfileImages()
                        lifecycleScope.launch {
                            request.successfulResponse(ignorePrintResopnseBody = true).ifLet({
                                globalPrint("SERVICE CONTROLLER: Profile Images Downloaded")
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeProfileImages(it.profileImages.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.RESEARCH_PROJECTS -> {
                        val request = ResearchProjectService.GetAllResearchProjects()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeResearchProjects(it.researchProjects.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.SKILLS -> {
                        val request = SkillService.GetAllSkills()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeSkills(it.skills.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.SKILL_CATEGORIES -> {
                        val request = SkillCategoryService.GetAllSkillCategories()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeSkillCategories(it.skillCategories.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.SKILL_PREREQS -> {
                        val request = SkillPrereqService.GetAllSkillPrereqs()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeSkillPrereqs(it.skillPrereqs.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.XP_REDUCTIONS -> {
                        val request = SpecialClassXpReductionService.GetAllXpReductions()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeXpReductions(it.specialClassXpReductions.toList())
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.CAMP_STATUS -> {
                        val request = CampStatusService.GetCampStatus()
                        lifecycleScope.launch {
                            request.successfulResponse().ifLet({
                                lifecycleScope.launch {
                                    LocalDataManager.shared.storeCampStatus(it)
                                    serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                }
                            }, {
                                lifecycleScope.launch {
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            })
                        }
                    }
                    DataManagerType.RULEBOOK -> {
                        if (isUnitTesting) {
                            val url = javaClass.getResource("/Rulebook.html")!!
                            val file = File(url.toURI()).readText()
                            lifecycleScope.launch {
                                LocalDataManager.shared.storeRulebook(Rulebook.parseWebDocumentAsRulebook(Jsoup.parse(file), updateTracker.rulebookVersion))
                                serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                            }
                        } else {
                            lifecycleScope.launch {
                                val jsoupAsyncTask = JsoupAsyncTask(Constants.URLs.rulebookUrl) { doc ->
                                    lifecycleScope.launch {
                                        LocalDataManager.shared.storeRulebook(Rulebook.parseWebDocumentAsRulebook(doc, updateTracker.rulebookVersion))
                                        serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                    }
                                }
                                jsoupAsyncTask.execute()
                            }
                        }

                    }
                    DataManagerType.TREATING_WOUNDS -> {
                        // TODO setup mocking for this.
                        lifecycleScope.launch {
                            if (isUnitTesting) {
                                serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                            } else {
                                try {
                                    // 1. Get the document and parse it off the main thread
                                    val imageUrl = withContext(Dispatchers.IO) {
                                        val doc = Jsoup.connect("https://stillalivelarp.com/healing").get()
                                        val imageElement = doc.getElementById("image")
                                        imageElement?.attr("src")
                                    }

                                    if (imageUrl != null) {
                                        // 2. Download the image off the main thread
                                        val bitmap = withContext(Dispatchers.IO) {
                                            val url = URL(imageUrl)
                                            val connection = url.openConnection() as HttpURLConnection
                                            connection.doInput = true
                                            connection.connect()

                                            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                                                val inputStream = connection.inputStream
                                                val bmp = BitmapFactory.decodeStream(inputStream)
                                                inputStream.close()
                                                bmp
                                            } else null
                                        }

                                        // 3. Store the image on the main thread
                                        if (bitmap != null) {
                                            LocalDataManager.shared.storeTreatingWounds(bitmap)
                                            serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                        } else {
                                            serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                        }
                                    } else {
                                        serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateLoadingText(): String {
        var text = "Loading:\n"
        updatesNeeded.forEachIndexed { index, dmt ->
            if (index > 0) {
                // Allow two per line
                text += (index % 2 == 0).ternary("\n", ", ")
            }
            text += dmt.name.capitalizeOnlyFirstLetterOfEachWord()
        }
        text += "..."
        text = text.replace("_", " ")
        return text
    }

    private suspend fun serviceFinished(lifecycleScope: CoroutineScope, type: DataManagerType, succeeded: Boolean, localUpdatesNeeded: List<DataManagerType>) {
        finishedCountMutexThreadLocker.withLock {
            if (succeeded) {
                updatesNeeded.remove(type)
                updatesCompleted.add(type)
            }
            finishedCount += 1
        }
        mutexThreadLocker.withLock {
            _updateLoadingText(generateLoadingText())
            stepCallbacks.forEach { it() }
        }
        if (finishedCount == localUpdatesNeeded.count()) {
            _updateLoadingText("Building Local Data Models...")
            stepCallbacks.forEach { it() }
            LocalDataManager.shared.updatesSucceeded(currentUpdateTracker, updatesCompleted)
            populateLocalData(lifecycleScope, true)
        }
    }

    private fun populateLocalData(lifecycleScope: CoroutineScope, updatesDownloaded: Boolean) {
        lifecycleScope.launch {
            mutexThreadLocker.withLock {
                if (firstLoad || updatesDownloaded) {
                    _updateLoadingText("Populating Data In Memory...")
                    stepCallbacks.forEach { it() }
                    firstLoad = false
                    // Normal Models
                    _updateAnnouncements(LocalDataManager.shared.getAnnouncements())
                    _updateContactRequests(LocalDataManager.shared.getContactRequests())
                    _updateFeatureFlags(LocalDataManager.shared.getFeatureFlags())
                    _updateIntrigues(LocalDataManager.shared.getIntrigues())
                    _updateResearchProjects(LocalDataManager.shared.getResearchProjects())
                    _updateCampStatus(LocalDataManager.shared.getCampStatus())

                    // Built Models
                    _updateSkills(LocalDataManager.shared.getFullSkills())
                    _updateEvents(LocalDataManager.shared.getFullEvents())
                    _updateCharacters(LocalDataManager.shared.getFullCharacters())
                    _updatePlayers(LocalDataManager.shared.getFullPlayers())
                    _updateRulebook(LocalDataManager.shared.getRulebook())
                    _updateTreatingWounds(LocalDataManager.shared.getTreatingWounds())
                    _updateCurrentPlayerId(LocalDataManager.shared.getPlayerId())
                }
                _updateLoadingText("")
                _updateLoading(false)
                stepCallbacks.forEach { it() }
                callbacks.forEach { it() }
                callbacks = mutableListOf()
                stepCallbacks = mutableListOf()
            }
        }
    }

    companion object {
        var shared = DataManager()
            private set

        fun forceReset() {
            shared = DataManager()
        }
    }

    //
    // Utility Functions
    //

    fun setOfflineModeExternally(enabled: Boolean) {
        _updateOfflineMode(enabled)
    }

    fun setCurrentPlayerIdExternally(id: Int) {
        LocalDataManager.shared.storePlayerId(id)
        _updateCurrentPlayerId(id)
    }

    fun setCurrentPlayerIdExternally(player: PlayerModel) {
        setCurrentPlayerIdExternally(player.id)
    }

    fun setTitleTextPotentiallyOffline(tv: TextView, baseText: String) {

        tv.text = offlineMode.ternary("$baseText\n[Offline]", baseText)
    }

    fun setUpdateCallback(key: KClass<*>, callback: () -> Unit) {
        updateCallbacks[getFragmentOrActivityName(key)] = callback
    }

    fun clearUpdateCallback(key: KClass<*>) {
        updateCallbacks.remove(getFragmentOrActivityName(key))
    }

    fun setPassedData(key: KClass<*>, dataKey: DataManagerPassedDataKey, data: Any) {
        passedData[getFragmentOrActivityName(key) + dataKey.toString()] = data
    }

    fun clearPassedData(key: KClass<*>, dataKey: DataManagerPassedDataKey) {
        passedData.remove(getFragmentOrActivityName(key) + dataKey.toString())
    }

    fun playerIsCurrentPlayer(id: Int): Boolean {
        return id == currentPlayerId
    }

    fun playerIsCurrentPlayer(player: FullPlayerModel): Boolean {
        return playerIsCurrentPlayer(player.id)
    }

    fun addActivityToClose(activity: Activity, resetListFirst: Boolean = true) {
        if (resetListFirst) {
            activitiesToClose.clear()
        }
        activitiesToClose.add(activity)
    }

    fun closeActiviesToClose() {
        while (activitiesToClose.isNotEmpty()) {
            activitiesToClose.removeFirstOrNull()?.finish()
        }
    }

    fun handleLoadingTextAndHidingViews(loadingLayout: LoadingLayout, thingsToHideWhileLoading: List<View> = listOf(), runIfLoading: () -> Unit = {}, runIfNotLoading: () -> Unit) {
        if (loading) {
            loadingLayout.setLoadingText(loadingText)
            thingsToHideWhileLoading.forEach { it.isGone = true }
            runIfLoading()
        } else {
            loadingLayout.setLoading(false)
            thingsToHideWhileLoading.forEach { it.isGone = false }
            runIfNotLoading()
        }
    }

    //
    // Getters
    //

    fun getSkillsAsFCMSM(): List<FullCharacterModifiedSkillModel> {
        return skills.map { it.fullCharacterModifiedSkillModel() }
    }

    fun getCurrentPlayer(): FullPlayerModel? {
        return players.firstOrNull { it.id == currentPlayerId }
    }

    fun getPlayerForCharacter(character: FullCharacterModel): FullPlayerModel {
        return players.first { it.id == character.playerId }
    }

    fun getActiveCharacter(): FullCharacterModel? {
        return getCurrentPlayer()?.getActiveCharacter()
    }

    fun getAllCharacters(type: CharacterType): List<FullCharacterModel> {
        return getAllCharacters(listOf(type))
    }

    fun getAllCharacters(types: List<CharacterType>): List<FullCharacterModel> {
        return characters.filter { types.contains(it.characterType()) }
    }

    fun getAllCharacters(): List<FullCharacterModel> {
        return getAllCharacters(CharacterType.values().toList())
    }

    fun getCharacter(id: Int): FullCharacterModel? {
        return characters.firstOrNull { it.id == id }
    }

    fun getOngoingEvent(): FullEventModel? {
        return events.firstOrNull { it.isOngoing() }
    }

    fun getOngoingOrTodayEvent(): FullEventModel? {
        return getOngoingEvent() ?: getEventToday()
    }

    private fun getEventToday(): FullEventModel? {
        return events.firstOrNull { it.isToday() }
    }

    fun getRelevantEvents(): List<FullEventModel> {
        return events.filter { it.isRelevant() }
    }

    fun callUpdateCallback(key: KClass<*>) {
        updateCallbacks[getFragmentOrActivityName(key)]?.let { it() }
    }

    fun callUpdateCallbacks(keys: List<KClass<*>>) {
        keys.forEach { key ->
            updateCallbacks[getFragmentOrActivityName(key)]?.let { it() }
        }
    }

    inline fun <reified T> getPassedData(key: KClass<*>, dataKey: DataManagerPassedDataKey, clear: Boolean = true): T? {
        val data = passedData[getFragmentOrActivityName(key) + dataKey.toString()] as? T
        if (clear) {
            clearPassedData(key, dataKey)
        }
        return data
    }

    inline fun <reified T> getPassedData(keys: List<KClass<*>>, dataKey: DataManagerPassedDataKey, clear: Boolean = true): T? {
        keys.forEach { key ->
            val data = getPassedData<T>(key, dataKey, clear)
            if (data != null) {
                return data
            }
        }
        return null
    }

    fun getCharactersWhoNeedBiosApproved(): List<FullCharacterModel> {
        return characters.filter { !it.approvedBio && it.bio.isNotEmpty() }
    }

}

class JsoupAsyncTask(private val url: String, private val callback: (Document?) -> Unit) :
    AsyncTask<Void, Void, Document>() {

    override fun doInBackground(vararg params: Void?): Document? {
        return try {
            Jsoup.connect(url).get()
        } catch (e: IOException) {
            null
        }
    }

    override fun onPostExecute(result: Document?) {
        super.onPostExecute(result)

        callback(result)
    }
}