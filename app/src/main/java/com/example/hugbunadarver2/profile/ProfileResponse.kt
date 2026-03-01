package com.example.hugbunadarver2.profile

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val email: String? = null,
    val username: String? = null,
    val profilePictureUrl: String? = null
) {
    val profilePictureBase64: String?
        get() = profilePictureUrl?.takeIf { it.isNotEmpty() }?.split(",")?.getOrNull(1)
}

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)


