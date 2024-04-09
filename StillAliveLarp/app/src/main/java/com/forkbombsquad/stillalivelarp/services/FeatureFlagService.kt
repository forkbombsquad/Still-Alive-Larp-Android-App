package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagListModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetFeatureFlagRequest {
    @HTTP(method ="GET", path = "feature-flag/{featureFlagId}")
    suspend fun makeRequest(@Path("featureFlagId") featureFlagId: Int): Response<FeatureFlagModel>
}

interface GetAllFeatureFlagsRequest {
    @HTTP(method ="GET", path = "feature-flag/all/")
    suspend fun makeRequest(): Response<FeatureFlagListModel>
}

class FeatureFlagService {
    class GetFeatureFlag: UAndPServiceInterface<GetFeatureFlagRequest, FeatureFlagModel, IdSP> {
        override val request: GetFeatureFlagRequest
            get() = retrofit.create(GetFeatureFlagRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<FeatureFlagModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllFeatureFlags:
        UAndPServiceInterface<GetAllFeatureFlagsRequest, FeatureFlagListModel, ServicePayload> {
        override val request: GetAllFeatureFlagsRequest
            get() = retrofit.create(GetAllFeatureFlagsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<FeatureFlagListModel> {
            return request.makeRequest()
        }
    }
}