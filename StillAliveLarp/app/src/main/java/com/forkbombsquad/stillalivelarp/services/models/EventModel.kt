package com.forkbombsquad.stillalivelarp.services.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddtoDate
import java.io.Serializable
import java.time.LocalDate
import java.time.Period

data class FullEventModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("date") val date: String,
    @JsonProperty("startTime") val startTime: String,
    @JsonProperty("endTime") val endTime: String,
    @JsonProperty("isStarted") var isStarted: Boolean,
    @JsonProperty("isFinished") var isFinished: Boolean,
    @JsonProperty("attendees") var attendees: List<EventAttendeeModel>,
    @JsonProperty("preregs") var preregs: List<EventPreregModel>,
    @JsonProperty("intrigue") var intrigue: IntrigueModel?
) : Serializable {

    constructor(event: EventModel, attendees: List<EventAttendeeModel>, preregs: List<EventPreregModel>, intrigue: IntrigueModel?): this(
        event.id,
        event.title,
        event.description,
        event.date,
        event.startTime,
        event.endTime,
        event.isStarted.toBoolean(),
        event.isFinished.toBoolean(),
        attendees,
        preregs,
        intrigue
    )

    fun isToday(): Boolean {
        val today = LocalDate.now()
        val eventDate = date.yyyyMMddtoDate()
        val betwixt = Period.between(eventDate, today).days
        return Period.between(eventDate, today).days == 0 && Period.between(eventDate, today).months == 0 && Period.between(eventDate, today).years == 0
    }

    fun isInFuture(): Boolean {
        val today = LocalDate.now()
        val eventDate = date.yyyyMMddtoDate()
        return today < eventDate
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventModel(
    @JsonProperty("id") val id: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("date") val date: String,
    @JsonProperty("startTime") val startTime: String,
    @JsonProperty("endTime") val endTime: String,
    @JsonProperty("isStarted") var isStarted: String,
    @JsonProperty("isFinished") var isFinished: String
) : Serializable {

    fun barcodeModel(): EventBarcodeModel {
        return EventBarcodeModel(this)
    }

    fun isToday(): Boolean {
        val today = LocalDate.now()
        val eventDate = date.yyyyMMddtoDate()
        val betwixt = Period.between(eventDate, today).days
        return Period.between(eventDate, today).days == 0 && Period.between(eventDate, today).months == 0 && Period.between(eventDate, today).years == 0
    }

    fun isInFuture(): Boolean {
        val today = LocalDate.now()
        val eventDate = date.yyyyMMddtoDate()
        return today < eventDate
    }

    fun startedFinishedText(): String {
        if (isFinished.toBoolean()) {
            return "Finished"
        }
        if (isStarted.toBoolean()) {
            return "Started"
        }
        return ""
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventBarcodeModel(
    val id: Int,
    val title: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val isStarted: String,
    val isFinished: String
) : Serializable {
    constructor(event: EventModel): this(
        event.id,
        event.title,
        event.date,
        event.startTime,
        event.endTime,
        event.isStarted,
        event.isFinished
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventCreateModel(
    @JsonProperty("title") val title: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("date") val date: String,
    @JsonProperty("startTime") val startTime: String,
    @JsonProperty("endTime") val endTime: String,
    @JsonProperty("isStarted") val isStarted: String,
    @JsonProperty("isFinished") val isFinished: String
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class EventListModel(
    @JsonProperty("events") val events: Array<EventModel>
) : Serializable