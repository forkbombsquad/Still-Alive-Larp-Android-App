package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP

interface GetCampStatusRequest {
    @HTTP(method ="GET", path = "camp-status/")
    suspend fun makeRequest(): Response<CampStatusModel>
}

class CampStatusService {
    class GetCampStatus:
        UAndPServiceInterface<GetCampStatusRequest, CampStatusModel, ServicePayload> {
        override val request: GetCampStatusRequest
            get() = retrofit.create(GetCampStatusRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CampStatusModel> {
            return request.makeRequest()
        }
    }
}
