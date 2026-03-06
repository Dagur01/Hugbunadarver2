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

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getFavorites()
                if (res.isSuccessful && res.body() != null) {
                    val ids = res.body()!!.map { it.movieId }.toSet()
                    state = state.copy(favoriteIds = ids)
                } else if (res.code() == 401) {

                    state = state.copy(error = "Unauthorized (please login again)")
                }
            } catch (_: Exception) {

            }
        }
    }

    fun toggleFavorite(movieId: Long) {
        viewModelScope.launch {
            val isFav = state.favoriteIds.contains(movieId)

            try {
                val res = if (isFav) {
                    ApiClient.api.removeFavorite(movieId)
                } else {
                    ApiClient.api.addFavorite(movieId)
                }

                if (res.isSuccessful) {
                    val newSet = if (isFav) state.favoriteIds - movieId else state.favoriteIds + movieId
                    state = state.copy(favoriteIds = newSet)
                } else {
                    state = state.copy(error = res.errorBody()?.string() ?: "Favorite action failed")
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }

    fun loadMoviesByGenre(genre: String) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                val response = ApiClient.api.getMoviesByGenre(genre)

                if (response.isSuccessful && response.body() != null) {
                    state = state.copy(
                        movies = response.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = response.errorBody()?.string() ?: "Failed to filter movies"
                    )
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
