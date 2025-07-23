package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.models.AppVersionModel
import com.forkbombsquad.stillalivelarp.services.models.AwardModel
import com.forkbombsquad.stillalivelarp.services.models.LDAwardModels
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.EventPreregModel
import com.forkbombsquad.stillalivelarp.services.models.EventRegType
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.LDEventAttendeeModels
import com.forkbombsquad.stillalivelarp.services.models.LDPreregModels
import com.forkbombsquad.stillalivelarp.services.models.LDSkillPrereqModels
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.ProfileImageModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryModel
import com.forkbombsquad.stillalivelarp.services.models.SkillModel
import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqModel
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.utils.Rulebook
import com.forkbombsquad.stillalivelarp.utils.addCreateListIfNecessary
import com.forkbombsquad.stillalivelarp.utils.compress
import com.forkbombsquad.stillalivelarp.utils.decompress
import com.forkbombsquad.stillalivelarp.utils.doesNotContain
import com.forkbombsquad.stillalivelarp.utils.equalsAnyOf
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import java.io.ByteArrayOutputStream
import java.util.Base64

private typealias DMT = DataManagerType
class LocalDataManager private constructor() {

    // TODO add camp status model to this
    companion object {

        // TODO update this number because models changed
        // TODO ROUTINE - update this number if any of the models change between releases
        const val LOCAL_DATA_VERSION = "1"

        var shared = LocalDataManager()
            private set

        fun clearAllLocalData() {
            UserAndPassManager.shared.clearAll()
            LDMKeys.allKeys.forEach {
                shared.clear(it)
            }
            DMT.values().forEach {
                shared.clear(it)
            }
        }
    }

    private class LDMKeys {
        companion object {
            val unpSharedPrefsKey = "StillAliveLarpSharedPrefs"
            val sharedPrefsBaseKey = "StillAliveLarpLocalDataPrefBaseKey"
            val fullSkillsKey = "fullskills_LDMKEYS_dm_sp_key"
            val fullEventsKey = "fullevents_LDMKEYS_dm_sp_key"
            val fullCharactersKey = "fullcharacters_LDMKEYS_dm_sp_key"
            val fullPlayersKey = "fullplayers_LDMKEYS_dm_sp_key"
            val playerIdKey = "playerid_LDMKEYS_dm_sp_key"

            val allKeys: List<String> = listOf(fullSkillsKey, fullEventsKey, fullCharactersKey, fullPlayersKey, playerIdKey)
        }
    }

    private fun getUnPSharedPrefs(context: Context? = null): SharedPreferences {
        return (context ?: globalGetContext())!!.getSharedPreferences(LDMKeys.unpSharedPrefsKey, Context.MODE_PRIVATE)
    }

    private fun getUnPSharedPrefsEditor(): SharedPreferences.Editor {
        return globalGetContext()!!.getSharedPreferences(LDMKeys.unpSharedPrefsKey, Context.MODE_PRIVATE).edit()
    }

    fun setUnPRelatedObject(key: String, value: String) {
        getUnPSharedPrefsEditor().putString(key, value).commit()
    }

    fun getUnPRelatedObject(context: Context? = null, key: String): String? {
        return getUnPSharedPrefs(context).getString(key, null)
    }

    fun clearUnPRelatedObject(key: String) {
        getUnPSharedPrefsEditor().remove(key).commit()
    }

