package com.forkbombsquad.stillalivelarp.services.utils

import com.forkbombsquad.stillalivelarp.services.managers.AuthManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.forkbombsquad.stillalivelarp.utils.bodyToString
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

object RequestLogInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        globalPrint("SERVICE CONTROLLER: Request:\n${request.url()}")
        globalPrint("SERVICE CONTROLLER: Request Headers:\n${request.headers()}")
        request.bodyToString().ifLet {
            globalPrint("SERVICE CONTROLLER: Request Body:\n${it}")
        }
        return chain.proceed(request)
    }

}

object GetTokenInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithAuth = chain.request()
            .newBuilder()
            .header("Authorization", Credentials.basic(ServiceUtils.cid, ServiceUtils.sec))
            .build()
        return chain.proceed(requestWithAuth)
    }

}

object AuthInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { AuthManager.shared.getAuthToken() } ?: ""
        val requestWithAuth = chain.request()
            .newBuilder()
            .header(
                "Authorization", "Bearer $token"
            )
            .header(
                "Content-Type", "application/json"
            )
            .build()
        return chain.proceed(requestWithAuth)
    }

}

object UAndPInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val em = UserAndPassManager.shared.getU() ?: ""
        val pp = UserAndPassManager.shared.getP() ?: ""
        val requestWithAuth = chain.request()
            .newBuilder()
            .header(
                "em", "$em"
            )
            .header(
                "pp", "$pp"
            )
            .build()
        return chain.proceed(requestWithAuth)
    }

}
