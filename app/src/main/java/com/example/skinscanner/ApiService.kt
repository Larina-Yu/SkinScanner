package com.example.skinscanner

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.Call

interface ApiService {
    @Multipart
    @POST("upload_image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody,
        @Part("lesion_type") lesionType: RequestBody
    ): retrofit2.Response<Any>

    @GET("get_images/{user_id}")
    fun getImages(
        @Path("user_id") userId: String
    ): retrofit2.Call<List<ImageData>>

}

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder().addInterceptor(logging).build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.4:5000/") // PC IP
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


