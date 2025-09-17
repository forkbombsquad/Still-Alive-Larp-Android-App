package com.forkbombsquad.stillalivelarp

import com.forkbombsquad.stillalivelarp.services.managers.LocalDataManager
import com.forkbombsquad.stillalivelarp.services.managers.UserAndPassManager
import com.forkbombsquad.stillalivelarp.services.models.ErrorModel
import com.forkbombsquad.stillalivelarp.services.utils.EmptyServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.utils.globalToJson
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

    @JvmName("loadMockDataEndpointAny")
    fun loadMockData(requestClass: KClass<*>, sp: ServicePayload = EmptyServicePayload(), data: Any) {
        loadFormattedMockData(MockServiceUtils.buildEndpointUrl(requestClass, sp), if (data is String) data else globalToJson(data))
    }

    @JvmName("loadMockDataEndpointAnyList")
    fun loadMockData(requestClass: KClass<*>, sp: ServicePayload = EmptyServicePayload(), data: List<Any>) {
        val newMockData = data.map { item -> if (item is String) item else globalToJson(item) }
        loadFormattedMockData(MockServiceUtils.buildEndpointUrl(requestClass, sp), newMockData)
    }

    @JvmName("loadFormattedMockDataEndpointAnyList")
    private fun loadFormattedMockData(endpoint: String, data: List<String>) {
        val list = mockDataStorage.getOrPut(endpoint) { mutableListOf() }
        list.addAll(data)
    }

    @JvmName("loadFormattedMockDataEndpointAny")
    private fun loadFormattedMockData(endpoint: String, data: String) {
        val list = mockDataStorage.getOrPut(endpoint) { mutableListOf() }
        list.add(data)
    }

    fun getMockData(endpoint: String): String? {
        val value = mockDataStorage.getOrDefault(endpoint, mutableListOf()).getOrNull(mockDataCounter.getOrDefault(endpoint, 0))
        mockDataCounter[endpoint] = mockDataCounter.getOrDefault(endpoint, 0) + 1
        return value
    }

    fun clearMockData() {
        mockDataStorage.clear()
        mockDataCounter.clear()
    }

}