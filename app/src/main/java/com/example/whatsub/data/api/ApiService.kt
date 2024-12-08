package com.example.whatsub.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 데이터 모델 클래스 정의 (PathData는 기본 예시이며, 실제 JSON 구조에 따라 수정 필요)
data class PathData(
    val totalTime: String,
    val totalCost: String,
    val segments: List<Segment>
)

data class Segment(
    val fromStation: String,
    val toStation: String,
    val lineNumber: Int,
    val timeOnLine: String,
    val costOnLine: String,
    val toiletCount: Int?,
    val storeCount: Int?
)

data class NewsData(
    val id: Int,
    val title: String,
    val content: String
)

interface ApiService {

    // 뉴스 API
    @GET("news")
    fun getNews(): Call<List<NewsData>>

    // 경로 통합 API
    @GET("combined-path")
    fun getCombinedPath(
        @Query("startStation") startStation: String,
        @Query("endStation") endStation: String
    ): Call<PathData>

    // 최단시간 API
    @GET("shortest-path")
    fun getShortestPath(
        @Query("startStation") startStation: String,
        @Query("endStation") endStation: String
    ): Call<PathData>

    // 최소요금 API
    @GET("cheapest-path")
    fun getCheapestPath(
        @Query("startStation") startStation: String,
        @Query("endStation") endStation: String
    ): Call<PathData>

    // 최소환승 API
    @GET("least-transfers-path")
    fun getLeastTransfersPath(
        @Query("startStation") startStation: String,
        @Query("endStation") endStation: String
    ): Call<PathData>

    // 즐겨찾기 API
    @GET("favorite")
    fun getFavorites(): Call<List<PathData>>
}
