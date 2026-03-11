package com.example.hugbunadarver2.profile

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonElement
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val email: String? = null,
    val username: String? = null,
    @SerializedName(
        value = "profilePicture",
        alternate = [
            "profilePictureUrl",
            "profilepicture",
            "profile_picture",
            "profileImage",
            "profilePictureBase64",
            "profilepicturebase64",
            "profilePic",
            "image",
            "picture"
        ]
    )
    val profilePictureRaw: JsonElement? = null,
    @SerializedName(value = "profilePictureContentType", alternate = ["profilepicturecontenttype", "contentType"])
    val profilePictureContentType: String? = null
) {
    private fun extractPictureString(): String? {
        val element = profilePictureRaw ?: return null

        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            return element.asString
        }

        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val candidateKeys = listOf("data", "base64", "value", "content", "bytes")
            for (key in candidateKeys) {
                val value = obj.get(key)
                if (value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                    return value.asString
                }
            }
        }

        return null
    }

    private fun extractPictureContentType(): String? {
        val topLevel = profilePictureContentType?.takeIf { it.isNotBlank() }
        if (topLevel != null) return topLevel

        val element = profilePictureRaw ?: return null
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val candidateKeys = listOf("contentType", "mimeType", "type")
            for (key in candidateKeys) {
                val value = obj.get(key)
                if (value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                    return value.asString
                }
            }
        }

        return null
    }

    val profilePictureDataUri: String?
        get() {
            val rawSource = extractPictureString()
                ?.takeIf { it.isNotBlank() }
                ?: return null

            val raw = rawSource
                .substringAfter("base64,", rawSource)
                ?.trim()

            if (rawSource.startsWith("data:image")) return rawSource

            val mimeType = extractPictureContentType() ?: "image/jpeg"
            return "data:$mimeType;base64,$raw"
        }

    val profilePictureBase64: String?
        get() {
            val rawSource = extractPictureString()?.takeIf { it.isNotBlank() } ?: return null
            return rawSource.substringAfter("base64,", rawSource)
        }
}

data class UpdateUsernameRequest(val username: String)
data class UpdateUsernameResponse(val username: String)


