package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import kotlin.reflect.KClass

class MockDataLoader private constructor() {

    companion object {
        var shared = MockDataLoader()
            private set
        val noResponseErrorJson: String = globalToJson(ErrorModel("Mock Response Error No Data Found"))
    }

    // Endpoint, list of calls in order
    private val mockDataStorage: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val mockDataCounter: MutableMap<String, Int> = mutableMapOf()

    // Set num calls to be -1 for infinite calls
    @JvmName("loadMockDataEndpointAny")
    fun loadMockData(requestClass: KClass<*>, sp: ServicePayload = EmptyServicePayload(), data: Any, numCalls: Int = 1) {
        loadFormattedMockData(MockServiceUtils.buildEndpointUrl(requestClass, sp), if (data is String) data else globalToJson(data), numCalls)
    }

    @JvmName("loadFormattedMockDataEndpointAny")
    private fun loadFormattedMockData(endpoint: String, data: String, numCalls: Int) {
        val list = mockDataStorage.getOrPut(endpoint) { mutableListOf() }
        if (numCalls > 0) {
            repeat(numCalls) {
                list.add(data)
            }
        } else {
            list.add(data)
            mockDataCounter[endpoint] = -1
        }
        globalUnitTestPrint("{${(numCalls == -1).ternary("Infinite", "$numCalls")}} Data Added For Endpoint: $endpoint", UnitTestColor.GREEN)
    }

    fun getMockData(endpoint: String): String? {
        val index = mockDataCounter.getOrDefault(endpoint, 0)
        globalUnitTestPrint("Attempting to Retrieve Data with Index: $index for Endpoint: $endpoint", UnitTestColor.GREEN)
        val value = mockDataStorage.getOrDefault(endpoint, mutableListOf()).getOrNull((index == -1).ternary(0, index))
        if (index >= 0) {
            mockDataCounter[endpoint] = index + 1
        }

        return value
    }

    fun clearMockData() {
        mockDataStorage.clear()
        mockDataCounter.clear()
    }

}