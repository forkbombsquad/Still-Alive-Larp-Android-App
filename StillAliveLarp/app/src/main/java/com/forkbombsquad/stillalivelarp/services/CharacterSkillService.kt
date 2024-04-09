package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.AwardListModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillListModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
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

interface TakeCharacterSkillRequest {
    @HTTP(method ="POST", path = "char-skill/create_with_player_id/{playerId}", hasBody = true)
    suspend fun makeRequest(@Path("playerId") playerId: Int, @Body characterSkill: RequestBody): Response<PlayerModel>
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

    class TakeCharacterSkill: UAndPServiceInterface<TakeCharacterSkillRequest, PlayerModel, CharacterSkillCreateSP> {
        override val request: TakeCharacterSkillRequest
            get() = retrofit.create(TakeCharacterSkillRequest::class.java)

        override suspend fun getResponse(payload: CharacterSkillCreateSP): Response<PlayerModel> {
            return request.makeRequest(payload.playerId(), payload.charSkill())
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