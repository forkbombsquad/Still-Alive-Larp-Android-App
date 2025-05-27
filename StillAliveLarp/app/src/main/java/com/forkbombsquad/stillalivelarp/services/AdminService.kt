package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterSkillListModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestListModel
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.models.EventAttendeeModel
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueListModel
import com.forkbombsquad.stillalivelarp.services.models.IntrigueModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCheckInSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.GiveCharacterCheckInRewardsSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.RefundSkillSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.TakeClassSP
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdatePSP
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface AwardPlayerRequest {
    @HTTP(method ="POST", path = "admin/award_player/", hasBody = true)
    suspend fun makeRequest(@Body award: RequestBody): Response<PlayerModel>
}

interface AwardCharacterRequest {
    @HTTP(method ="POST", path = "admin/award_character/", hasBody = true)
    suspend fun makeRequest(@Body award: RequestBody): Response<CharacterModel>
}

interface CreateAnnouncementRequest {
    @HTTP(method ="POST", path = "announcements/create/", hasBody = true)
    suspend fun makeRequest(@Body announcement: RequestBody): Response<AnnouncementModel>
}

interface CreateEventRequest {
    @HTTP(method ="POST", path = "event/create/", hasBody = true)
    suspend fun makeRequest(@Body event: RequestBody): Response<EventModel>
}

interface UpdateEventRequest {
    @HTTP(method ="PUT", path = "event/update/", hasBody = true)
    suspend fun makeRequest(@Body event: RequestBody): Response<EventModel>
}

interface CheckInPlayerRequest {
    @HTTP(method ="POST", path = "event-attendee/create/", hasBody = true)
    suspend fun makeRequest(@Body attendee: RequestBody): Response<EventAttendeeModel>
}

interface GiveCharacterCheckInRewardsRequest {
    @HTTP(method ="PUT", path = "event-attendee/give_character_check_in_rewards/{eventId}", hasBody = true)
    suspend fun makeRequest(@Path("eventId") eventId: Int, @Query("player_id") playerId: Int, @Query("character_id") charId: Int, @Query("new_bullet_count") newBulletCount: Int): Response<CharacterModel>
}

interface CheckInCharacterRequest {
    @HTTP(method ="PUT", path = "event-attendee/check_in_character/{eventId}", hasBody = true)
    suspend fun makeRequest(@Path("eventId") eventId: Int, @Query("player_id") playerId: Int, @Query("character_id") charId: Int): Response<EventAttendeeModel>
}

interface UpdateCharacterRequest {
    @HTTP(method ="PUT", path = "characters/update/", hasBody = true)
    suspend fun makeRequest(@Body character: RequestBody): Response<CharacterModel>
}

interface UpdateContactRequestRequest {
    @HTTP(method = "PUT", path = "contact/update/", hasBody = true)
    suspend fun makeRequest(@Body characterSkill: RequestBody): Response<ContactRequestModel>
}

interface UpdatePAdminRequest {
    @HTTP(method ="PUT", path = "players/update_p_admin/{playerId}", hasBody = true)
    suspend fun makeRequest(@Path("playerId") playerId: Int, @Header("p") newP: String): Response<PlayerModel>
}

interface CreateIntrigueRequest {
    @HTTP(method ="POST", path = "intrigue/create/", hasBody = true)
    suspend fun makeRequest(@Body intrigue: RequestBody): Response<IntrigueModel>
}

interface UpdateIntrigueRequest {
    @HTTP(method ="PUT", path = "intrigue/update/", hasBody = true)
    suspend fun makeRequest(@Body intrigue: RequestBody): Response<IntrigueModel>
}

interface UpdatePlayerRequest {
    @HTTP(method ="PUT", path = "players/update/", hasBody = true)
    suspend fun makeRequest(@Body player: RequestBody): Response<PlayerModel>
}

interface UpdateEventAttendeeRequest {
    @HTTP(method ="PUT", path = "event-attendee/update/", hasBody = true)
    suspend fun makeRequest(@Body player: RequestBody): Response<EventAttendeeModel>
}

interface GiveXpReductionRequest {
    @HTTP(method ="POST", path = "xp-red/take_class/{characterId}", hasBody = true)
    suspend fun makeRequest(@Path("characterId") characterId: Int, @Query("skill_id") skillId: Int): Response<XpReductionModel>
}

interface CreateGearRequest {
    @HTTP(method ="POST", path = "gear/create/", hasBody = true)
    suspend fun makeRequest(@Body gear: RequestBody): Response<GearModel>
}

interface UpdateGearRequest {
    @HTTP(method ="PUT", path = "gear/update/", hasBody = true)
    suspend fun makeRequest(@Body gear: RequestBody): Response<GearModel>
}

