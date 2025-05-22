package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import java.io.Serializable

private typealias DMT = DataManagerType

@JsonIgnoreProperties(ignoreUnknown = true)
data class UpdateTrackerModel(
    @JsonProperty("id") var id: Int,
    @JsonProperty("announcements") var announcements: Int,
    @JsonProperty("awards") var awards: Int,
    @JsonProperty("characters") var characters: Int,
    @JsonProperty("gear") var gear: Int,
    @JsonProperty("characterSkills") var characterSkills: Int,
    @JsonProperty("contactRequests") var contactRequests: Int,
    @JsonProperty("events") var events: Int,
    @JsonProperty("eventAttendees") var eventAttendees: Int,
    @JsonProperty("preregs") var preregs: Int,
    @JsonProperty("featureFlags") var featureFlags: Int,
    @JsonProperty("intrigues") var intrigues: Int,
    @JsonProperty("players") var players: Int,
    @JsonProperty("profileImages") var profileImages: Int,
    @JsonProperty("researchProjects") var researchProjects: Int,
    @JsonProperty("skills") var skills: Int,
    @JsonProperty("skillCategories") var skillCategories: Int,
    @JsonProperty("skillPrereqs") var skillPrereqs: Int,
    @JsonProperty("xpReductions") var xpReductions: Int
) : Serializable {
    fun getDifferences(other: UpdateTrackerModel): List<DMT> {
        val updates: MutableList<DMT> = mutableListOf()
        if (other.announcements != this.announcements) {
            updates.add(DMT.ANNOUNCEMENTS)
        }
        if (other.awards != this.awards) {
            updates.add(DMT.AWARDS)
        }
        if (other.characters != this.characters) {
            updates.add(DMT.CHARACTERS)
        }
        if (other.gear != this.gear) {
            updates.add(DMT.GEAR)
        }
        if (other.characterSkills != this.characterSkills) {
            updates.add(DMT.CHARACTER_SKILLS)
        }
        if (other.contactRequests != this.contactRequests) {
            updates.add(DMT.CONTACT_REQUESTS)
        }
        if (other.events != this.events) {
            updates.add(DMT.EVENTS)
        }
        if (other.eventAttendees != this.eventAttendees) {
            updates.add(DMT.EVENT_ATTENDEES)
        }
        if (other.preregs != this.preregs) {
            updates.add(DMT.PREREGS)
        }
        if (other.featureFlags != this.featureFlags) {
            updates.add(DMT.FEATURE_FLAGS)
        }
        if (other.intrigues != this.intrigues) {
            updates.add(DMT.INTRIGUES)
        }
        if (other.players != this.players) {
            updates.add(DMT.PLAYERS)
        }
        if (other.profileImages != this.profileImages) {
            updates.add(DMT.PROFILE_IMAGES)
        }
        if (other.researchProjects != this.researchProjects) {
            updates.add(DMT.RESEARCH_PROJECTS)
        }
        if (other.skills != this.skills) {
            updates.add(DMT.SKILLS)
        }
        if (other.skillCategories != this.skillCategories) {
            updates.add(DMT.SKILL_CATEGORIES)
        }
        if (other.skillPrereqs != this.skillPrereqs) {
            updates.add(DMT.SKILL_PREREQS)
        }
        if (other.xpReductions != this.xpReductions) {
            updates.add(DMT.XP_REDUCTIONS)
        }
        return updates
    }

    fun updateInPlace(newTracker: UpdateTrackerModel, successfulUpdates: List<DMT>) {
        for (update in successfulUpdates) {
            when (update) {
                DataManagerType.UPDATE_TRACKER -> continue
                DataManagerType.ANNOUNCEMENTS -> this.announcements = newTracker.announcements
                DataManagerType.AWARDS -> this.awards = newTracker.awards
                DataManagerType.CHARACTERS -> this.characters = newTracker.characters
                DataManagerType.GEAR -> this.gear = newTracker.gear
                DataManagerType.CHARACTER_SKILLS -> this.characterSkills = newTracker.characterSkills
                DataManagerType.CONTACT_REQUESTS -> this.contactRequests = newTracker.contactRequests
                DataManagerType.EVENTS -> this.events = newTracker.events
                DataManagerType.EVENT_ATTENDEES -> this.eventAttendees = newTracker.eventAttendees
                DataManagerType.PREREGS -> this.preregs = newTracker.preregs
                DataManagerType.FEATURE_FLAGS -> this.featureFlags = newTracker.featureFlags
                DataManagerType.INTRIGUES -> this.intrigues = newTracker.featureFlags
                DataManagerType.PLAYERS -> this.players = newTracker.players
                DataManagerType.PROFILE_IMAGES -> this.profileImages = newTracker.profileImages
                DataManagerType.RESEARCH_PROJECTS -> this.researchProjects = newTracker.researchProjects
                DataManagerType.SKILLS -> this.skills = newTracker.skills
                DataManagerType.SKILL_CATEGORIES -> this.skillCategories = newTracker.skillCategories
                DataManagerType.SKILL_PREREQS -> this.skillPrereqs = newTracker.skillPrereqs
                DataManagerType.XP_REDUCTIONS -> this.xpReductions = newTracker.xpReductions
            }
        }
    }

    fun updateToNew(successfulUpdates: List<DMT>): UpdateTrackerModel {
        val tracker = UpdateTrackerModel(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
        tracker.updateInPlace(this, successfulUpdates)
        return tracker
    }

}