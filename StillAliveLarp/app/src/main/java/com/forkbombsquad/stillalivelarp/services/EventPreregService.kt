package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.EventPreregListModel
import com.forkbombsquad.stillalivelarp.services.models.EventPreregModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetPreregsForEventRequest {
    @HTTP(method ="GET", path = "prereg/all_for_event/{eventId}")
    suspend fun makeRequest(@Path("eventId") eventId: Int): Response<EventPreregListModel>
}

interface GetAllPreregsRequest {
    @HTTP(method ="GET", path = "prereg/all/")
    suspend fun makeRequest(): Response<EventPreregListModel>
}

interface UpdatePreregRequest {
    @HTTP(method ="PUT", path = "prereg/update/", hasBody = true)
    suspend fun makeRequest(@Body preregModel: RequestBody): Response<EventPreregModel>
}

interface PreregPlayerRequest {
    @HTTP(method ="POST", path = "prereg/create/", hasBody = true)
    suspend fun makeRequest(@Body preregCreateModel: RequestBody): Response<EventPreregModel>
}

interface DeletePreregistrationsForPlayerRequest {
    @HTTP(method ="DELETE", path = "prereg/delete/")
    suspend fun makeRequest(): Response<EventPreregListModel>
}

class EventPreregService {
    class GetPreregsForEvent:
        UAndPServiceInterface<GetPreregsForEventRequest, EventPreregListModel, IdSP> {
        override val request: GetPreregsForEventRequest
            get() = retrofit.create(GetPreregsForEventRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<EventPreregListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllPreregs:
        UAndPServiceInterface<GetAllPreregsRequest, EventPreregListModel, ServicePayload> {
        override val request: GetAllPreregsRequest
            get() = retrofit.create(GetAllPreregsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<EventPreregListModel> {
            return request.makeRequest()
        }
    }

    class UpdatePrereg:
        UAndPServiceInterface<UpdatePreregRequest, EventPreregModel, UpdateModelSP> {
        override val request: UpdatePreregRequest
            get() = retrofit.create(UpdatePreregRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<EventPreregModel> {
            return request.makeRequest(payload.model())
        }
    }

    class PreregPlayer:
        UAndPServiceInterface<PreregPlayerRequest, EventPreregModel, CreateModelSP> {
        override val request: PreregPlayerRequest
            get() = retrofit.create(PreregPlayerRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<EventPreregModel> {
            return request.makeRequest(payload.model())
        }
    }

    class DeletePreregistrationsForPlayer:
        UAndPServiceInterface<DeletePreregistrationsForPlayerRequest, EventPreregListModel, ServicePayload> {
        override val request: DeletePreregistrationsForPlayerRequest
            get() = retrofit.create(DeletePreregistrationsForPlayerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<EventPreregListModel> {
            return request.makeRequest()
        }
    }

}