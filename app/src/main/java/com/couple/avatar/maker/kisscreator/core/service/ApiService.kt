package com.couple.avatar.maker.kisscreator.core.service
import com.couple.avatar.maker.kisscreator.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/app/ST230_DressUpOcMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}