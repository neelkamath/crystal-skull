package com.neelkamath.crystalskull.intTest

import com.neelkamath.crystalskull.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object Server {
    private val service = Retrofit.Builder()
        .baseUrl(System.getenv("CRYSTAL_SKULL_URL"))
        .client(OkHttpClient.Builder().readTimeout(10, TimeUnit.MINUTES).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(QuizService::class.java)

    private interface QuizService {
        @GET("search")
        fun search(@Query("query") topic: String): Call<SearchResponse>

        @GET("search_trending")
        fun searchTrending(): Call<SearchResponse>

        @GET("search_trending")
        fun searchTrending(@Query("max") max: Int): Call<SearchResponse>

        @POST("quiz")
        fun quiz(@Body request: QuizRequest): Call<QuizResponse>

        @GET("health_check")
        fun checkHealth(): Call<HealthCheck>
    }

    fun requestSearch(query: String): Response<SearchResponse> = service.search(query).execute()

    fun requestTrendingSearch(): Response<SearchResponse> = service.searchTrending().execute()

    fun searchTrending(max: Int): List<Topic> = service.searchTrending(max).execute().body()!!.topics

    fun quiz(request: QuizRequest): QuizResponse = requestQuiz(request).body()!!

    fun requestQuiz(request: QuizRequest): Response<QuizResponse> = service.quiz(request).execute()

    fun requestHealthCheck(): Response<HealthCheck> = service.checkHealth().execute()
}