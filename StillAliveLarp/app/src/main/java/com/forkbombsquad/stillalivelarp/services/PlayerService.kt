package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.PlayerListModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.PlayerCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.UpdatePSP
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Path

interface SignInPlayerRequest {
    @HTTP(method ="GET", path = "players/sign_in/")
    suspend fun makeRequest(): Response<PlayerModel>
}

interface GetPlayerRequest {
    @HTTP(method ="GET", path = "players/{playerId}")
    suspend fun makeRequest(@Path("playerId") playerId: Int): Response<PlayerModel>
}

interface GetAllPlayersRequest {
    @HTTP(method ="GET", path = "players/all/")
    suspend fun makeRequest(): Response<PlayerListModel>
}

interface CreatePlayerRequest {
    @HTTP(method ="POST", path = "players/create", hasBody = true)
    suspend fun makeRequest(@Header("preapprovalcode") preapprovalcode: String, @Body player: RequestBody): Response<PlayerModel>
}

interface UpdatePRequest {
    @HTTP(method ="PUT", path = "players/update_p/{playerId}", hasBody = true)
    suspend fun makeRequest(@Path("playerId") playerId: Int, @Header("p") newP: String): Response<PlayerModel>
}

interface DeletePlayerRequest {
    @HTTP(method ="DELETE", path = "players/delete/")
    suspend fun makeRequest(): Response<PlayerModel>
}

class PlayerService {
    class SignInPlayer: UAndPServiceInterface<SignInPlayerRequest, PlayerModel, ServicePayload> {
        override val request: SignInPlayerRequest
            get() = retrofit.create(SignInPlayerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<PlayerModel> {
            return request.makeRequest()
        }
    }

    class GetPlayer: UAndPServiceInterface<GetPlayerRequest, PlayerModel, IdSP> {
        override val request: GetPlayerRequest
            get() = retrofit.create(GetPlayerRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<PlayerModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllPlayers: UAndPServiceInterface<GetAllPlayersRequest, PlayerListModel, ServicePayload> {
        override val request: GetAllPlayersRequest
            get() = retrofit.create(GetAllPlayersRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<PlayerListModel> {
            return request.makeRequest()
        }
    }

    class CreatePlayer: UAndPServiceInterface<CreatePlayerRequest, PlayerModel, PlayerCreateSP> {
        override val request: CreatePlayerRequest
            get() = retrofit.create(CreatePlayerRequest::class.java)

        override suspend fun getResponse(payload: PlayerCreateSP): Response<PlayerModel> {
            return request.makeRequest(payload.preApprovalCode(), payload.player())
        }
    }

    class UpdateP: UAndPServiceInterface<UpdatePRequest, PlayerModel, UpdatePSP> {
        override val request: UpdatePRequest
            get() = retrofit.create(UpdatePRequest::class.java)

        override suspend fun getResponse(payload: UpdatePSP): Response<PlayerModel> {
            return request.makeRequest(payload.playerId(), payload.p())
        }
    }

    class DeletePlayer: UAndPServiceInterface<DeletePlayerRequest, PlayerModel, ServicePayload> {
        override val request: DeletePlayerRequest
            get() = retrofit.create(DeletePlayerRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<PlayerModel> {
            return request.makeRequest()
        }
    }
}