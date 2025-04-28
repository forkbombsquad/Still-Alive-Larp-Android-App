package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.OAuthTokenModel
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.ServiceUtils
import com.forkbombsquad.stillalivelarp.services.utils.TokenServiceInterface
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST


interface AuthTokenRequest {
    @FormUrlEncoded
    @Headers(
        "accept: application/json"
    )
    @POST("auth/login")
    suspend fun getAuthToken(@Field("username") username: String, @Field("password") password: String): Response<OAuthTokenModel>
}

class AuthService: TokenServiceInterface<AuthTokenRequest, OAuthTokenModel, ServicePayload> {

    override val request: AuthTokenRequest
        get() = retrofit.create(AuthTokenRequest::class.java)

    override suspend fun getResponse(payload: ServicePayload): Response<OAuthTokenModel> {
        return request.getAuthToken(ServiceUtils.user, ServiceUtils.pass)
    }

}