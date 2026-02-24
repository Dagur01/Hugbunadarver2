package com.example.hugbunadarver2.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val email: String,
    val username: String,
    val profilePictureUrl: String? = null
) {
    val profilePictureBase64: String?
        get() = profilePictureUrl?.split(",")?.getOrNull(1)
}
