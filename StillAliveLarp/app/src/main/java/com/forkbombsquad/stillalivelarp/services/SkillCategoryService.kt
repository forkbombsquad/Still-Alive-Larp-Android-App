package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.SkillCategoryListModel
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP

interface GetAllSkillCategoriesRequest {
    @HTTP(method ="GET", path = "skill-categories/all/")
    suspend fun makeRequest(): Response<SkillCategoryListModel>
}

class SkillCategoryService {
    class GetAllSkillCategories:
        UAndPServiceInterface<GetAllSkillCategoriesRequest, SkillCategoryListModel, ServicePayload> {
        override val request: GetAllSkillCategoriesRequest
            get() = retrofit.create(GetAllSkillCategoriesRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<SkillCategoryListModel> {
            return request.makeRequest()
        }
    }
}