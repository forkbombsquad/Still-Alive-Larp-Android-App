package com.forkbombsquad.stillalivelarp.services.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonProperty
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.Base64


class OldSharedPrefsManager private constructor() {

    private val SharedPrefsName = "StillAliveLarpSharedPrefs"
    private val playerKey = "player_sp_key"
    private val characterKey = "character_sp_key"
    private val gearKey = "gear_sp_key"
    private val rulebookVersionKey = "rulebook_version_sp_key"
    private val rulebookKey = "rulebook_sp_key"
    private val skillsKey = "skills_sp_key"
    private val npcsKey = "npcs_sp_key"
    private val skillCategoriesKey = "skill_category_sp_key"

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
        this.set(globalGetContext()!!, playerKey, gson.toJson(player))
    }

    fun getPlayer(): PlayerModel? {
        val gson = Gson()
        get(globalGetContext()!!, playerKey)?.let {
            return gson.fromJson(it, PlayerModel::class.java)
        } ?: run {
            return null
        }
    }

    fun storeCharacter(character: OldFullCharacterModel) {
        val gson = Gson()
        this.set(globalGetContext()!!, characterKey, gson.toJson(character))
    }

    fun getCharacter(): OldFullCharacterModel? {
        val gson = Gson()
        get(globalGetContext()!!, characterKey)?.let {
            return gson.fromJson(it, OldFullCharacterModel::class.java)
        } ?: run {
            return null
        }
    }

    fun storeGear(gearListModel: GearListModel) {
        val gson = Gson()
        this.set(globalGetContext()!!, gearKey, gson.toJson(gearListModel))
    }

    fun getGear(): Array<GearModel>? {
        val gson = Gson()
        get(globalGetContext()!!, gearKey)?.let {
            return gson.fromJson(it, GearListModel::class.java).charGear
        } ?: run {
            return null
        }
    }

    fun getRulebookVersion(): String? {
        return get(globalGetContext()!!, rulebookVersionKey)
    }

    fun storeRulebookVersion(version: String) {
        this.set(globalGetContext()!!, rulebookVersionKey, version)
    }

    fun getRulebook(): String? {
        return get(globalGetContext()!!, rulebookKey)
    }

    fun storeRulebook(rulebook: String) {
        this.set(globalGetContext()!!, rulebookKey, rulebook)
    }

    fun storeSkills(skills: List<OldFullSkillModel>) {
        this.set(globalGetContext()!!, skillsKey, globalToJson(skills))
    }

    fun getSkills(): List<OldFullSkillModel> {
        return globalFromJson(get(globalGetContext()!!, skillsKey) ?: "") ?: listOf()
    }

    private data class NPCListModel(@JsonProperty("npcs") val npcs: Array<OldFullCharacterModel>
    ) : Serializable

    fun storeNPCs(npcs: List<OldFullCharacterModel>) {
        val npcListModel = NPCListModel(npcs.toTypedArray())
        this.set(globalGetContext()!!, npcsKey, globalToJson(npcListModel))
    }

    fun getNPCs(): List<OldFullCharacterModel> {
        return globalFromJson<NPCListModel>(get(globalGetContext()!!, npcsKey) ?: "")?.npcs?.toList() ?: listOf()
    }

    private data class SkillCategoryListModel(@JsonProperty("cats") val cats: Array<SkillCategoryModel>
    ) : Serializable
    fun storeSkillCategories(skillCategories: List<SkillCategoryModel>) {
        val skilCatListModel = SkillCategoryListModel(skillCategories.toTypedArray())
        this.set(globalGetContext()!!, skillCategoriesKey, globalToJson(skilCatListModel))
    }
    fun getSkillCategories(): List<SkillCategoryModel> {
        return globalFromJson<SkillCategoryListModel>(get(globalGetContext()!!, skillCategoriesKey) ?: "")?.cats?.toList() ?: listOf()
    }

    companion object {
        val shared = OldSharedPrefsManager()
    }
}