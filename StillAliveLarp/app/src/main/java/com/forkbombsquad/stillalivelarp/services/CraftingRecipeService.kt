package com.forkbombsquad.stillalivelarp.services

import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeListModel
import com.forkbombsquad.stillalivelarp.services.models.CraftingRecipeModel
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.ServicePayload
import com.forkbombsquad.stillalivelarp.services.utils.UAndPServiceInterface
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Path

interface GetCraftingRecipeRequest {
    @HTTP(method = "GET", path = "crafting-recipe/{craftingRecipeId}")
    suspend fun makeRequest(@Path("craftingRecipeId") craftingRecipeId: Int): Response<CraftingRecipeModel>
}

interface GetAllCraftingRecipesRequest {
    @HTTP(method = "GET", path = "crafting-recipe/all/")
    suspend fun makeRequest(): Response<CraftingRecipeListModel>
}

class CraftingRecipeService {
    class GetAllCraftingRecipes:
        UAndPServiceInterface<GetAllCraftingRecipesRequest, CraftingRecipeListModel, ServicePayload> {
        override val request: GetAllCraftingRecipesRequest
            get() = retrofit.create(GetAllCraftingRecipesRequest::class.java)

        override suspend fun getResponse(payload: ServicePayload): Response<CraftingRecipeListModel> {
            return request.makeRequest()
        }
    }

    class GetCraftingRecipe:
        UAndPServiceInterface<GetCraftingRecipeRequest, CraftingRecipeModel, IdSP> {
        override val request: GetCraftingRecipeRequest
            get() = retrofit.create(GetCraftingRecipeRequest::class.java)

        override suspend fun getResponse(payload: IdSP): Response<CraftingRecipeModel> {
            return request.makeRequest(payload.id())
        }
    }
}