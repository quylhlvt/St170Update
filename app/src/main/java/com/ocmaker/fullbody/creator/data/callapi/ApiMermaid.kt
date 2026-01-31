package com.ocmaker.fullbody.creator.data.callapi

import com.ocmaker.fullbody.creator.data.model.CharacterResponse
import retrofit2.http.GET

interface ApiMermaid {
    @GET("api/ST203_OCMakerFullBody")
    suspend fun getAllData(): CharacterResponse
}