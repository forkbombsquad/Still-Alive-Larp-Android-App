package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.AppVersionModel
import com.forkbombsquad.stillalivelarp.services.utils.AuthServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import retrofit2.Response
import retrofit2.http.*

interface VersionRequest {
    @Headers(
        "accept: application/json"
    )
    @GET("app-version/")
    suspend fun getVersions(): Response<AppVersionModel>
}

class VersionService: AuthServiceInterface<VersionRequest, AppVersionModel, ServicePayload> {

    override val request: VersionRequest
        get() = retrofit.create(VersionRequest::class.java)

    override suspend fun getResponse(payload: ServicePayload): Response<AppVersionModel> {
        return request.getVersions()
    }

}