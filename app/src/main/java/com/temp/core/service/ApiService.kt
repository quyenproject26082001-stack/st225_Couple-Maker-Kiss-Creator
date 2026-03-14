package com.temp.core.service
import com.temp.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/app/ST230_DressUpOcMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}