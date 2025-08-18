package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context

class UserAndPassManager private constructor() {

    private val rememberKey = "remkey"
    private val ukey = "emanresu"
    private val pkey = "taxkey"

    private val tempukey = "temp$ukey"
    private val temppkey = "temp$pkey"

    fun setTemp(u: String, p: String) {
        LocalDataManager.shared.setUnPRelatedObject(tempukey, u)
        LocalDataManager.shared.setUnPRelatedObject(temppkey, p)
    }

    fun clearTemp() {
        LocalDataManager.shared.clearUnPRelatedObject(tempukey)
        LocalDataManager.shared.clearUnPRelatedObject(temppkey)
    }

    private fun clear() {
        LocalDataManager.shared.clearUnPRelatedObject(ukey)
        LocalDataManager.shared.clearUnPRelatedObject(pkey)
        LocalDataManager.shared.clearUnPRelatedObject(rememberKey)
    }

    fun clearAll() {
        clear()
        clearTemp()
    }

    fun setUandP(u: String, p: String, remember: Boolean) {
        LocalDataManager.shared.setUnPRelatedObject(ukey, u)
        LocalDataManager.shared.setUnPRelatedObject(pkey, p)
        LocalDataManager.shared.setUnPRelatedObject(rememberKey, remember.toString())
    }

    fun getU(context: Context? = null): String? {
        return LocalDataManager.shared.getUnPRelatedObject(context, ukey)
    }

    fun getP(context: Context? = null): String? {
        return LocalDataManager.shared.getUnPRelatedObject(context, pkey)
    }

    fun getRemember(context: Context? = null): Boolean {
        return LocalDataManager.shared.getUnPRelatedObject(context, rememberKey)?.toBoolean() ?: false
    }

    fun getTempU(context: Context? = null): String? {
        return LocalDataManager.shared.getUnPRelatedObject(context, tempukey)
    }

    fun getTempP(context: Context? = null): String? {
        return LocalDataManager.shared.getUnPRelatedObject(context, temppkey)
    }

    companion object {
        val shared = UserAndPassManager()
    }
}