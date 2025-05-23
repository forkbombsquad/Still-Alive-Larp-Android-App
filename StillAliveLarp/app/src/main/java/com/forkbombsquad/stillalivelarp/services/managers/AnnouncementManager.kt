package com.forkbombsquad.stillalivelarp.services.managers

import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.AnnouncementService
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementSubModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

class AnnouncementManager {

    private val cacheKey = "cachedAnnouncement"

    private var cachedAnnouncements: MutableList<AnnouncementModel>
    private var allAnnouncements: MutableList<AnnouncementSubModel> = mutableListOf()
    private var announcementExpireDate = LocalDate.now()

    private constructor() {
        this.cachedAnnouncements = mutableListOf()
        var counter = 1
        var announcementJson: String? = OldSharedPrefsManager.shared.get(globalGetContext()!!, "$cacheKey$counter")
        var gson = Gson()
        while (announcementJson != null) {
            gson.fromJson(announcementJson, AnnouncementModel::class.java).ifLet({
                cachedAnnouncements.add(it)
            },{})
            counter++
            announcementJson = OldSharedPrefsManager.shared.get(globalGetContext()!!, "$cacheKey$counter")
        }
    }

    fun getAnnouncements(lifecycleScope: LifecycleCoroutineScope, forceNewAnnouncements: Boolean = false, callback: (announcements: List<AnnouncementSubModel>) -> Unit) {
        if (needToRequestAnnouncements() || forceNewAnnouncements) {
            val request = AnnouncementService.GetAllAnnouncements()
            lifecycleScope.launch {
                request.successfulResponse().ifLet({
                    this@AnnouncementManager.allAnnouncements = it.announcements.toMutableList()
                    callback(it.announcements.asList())
                    startCachingNewAnnouncements(lifecycleScope)
                    setAnnouncementExpireDate()
                }, {
                    callback(listOf())
                })
            }
        } else {
            callback(allAnnouncements)
        }
    }

    fun getAnnouncement(lifecycleScope: LifecycleCoroutineScope, id: Int, callback: (announcement: AnnouncementModel?) -> Unit) {
        val an = cachedAnnouncements.firstOrNull { it.id == id }
        an.ifLet({
            callback(an)
        }, {
            getAnnouncementFromService(lifecycleScope, id) {
                it.ifLet({ an ->
                    cacheAnnouncement(an)
                },{})
                callback(it)
            }
        })
    }

    private fun needToRequestAnnouncements(): Boolean {
        if (allAnnouncements.isEmpty()) {
            return true
        }
        return Period.between(LocalDate.now(), announcementExpireDate).isNegative
    }

    private fun setAnnouncementExpireDate() {
        announcementExpireDate = LocalDate.now().plusDays(1)
    }

    private fun startCachingNewAnnouncements(lifecycleScope: LifecycleCoroutineScope) {
        val uncached = allAnnouncements.filter { allAn ->
            !cachedAnnouncements.any { allAn.id == it.id }
        }.toTypedArray()

        uncached.firstOrNull().ifLet({
            cacheAnnouncementRecursive(lifecycleScope, it.id, uncached)
        }, {})
    }

    private fun cacheAnnouncementRecursive(lifecycleScope: LifecycleCoroutineScope, id: Int, uncachedAnnouncements: Array<AnnouncementSubModel>) {
        getAnnouncementFromService(lifecycleScope, id) {
            it.ifLet({
                cacheAnnouncement(it)
            }, {})
            val ua = uncachedAnnouncements.filter { ua -> ua.id != id }.toTypedArray()
            ua.firstOrNull().ifLet({ u ->
                cacheAnnouncementRecursive(lifecycleScope, u.id, ua)
            }, {})
        }
    }

    private fun cacheAnnouncement(announcement: AnnouncementModel) {
        val gson = Gson()
        OldSharedPrefsManager.shared.set(globalGetContext()!!, "$cacheKey${announcement.id}", gson.toJson(announcement))
    }

    private fun getAnnouncementFromService(lifecycleScope: LifecycleCoroutineScope, id: Int, callback: (announcement: AnnouncementModel?) -> Unit) {
        val request = AnnouncementService.GetAnnouncement()
        lifecycleScope.launch {
            request.successfulResponse(IdSP(id)).ifLet({
                callback(it)
            }, {
                callback(null)
            })
        }
    }

    companion object {
        val shared = AnnouncementManager()
    }

}
