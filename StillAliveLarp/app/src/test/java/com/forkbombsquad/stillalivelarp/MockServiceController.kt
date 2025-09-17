package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
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
import com.forkbombsquad.stillalivelarp.services.utils.ServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.TakeClassSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdatePSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.globalFromJson
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.globalUnitTestPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.gson.reflect.TypeToken
import io.mockk.mockkClass
import retrofit2.Response
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible


class MockServiceController<G : Any, T, H : ServicePayload>(
    private val realService: ServiceInterface<G, T, H>,
    private val requestClass: KClass<G>
) : ServiceInterface<G, T, H> by realService {

    override val request: G = mockkClass(requestClass)

    override suspend fun successfulResponse(payload: H, ignoreErrors: Boolean, ignorePrintResopnseBody: Boolean): T? {
        val mockResponseJson = getMockResponse(payload)
        val type: Type = exactReturnType()
        val model: T? = globalFromJson(mockResponseJson, type)
        return if (model != null) {
            if (!ignorePrintResopnseBody) {
                globalUnitTestPrint("MOCK SERVICE CONTROLLER: Response Body:\n$mockResponseJson")
            }
            model
        } else {
            globalFromJson<ErrorModel>(mockResponseJson).ifLet({
                globalUnitTestPrint("!!ERROR!! MOCK SERVICE CONTROLLER\n${it.detail}")
            }, {
                globalUnitTestPrint("!!ERROR!! MOCK SERVICE CONTROLLER\nUNKNOWN ERROR")
            })
            null
        }
    }

    private fun exactReturnType(): Type {
        return (realService::class.java.genericInterfaces.first() as ParameterizedType).actualTypeArguments[1]
    }

    private fun getMockResponse(payload: ServicePayload): String {
        return MockDataLoader.shared.getMockData(MockServiceUtils.buildEndpointUrl(requestClass, payload)) ?: MockDataLoader.noResponseErrorJson
    }

}

fun <G : Any, T, H : ServicePayload> MockService(realService: ServiceInterface<G, T, H>): MockServiceController<G, T, H> {
    // Get the 'request' property
    val prop = realService::class.declaredMemberProperties.firstOrNull { it.name == "request" }
        ?: error("Service ${realService::class.simpleName} has no 'request' property")

    prop.isAccessible = true

    val requestKClass: KClass<G> = prop.returnType.classifier as? KClass<G>
        ?: error("Cannot determine request type for service ${realService::class.simpleName}")

    return MockServiceController(realService, requestKClass)
}