    private fun getSharedPrefs(): SharedPreferences {
        return globalGetContext()!!.getSharedPreferences(LDMKeys.sharedPrefsBaseKey + LOCAL_DATA_VERSION, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(): SharedPreferences.Editor {
        return globalGetContext()!!.getSharedPreferences(LDMKeys.sharedPrefsBaseKey + LOCAL_DATA_VERSION, Context.MODE_PRIVATE).edit()
    }

    private fun clear(key: String) {
        getSharedPrefsEditor().remove(key).commit()
    }

    private fun clear(key: DMT) {
        clear(key.localDataKey)
    }

    private fun store(obj: Any, key: String) {
        val json = globalToJson(obj)
        val compressed = json.compress()
        getSharedPrefsEditor().putString(key, compressed).commit()
    }

    private fun store(obj: Any, key: DMT) {
        store(obj, key.localDataKey)
    }

    private inline fun <reified T> get(key: String): T? {
        val compressed = getSharedPrefs().getString(key, null) ?: return null
        val json = compressed.decompress()
        return globalFromJson<T>(json)
    }

    private inline fun <reified T> get(key: DMT): T? {
        return get(key.localDataKey)
    }

    private fun getUpdateTracker(): UpdateTrackerModel? {
        return get(DMT.UPDATE_TRACKER)
    }

    fun storeUpdateTracker(tracker: UpdateTrackerModel) {
        store(tracker, DMT.UPDATE_TRACKER)
    }

    fun storeAnnouncements(announcements: List<AnnouncementModel>) {
        store(announcements, DMT.ANNOUNCEMENTS)
    }

    fun getAnnouncements(): List<AnnouncementModel> {
        return get(DMT.ANNOUNCEMENTS) ?: listOf()
    }

    fun storeAwards(awards: List<AwardModel>) {
        val playerAwards: MutableMap<Int, MutableList<AwardModel>> = mutableMapOf()
        val characterAwards: MutableMap<Int, MutableList<AwardModel>> = mutableMapOf()
        awards.forEach { award ->
            award.characterId.ifLet({ charId ->
                if (charId != -1) {
                    characterAwards.addCreateListIfNecessary(charId, award)
                }
            }, {
                playerAwards.addCreateListIfNecessary(award.playerId, award)
            })
        }
        val ams = LDAwardModels(playerAwards, characterAwards)
        store(ams, DMT.AWARDS)
    }

    // Returns Awards in two separate dictionaries, where the key is playerId and characterId
    // You can call .getAllAwardsFor() to get ones for player and character if needed
    fun getAwards(): LDAwardModels {
        return get(DMT.AWARDS) ?: LDAwardModels.empty()
    }

    fun storeCharacters(characters: List<CharacterModel>) {
        store(characters, DMT.CHARACTERS)
    }

    fun getCharacters(): List<CharacterModel> {
        return get(DMT.CHARACTERS) ?: listOf()
    }

    fun storeGear(gear: List<GearModel>) {
        val gearMap: MutableMap<Int, GearModel> = mutableMapOf()
        gear.forEach {
            gearMap[it.characterId] = it
        }
        store(gearMap, DMT.GEAR)
    }

    // Returns a map of GearModels with keys equal to the characterId
    fun getGear(): Map<Int, GearModel> {
        return get(DMT.GEAR) ?: mapOf()
    }

    fun storeCharacterSkills(charSkills: List<CharacterSkillModel>) {
        val charSkillMap: MutableMap<Int, MutableList<CharacterSkillModel>> = mutableMapOf()
        charSkills.forEach {
            charSkillMap.addCreateListIfNecessary(it.characterId, it)
        }
        store(charSkillMap, DMT.CHARACTER_SKILLS)
    }

    // Returns a map of CharacterSkills with keys equal to the characterId
    fun getCharacterSkills(): Map<Int, List<CharacterSkillModel>> {
        return get(DMT.CHARACTER_SKILLS) ?: mapOf()
    }

    fun storeContactRequests(contactRequests: List<ContactRequestModel>) {
        store(contactRequests, DMT.CONTACT_REQUESTS)
    }

    fun getContactRequests(): List<ContactRequestModel> {
        return get(DMT.CONTACT_REQUESTS) ?: listOf()
    }

    fun storeEvents(events: List<EventModel>) {
        store(events, DMT.EVENTS)
    }

    fun getEvents(): List<EventModel> {
        return get(DMT.EVENTS) ?: listOf()
    }

    fun storeEventAttendees(eventAttendees: List<EventAttendeeModel>) {
        val eventMap: MutableMap<Int, MutableList<EventAttendeeModel>> = mutableMapOf()
        val playerMap: MutableMap<Int, MutableList<EventAttendeeModel>> = mutableMapOf()
        val characterMap: MutableMap<Int, MutableList<EventAttendeeModel>> = mutableMapOf()
        eventAttendees.forEach { attendee ->
            eventMap.addCreateListIfNecessary(attendee.eventId, attendee)
            playerMap.addCreateListIfNecessary(attendee.playerId, attendee)
            attendee.characterId.ifLet { charId ->
                if (charId != -1) {
                    characterMap.addCreateListIfNecessary(charId, attendee)
                }
            }
        }
        store(LDEventAttendeeModels(eventMap, playerMap, characterMap), DMT.EVENT_ATTENDEES)
    }

    // Returns the event attendees organized by events, players and characters. There ARE duplicate entries in the maps as these categories overlap.
    fun getEventAttendees(): LDEventAttendeeModels {
        return get(DMT.EVENT_ATTENDEES) ?: LDEventAttendeeModels.empty()
    }

    fun storePreregs(preregs: List<EventPreregModel>) {
        val eventMap: MutableMap<Int, MutableList<EventPreregModel>> = mutableMapOf()
        val playerMap: MutableMap<Int, MutableList<EventPreregModel>> = mutableMapOf()
        val characterMap: MutableMap<Int, MutableList<EventPreregModel>> = mutableMapOf()
        val regTypeMap: MutableMap<EventRegType, MutableList<EventPreregModel>> = mutableMapOf()
        preregs.forEach { prereg ->
            eventMap.addCreateListIfNecessary(prereg.eventId, prereg)
            playerMap.addCreateListIfNecessary(prereg.playerId, prereg)
            prereg.getCharId().ifLet { charId ->
                characterMap.addCreateListIfNecessary(charId, prereg)
            }
            regTypeMap.addCreateListIfNecessary(prereg.eventRegType(), prereg)
        }
        store(LDPreregModels(eventMap, playerMap, characterMap, regTypeMap), DMT.PREREGS)
    }

    // Returns the preregs organized by events, players, characters, and regType. There ARE duplicate entries in the maps as these categories overlap.
    fun getPreregs(): LDPreregModels {
        return get(DMT.PREREGS) ?: LDPreregModels.empty()
    }

    fun storeFeatureFlags(featureFlags: List<FeatureFlagModel>) {
        store(featureFlags, DMT.FEATURE_FLAGS)
    }

    fun getFeatureFlags(): List<FeatureFlagModel> {
        return get(DMT.FEATURE_FLAGS) ?: listOf()
    }

    fun storeIntrigues(intrigues: List<IntrigueModel>) {
        val intrigueMap: MutableMap<Int, IntrigueModel> = mutableMapOf()
        intrigues.forEach {
            intrigueMap[it.eventId] = it
        }
        store(intrigueMap, DMT.INTRIGUES)
    }

    // Returns a map of intrigues with keys equal to the event id
    fun getIntrigues(): Map<Int, IntrigueModel> {
        return get(DMT.INTRIGUES) ?: mapOf()
    }

    fun storePlayers(players: List<PlayerModel>) {
        store(players, DMT.PLAYERS)
    }

    fun getPlayers(): List<PlayerModel> {
        return get(DMT.PLAYERS) ?: listOf()
    }

    fun storeProfileImages(profileImages: List<ProfileImageModel>) {
        val profileImageMap: MutableMap<Int, ProfileImageModel> = mutableMapOf()
        profileImages.forEach {
            profileImageMap[it.playerId] = it
        }
        store(profileImageMap, DMT.PROFILE_IMAGES)
    }

    // Returns a map of profile images with keys equal to the playerId
    fun getProfileImages(): Map<Int, ProfileImageModel> {
        return get(DMT.PROFILE_IMAGES) ?: mapOf()
    }

    fun storeResearchProjects(researchProjects: List<ResearchProjectModel>) {
        store(researchProjects, DMT.RESEARCH_PROJECTS)
    }

    fun getResearchProjects(): List<ResearchProjectModel> {
        return get(DMT.RESEARCH_PROJECTS) ?: listOf()
    }

    fun storeSkills(skills: List<SkillModel>) {
        store(skills, DMT.SKILLS)
    }

    fun getSkills(): List<SkillModel> {
        return get(DMT.SKILLS) ?: listOf()
    }

    fun storeSkillCategories(skillCategories: List<SkillCategoryModel>) {
        store(skillCategories, DMT.SKILL_CATEGORIES)
    }

    fun getSkillCategories(): List<SkillCategoryModel> {
        return get(DMT.SKILL_CATEGORIES) ?: listOf()
    }

    fun storeSkillPrereqs(prereqs: List<SkillPrereqModel>) {
        val byBaseMap: MutableMap<Int, MutableList<SkillPrereqModel>> = mutableMapOf()
        val byPrereqMap: MutableMap<Int, MutableList<SkillPrereqModel>> = mutableMapOf()
        prereqs.forEach { prereq ->
            byBaseMap.addCreateListIfNecessary(prereq.baseSkillId, prereq)
            byPrereqMap.addCreateListIfNecessary(prereq.prereqSkillId, prereq)
        }
        store(LDSkillPrereqModels(prereqs, byBaseMap, byPrereqMap), DMT.SKILL_PREREQS)
    }

    // Returns the prereqs in a full list, as well as organized by baseSkillId and prereqSkillId
    fun getSkillPrereqs(): LDSkillPrereqModels {
        return get(DMT.SKILL_PREREQS) ?: LDSkillPrereqModels(listOf(), mapOf(), mapOf())
    }

    fun storeXpReductions(xpReductions: List<XpReductionModel>) {
        val xpReductionMap: MutableMap<Int, MutableList<XpReductionModel>> = mutableMapOf()
        xpReductions.forEach {
            xpReductionMap.addCreateListIfNecessary(it.characterId, it)
        }
        store(xpReductionMap, DMT.XP_REDUCTIONS)
    }

    // Returns a map of xp reductions images with keys equal to the character Id
    fun getXpReductions(): Map<Int, List<XpReductionModel>> {
        return get(DMT.XP_REDUCTIONS) ?: mapOf()
    }

    fun storeRulebook(rulebook: Rulebook) {
        store(rulebook, DMT.RULEBOOK)
    }

    fun getRulebook(): Rulebook? {
        val rb: Rulebook? = get(DMT.RULEBOOK)
        return rb
    }

    fun storeTreatingWounds(treatingWoundsBmp: Bitmap) {
        val baos = ByteArrayOutputStream()
        treatingWoundsBmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        val encodedImage: String = Base64.getEncoder().encodeToString(b)
        store(encodedImage, DMT.TREATING_WOUNDS)
    }

    fun getTreatingWounds(): Bitmap? {
        val encodedImage: String? = get(DMT.TREATING_WOUNDS)
        return if (encodedImage != null) {
            val imageAsBytes = Base64.getDecoder().decode(encodedImage.toByteArray())
            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        } else {
            null
        }
    }

    fun storePlayerId(id: Int) {
        store(id, LDMKeys.playerIdKey)
    }

    fun getPlayerId(): Int {
        return get(LDMKeys.playerIdKey) ?: -1
    }

    fun determineWhichTypesNeedUpdates(newUpdateTracker: UpdateTrackerModel): List<DMT> {
        return getUpdateTracker()?.getDifferences(newUpdateTracker) ?: return DMT.values().filter { it != DataManagerType.UPDATE_TRACKER }
    }

    fun updatesSucceeded(newUpdateTracker: UpdateTrackerModel, successfulUpdates: List<DMT>) {
        getUpdateTracker().ifLet({ oldUpdateTracker ->
            oldUpdateTracker.updateInPlace(newUpdateTracker, successfulUpdates)
            storeUpdateTracker(oldUpdateTracker)
        }, {
            storeUpdateTracker(newUpdateTracker.updateToNew(successfulUpdates))
        })
        recalculateFullModels(newUpdateTracker)
    }

    private fun recalculateFullModels(newUpdateTracker: UpdateTrackerModel) {
        val neededUpdates = determineWhichTypesNeedUpdates(newUpdateTracker)
        var builtFullSkills = false
        var builtFullEvents = false
        var builtFullCharacters = false

        val attendees = getEventAttendees()
        val preregs = getPreregs()
        val awards = getAwards()

        // FULL SKILLS
        if (neededUpdates.doesNotContain(listOf(DMT.SKILLS, DMT.SKILL_CATEGORIES, DMT.SKILL_PREREQS))) {
            buildAndStoreFullSkills(getSkills(), getSkillCategories(), getSkillPrereqs())
            builtFullSkills = true
        }

        // FULL EVENTS
        if (neededUpdates.doesNotContain(listOf(DMT.EVENTS, DMT.EVENT_ATTENDEES, DMT.PREREGS, DMT.INTRIGUES))) {
            buildAndStoreFullEvents(getEvents(), attendees, preregs, getIntrigues())
            builtFullEvents = true
        }

        // FULL CHARACTERS
        if (builtFullSkills && builtFullEvents && neededUpdates.doesNotContain(listOf(DMT.CHARACTERS, DMT.CHARACTER_SKILLS, DMT.GEAR, DMT.AWARDS, DMT.XP_REDUCTIONS))) {
            buildAndStoreFullCharacters(
                characters = getCharacters(),
                fullSkills = getFullSkills(),
                characterSkills = getCharacterSkills(),
                gear = getGear(),
                awards = awards,
                attendees = attendees,
                preregs = preregs,
                xpReductions = getXpReductions()
            )
            builtFullCharacters = true
        }

        // FULL PLAYERS

        if (builtFullCharacters && builtFullEvents && neededUpdates.doesNotContain(listOf(DMT.PLAYERS, DMT.PROFILE_IMAGES)) ) {
            buildAndStoreFullPlayers(
                players = getPlayers(),
                characters = getFullCharacters(),
                awards = awards,
                attendees = attendees,
                preregs = preregs,
                profileImages = getProfileImages()
            )
        }

    }

    private fun buildAndStoreFullSkills(skills: List<SkillModel>, skillCategories: List<SkillCategoryModel>, skillPrereqs: LDSkillPrereqModels) {
        val fullSkills: MutableList<FullSkillModel> = mutableListOf()
        skills.forEach { skill ->
            val prereqMods = skillPrereqs.byBaseSkill[skill.id] ?: listOf()
            val prereqs = skills.filter { skl -> skl.id.equalsAnyOf(prereqMods.map { it.prereqSkillId }) }

            val postreqMods = skillPrereqs.byPrereqSkill[skill.id] ?: listOf()
            val postreqs = skills.filter { skl -> skl.id.equalsAnyOf(postreqMods.map { it.baseSkillId }) }

            fullSkills.add(FullSkillModel(
                skillModel = skill,
                prereqs = prereqs,
                postreqs = postreqs,
                category = skillCategories.first { it.id == skill.skillCategoryId.toInt() }))
        }
        store(fullSkills, LDMKeys.fullSkillsKey)
    }

    fun getFullSkills(): List<FullSkillModel> {
        return get(LDMKeys.fullSkillsKey) ?: listOf()
    }

    private fun buildAndStoreFullEvents(events: List<EventModel>, attendees: LDEventAttendeeModels, preregs: LDPreregModels, intrigues: Map<Int, IntrigueModel>) {
        val fullEvents: MutableList<FullEventModel> = mutableListOf()
        events.forEach { event ->
            fullEvents.add(
                FullEventModel(
                    event = event,
                    attendees = attendees.byEvent[event.id] ?: listOf(),
                    preregs = preregs.byEvent[event.id] ?: listOf(),
                    intrigue = intrigues[event.id]
                )
            )
        }
        store(fullEvents, LDMKeys.fullEventsKey)
    }

    fun getFullEvents(): List<FullEventModel> {
        return get(LDMKeys.fullEventsKey) ?: listOf()
    }

    private fun buildAndStoreFullCharacters(characters: List<CharacterModel>, fullSkills: List<FullSkillModel>, characterSkills: Map<Int, List<CharacterSkillModel>>, gear: Map<Int, GearModel>, awards: LDAwardModels, attendees: LDEventAttendeeModels, preregs: LDPreregModels, xpReductions: Map<Int, List<XpReductionModel>>) {
        val fullChars: MutableList<FullCharacterModel> = mutableListOf()
        characters.forEach { character ->
            val charSkills = characterSkills[character.id] ?: listOf()
            val gearC = gear[character.id]
            val awardsC = awards.characterAwards[character.id] ?: listOf()
            val attendeesC = attendees.byCharacter[character.id] ?: listOf()
            val preregsC = preregs.byCharacter[character.id] ?: listOf()
            val xpRed = xpReductions[character.id] ?: listOf()
            fullChars.add(
                FullCharacterModel(
                    charModel = character,
                    allSkills = fullSkills,
                    charSkills = charSkills,
                    gear = gearC,
                    awards = awardsC,
                    eventAttendees = attendeesC,
                    preregs = preregsC,
                    xpReductions = xpRed
                )
            )
        }
        store(fullChars, LDMKeys.fullCharactersKey)
    }

    fun getFullCharacters(): List<FullCharacterModel> {
        return get(LDMKeys.fullCharactersKey) ?: listOf()
    }

    private fun buildAndStoreFullPlayers(players: List<PlayerModel>, characters: List<FullCharacterModel>, awards: LDAwardModels, attendees: LDEventAttendeeModels, preregs: LDPreregModels, profileImages: Map<Int, ProfileImageModel>) {
        val fullPlayers: MutableList<FullPlayerModel> = mutableListOf()
        players.forEach { player ->
            fullPlayers.add(
                FullPlayerModel(
                    player = player,
                    characters = characters.filter { it.playerId == player.id },
                    awards = awards.playerAwards[player.id] ?: listOf(),
                    eventAttendees = attendees.byPlayer[player.id] ?: listOf(),
                    preregs = preregs.byPlayer[player.id] ?: listOf(),
                    profileImage = profileImages[player.id]
                )
            )
        }
        store(fullPlayers, LDMKeys.fullPlayersKey)
    }

    fun getFullPlayers(): List<FullPlayerModel> {
        return get(LDMKeys.fullPlayersKey) ?: listOf()
    }

}