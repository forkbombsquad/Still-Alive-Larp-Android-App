package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillListModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAllCharacterSkillsForCharacterRequest {
    @HTTP(method ="GET", path = "char-skill/all_for_char/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<CharacterSkillListModel>
}

interface GetAllCharacterSkillsRequest {
    @HTTP(method ="GET", path = "char-skill/all/")
    suspend fun makeRequest(): Response<CharacterSkillListModel>
}

interface TakeCharacterSkillRequest {
    @HTTP(method ="POST", path = "char-skill/create_with_player_id/{playerId}", hasBody = true)
    suspend fun makeRequest(@Path("playerId") playerId: Int, @Body characterSkill: RequestBody): Response<PlayerModel>
}

interface TakePlannedCharacterSkillRequest {
    @HTTP(method ="POST", path = "char-skill/create_with_plan/", hasBody = true)
    suspend fun makeRequest(@Body characterSkill: RequestBody): Response<CharacterSkillModel>
}

interface DeleteAllCharacterSkillRequest {
    @HTTP(method ="DELETE", path = "char-skill/delete/{charId}", hasBody = false)
    suspend fun makeRequest(@Path("charId") characterId: Int): Response<CharacterSkillListModel>
}

class CharacterSkillService {
    class GetAllCharacterSkillsForCharacter:
        UAndPServiceInterface<GetAllCharacterSkillsForCharacterRequest, CharacterSkillListModel, IdSP> {
        override val request: GetAllCharacterSkillsForCharacterRequest
            get() = retrofit.create(GetAllCharacterSkillsForCharacterRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<CharacterSkillListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllCharacterSkills:
        UAndPServiceInterface<GetAllCharacterSkillsRequest, CharacterSkillListModel, ServicePayload> {
        override val request: GetAllCharacterSkillsRequest
            get() = retrofit.create(GetAllCharacterSkillsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CharacterSkillListModel> {
            return request.makeRequest()
        }
    }

    class TakeCharacterSkill: UAndPServiceInterface<TakeCharacterSkillRequest, PlayerModel, CharacterSkillCreateSP> {
        override val request: TakeCharacterSkillRequest
            get() = retrofit.create(TakeCharacterSkillRequest::class.java)

        override suspend fun getResponse(payload: CharacterSkillCreateSP): Response<PlayerModel> {
            return request.makeRequest(payload.playerId(), payload.charSkill())
        }
    }

    class TakePlannedCharacterSkill: UAndPServiceInterface<TakePlannedCharacterSkillRequest, CharacterSkillModel, CreateModelSP> {
        override val request: TakePlannedCharacterSkillRequest
            get() = retrofit.create(TakePlannedCharacterSkillRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<CharacterSkillModel> {
            return request.makeRequest(payload.model())
        }
    }

    class DeleteCharacterSkills: UAndPServiceInterface<DeleteAllCharacterSkillRequest, CharacterSkillListModel, IdSP> {
        override val request: DeleteAllCharacterSkillRequest
            get() = retrofit.create(DeleteAllCharacterSkillRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<CharacterSkillListModel> {
            return request.makeRequest(payload.id())
        }
    }
}