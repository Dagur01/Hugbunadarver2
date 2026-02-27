package com.example.hugbunadarver2.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.ApiService
import com.example.hugbunadarver2.network.UpdateUsernameRequest
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.hugbunadarver2.network.UploadPictureRequest


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
                        },
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

    fun updateUsername(
        token: String,
        newUsername: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                ApiClient.setToken(token)

                val response = ApiClient.api.updateUsername(
                    UpdateUsernameRequest(newUsername)
                )

                if (response.isSuccessful) {
                    state = state.copy(
                        username = newUsername,
                        loading = false
                    )
                    onSuccess()
                } else {
                    state = state.copy(
                        loading = false,
                        error = response.errorBody()?.string()
                            ?: "Failed to update username"
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
    fun uploadProfilePicture(
        token: String,
        uri: Uri,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                ApiClient.setToken(token)

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: run {
                        state = state.copy(loading = false, error = "Could not read image")
                        return@launch
                    }

                // Base64 encode (NO_WRAP svo þetta verði ein lína)
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                // Sumir backend vilja bara base64 streng, aðrir vilja data-url.
                // Prófaðu fyrst bara base64:
                val res = ApiClient.api.uploadProfilePicture(
                    UploadPictureRequest(base64)
                )

                if (res.isSuccessful) {
                    state = state.copy(loading = false)
                    onSuccess()
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Upload failed"
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                state = state.copy(
                    loading = false,
                    error = "Upload failed: ${e.message}"
                )
            }
        }
    }
}
