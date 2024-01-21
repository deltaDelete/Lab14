package ru.deltadelete.lab14.api

import java.util.Date

data class RegisterBody(
    val lastName: String,
    val firstName: String,
    val birthDate: Date,
    val email: String,
    val password: String,
    val passwordConfirm: String
)