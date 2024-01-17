package ru.deltadelete.lab14.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.Date
import java.util.UUID

interface LoginService {
    @POST("auth/login")
    @Headers("Content-Type: application/json")
    suspend fun loginSuspend(@Body loginBody: LoginBody): Response<User>

    @POST("auth/login")
    @Headers("Content-Type: application/json")
    fun login(@Body loginBody: LoginBody): Call<User>

    @POST("auth/register")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body registerBody: RegisterBody): Response<User>
}

data class LoginBody(
    val email: String,
    val password: String
)

data class RegisterBody(
    val lastName: String,
    val firstName: String,
    val birthDate: Date,
    val email: String,
    val password: String,
    val passwordConfirm: String
)

data class User(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val birthDate: Date,
    val creationDate: Date,
    val rating: Int,
    val email: String,
    val emailNormalized: String,
    val isEmailConfirmed: Boolean
)

