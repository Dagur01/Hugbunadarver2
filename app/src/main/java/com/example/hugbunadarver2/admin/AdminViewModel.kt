package com.example.hugbunadarver2.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.AddMovieRequest
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.UpdateMovieRequest
import com.example.hugbunadarver2.home.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

data class EditMovieState(
    val movieId: Long = 0,
    val title: String = "",
    val genre: String = "",
    val ageRating: String = "",
    val duration: String = "",
    val nowShowing: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

data class MovieListState(
    val movies: List<Movie> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    var state by mutableStateOf(AdminState())
        private set

    var editMovieState by mutableStateOf(EditMovieState())
        private set

    var movieListState by mutableStateOf(MovieListState())
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

    fun loadMoviesForSelection() {
        viewModelScope.launch {
            movieListState = movieListState.copy(loading = true, error = null)
            try {
                val response = ApiClient.api.getMovies()
                if (response.isSuccessful && response.body() != null) {
                    val moviesWithPosters = hydrateMoviesWithDetails(response.body()!!)
                    movieListState = movieListState.copy(
                        movies = moviesWithPosters,
                        loading = false
                    )
                } else {
                    movieListState = movieListState.copy(
                        loading = false,
                        error = "Failed to load movies"
                    )
                }
            } catch (e: Exception) {
                movieListState = movieListState.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun selectMovieForEdit(movie: Movie) {
        editMovieState = EditMovieState(
            movieId = movie.movieId,
            title = movie.title,
            genre = movie.genre ?: "",
            ageRating = movie.ageRating?.toString() ?: "",
            duration = movie.duration?.toString() ?: "",
            nowShowing = movie.nowShowing ?: false
        )
    }

    fun onEditTitleChange(value: String) {
        editMovieState = editMovieState.copy(title = value)
    }

    fun onEditGenreChange(value: String) {
        editMovieState = editMovieState.copy(genre = value)
    }

    fun onEditAgeRatingChange(value: String) {
        editMovieState = editMovieState.copy(ageRating = value)
    }

    fun onEditDurationChange(value: String) {
        editMovieState = editMovieState.copy(duration = value)
    }

    fun onEditNowShowingChange(value: Boolean) {
        editMovieState = editMovieState.copy(nowShowing = value)
    }

    fun submitMovieEdit(onSuccess: () -> Unit) {
        val title = editMovieState.title.trim()
        val genre = editMovieState.genre.trim()
        val ageRating = editMovieState.ageRating.toIntOrNull()
        val duration = editMovieState.duration.toIntOrNull()

        if (title.isBlank() || genre.isBlank() || ageRating == null || duration == null) {
            editMovieState = editMovieState.copy(error = "Please fill all fields correctly")
            return
        }

        viewModelScope.launch {
            editMovieState = editMovieState.copy(loading = true, error = null)
            try {
                val response = ApiClient.api.updateMovie(
                    editMovieState.movieId,
                    UpdateMovieRequest(
                        title = title,
                        genre = genre,
                        ageRating = ageRating,
                        duration = duration,
                        nowShowing = editMovieState.nowShowing
                    )
                )

                if (response.isSuccessful) {
                    editMovieState = EditMovieState()
                    state = state.copy(successMessage = "Movie updated successfully")
                    onSuccess()
                } else {
                    val msg = response.errorBody()?.string()?.ifBlank { null }
                        ?: "Failed to update movie (${response.code()})"
                    editMovieState = editMovieState.copy(loading = false, error = msg)
                }
            } catch (e: Exception) {
                editMovieState = editMovieState.copy(
                    loading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun clearEditError() {
        editMovieState = editMovieState.copy(error = null)
    }

    private suspend fun hydrateMoviesWithDetails(movies: List<Movie>): List<Movie> = coroutineScope {
        movies.map { movie ->
            async {
                if (!movie.posterBase64.isNullOrBlank()) {
                    movie
                } else {
                    try {
                        val detailResponse = ApiClient.api.getMovieById(movie.movieId.toInt())
                        if (detailResponse.isSuccessful && detailResponse.body() != null) {
                            detailResponse.body()!!
                        } else {
                            movie
                        }
                    } catch (_: Exception) {
                        movie
                    }
                }
            }
        }.awaitAll()
    }
}
