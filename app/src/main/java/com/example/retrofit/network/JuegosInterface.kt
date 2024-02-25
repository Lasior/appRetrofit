package com.example.retrofit.network

import com.example.retrofit.model.Juego
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

private const val BASE_URL =
    "http://10.0.2.2:8080"

/**
 * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
 */
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object JuegoApi {
    val retrofitService: JuegosInterface by lazy {
        retrofit.create(JuegosInterface::class.java)
    }
}

interface JuegosInterface {
    @GET("/juego")
    @Headers("Accept: application/json")
    suspend fun getAllJuegos(): Response<List<Juego>>

    @GET("/juego/{titulo}")
    @Headers("Accept: application/json")
    suspend fun getJuegoByTitulo(@Path("titulo") titulo : String?): Response<List<Juego>>

    @POST("/juego")
    suspend fun postJuego(@Body juego: Juego): Response<Void>

    @DELETE("/juego/{id}")
    suspend fun deleteJuego(@Path("id") id: String): Response<Void>

    @PUT("/juego")
    suspend fun putJuego(@Body juego: Juego): Response<Void>
}