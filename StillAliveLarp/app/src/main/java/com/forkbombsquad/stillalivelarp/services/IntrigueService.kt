package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.IntrigueListModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetIntrigueForEventRequest {
    @HTTP(method ="GET", path = "intrigue/for_event/{eventId}")
    suspend fun makeRequest(@Path("eventId") eventId: Int): Response<IntrigueModel>
}

interface GetAllIntriguesRequest {
    @HTTP(method ="GET", path = "intrigue/all/")
    suspend fun makeRequest(): Response<IntrigueListModel>
}


class IntrigueService {
    class GetIntrigueForEvent:
        UAndPServiceInterface<GetIntrigueForEventRequest, IntrigueModel, IdSP> {
        override val request: GetIntrigueForEventRequest
            get() = retrofit.create(GetIntrigueForEventRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<IntrigueModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllIntrigues:
        UAndPServiceInterface<GetAllIntriguesRequest, IntrigueListModel, ServicePayload> {
        override val request: GetAllIntriguesRequest
            get() = retrofit.create(GetAllIntriguesRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<IntrigueListModel> {
            return request.makeRequest()
        }
    }

}