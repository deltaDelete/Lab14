package ru.deltadelete.lab14.api

import retrofit2.create

object Common {
    private const val BASE_URL = "https://netial.deltadelete.keenetic.link/api/"
    val loginService: LoginService
        get() = RetrofitClient.getClient(BASE_URL).create<LoginService>()
}