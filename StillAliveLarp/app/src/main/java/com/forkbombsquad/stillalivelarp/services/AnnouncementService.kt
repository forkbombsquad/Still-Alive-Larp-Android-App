package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.AnnouncementFullListModel
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementListModel
import com.forkbombsquad.stillalivelarp.services.models.AnnouncementModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetAnnouncementRequest {
    @HTTP(method ="GET", path = "announcements/{announcementId}")
    suspend fun makeRequest(@Path("announcementId") announcementId: Int): Response<AnnouncementModel>
}

interface GetAllAnnouncementsRequest {
    @HTTP(method ="GET", path = "announcements/all_ids/")
    suspend fun makeRequest(): Response<AnnouncementListModel>
}

interface GetAllFullAnnouncementsRequest {
    @HTTP(method ="GET", path = "announcements/all/")
    suspend fun makeRequest(): Response<AnnouncementFullListModel>
}

class AnnouncementService {
    class GetAnnouncement: UAndPServiceInterface<GetAnnouncementRequest, AnnouncementModel, IdSP> {
        override val request: GetAnnouncementRequest
            get() = retrofit.create(GetAnnouncementRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<AnnouncementModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllAnnouncements:
        UAndPServiceInterface<GetAllAnnouncementsRequest, AnnouncementListModel, ServicePayload> {
        override val request: GetAllAnnouncementsRequest
            get() = retrofit.create(GetAllAnnouncementsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<AnnouncementListModel> {
            return request.makeRequest()
        }
    }

    class GetAllFullAnnouncements:
        UAndPServiceInterface<GetAllFullAnnouncementsRequest, AnnouncementFullListModel, ServicePayload> {
        override val request: GetAllFullAnnouncementsRequest
            get() = retrofit.create(GetAllFullAnnouncementsRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<AnnouncementFullListModel> {
            return request.makeRequest()
        }
    }

}