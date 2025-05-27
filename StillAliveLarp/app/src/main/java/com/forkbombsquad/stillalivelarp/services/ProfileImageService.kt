package com.forkbombsquad.stillalivelarp.services
import com.forkbombsquad.stillalivelarp.services.models.ProfileImageListModel
import com.forkbombsquad.stillalivelarp.services.models.ProfileImageModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetProfileImageRequest {
    @HTTP(method ="GET", path = "profile/player/{playerId}")
    suspend fun makeRequest(@Path("playerId") playerId: Int): Response<ProfileImageModel>
}

interface GetAllProfileImagesRequest {
    @HTTP(method ="GET", path = "profile/all/")
    suspend fun makeRequest(): Response<ProfileImageListModel>
}

interface CreateProfileImageRequest {
    @HTTP(method ="POST", path = "profile/create/", hasBody = true)
    suspend fun makeRequest(@Body profileImage: RequestBody): Response<ProfileImageModel>
}

interface UpdateProfileImageRequest {
    @HTTP(method ="PUT", path = "profile/update/", hasBody = true)
    suspend fun makeRequest(@Body profileImage: RequestBody): Response<ProfileImageModel>
}

interface DeleteProfileImageRequest {
    @HTTP(method ="DELETE", path = "profile/delete/{playerId}")
    suspend fun makeRequest(@Path("playerId") playerId: Int): Response<ProfileImageListModel>
}

class ProfileImageService {
    class GetProfileImage: UAndPServiceInterface<GetProfileImageRequest, ProfileImageModel, IdSP> {
        override val request: GetProfileImageRequest
            get() = retrofit.create(GetProfileImageRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<ProfileImageModel> {
            return request.makeRequest(payload.id())
        }
    }

    class GetAllProfileImages: UAndPServiceInterface<GetAllProfileImagesRequest, ProfileImageListModel, ServicePayload> {
        override val request: GetAllProfileImagesRequest
            get() = retrofit.create(GetAllProfileImagesRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<ProfileImageListModel> {
            return request.makeRequest()
        }
    }

    class CreateProfileImage: UAndPServiceInterface<CreateProfileImageRequest, ProfileImageModel, CreateModelSP> {
        override val request: CreateProfileImageRequest
            get() = retrofit.create(CreateProfileImageRequest::class.java)

        override suspend fun getResponse(payload: CreateModelSP): Response<ProfileImageModel> {
            return request.makeRequest(payload.model())
        }
    }

    class UpdateProfileImage:
        UAndPServiceInterface<UpdateProfileImageRequest, ProfileImageModel, UpdateModelSP> {
        override val request: UpdateProfileImageRequest
            get() = retrofit.create(UpdateProfileImageRequest::class.java)

        override suspend fun getResponse(payload: UpdateModelSP): Response<ProfileImageModel> {
            return request.makeRequest(payload.model())
        }
    }

    class DeleteProfileImages: UAndPServiceInterface<DeleteProfileImageRequest, ProfileImageListModel, IdSP> {
        override val request: DeleteProfileImageRequest
            get() = retrofit.create(DeleteProfileImageRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<ProfileImageListModel> {
            return request.makeRequest(payload.id())
        }
    }
}
