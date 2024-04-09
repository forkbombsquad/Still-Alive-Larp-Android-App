package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.preference.PreferenceManager
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.google.gson.Gson

class SharedPrefsManager private constructor() {

    private val SharedPrefsName = "StillAliveLarpSharedPrefs"
    private val playerKey = "player_sp_key"
    private val characterKey = "character_sp_key"
    private val gearKey = "gear_sp_key"
    private val rulebookVersionKey = "rulebook_version_sp_key"
    private val rulebookKey = "rulebook_sp_key"

    fun clearAll(context: Context) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        editor.remove(playerKey)
        editor.remove(characterKey)
        editor.remove(gearKey)
        editor.commit()
    }

    fun clear(context: Context, key: String) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        editor.remove(key)
        editor.commit()
    }

    fun set(context: Context, key: String, value: String) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun setMultiple(context: Context, keyValuePairs: Map<String, String>) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        for ((k, v) in keyValuePairs) {
            editor.putString(k, v)
        }
        editor.commit()
    }

    fun get(context: Context, key: String): String? {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        return sharedPrefs.getString(key, null)
    }

    fun setBool(context: Context, key: String, value: Boolean) {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun getBool(context: Context, key: String): Boolean {
        val sharedPrefs = context.getSharedPreferences(SharedPrefsName, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(key, false)
    }

    fun storePlayer(player: PlayerModel) {
        val gson = Gson()
        this.set(StillAliveLarpApplication.context, playerKey, gson.toJson(player))
    }

    fun getPlayer(): PlayerModel? {
        val gson = Gson()
        get(StillAliveLarpApplication.context, playerKey)?.let {
            return gson.fromJson(it, PlayerModel::class.java)
        } ?: run {
            return null
        }
    }

    fun storeCharacter(character: FullCharacterModel) {
        val gson = Gson()
        this.set(StillAliveLarpApplication.context, characterKey, gson.toJson(character))
    }

    fun getCharacter(): FullCharacterModel? {
        val gson = Gson()
        get(StillAliveLarpApplication.context, characterKey)?.let {
            return gson.fromJson(it, FullCharacterModel::class.java)
        } ?: run {
            return null
        }
    }

    fun storeGear(gearListModel: GearListModel) {
        val gson = Gson()
        this.set(StillAliveLarpApplication.context, gearKey, gson.toJson(gearListModel))
    }

    fun getGear(): Array<GearModel>? {
        val gson = Gson()
        get(StillAliveLarpApplication.context, gearKey)?.let {
            return gson.fromJson(it, GearListModel::class.java).charGear
        } ?: run {
            return null
        }
    }

    fun getRulebookVersion(): String? {
        return get(StillAliveLarpApplication.context, rulebookVersionKey)
    }

    fun storeRulebookVersion(version: String) {
        this.set(StillAliveLarpApplication.context, rulebookVersionKey, version)
    }

    fun getRulebook(): String? {
        return get(StillAliveLarpApplication.context, rulebookKey)
    }

    fun storeRulebook(rulebook: String) {
        this.set(StillAliveLarpApplication.context, rulebookKey, rulebook)
    }

    companion object {
        val shared = SharedPrefsManager()
    }
}