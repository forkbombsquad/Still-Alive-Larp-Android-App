package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.AwardListModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAllAwardsForPlayerRequest {
    @HTTP(method ="GET", path = "award/all/{playerId}")
    suspend fun makeRequest(@Path("playerId") playerId: Int): Response<AwardListModel>
}

interface GetAllAwardsRequest {
    @HTTP(method ="GET", path = "award/all/")
    suspend fun makeRequest(): Response<AwardListModel>
}

interface DeleteAwardsForPlayerRequest {
    @HTTP(method ="DELETE", path = "award/delete/")
    suspend fun makeRequest(): Response<AwardListModel>
}

class AwardService {
    class GetAllAwardsForPlayer: UAndPServiceInterface<GetAllAwardsForPlayerRequest, AwardListModel, IdSP> {
        override val request: GetAllAwardsForPlayerRequest
            get() = retrofit.create(GetAllAwardsForPlayerRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<AwardListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllAwards: UAndPServiceInterface<GetAllAwardsRequest, AwardListModel, ServicePayload> {
        override val request: GetAllAwardsRequest
            get() = retrofit.create(GetAllAwardsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<AwardListModel> {
            return request.makeRequest()
        }
    }

    class DeleteAwardsForPlayer: UAndPServiceInterface<DeleteAwardsForPlayerRequest, AwardListModel, ServicePayload> {
        override val request: DeleteAwardsForPlayerRequest
            get() = retrofit.create(DeleteAwardsForPlayerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<AwardListModel> {
            return request.makeRequest()
        }
    }
}