interface CreateFeatureFlagRequest {
    @HTTP(method ="POST", path = "feature-flag/create/", hasBody = true)
    suspend fun makeRequest(@Body flag: RequestBody): Response<FeatureFlagModel>
}

interface UpdateFeatureFlagRequest {
    @HTTP(method = "PUT", path = "feature-flag/update/", hasBody = true)
    suspend fun makeRequest(@Body flag: RequestBody): Response<FeatureFlagModel>
}

interface DeleteFeatureFlagRequest {
    @HTTP(method ="DELETE", path = "feature-flag/delete/{flagId}")
    suspend fun makeRequest(@Path("flagId") flagId: Int): Response<FeatureFlagModel>
}

interface DeleteCharacterSkillRequest {
    @HTTP(method ="DELETE", path = "char-skill/delete-skill/{playerId}/{charId}/{skillId}", hasBody = false)
    suspend fun makeRequest(@Path("playerId") playerId: Int, @Path("charId") characterId: Int, @Path("skillId") skillId: Int): Response<CharacterSkillListModel>
}

interface CreateResearchProjectRequest {
    @HTTP(method ="POST", path = "research-project/create/", hasBody = true)
    suspend fun makeRequest(@Body researchProject: RequestBody): Response<ResearchProjectModel>
}

interface UpdateResearchProjectRequest {
    @HTTP(method = "PUT", path = "research-project/update/", hasBody = true)
    suspend fun makeRequest(@Body researchProject: RequestBody): Response<ResearchProjectModel>
}


class AdminService {
    class AwardPlayer:
        UAndPServiceInterface<AwardPlayerRequest, PlayerModel, AwardCreateSP> {
        override val request: AwardPlayerRequest
            get() = retrofit.create(AwardPlayerRequest::class.java)

        override suspend fun getResponse(payload: AwardCreateSP): Response<PlayerModel> {
            return request.makeRequest(payload.award())
        }
    }

    class AwardCharacter:
        UAndPServiceInterface<AwardCharacterRequest, CharacterModel, AwardCreateSP> {
        override val request: AwardCharacterRequest
            get() = retrofit.create(AwardCharacterRequest::class.java)

        override suspend fun getResponse(payload: AwardCreateSP): Response<CharacterModel> {
            return request.makeRequest(payload.award())
        }
    }

