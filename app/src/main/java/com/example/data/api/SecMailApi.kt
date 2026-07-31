package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SecMailApi {

    @GET("api/v1/?action=genRandomMailbox")
    suspend fun genRandomMailbox(
        @Query("count") count: Int = 1
    ): Response<List<String>>

    @GET("api/v1/?action=getDomainList")
    suspend fun getDomainList(): Response<List<String>>

    @GET("api/v1/?action=getMessages")
    suspend fun getMessages(
        @Query("login") login: String,
        @Query("domain") domain: String
    ): Response<List<SecMailSummaryDto>>

    @GET("api/v1/?action=readMessage")
    suspend fun readMessage(
        @Query("login") login: String,
        @Query("domain") domain: String,
        @Query("id") id: Long
    ): Response<SecMailDetailDto>
}
