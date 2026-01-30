package com.dragon.tribe.fire.oc.maker.core.service
import com.dragon.tribe.fire.oc.maker.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST170_WingsOfFire")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}