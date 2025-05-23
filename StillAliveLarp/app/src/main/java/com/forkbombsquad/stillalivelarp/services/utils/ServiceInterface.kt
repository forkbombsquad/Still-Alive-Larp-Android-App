package com.forkbombsquad.stillalivelarp.services.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import retrofit2.Response
import retrofit2.Retrofit

interface ServiceInterface<G, T, H: ServicePayload> {
    val retrofit: Retrofit
        get() = RetrofitClient.getClient()

    val request: G

    suspend fun getResponse(payload: H): Response<T>

    suspend fun successfulResponse(payload: H = ServicePayload.empty() as H, ignoreErrors: Boolean = false): T? {
        val response: Response<T>? = try {
            getResponse(payload)
        } catch (e: Exception) {
            globalPrint(e.message.toString())
            null
        }
        response?.body().ifLet({
            globalPrint("SERVICE CONTROLLER: Response Body:\n${globalToJson(it)}")
        }, {
            if (!ignoreErrors) {
                response?.errorBody().ifLet({
                    globalFromJson<ErrorModel>(it.string()).ifLet({ error ->
                        AlertUtils.displayError(StillAliveLarpApplication.activity, response?.code() ?: 502, error)
                    }, {
                        AlertUtils.displaySomethingWentWrong(StillAliveLarpApplication.activity)
                    })
                }, {
                    AlertUtils.displaySomethingWentWrong(StillAliveLarpApplication.activity)
                })
            }
        })
        return response?.body()
    }

    suspend fun errorResponse(payload: H = ServicePayload.empty() as H): ErrorModel {
        val response = getResponse(payload)
        val error = response.errorBody()

        val mapper = ObjectMapper()
        val mappedError: ErrorModel? = error.let { errorBody ->
            mapper.readValue(errorBody.toString(), ErrorModel::class.java)
        }
        return mappedError ?: ErrorModel("Unknown Error Occurred")
    }

}

interface TokenServiceInterface<G, T, H: ServicePayload>: ServiceInterface<G, T, H> {
    override val retrofit: Retrofit
        get() = RetrofitClient.getTokenClient()
}

interface AuthServiceInterface<G, T, H: ServicePayload>: ServiceInterface<G, T, H> {
    override val retrofit: Retrofit
        get() = RetrofitClient.getAuthClient()
}

interface UAndPServiceInterface<G, T, H: ServicePayload>: ServiceInterface<G, T, H> {
    override val retrofit: Retrofit
        get() = RetrofitClient.getUAndPClient()
}