package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel
import com.forkbombsquad.stillalivelarp.services.utils.ContactCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP

interface CreateContactRequestRequest {
    @HTTP(method ="POST", path = "contact/create/", hasBody = true)
    suspend fun makeRequest(@Body characterSkill: RequestBody): Response<ContactRequestModel>
}

class ContactRequestService {
    class CreateContactRequest:
        UAndPServiceInterface<CreateContactRequestRequest, ContactRequestModel, ContactCreateSP> {
        override val request: CreateContactRequestRequest
            get() = retrofit.create(CreateContactRequestRequest::class.java)

        override suspend fun getResponse(payload: ContactCreateSP): Response<ContactRequestModel> {
            return request.makeRequest(payload.contact())
        }
    }
}