package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayloadKey
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query


interface GetSkillRequest {
    @HTTP(method ="GET", path = "skills/{skillId}")
    suspend fun makeRequest(@Path("skillId") skillId: Int): Response<SkillModel>
}

interface GetAllSkillsRequest {
    @HTTP(method ="GET", path = "skills/all/")
    suspend fun makeRequest(): Response<SkillListModel>
}

class SkillService {
    class GetSkill: UAndPServiceInterface<GetSkillRequest, SkillModel, IdSP> {
        override val request: GetSkillRequest
            get() = retrofit.create(GetSkillRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<SkillModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllSkills:
        UAndPServiceInterface<GetAllSkillsRequest, SkillListModel, ServicePayload> {
        override val request: GetAllSkillsRequest
            get() = retrofit.create(GetAllSkillsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<SkillListModel> {
            return request.makeRequest()
        }
    }
}