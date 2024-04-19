package com.forkbombsquad.stillalivelarp.services.utils

import android.app.Service
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import okhttp3.RequestBody

enum class ServicePayloadKey(val key: String) {
    PARAM1("param1"),
    PARAM2("param2"),
    PARAM3("param3"),
    PARAM4("param4")
}

abstract class ServicePayload() {

    var map: MutableMap<String, Any> = mutableMapOf()

    constructor(map: MutableMap<String, Any>) : this() {
        this.map = map
    }

    fun set(spk: ServicePayloadKey, value: Any) {
        map[spk.key] = value
    }

    inline fun <reified T> get(spk: ServicePayloadKey): T {
        return map[spk.key] as T
    }

    inline fun <reified T> getOptional(spk: ServicePayloadKey): T? {
        return map[spk.key] as? T
    }

    companion object {
        fun empty(): ServicePayload {
            return EmptyServicePayload()
        }
    }

}

class EmptyServicePayload(): ServicePayload()

class IdSP(id: Int): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to id
)) {
    fun id(): Int {
        return get(ServicePayloadKey.PARAM1)
    }
}

class PlayerCreateSP(player: PlayerCreateModel, preapprovalcode: String): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to player,
    ServicePayloadKey.PARAM2.key to preapprovalcode
)) {
    fun player(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }

    fun preApprovalCode(): String {
        return get(ServicePayloadKey.PARAM2)
    }
}

class UpdatePSP(playerId: Int, p: String): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to playerId,
    ServicePayloadKey.PARAM2.key to p
)) {
    fun playerId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun p(): String {
        return get(ServicePayloadKey.PARAM2)
    }
}

class CharacterCreateSP(char: CharacterCreateModel): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to char
)) {
    fun character(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class CharacterSP(char: CharacterModel): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to char
)) {
    fun character(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class CharacterSkillCreateSP(playerId: Int, charSkill: CharacterSkillCreateModel): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to playerId,
    ServicePayloadKey.PARAM2.key to charSkill
)) {
    fun playerId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun charSkill(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM2)))
    }
}

class ContactCreateSP(contact: ContactRequestCreateModel): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to contact
)) {
    fun contact(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class AwardCreateSP(award: AwardCreateModel): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to award
)) {
    fun award(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class CreateModelSP(model: Any): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to model
)) {
    fun  model(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class UpdateModelSP(model: Any): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to model
)) {
    fun model(): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), globalToJson(get(ServicePayloadKey.PARAM1)))
    }
}

class CharacterCheckInSP(eventId: Int, playerId: Int, characterId: Int): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to eventId,
    ServicePayloadKey.PARAM2.key to playerId,
    ServicePayloadKey.PARAM3.key to characterId
)) {
    fun eventId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun playerId(): Int {
        return get(ServicePayloadKey.PARAM2)
    }

    fun characterId(): Int {
        return get(ServicePayloadKey.PARAM3)
    }
}

class GiveCharacterCheckInRewardsSP(eventId: Int, playerId: Int, characterId: Int, newBulletCount: Int): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to eventId,
    ServicePayloadKey.PARAM2.key to playerId,
    ServicePayloadKey.PARAM3.key to characterId,
    ServicePayloadKey.PARAM4.key to newBulletCount
)) {
    fun eventId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun playerId(): Int {
        return get(ServicePayloadKey.PARAM2)
    }

    fun characterId(): Int {
        return get(ServicePayloadKey.PARAM3)
    }

    fun newBulletCount(): Int {
        return get(ServicePayloadKey.PARAM4)
    }
}

class TakeClassSP(characterId: Int, skillId: Int): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to characterId,
    ServicePayloadKey.PARAM2.key to skillId
)) {
    fun characterId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun skillId(): Int {
        return get(ServicePayloadKey.PARAM2)
    }
}

class RefundSkillSP(playerId: Int, characterId: Int, skillId: Int): ServicePayload(mutableMapOf(
    ServicePayloadKey.PARAM1.key to playerId,
    ServicePayloadKey.PARAM2.key to characterId,
    ServicePayloadKey.PARAM3.key to skillId
)) {
    fun playerId(): Int {
        return get(ServicePayloadKey.PARAM1)
    }

    fun characterId(): Int {
        return get(ServicePayloadKey.PARAM2)
    }

    fun skillId(): Int {
        return get(ServicePayloadKey.PARAM3)
    }
}