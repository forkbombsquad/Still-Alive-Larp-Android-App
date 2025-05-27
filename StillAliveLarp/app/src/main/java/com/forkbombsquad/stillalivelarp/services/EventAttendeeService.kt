package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeListModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAllEventAttendeesRequest {
    @HTTP(method ="GET", path = "event-attendee/all/")
    suspend fun makeRequest(): Response<EventAttendeeListModel>
}

interface GetEventsForPlayerRequest {
    @HTTP(method ="GET", path = "event-attendee/all_for_player/{playerId}")
    suspend fun makeRequest(@Path("playerId") playerId: Int): Response<EventAttendeeListModel>
}

interface GetEventsForEventRequest {
    @HTTP(method ="GET", path = "event-attendee/all_for_event/{eventId}")
    suspend fun makeRequest(@Path("eventId") eventId: Int): Response<EventAttendeeListModel>
}

interface DeleteEventAttendeesForPlayerRequest {
    @HTTP(method ="DELETE", path = "event-attendee/delete/")
    suspend fun makeRequest(): Response<EventAttendeeListModel>
}

class EventAttendeeService {
    class GetEventsForPlayer:
        UAndPServiceInterface<GetEventsForPlayerRequest, EventAttendeeListModel, IdSP> {
        override val request: GetEventsForPlayerRequest
            get() = retrofit.create(GetEventsForPlayerRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<EventAttendeeListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllEventAttendees:
        UAndPServiceInterface<GetAllEventAttendeesRequest, EventAttendeeListModel, ServicePayload> {
        override val request: GetAllEventAttendeesRequest
            get() = retrofit.create(GetAllEventAttendeesRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<EventAttendeeListModel> {
            return request.makeRequest()
        }
    }

    class GetAttendeesForEvent:
        UAndPServiceInterface<GetEventsForEventRequest, EventAttendeeListModel, IdSP> {
        override val request: GetEventsForEventRequest
            get() = retrofit.create(GetEventsForEventRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<EventAttendeeListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class DeleteEventAttendeesForPlayer:
        UAndPServiceInterface<DeleteEventAttendeesForPlayerRequest, EventAttendeeListModel, ServicePayload> {
        override val request: DeleteEventAttendeesForPlayerRequest
            get() = retrofit.create(DeleteEventAttendeesForPlayerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<EventAttendeeListModel> {
            return request.makeRequest()
        }
    }
}