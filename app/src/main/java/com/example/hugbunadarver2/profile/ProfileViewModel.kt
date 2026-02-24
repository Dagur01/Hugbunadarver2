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
                println("DEBUG: Token being used: ${token.take(20)}...") // Log first 20 chars
                ApiClient.setToken(token)
                val response = ApiClient.api.getUserProfile()

                state = state.copy(
                    username = response.username,
                    email = response.email,
                    profilePictureUrl = response.profilePictureBase64?.let {
                        "data:image/jpeg;base64,$it"
                    },
                    loading = false
                )
            } catch (e: Exception) {
                println("DEBUG: Error details: ${e.message}")
                e.printStackTrace()
                state = state.copy(
                    loading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    /**
     * TODO laga routing aftur a profile skja ekki home
     * */
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
}
