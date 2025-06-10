package com.forkbombsquad.stillalivelarp.services.managers

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.AnnouncementService
import com.forkbombsquad.stillalivelarp.services.AwardService
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
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.Rulebook
import com.forkbombsquad.stillalivelarp.utils.getFragmentOrActivityName
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Evaluator.Id
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KClass

enum class DataManagerPassedDataKey {
    CHECKOUT_BARCODE,
    CHECKIN_BARCODE,
    SELECTED_EVENT,
    SELECTED_PLAYER,
    SELECTED_CHARACTER,
    AWARDS_LIST,
    CHARACTER_LIST,
    DESTINATION_CLASS,
    VIEW_TITLE,
    PLAYER_LIST
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
    TREATING_WOUNDS("treatingwounds_dm_sp_key");
}

enum class DataManagerLoadType {
    OFFLINE, DOWNLOAD_IF_NECESSARY, FORCE_DOWNLOAD_ALL
}

class DataManager private constructor() {
    // Global Settings
    var offlineMode: Boolean = false
        private set

    var currentPlayerId: Int = -1
        private set

    var updateCallbacks: MutableMap<String, () -> Unit> = mutableMapOf()
        private set

    var passedData: MutableMap<String, Any> = mutableMapOf()

    // Unchanged From Server
    var announcements: List<AnnouncementModel> = listOf()
    var contactRequests: List<ContactRequestModel> = listOf()
    var featureFlags: List<FeatureFlagModel> = listOf()
    var intrigues: Map<Int, IntrigueModel> = mapOf()
    var researchProjects: List<ResearchProjectModel> = listOf()

    // Built Models that combine lots of different sub models
    var skills: List<FullSkillModel> = listOf()
    var events: List<FullEventModel> = listOf()
    private var characters: List<FullCharacterModel> = listOf()
    var players: List<FullPlayerModel> = listOf()
    var rulebook: Rulebook? = null
    var treatingWounds: Bitmap? = null

    // DM variables
    private var firstLoad: Boolean = true
    var loading: Boolean = false
    var loadingText = ""
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

    fun load(lifecycleScope: LifecycleCoroutineScope, loadType: DataManagerLoadType = DataManagerLoadType.DOWNLOAD_IF_NECESSARY, stepFinished: () -> Unit = {}, finished: () -> Unit) {
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
                loading = true
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

    private fun loadOffline(lifecycleScope: LifecycleCoroutineScope) {
        populateLocalData(lifecycleScope, false)
    }

    private fun loadDownloadIfNecessary(lifecycleScope: LifecycleCoroutineScope) {
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

    private fun loadForceDownloadAll(lifecycleScope: LifecycleCoroutineScope) {
        lifecycleScope.launch {
            mutexThreadLocker.withLock {
                loadingText = "Force Clearing Data..."
                firstLoad = true
                LocalDataManager.shared.storeUpdateTracker(UpdateTrackerModel.empty())
            }
            loadDownloadIfNecessary(lifecycleScope)
        }
    }

    private fun handleUpdates(lifecycleScope: LifecycleCoroutineScope, updateTracker: UpdateTrackerModel) {
        updatesNeeded = LocalDataManager.shared.determineWhichTypesNeedUpdates(updateTracker).toMutableList()
        updatesCompleted = mutableListOf()
        currentUpdateTracker = updateTracker
        finishedCount = 0
        if (updatesNeeded.isEmpty()) {
            populateLocalData(lifecycleScope, false)
        } else {
            lifecycleScope.launch {
                mutexThreadLocker.withLock {
                    loadingText = generateLoadingText()
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
                            request.successfulResponse().ifLet({
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
                    DataManagerType.RULEBOOK -> {
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
                    DataManagerType.TREATING_WOUNDS -> {
                        lifecycleScope.launch {
                            val jsoupAsyncTask = JsoupAsyncTask("https://stillalivelarp.com/healing") { doc ->
                                lifecycleScope.launch {
                                    val imageElement = doc?.getElementById("image")
                                    val imgPath = imageElement?.attr("src")
                                    val url = URL(imgPath)
                                    val connection = url.openConnection() as? HttpURLConnection
                                    connection?.doInput = true
                                    connection?.connect()
                                    val responseCode = connection?.responseCode ?: -1
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        val imageStream = connection?.inputStream
                                        val bmp = BitmapFactory.decodeStream(imageStream)
                                        imageStream?.close()
                                        LocalDataManager.shared.storeTreatingWounds(bmp)
                                        serviceFinished(lifecycleScope, updateType, true, updatesNeededCopy)
                                    } else {
                                        serviceFinished(lifecycleScope, updateType, false, updatesNeededCopy)
                                    }
                                }
                            }
                            jsoupAsyncTask.execute()
                        }
                    }
                }
            }
        }
    }

    private fun generateLoadingText(): String {
        var text = "Loading: "
        updatesNeeded.forEachIndexed { index, dmt ->
            if (index > 0) {
                text += ", "
            }
            text += dmt.name.lowercase()
        }
        text += "..."
        text = text.replace("_", " ")
        return text
    }

    private suspend fun serviceFinished(lifecycleScope: LifecycleCoroutineScope, type: DataManagerType, succeeded: Boolean, localUpdatesNeeded: List<DataManagerType>) {
        finishedCountMutexThreadLocker.withLock {
            if (succeeded) {
                updatesNeeded.remove(type)
                updatesCompleted.add(type)
            }
            finishedCount += 1
        }
        mutexThreadLocker.withLock {
            loadingText = generateLoadingText()
            stepCallbacks.forEach { it() }
        }
        if (finishedCount == localUpdatesNeeded.count()) {
            LocalDataManager.shared.updatesSucceeded(currentUpdateTracker, updatesCompleted)
            populateLocalData(lifecycleScope, true)
        }
    }

    private fun populateLocalData(lifecycleScope: LifecycleCoroutineScope, updatesDownloaded: Boolean) {
        lifecycleScope.launch {
            mutexThreadLocker.withLock {
                if (firstLoad || updatesDownloaded) {
                    firstLoad = false
                    // Normal Models
                    announcements = LocalDataManager.shared.getAnnouncements()
                    contactRequests = LocalDataManager.shared.getContactRequests()
                    featureFlags = LocalDataManager.shared.getFeatureFlags()
                    intrigues = LocalDataManager.shared.getIntrigues()
                    researchProjects = LocalDataManager.shared.getResearchProjects()

                    // Built Models
                    skills = LocalDataManager.shared.getFullSkills()
                    events = LocalDataManager.shared.getFullEvents()
                    characters = LocalDataManager.shared.getFullCharacters()
                    players = LocalDataManager.shared.getFullPlayers()
                    rulebook = LocalDataManager.shared.getRulebook()
                    treatingWounds = LocalDataManager.shared.getTreatingWounds()
                    currentPlayerId = LocalDataManager.shared.getPlayerId()
                }
                loadingText = ""
                loading = false
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

    fun setOfflineMode(enabled: Boolean) {
        offlineMode = enabled
    }

    fun setCurrentPlayerId(id: Int) {
        LocalDataManager.shared.storePlayerId(id)
        currentPlayerId = id
    }

    fun setCurrentPlayerId(player: PlayerModel) {
        setCurrentPlayerId(player.id)
    }

    fun setTitleTextPotentiallyOffline(tv: TextView, baseText: String) {
        tv.text = offlineMode.ternary("[Offline] $baseText", baseText)
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

    //
    // Getters
    //

    fun getCurrentPlayer(): FullPlayerModel? {
        return players.firstOrNull { it.id == currentPlayerId }
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