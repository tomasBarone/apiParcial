package com.example.apiparcial.service
import com.example.apiparcial.model.ApiResponse
import com.example.apiparcial.model.Personaje
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface RickMortyApiservice {


    // method to search for a character by name
    @GET("character/")
    fun getCharacter(@Query("name") characterName: String): Call<ApiResponse>

    // function to search for a character by id
    @GET("character/{id}")
    fun getCharacterById(@Path("id") characterId: String): Call<Personaje>

}