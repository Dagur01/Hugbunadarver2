package com.example.hugbunadarver2.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import kotlinx.coroutines.launch

data class HomeState(
    val movies: List<Movie> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val loading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val response = ApiClient.api.getMovies()
                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(movies = response.body()!!, loading = false)
                } else {
                    state = state.copy(
                        loading = false,
                        error = response.errorBody()?.string() ?: "Failed to load movies"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(loading = false, error = "Network error: ${e.message}")
            }
        }
    }
    fun toggleFavorite(movieId: Long) {
        val newSet =
            if (state.favoriteIds.contains(movieId)) state.favoriteIds - movieId
            else state.favoriteIds + movieId

        state = state.copy(favoriteIds = newSet)
    }
}
