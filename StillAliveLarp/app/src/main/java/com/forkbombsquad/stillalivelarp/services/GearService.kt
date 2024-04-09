package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.GearListModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAllGearForCharacterRequest {
    @HTTP(method ="GET", path = "gear/all_for_char/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<GearListModel>
}

interface GetAllGearRequest {
    @HTTP(method ="GET", path = "gear/all/")
    suspend fun makeRequest(): Response<GearListModel>
}

interface DeleteGearRequest {
    @HTTP(method ="DELETE", path = "gear/delete/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<GearListModel>
}

class GearService {
    class GetAllGearForCharacter:
        UAndPServiceInterface<GetAllGearForCharacterRequest, GearListModel, IdSP> {
        override val request: GetAllGearForCharacterRequest
            get() = retrofit.create(GetAllGearForCharacterRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<GearListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllGear:
        UAndPServiceInterface<GetAllGearRequest, GearListModel, ServicePayload> {
        override val request: GetAllGearRequest
            get() = retrofit.create(GetAllGearRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<GearListModel> {
            return request.makeRequest()
        }
    }

    class DeleteGear:
        UAndPServiceInterface<DeleteGearRequest, GearListModel, IdSP> {
        override val request: DeleteGearRequest
            get() = retrofit.create(DeleteGearRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<GearListModel> {
            return request.makeRequest(payload.id())
        }
    }
}