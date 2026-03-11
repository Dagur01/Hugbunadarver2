package com.example.hugbunadarver2.profile

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val email: String? = null,
    val username: String? = null,
    @SerializedName(value = "profilePicture", alternate = ["profilePictureUrl", "image", "picture"])
    val profilePictureUrl: String? = null
) {
    val profilePictureBase64: String?
        get() = profilePictureUrl
            ?.takeIf { it.isNotBlank() }
            ?.substringAfter("base64,", profilePictureUrl)
}

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)


