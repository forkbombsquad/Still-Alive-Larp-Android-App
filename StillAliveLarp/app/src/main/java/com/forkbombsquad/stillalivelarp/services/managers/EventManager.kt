package com.forkbombsquad.stillalivelarp.services.managers

import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.EventService
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class EventManager private constructor() {

    private var events: MutableList<EventModel>? = null
    private var fetching = false
    private var completionBlocks: MutableList<(events: List<EventModel>?) -> Unit> = mutableListOf()

    fun getEvents(lifecycleScope: LifecycleCoroutineScope, overrideLocal: Boolean = false, callback: (events: List<EventModel>?) -> Unit) {
        if (!overrideLocal && events != null) {
            callback(events)
        } else {
            completionBlocks.add(callback)
            if (!fetching) {
                fetching = true
                val eventRequest = EventService.GetAllEvents()
                lifecycleScope.launch {
                    eventRequest.successfulResponse().ifLet({ eventListModel ->
                        events = eventListModel.events.toMutableList()
                        fetching = false
                        completionBlocks.forEach { cb ->
                            cb(events)
                        }
                        completionBlocks = mutableListOf()
                    }, {
                        fetching = false
                        completionBlocks.forEach { cb ->
                            cb(events)
                        }
                        completionBlocks = mutableListOf()
                    })
                }
            }
        }
    }

    companion object {
        val shared = EventManager()
    }

}