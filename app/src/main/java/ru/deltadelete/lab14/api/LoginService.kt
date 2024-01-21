package ru.deltadelete.lab14.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun loginSuspend(@Body loginBody: LoginBody): Response<User>

    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    fun login(@Body loginBody: LoginBody): Call<User>

    @POST("api/auth/register")
    @Headers("Content-Type: application/json")
    suspend fun registerSuspend(@Body registerBody: RegisterBody): Response<User>

    @POST("api/auth/register")
    @Headers("Content-Type: application/json")
    fun register(@Body registerBody: RegisterBody): Call<User>
}

