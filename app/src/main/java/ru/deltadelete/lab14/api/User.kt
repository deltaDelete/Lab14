package ru.deltadelete.lab14.api

import java.util.Date
import java.util.UUID

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
) {
    val avatarUrl: String
        get() = "https://netial.deltadelete.keenetic.link/images/users/$id"
}