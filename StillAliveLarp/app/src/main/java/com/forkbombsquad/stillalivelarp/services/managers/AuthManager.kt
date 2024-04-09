package com.forkbombsquad.stillalivelarp.services.managers

import com.forkbombsquad.stillalivelarp.services.AuthService
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.math.abs

class AuthManager private constructor() {

    private val tokenExpireDays = 6

    private var token: String? = null
    private var tokenExpireDate: LocalDate? = null

    fun forceRefreshToken() {
        token = null
        tokenExpireDate = null
    }

    suspend fun getAuthToken(): String? {
        if (tokenIsExpired() || token == null) {
            globalPrint("Fetching Token")
            val authService = AuthService()
            val result = coroutineScope {
                async {
                    val accessToken: String? = authService.successfulResponse()?.access_token
                    accessToken.let {
                        token = it
                        tokenExpireDate = LocalDate.now()
                        it
                    } ?: run {
                        token = null
                        tokenExpireDate = null
                        null
                    }
                }
            }.await()
            return result
        } else {
            globalPrint("Reusing Token")
            return token
        }
    }

    fun tokenIsExpired(): Boolean {
        tokenExpireDate?.let {
            val currentDate = LocalDate.now()
            val period = Period.between(it, currentDate)
            return abs(period.years) >= tokenExpireDays
        } ?: run {
            return true
        }
    }

    companion object {
        val shared = AuthManager()
    }
}