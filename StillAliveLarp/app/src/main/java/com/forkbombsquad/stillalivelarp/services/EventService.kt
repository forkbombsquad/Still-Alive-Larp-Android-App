package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.EventListModel
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP


interface GetAllEventsRequest {
    @HTTP(method ="GET", path = "event/all/")
    suspend fun makeRequest(): Response<EventListModel>
}

class EventService {
    class GetAllEvents:
        UAndPServiceInterface<GetAllEventsRequest, EventListModel, ServicePayload> {
        override val request: GetAllEventsRequest
            get() = retrofit.create(GetAllEventsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<EventListModel> {
            return request.makeRequest()
        }
    }
}