package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillListModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterSkillCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetIntrigueForEventRequest {
    @HTTP(method ="GET", path = "intrigue/for_event/{eventId}")
    suspend fun makeRequest(@Path("eventId") eventId: Int): Response<IntrigueModel>
}

class IntrigueService {
    class GetIntrigueForEvent:
        UAndPServiceInterface<GetIntrigueForEventRequest, IntrigueModel, IdSP> {
        override val request: GetIntrigueForEventRequest
            get() = retrofit.create(GetIntrigueForEventRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<IntrigueModel> {
            return request.makeRequest(payload.id())
        }
    }
}