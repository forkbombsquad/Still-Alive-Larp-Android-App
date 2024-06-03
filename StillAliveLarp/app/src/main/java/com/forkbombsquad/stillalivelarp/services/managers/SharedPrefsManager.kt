package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.util.Base64


class SharedPrefsManager private constructor() {

    private val SharedPrefsName = "StillAliveLarpSharedPrefs"
    private val playerKey = "player_sp_key"
    private val characterKey = "character_sp_key"
    private val gearKey = "gear_sp_key"
    private val rulebookVersionKey = "rulebook_version_sp_key"
    private val rulebookKey = "rulebook_sp_key"
    private val skillsKey = "skills_sp_key"

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

    fun set(context: Context, key: String, value: Bitmap) {
        val baos = ByteArrayOutputStream()
        value.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        val encodedImage: String = Base64.getEncoder().encodeToString(b)
        set(context, key, encodedImage)
    }

    fun getBitmap(context: Context, key: String): Bitmap? {
        val encodedImage: String? = get(context, key)
        if (encodedImage != null) {
            val imageAsBytes = Base64.getDecoder().decode(encodedImage.toByteArray())
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
        } else {
            return null
        }
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

    fun storeSkills(skills: List<FullSkillModel>) {
        this.set(StillAliveLarpApplication.context, skillsKey, globalToJson(skills))
    }

    fun getSkills(): List<FullSkillModel> {
        val type = object: TypeToken<List<FullSkillModel>>() {}.type
        return globalFromJson(get(StillAliveLarpApplication.context, skillsKey) ?: "", type) ?: listOf()
    }

    companion object {
        val shared = SharedPrefsManager()
    }
}