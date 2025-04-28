package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.GearListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectListModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetResearchProjectRequest {
    @HTTP(method ="GET", path = "research-project/{researchProjectId}")
    suspend fun makeRequest(@Path("researchProjectId") researchProjectId: Int): Response<ResearchProjectModel>
}

interface GetAllResearchProjectsRequest {
    @HTTP(method = "GET", path = "research-project/all/")
    suspend fun makeRequest(): Response<ResearchProjectListModel>

}

class ResearchProjectService {
    class GetAllResearchProjects:
        UAndPServiceInterface<GetAllResearchProjectsRequest, ResearchProjectListModel, ServicePayload> {
        override val request: GetAllResearchProjectsRequest
            get() = retrofit.create(GetAllResearchProjectsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<ResearchProjectListModel> {
            return request.makeRequest()
        }
    }

    class GetResearchProject:
        UAndPServiceInterface<GetResearchProjectRequest, ResearchProjectModel, IdSP> {
        override val request: GetResearchProjectRequest
            get() = retrofit.create(GetResearchProjectRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<ResearchProjectModel> {
            return request.makeRequest(payload.id())
        }
    }
}