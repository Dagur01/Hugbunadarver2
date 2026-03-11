package com.example.hugbunadarver2.mybookings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.BookingDto
import kotlinx.coroutines.launch

data class MyBookingsState(
    val bookings: List<BookingDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class MyBookingsViewModel : ViewModel() {
    var state by mutableStateOf(MyBookingsState())
        private set

    fun loadBookings() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.getMyBookings()
                if (res.isSuccessful && res.body() != null) {
                    state = state.copy(
                        bookings = res.body()!!,
                        loading = false
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = res.errorBody()?.string() ?: "Failed to load bookings"
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

    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            try {
                val res = ApiClient.api.cancelBooking(bookingId)
                if (res.isSuccessful) {
                    state = state.copy(
                        bookings = state.bookings.filterNot { it.bookingid == bookingId }
                    )
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to cancel booking"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(error = "Network error: ${e.message}")
            }
        }
    }
}