package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.GearListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.UpdateTrackerModel
import com.forkbombsquad.stillalivelarp.services.utils.AuthServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetUpdateTrackerRequest {
    @HTTP(method ="GET", path = "update-tracker/updates/")
    suspend fun makeRequest(): Response<UpdateTrackerModel>
}

class UpdateTrackerService {
    class GetUpdateTracker:
        AuthServiceInterface<GetUpdateTrackerRequest, UpdateTrackerModel, ServicePayload> {
        override val request: GetUpdateTrackerRequest
            get() = retrofit.create(GetUpdateTrackerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<UpdateTrackerModel> {
            return request.makeRequest()
        }
    }
}