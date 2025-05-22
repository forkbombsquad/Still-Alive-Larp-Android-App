package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.content.SharedPreferences
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.compress
import com.forkbombsquad.stillalivelarp.utils.globalToJson

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
    XP_REDUCTIONS("xpReductions_dm_sp_key");
}

class DataManager private constructor() {
    companion object {
        var shared = DataManager()
            private set

        fun forceReset() {
            // TODO
        }
    }

}