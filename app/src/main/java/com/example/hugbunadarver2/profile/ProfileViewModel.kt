package com.example.hugbunadarver2.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.UpdateUsernameRequest
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody




class ProfileViewModel : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    fun loadUserProfile(token: String) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                ApiClient.setToken(token)

                val response = ApiClient.api.getUserProfile()

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    state = state.copy(
                        username = profile.username,
                        email = profile.email,
                        profilePictureUrl = profile.profilePictureBase64?.let {
                            "data:image/jpeg;base64,$it"
                        } ?: state.profilePictureUrl,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = response.errorBody()?.string()
                            ?: "Failed to load profile"
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun updateUsername(token: String, newUsername: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                ApiClient.setToken(token)
                ApiClient.api.updateUsername(UpdateUsernameRequest(newUsername))

                state = state.copy(
                    username = newUsername,
                    loading = false
                )
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                state = state.copy(
                    loading = false,
                    error = "Failed to update username: ${e.message}"
                )
            }
        }
    }
    fun uploadProfilePicture(
        token: String,
        imageUri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                println("DEBUG: Starting picture upload")
                ApiClient.setToken(token)

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    state = state.copy(loading = false, error = "Failed to read image")
                    return@launch
                }

                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

                // Convert to Base64 for local display
                val base64String = Base64.encodeToString(
                    bytes,
                    Base64.NO_WRAP
                )

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    "profile.jpg",
                    requestBody
                )

                ApiClient.api.uploadProfilePicture(filePart)

                state = state.copy(
                    profilePictureUrl = "data:$mimeType;base64,$base64String",
                    loading = false
                )

                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                state = state.copy(
                    loading = false,
                    error = "Failed to upload picture: ${e.message}"
                )
            }
        }
    }
}
