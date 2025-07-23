package com.forkbombsquad.stillalivelarp.services.managers

import com.forkbombsquad.stillalivelarp.services.AuthService
import com.forkbombsquad.stillalivelarp.services.PlayerAuthService
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.Period
import kotlin.math.abs

class AuthManager private constructor() {

    private val tokenExpireDays = 6

    private var token: String? = null
    private var tokenExpireDate: LocalDate? = null

    private var playerToken: String? = null
    private var playerTokenExpireDate: LocalDate? = null

    fun forceRefreshToken() {
        token = null
        tokenExpireDate = null
        playerToken = null
        playerTokenExpireDate = null
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

    suspend fun getPlayerToken(): String? {
        if (playerTokenIsExpired() || playerToken == null) {
            globalPrint("Fetching Player Token")
            val authService = PlayerAuthService()
            val result = coroutineScope {
                async {
                    val accessToken: String? = authService.successfulResponse()?.access_token
                    accessToken.let {
                        playerToken = it
                        playerTokenExpireDate = LocalDate.now()
                        it
                    } ?: run {
                        playerToken = null
                        playerTokenExpireDate = null
                        null
                    }
                }
            }.await()
            return result
        } else {
            globalPrint("Reusing Player Token")
            return playerToken
        }
    }

    fun playerTokenIsExpired(): Boolean {
        playerTokenExpireDate?.let {
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