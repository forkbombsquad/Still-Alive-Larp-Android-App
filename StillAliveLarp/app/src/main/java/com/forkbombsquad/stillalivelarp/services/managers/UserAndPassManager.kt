package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context

class UserAndPassManager private constructor() {

    val rememberKey = "remkey"
    val ukey = "emanresu"
    val pkey = "taxkey"

    fun setTemp(context: Context, u: String, p: String) {
        OldSharedPrefsManager.shared.setMultiple(context, mapOf("temp$ukey" to u, "temp$pkey" to p))
    }

    fun clearTemp(context: Context) {
        OldSharedPrefsManager.shared.clear(context, "temp$ukey")
        OldSharedPrefsManager.shared.clear(context, "temp$pkey")
    }

    fun clear(context: Context) {
        OldSharedPrefsManager.shared.clear(context, ukey)
        OldSharedPrefsManager.shared.clear(context, pkey)
    }

    fun setUandP(context: Context, u: String, p: String, remember: Boolean) {
        OldSharedPrefsManager.shared.setMultiple(context, mapOf(ukey to u, pkey to p, rememberKey to remember.toString()))
    }

    fun getU(context: Context): String? {
        return OldSharedPrefsManager.shared.get(context, ukey)
    }

    fun getP(context: Context): String? {
        return OldSharedPrefsManager.shared.get(context, pkey)
    }

    fun getRemember(context: Context): Boolean {
        return OldSharedPrefsManager.shared.get(context, rememberKey)?.toBoolean() ?: false
    }

    fun getTempU(context: Context): String? {
        return OldSharedPrefsManager.shared.get(context, "temp$ukey")
    }

    fun getTempP(context: Context): String? {
        return OldSharedPrefsManager.shared.get(context, "temp$pkey")
    }

    companion object {
        val shared = UserAndPassManager()
    }
}