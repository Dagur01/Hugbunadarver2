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
}
