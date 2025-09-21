package com.example.apiparcial.model
import com.google.gson.annotations.SerializedName

// class that represents the full API response when you search by name
data class ApiResponse(
    @SerializedName("results")
    val results: List<Personaje>
)

// class that defines the structure of an individual character.
data class Personaje(

    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("species")
    val species: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("gender")
     val gender: String,
    @SerializedName("origin")
    val origin: Origin,
    @SerializedName("location")
    val location: Location
)


// class for the origin data
data class Origin(

    @SerializedName("name")
    val name: String

)

// class for the location data
data class Location(

    @SerializedName("name")
    val name: String

)