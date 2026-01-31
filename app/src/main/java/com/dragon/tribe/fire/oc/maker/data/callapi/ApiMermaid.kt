package com.dragon.tribe.fire.oc.maker.data.callapi

import com.dragon.tribe.fire.oc.maker.data.model.CharacterResponse
import retrofit2.http.GET

interface ApiMermaid {
    @GET("api/ST170_WingsOfFire")
    suspend fun getAllData(): CharacterResponse
}