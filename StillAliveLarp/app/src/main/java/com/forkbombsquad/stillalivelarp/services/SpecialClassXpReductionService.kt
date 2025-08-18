package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.XpReductionListModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetXpReductionsForCharacterRequest {
    @HTTP(method ="GET", path = "xp-red/all_for_char/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<XpReductionListModel>
}

interface GetAllXpReductionsRequest {
    @HTTP(method ="GET", path = "xp-red/all/")
    suspend fun makeRequest(): Response<XpReductionListModel>
}

interface DeleteXpReductionsForCharacterRequest {
    @HTTP(method ="DELETE", path = "xp-red/delete/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<XpReductionListModel>
}

class SpecialClassXpReductionService {
    class GetXpReductionsForCharacter:
        UAndPServiceInterface<GetXpReductionsForCharacterRequest, XpReductionListModel, IdSP> {
        override val request: GetXpReductionsForCharacterRequest
            get() = retrofit.create(GetXpReductionsForCharacterRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<XpReductionListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllXpReductions:
        UAndPServiceInterface<GetAllXpReductionsRequest, XpReductionListModel, ServicePayload> {
        override val request: GetAllXpReductionsRequest
            get() = retrofit.create(GetAllXpReductionsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<XpReductionListModel> {
            return request.makeRequest()
        }
    }

    class DeleteXpReductionsForCharacter:
        UAndPServiceInterface<DeleteXpReductionsForCharacterRequest, XpReductionListModel, IdSP> {
        override val request: DeleteXpReductionsForCharacterRequest
            get() = retrofit.create(DeleteXpReductionsForCharacterRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<XpReductionListModel> {
            return request.makeRequest(payload.id())
        }
    }
}