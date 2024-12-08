package com.example.whatsub.data.api

import com.example.whatsub.data.api.model.PathData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("combined-path")
    fun getShortestPath(
        @Query("startStation") startStation: Int,
        @Query("endStation") endStation: Int
    ): Call<PathData>
}
