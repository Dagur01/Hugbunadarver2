package com.example.hugbunadarver2.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.home.Movie
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.CreateBookingRequest
import com.example.hugbunadarver2.network.MovieHallDto
import com.example.hugbunadarver2.network.ScreeningDto
import com.example.hugbunadarver2.network.SeatDto
import kotlinx.coroutines.launch

data class BookingState(
    val halls: List<MovieHallDto> = emptyList(),
    val screenings: List<ScreeningDto> = emptyList(),
    val seats: List<SeatDto> = emptyList(),
    val bookedSeatIds: Set<Long> = emptySet(),
    val selectedHallId: Long? = null,
    val selectedScreeningId: Long? = null,
    val selectedSeatId: Long? = null,
    val discountCode: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class BookingViewModel : ViewModel() {
    var state by mutableStateOf(BookingState())
        private set

    fun loadInitialData() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val hallsRes = ApiClient.api.getMovieHalls()
                val screeningsRes = ApiClient.api.getScreenings()

                if (hallsRes.isSuccessful && screeningsRes.isSuccessful &&
                    hallsRes.body() != null && screeningsRes.body() != null
                ) {
                    state = state.copy(
                        halls = hallsRes.body()!!,
                        screenings = screeningsRes.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = "Failed to load booking data"
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

    fun selectHall(hallId: Long) {
        state = state.copy(
            selectedHallId = hallId,
            selectedSeatId = null
        )
        loadSeats(hallId)
    }

    private fun loadSeats(hallId: Long) {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.getSeatsByHall(hallId)
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(
                        seats = res.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = "Failed to load seats"
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

    fun selectScreening(screeningId: Long) {
        state = state.copy(
            selectedScreeningId = screeningId,
            selectedSeatId = null
        )
        loadBookedSeats(screeningId)
    }

    fun selectSeat(seatId: Long) {
        state = state.copy(selectedSeatId = seatId)
    }

    fun onDiscountCodeChange(value: String) {
        state = state.copy(discountCode = value)
    }

    fun createBooking(movie: Movie, onSuccess: () -> Unit = {}) {
        val hallId = state.selectedHallId
        val screeningId = state.selectedScreeningId
        val seatId = state.selectedSeatId

        if (hallId == null || screeningId == null || seatId == null) {
            state = state.copy(error = "Please choose hall, screening and seat")
            return
        }

        viewModelScope.launch {
            state = state.copy(loading = true, error = null, successMessage = null)
            try {
                val res = ApiClient.api.createBooking(
                    CreateBookingRequest(
                        movieId = movie.movieId,
                        hallId = hallId,
                        seatId = seatId,
                        screeningId = screeningId,
                        discountCode = state.discountCode.ifBlank { null }
                    )
                )

                if (res.isSuccessful) {
                    state = state.copy(
                        loading = false,
                        successMessage = res.body()?.string() ?: "Booking created"
                    )
                    onSuccess()
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Booking failed"
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

    private fun loadBookedSeats(screeningId: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.getBookedSeatsForScreening(screeningId)
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(bookedSeatIds = res.body()!!.toSet())
                }
            } catch (_: Exception) {
            }
        }
    }
}