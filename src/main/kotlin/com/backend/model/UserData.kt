package com.backend.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val userId: String = "",
    val username: String = "",
    val chipAmount: Int = 1000,
    val avatarUrl: String? = null
)
