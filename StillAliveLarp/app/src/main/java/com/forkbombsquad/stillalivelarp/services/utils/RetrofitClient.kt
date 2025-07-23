package com.forkbombsquad.stillalivelarp.services.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://stillalivelarp.com/api/v2/"

    private val okHttpClient = OkHttpClient().newBuilder().addInterceptor(RequestLogInterceptor).build()
    private val tokenHttpClient = OkHttpClient().newBuilder().addInterceptor(GetTokenInterceptor).build()
    private val authHttpClient = OkHttpClient().newBuilder().addInterceptor(AuthInterceptor).addInterceptor(RequestLogInterceptor).build()
    private val uandpNoPlayerTokenClient = OkHttpClient().newBuilder().addInterceptor(AuthInterceptor).addInterceptor(UAndPInterceptor).addInterceptor(RequestLogInterceptor).build()
    private val uandpClient = OkHttpClient().newBuilder().addInterceptor(AuthInterceptor).addInterceptor(PlayerTokenAuthInterceptor).addInterceptor(RequestLogInterceptor).build()

    fun getClient(): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

    fun getTokenClient(): Retrofit =
        Retrofit.Builder()
            .client(tokenHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()


    fun getAuthClient(): Retrofit =
        Retrofit.Builder()
            .client(authHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

    fun getUAndPClient(): Retrofit =
        Retrofit.Builder()
            .client(uandpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

    fun getUAndPNoPlayerTokenClient(): Retrofit =
        Retrofit.Builder()
            .client(uandpNoPlayerTokenClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

}