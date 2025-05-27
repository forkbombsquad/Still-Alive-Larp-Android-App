package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.CharacterListFullModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterListModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSP
import com.forkbombsquad.stillalivelarp.services.utils.CharactersForTypeWithIdSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import com.forkbombsquad.stillalivelarp.utils.Constants
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.Query

interface GetCharacterRequest {
    @HTTP(method ="GET", path = "characters/{characterId}")
    suspend fun makeRequest(@Path("characterId") characterId: Int): Response<CharacterModel>
}

interface GetAllPlayerCharactersRequest {
    @HTTP(method ="GET", path = "characters/all_with_player_id/")
    suspend fun makeRequest(@Query("player_id_in") playerId: Int): Response<CharacterListModel>
}

interface GetAllPlayerCharactersForCharacterTypeRequest {
    @HTTP(method ="GET", path = "characters/all_with_type_for_player_id/{characterTypeId}")
    suspend fun makeRequest(@Path("characterTypeId") characterTypeId: Int, @Query("player_id_in") playerId: Int): Response<CharacterListFullModel>
}

interface GetAllCharactersRequest {
    @HTTP(method ="GET", path = "characters/all_with_type/${Constants.CharacterTypeId.standard}")
    suspend fun makeRequest(): Response<CharacterListFullModel>
}

interface GetAllFullCharactersRequest {
    @HTTP(method ="GET", path = "characters/all/")
    suspend fun makeRequest(): Response<CharacterListFullModel>
}

interface GetAllNPCCharactersRequest {
    @HTTP(method ="GET", path = "characters/all_with_type/${Constants.CharacterTypeId.NPC}")
    suspend fun makeRequest(): Response<CharacterListFullModel>
}

interface CreateCharacterRequest {
    @HTTP(method ="POST", path = "characters/create/", hasBody = true)
    suspend fun makeRequest(@Body character: RequestBody): Response<CharacterModel>
}

interface CreatePlannedCharacterRequest {
    @HTTP(method ="POST", path = "characters/createplan/", hasBody = true)
    suspend fun makeRequest(@Body character: RequestBody): Response<CharacterModel>
}

interface UpdateCharacterBioRequest {
    @HTTP(method ="PUT", path = "characters/update_bio/", hasBody = true)
    suspend fun makeRequest(@Body character: RequestBody): Response<CharacterModel>
}

interface DeleteCharactersRequest {
    @HTTP(method ="DELETE", path = "characters/delete/")
    suspend fun makeRequest(): Response<CharacterListFullModel>
}


class CharacterService {
    class GetCharacter: UAndPServiceInterface<GetCharacterRequest, CharacterModel, IdSP> {
        override val request: GetCharacterRequest
            get() = retrofit.create(GetCharacterRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<CharacterModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllPlayerCharacters: UAndPServiceInterface<GetAllPlayerCharactersRequest, CharacterListModel, IdSP> {
        override val request: GetAllPlayerCharactersRequest
            get() = retrofit.create(GetAllPlayerCharactersRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<CharacterListModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllPlayerCharactersForCharacterType: UAndPServiceInterface<GetAllPlayerCharactersForCharacterTypeRequest, CharacterListFullModel, CharactersForTypeWithIdSP> {
        override val request: GetAllPlayerCharactersForCharacterTypeRequest
            get() = retrofit.create(GetAllPlayerCharactersForCharacterTypeRequest::class.java)

        override suspend fun getResponse(payload: CharactersForTypeWithIdSP): Response<CharacterListFullModel> {
            return request.makeRequest(payload.characterTypeId(), payload.playerId())
        }
    }

    class GetAllCharacters: UAndPServiceInterface<GetAllCharactersRequest, CharacterListFullModel, ServicePayload> {
        override val request: GetAllCharactersRequest
            get() = retrofit.create(GetAllCharactersRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CharacterListFullModel> {
            return request.makeRequest()
        }
    }

    class GetAllFullCharacters: UAndPServiceInterface<GetAllFullCharactersRequest, CharacterListFullModel, ServicePayload> {
        override val request: GetAllFullCharactersRequest
            get() = retrofit.create(GetAllFullCharactersRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CharacterListFullModel> {
            return request.makeRequest()
        }
    }

    class GetAllNPCCharacters: UAndPServiceInterface<GetAllNPCCharactersRequest, CharacterListFullModel, ServicePayload> {
        override val request: GetAllNPCCharactersRequest
            get() = retrofit.create(GetAllNPCCharactersRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CharacterListFullModel> {
            return request.makeRequest()
        }
    }

    class CreateCharacter: UAndPServiceInterface<CreateCharacterRequest, CharacterModel, CharacterCreateSP> {
        override val request: CreateCharacterRequest
            get() = retrofit.create(CreateCharacterRequest::class.java)

        override suspend fun getResponse(payload: CharacterCreateSP): Response<CharacterModel> {
            return request.makeRequest(payload.character())
        }
    }

    class CreatePlannedCharacter: UAndPServiceInterface<CreatePlannedCharacterRequest, CharacterModel, CharacterCreateSP> {
        override val request: CreatePlannedCharacterRequest
            get() = retrofit.create(CreatePlannedCharacterRequest::class.java)

        override suspend fun getResponse(payload: CharacterCreateSP): Response<CharacterModel> {
            return request.makeRequest(payload.character())
        }
    }

    class UpdateCharacterBio: UAndPServiceInterface<UpdateCharacterBioRequest, CharacterModel, CharacterSP> {
        override val request: UpdateCharacterBioRequest
            get() = retrofit.create(UpdateCharacterBioRequest::class.java)

        override suspend fun getResponse(payload: CharacterSP): Response<CharacterModel> {
            return request.makeRequest(payload.character())
        }
    }

    class DeleteCharacters: UAndPServiceInterface<DeleteCharactersRequest, CharacterListFullModel, ServicePayload> {
        override val request: DeleteCharactersRequest
            get() = retrofit.create(DeleteCharactersRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CharacterListFullModel> {
            return request.makeRequest()
        }
    }
}