package com.example.whatsub.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://whatsub-env-2.eba-wjpixdy5.ap-northeast-2.elasticbeanstalk.com/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱을 위한 Gson 컨버터
            .build()
        retrofit.create(ApiService::class.java)
    }
}
