package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCheckInSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharactersForTypeWithIdSP
import com.forkbombsquad.stillalivelarp.services.utils.ContactCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.GiveCharacterCheckInRewardsSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.PlayerCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.RefundSkillSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.TakeClassSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdatePSP
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class MockServiceUtils {
    companion object {

        fun buildEndpointUrl(requestClass: KClass<*>, sp: ServicePayload): String {
            return getEndpointUrl(requestClass, sp)
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: ServicePayload): String {
            return when (payload) {
                is EmptyServicePayload -> getEndpointUrl(requestClass, payload)
                is IdSP -> getEndpointUrl(requestClass, payload)
                is PlayerCreateSP -> getEndpointUrl(requestClass, payload)
                is UpdatePSP -> getEndpointUrl(requestClass, payload)
                is CharacterCreateSP -> getEndpointUrl(requestClass, payload)
                is CharacterSP -> getEndpointUrl(requestClass, payload)
                is CharacterSkillCreateSP -> getEndpointUrl(requestClass, payload)
                is ContactCreateSP -> getEndpointUrl(requestClass, payload)
                is AwardCreateSP -> getEndpointUrl(requestClass, payload)
                is CreateModelSP -> getEndpointUrl(requestClass, payload)
                is UpdateModelSP -> getEndpointUrl(requestClass, payload)
                is CharacterCheckInSP -> getEndpointUrl(requestClass, payload)
                is GiveCharacterCheckInRewardsSP -> getEndpointUrl(requestClass, payload)
                is TakeClassSP -> getEndpointUrl(requestClass, payload)
                is RefundSkillSP -> getEndpointUrl(requestClass, payload)
                is CharactersForTypeWithIdSP -> getEndpointUrl(requestClass, payload)
                else -> ""
            }
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: EmptyServicePayload): String {
            return buildEndpointUrl(requestClass, listOf())
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: IdSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.id()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: PlayerCreateSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.preApprovalCode(), payload.player()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: UpdatePSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.playerId(), payload.p()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CharacterCreateSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.character()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CharacterSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.character()))

        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CharacterSkillCreateSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.playerId(), payload.charSkill()))

        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: ContactCreateSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.contact()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: AwardCreateSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.award()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CreateModelSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.model()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: UpdateModelSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.model()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CharacterCheckInSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.eventId(), payload.playerId(), payload.characterId()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: GiveCharacterCheckInRewardsSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.eventId(), payload.playerId(), payload.characterId(), payload.newBulletCount()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: TakeClassSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.characterId(), payload.skillId()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: RefundSkillSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.playerId(), payload.characterId(), payload.skillId()))
        }

        private fun getEndpointUrl(requestClass: KClass<*>, payload: CharactersForTypeWithIdSP): String {
            return buildEndpointUrl(requestClass, listOf(payload.characterTypeId(), payload.playerId()))
        }

        private fun buildEndpointUrl(requestClass: KClass<*>, args: List<Any?>): String {
            val function = requestClass.memberFunctions.firstOrNull { it.name == "makeRequest" }

            return if (function == null) {
                val func = requestClass.memberFunctions.first { it.name == "getAuthToken" || it.name == "getAuthPlayerToken" || it.name == "getVersions" }
                if (func.findAnnotation<retrofit2.http.POST>() != null) func.findAnnotation<retrofit2.http.POST>()!!.value else (func.findAnnotation<retrofit2.http.GET>()?.value ?: error("Function \"custom\" does not have @POST or @GET annotation"))
            } else {
                val httpAnnotation = function.findAnnotation<retrofit2.http.HTTP>()
                    ?: error("Function \"makeRequest\" does not have @HTTP annotation")

                var path = httpAnnotation.path

                val usedArgs = mutableSetOf<Int>()

                // Replace path parameters in order
                val pathParamRegex = "\\{[^}]+}".toRegex()
                pathParamRegex.findAll(path).forEachIndexed { index, matchResult ->
                    if (index < args.size) {
                        path = path.replace(matchResult.value, args[index].toString())
                        usedArgs.add(index)
                    }
                }

                // Treat remaining arguments as query parameters if they're string, int, or boolean
                val remainingArgs = args.withIndex().filter { it.index !in usedArgs }.filter { it.value is String || it.value is Int || it.value is Boolean }
                val queryString = if (remainingArgs.isNotEmpty()) {
                    remainingArgs.joinToString("&") { "param${it.index + 1}=${it.value}" }
                } else {
                    ""
                }

                if (queryString.isNotEmpty()) "$path?$queryString" else path
            }


        }

    }
}