package com.example.apiparcial.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // The base URL of the API.
    private const val BASE_URL = "https://rickandmortyapi.com/api/"


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val rickMortyApiService : RickMortyApiservice by lazy{

        retrofit.create(RickMortyApiservice::class.java)
    }
}
