package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
import com.forkbombsquad.stillalivelarp.services.utils.ServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import io.mockk.mockkClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
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
                globalUnitTestPrint("MOCK SERVICE CONTROLLER: Response Body:\n$mockResponseJson", UnitTestColor.GREEN)
            }
            model
        } else {
            globalFromJson<ErrorModel>(mockResponseJson).ifLet({
                globalUnitTestPrint("MOCK SERVICE CONTROLLER: Error Model: \n${it.detail}", UnitTestColor.YELLOW)
            }, {
                globalUnitTestPrint("!!ERROR!! MOCK SERVICE CONTROLLER: \nUNKNOWN ERROR", UnitTestColor.RED)
            })
            null
        }
    }

    private fun exactReturnType(): Type {
        return (realService::class.java.genericInterfaces.first() as ParameterizedType).actualTypeArguments[1]
    }

    private fun getMockResponse(payload: ServicePayload): String {
        return MockDataLoader.shared.getMockData(
            MockServiceUtils.buildEndpointUrl(
                requestClass,
                payload
            )
        ) ?: MockDataLoader.noResponseErrorJson
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