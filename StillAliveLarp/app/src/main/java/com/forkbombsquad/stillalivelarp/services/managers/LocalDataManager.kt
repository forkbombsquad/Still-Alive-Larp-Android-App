package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.content.SharedPreferences
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.models.AwardModel
import com.forkbombsquad.stillalivelarp.services.models.AwardModels_LD
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.utils.compress
import com.forkbombsquad.stillalivelarp.utils.decompress
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.ifLet

private typealias DMT = DataManagerType
class LocalDataManager private constructor() {
    companion object {
        var shared = LocalDataManager()
            private set

        fun forceReset() {
            // TODO
        }
    }

    private val sharedPrefsBaseKey = "StillAliveLarpLocalDataPrefBaseKey"

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(sharedPrefsBaseKey, Context.MODE_PRIVATE)
    }

    private fun getSharedPrefsEditor(context: Context): SharedPreferences.Editor {
        return context.getSharedPreferences(sharedPrefsBaseKey, Context.MODE_PRIVATE).edit()
    }

    private fun store(context: Context, obj: Any, key: String) {
        val json = globalToJson(obj)
        val compressed = json.compress()
        getSharedPrefsEditor(context).putString(key, compressed).commit()
    }

    private fun store(context: Context, obj: Any, key: DMT) {
        store(context, obj, key.localDataKey)
    }

    private inline fun <reified T> get(context: Context, key: String): T? {
        val compressed = getSharedPrefs(context).getString(key, null) ?: return null
        val json = compressed.decompress()
        return globalFromJson<T>(json)
    }

    private inline fun <reified T> get(context: Context, key: DMT): T? {
        return get(context, key.localDataKey)
    }

    private fun getUpdateTracker(context: Context): UpdateTrackerModel? {
        return get(context, DMT.UPDATE_TRACKER)
    }

    private fun storeUpdateTracker(context: Context, tracker: UpdateTrackerModel) {
        store(context, tracker, DMT.UPDATE_TRACKER)
    }

    fun storeAnnouncements(context: Context, announcements: List<AnnouncementModel>) {
        store(context, announcements, DMT.ANNOUNCEMENTS)
    }

    fun getAnnouncements(context: Context): List<AnnouncementModel> {
        return get(context, DMT.ANNOUNCEMENTS) ?: listOf()
    }

    fun storeAwards(context: Context, awards: List<AwardModel>) {
        val playerAwards: MutableMap<Int, MutableList<AwardModel>> = mutableMapOf()
        val characterAwards: MutableMap<Int, MutableList<AwardModel>> = mutableMapOf()
        awards.forEach { award ->
            award.characterId.ifLet({ charId ->
                if (characterAwards[charId] == null) {
                    characterAwards[charId] = mutableListOf(award)
                } else {
                    characterAwards[charId]!!.add(award)
                }
            }, {
                if (playerAwards[award.playerId] == null) {
                    playerAwards[award.playerId] = mutableListOf(award)
                } else {
                    playerAwards[award.playerId]!!.add(award)
                }
            })
        }

        val amld = AwardModels_LD(playerAwards, characterAwards)
        store(context, amld, DMT.AWARDS)
    }

    fun getAwards(context: Context): AwardModels_LD {
        return get(context, DMT.AWARDS) ?: AwardModels_LD(mapOf(), mapOf())
    }

    fun determineWhichTypesNeedUpdates(context: Context, newUpdateTracker: UpdateTrackerModel): List<DMT> {
        return getUpdateTracker(context)?.getDifferences(newUpdateTracker) ?: return DMT.values().asList()
    }

    fun updatesSucceeded(context: Context, newUpdateTracker: UpdateTrackerModel, successfulUpdates: List<DMT>) {
        getUpdateTracker(context).ifLet({ oldUpdateTracker ->
            oldUpdateTracker.updateInPlace(newUpdateTracker, successfulUpdates)
            storeUpdateTracker(context, oldUpdateTracker)
        }, {
            storeUpdateTracker(context, newUpdateTracker.updateToNew(successfulUpdates))
        })
    }

}