    class CreateAnnouncement:
        UAndPServiceInterface<CreateAnnouncementRequest, AnnouncementModel, CreateModelSP> {
        override val request: CreateAnnouncementRequest
            get() = retrofit.create(CreateAnnouncementRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<AnnouncementModel> {
            return request.makeRequest(payload.model())
        }
    }

    class CreateEvent:
        UAndPServiceInterface<CreateEventRequest, EventModel, CreateModelSP> {
        override val request: CreateEventRequest
            get() = retrofit.create(CreateEventRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<EventModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateEvent:
        UAndPServiceInterface<UpdateEventRequest, EventModel, UpdateModelSP> {
        override val request: UpdateEventRequest
            get() = retrofit.create(UpdateEventRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<EventModel> {
            return request.makeRequest(payload.model())
        }
    }

    class CheckInPlayer:
        UAndPServiceInterface<CheckInPlayerRequest, EventAttendeeModel, CreateModelSP> {
        override val request: CheckInPlayerRequest
            get() = retrofit.create(CheckInPlayerRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<EventAttendeeModel> {
            return request.makeRequest(payload.model())
        }
    }

    class GiveCharacterCheckInRewards:
        UAndPServiceInterface<GiveCharacterCheckInRewardsRequest, CharacterModel, GiveCharacterCheckInRewardsSP> {
        override val request: GiveCharacterCheckInRewardsRequest
            get() = retrofit.create(GiveCharacterCheckInRewardsRequest::class.java)

        override suspend fun getResponse(payload: GiveCharacterCheckInRewardsSP): Response<CharacterModel> {
            return request.makeRequest(payload.eventId(), payload.playerId(), payload.characterId(), payload.newBulletCount())
        }
    }

    class CheckInCharacter:
        UAndPServiceInterface<CheckInCharacterRequest, EventAttendeeModel, CharacterCheckInSP> {
        override val request: CheckInCharacterRequest
            get() = retrofit.create(CheckInCharacterRequest::class.java)

        override suspend fun getResponse(payload: CharacterCheckInSP): Response<EventAttendeeModel> {
            return request.makeRequest(payload.eventId(), payload.playerId(), payload.characterId())
        }
    }

    class UpdateCharacter:
        UAndPServiceInterface<UpdateCharacterRequest, CharacterModel, UpdateModelSP> {
        override val request: UpdateCharacterRequest
            get() = retrofit.create(UpdateCharacterRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<CharacterModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateContactRequest:
        UAndPServiceInterface<UpdateContactRequestRequest, ContactRequestModel, UpdateModelSP> {
        override val request: UpdateContactRequestRequest
            get() = retrofit.create(UpdateContactRequestRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<ContactRequestModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdatePAdmin: UAndPServiceInterface<UpdatePAdminRequest, PlayerModel, UpdatePSP> {
        override val request: UpdatePAdminRequest
            get() = retrofit.create(UpdatePAdminRequest::class.java)

        override suspend fun getResponse(payload: UpdatePSP): Response<PlayerModel> {
            return request.makeRequest(payload.playerId(), payload.p())
        }
    }

    class CreateIntrigue:
        UAndPServiceInterface<CreateIntrigueRequest, IntrigueModel, CreateModelSP> {
        override val request: CreateIntrigueRequest
            get() = retrofit.create(CreateIntrigueRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<IntrigueModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateIntrigue:
        UAndPServiceInterface<UpdateIntrigueRequest, IntrigueModel, UpdateModelSP> {
        override val request: UpdateIntrigueRequest
            get() = retrofit.create(UpdateIntrigueRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<IntrigueModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdatePlayer:
        UAndPServiceInterface<UpdatePlayerRequest, PlayerModel, UpdateModelSP> {
        override val request: UpdatePlayerRequest
            get() = retrofit.create(UpdatePlayerRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<PlayerModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateEventAttendee:
        UAndPServiceInterface<UpdateEventAttendeeRequest, EventAttendeeModel, UpdateModelSP> {
        override val request: UpdateEventAttendeeRequest
            get() = retrofit.create(UpdateEventAttendeeRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<EventAttendeeModel> {
            return request.makeRequest(payload.model())
        }
    }

    class GiveXpReduction:
        UAndPServiceInterface<GiveXpReductionRequest, XpReductionModel, TakeClassSP> {
        override val request: GiveXpReductionRequest
            get() = retrofit.create(GiveXpReductionRequest::class.java)

        override suspend fun getResponse(payload: TakeClassSP): Response<XpReductionModel> {
            return request.makeRequest(payload.characterId(), payload.skillId())
        }
    }

    class CreateGear:
        UAndPServiceInterface<CreateGearRequest, GearModel, CreateModelSP> {
        override val request: CreateGearRequest
            get() = retrofit.create(CreateGearRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<GearModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateGear:
        UAndPServiceInterface<UpdateGearRequest, GearModel, UpdateModelSP> {
        override val request: UpdateGearRequest
            get() = retrofit.create(UpdateGearRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<GearModel> {
            return request.makeRequest(payload.model())
        }
    }

    class CreateFeatureFlag:
        UAndPServiceInterface<CreateFeatureFlagRequest, FeatureFlagModel, CreateModelSP> {
        override val request: CreateFeatureFlagRequest
            get() = retrofit.create(CreateFeatureFlagRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<FeatureFlagModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateFeatureFlag:
        UAndPServiceInterface<UpdateFeatureFlagRequest, FeatureFlagModel, UpdateModelSP> {
        override val request: UpdateFeatureFlagRequest
            get() = retrofit.create(UpdateFeatureFlagRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<FeatureFlagModel> {
            return request.makeRequest(payload.model())
        }
    }

    class DeleteFeatureFlag: UAndPServiceInterface<DeleteFeatureFlagRequest, FeatureFlagModel, IdSP> {
        override val request: DeleteFeatureFlagRequest
            get() = retrofit.create(DeleteFeatureFlagRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<FeatureFlagModel> {
            return request.makeRequest(payload.id())
        }
    }

    class DeleteCharacterSkill: UAndPServiceInterface<DeleteCharacterSkillRequest, CharacterSkillListModel, RefundSkillSP> {
        override val request: DeleteCharacterSkillRequest
            get() = retrofit.create(DeleteCharacterSkillRequest::class.java)

        override suspend fun getResponse(payload: RefundSkillSP): Response<CharacterSkillListModel> {
            return request.makeRequest(payload.playerId(), payload.characterId(), payload.skillId())
        }
    }

    class CreateResearchProject:
        UAndPServiceInterface<CreateResearchProjectRequest, ResearchProjectModel, CreateModelSP> {
        override val request: CreateResearchProjectRequest
            get() = retrofit.create(CreateResearchProjectRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<ResearchProjectModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateResearchProject:
        UAndPServiceInterface<UpdateResearchProjectRequest, ResearchProjectModel, UpdateModelSP> {
        override val request: UpdateResearchProjectRequest
            get() = retrofit.create(UpdateResearchProjectRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<ResearchProjectModel> {
            return request.makeRequest(payload.model())
        }
    }

}