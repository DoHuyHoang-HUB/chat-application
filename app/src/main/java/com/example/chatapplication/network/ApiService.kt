package com.example.chatapplication.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

private const val BASE_URL = "https://fcm.googleapis.com/fcm/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface ApiService {

    @POST("send")
    fun sendMessage(@HeaderMap headers: HashMap<String, String>, @Body messageBody: String): Call<String>

}

object Api {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}

