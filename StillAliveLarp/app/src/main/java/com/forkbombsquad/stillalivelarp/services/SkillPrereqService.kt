package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.SkillListModel
import com.forkbombsquad.stillalivelarp.services.models.SkillModel
import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqListModel
import com.forkbombsquad.stillalivelarp.services.models.SkillPrereqModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAllSkillPrereqsForSkillRequest {
    @HTTP(method ="GET", path = "skill-prereqs/all_with_skill_id/{baseSkillId}")
    suspend fun makeRequest(@Path("baseSkillId") baseSkillId: Int): Response<SkillPrereqListModel>
}

interface GetAllSkillPrereqsRequest {
    @HTTP(method ="GET", path = "skill-prereqs/all/")
    suspend fun makeRequest(): Response<SkillPrereqListModel>
}

class SkillPrereqService {
    class GetAllSkillPrereqsForSkill: UAndPServiceInterface<GetAllSkillPrereqsForSkillRequest, SkillPrereqListModel, IdSP> {
        override val request: GetAllSkillPrereqsForSkillRequest
            get() = retrofit.create(GetAllSkillPrereqsForSkillRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<SkillPrereqListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllSkillPrereqs: UAndPServiceInterface<GetAllSkillPrereqsRequest, SkillPrereqListModel, ServicePayload> {
        override val request: GetAllSkillPrereqsRequest
            get() = retrofit.create(GetAllSkillPrereqsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<SkillPrereqListModel> {
            return request.makeRequest()
        }
    }
}