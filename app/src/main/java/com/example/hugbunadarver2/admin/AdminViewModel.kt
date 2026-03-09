package com.example.hugbunadarver2.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.AddMovieRequest
import com.example.hugbunadarver2.network.ApiClient
import kotlinx.coroutines.launch

data class AdminState(
    val title: String = "",
    val genre: String = "",
    val ageRating: String = "",
    val duration: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminViewModel : ViewModel() {
    var state by mutableStateOf(AdminState())
        private set

    fun onTitleChange(value: String) {
        state = state.copy(title = value)
    }

    fun onGenreChange(value: String) {
        state = state.copy(genre = value)
    }

    fun onAgeRatingChange(value: String) {
        state = state.copy(ageRating = value)
    }

    fun onDurationChange(value: String) {
        state = state.copy(duration = value)
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun clearSuccess() {
        state = state.copy(successMessage = null)
    }

    fun submitMovie(onSuccess: () -> Unit) {
        val title = state.title.trim()
        val genre = state.genre.trim()
        val ageRating = state.ageRating.toIntOrNull()
        val duration = state.duration.toIntOrNull()

        if (title.isBlank() || genre.isBlank() || ageRating == null || duration == null) {
            state = state.copy(error = "Please fill all fields correctly")
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val response = ApiClient.api.addMovie(
                    AddMovieRequest(
                        title = title,
                        genre = genre,
                        ageRating = ageRating,
                        duration = duration
                    )
                )

                if (response.isSuccessful) {
                    state = state.copy(
                        title = "",
                        genre = "",
                        ageRating = "",
                        duration = "",
                        loading = false,
                        error = null,
                        successMessage = "Movie uploaded"
                    )
                    onSuccess()
                } else {
                    val msg = response.errorBody()?.string()?.ifBlank { null }
                        ?: "Failed to upload movie (${response.code()})"
                    state = state.copy(loading = false, error = msg)
                }
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }
}

