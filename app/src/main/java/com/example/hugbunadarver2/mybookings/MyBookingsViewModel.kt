package com.example.hugbunadarver2.mybookings

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.BookingDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MyBookingsState(
    val bookings: List<BookingDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val isOfflineData: Boolean = false,
    val selectedQrBookingId: Long? = null,
    val qrBitmap: Bitmap? = null,
    val qrLoading: Boolean = false
)

class MyBookingsViewModel : ViewModel() {
    var state by mutableStateOf(MyBookingsState())
        private set

    private val gson = Gson()

    fun loadBookings(context: Context) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            try {
                val res = ApiClient.api.getMyBookings()
                if (res.isSuccessful && res.body() != null) {
                    val bookings = res.body()!!
                    saveCachedBookings(appContext, bookings)
                    state = state.copy(
                        bookings = bookings,
                        loading = false,
                        isOfflineData = false,
                        error = null
                    )
                } else {
                    val fallback = loadCachedBookings(appContext)
                    if (fallback.isNotEmpty()) {
                        state = state.copy(
                            bookings = fallback,
                            loading = false,
                            isOfflineData = true,
                            error = null
                        )
                    } else {
                        state = state.copy(
                            loading = false,
                            error = res.errorBody()?.string() ?: "Failed to load bookings"
                        )
                    }
                }
            } catch (e: Exception) {
                val fallback = loadCachedBookings(appContext)
                if (fallback.isNotEmpty()) {
                    state = state.copy(
                        bookings = fallback,
                        loading = false,
                        isOfflineData = true,
                        error = null
                    )
                } else {
                    state = state.copy(
                        loading = false,
                        error = "Network error: ${e.message}"
                    )
                }
            }
        }
    }

    fun showBookingQr(context: Context, bookingId: Long) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            state = state.copy(
                selectedQrBookingId = bookingId,
                qrBitmap = null,
                qrLoading = true,
                error = null
            )

            val qrBitmap = withContext(Dispatchers.IO) {
                BookingQrStorage.getOrCreateQrBitmap(appContext, bookingId)
            }

            if (qrBitmap != null) {
                state = state.copy(
                    qrBitmap = qrBitmap,
                    qrLoading = false
                )
            } else {
                state = state.copy(
                    qrLoading = false,
                    error = "Failed to generate QR code"
                )
            }
        }
    }

    fun hideBookingQr() {
        state = state.copy(
            selectedQrBookingId = null,
            qrBitmap = null,
            qrLoading = false
        )
    }

    fun cancelBooking(context: Context, bookingId: Long) {
        val appContext = context.applicationContext
        viewModelScope.launch {
            try {
                val res = ApiClient.api.cancelBooking(bookingId)
                if (res.isSuccessful) {
                    val updatedBookings = state.bookings.filterNot { it.bookingid == bookingId }
                    saveCachedBookings(appContext, updatedBookings)
                    BookingQrStorage.deleteQrBitmap(appContext, bookingId)

                    val shouldHideQr = state.selectedQrBookingId == bookingId
                    state = state.copy(
                        bookings = updatedBookings,
                        selectedQrBookingId = if (shouldHideQr) null else state.selectedQrBookingId,
                        qrBitmap = if (shouldHideQr) null else state.qrBitmap,
                        qrLoading = false
                    )
                } else {
                    state = state.copy(
                        error = res.errorBody()?.string() ?: "Failed to cancel booking"
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    private fun saveCachedBookings(context: Context, bookings: List<BookingDto>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKINGS_JSON, gson.toJson(bookings)).apply()
    }

    private fun loadCachedBookings(context: Context): List<BookingDto> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString(KEY_BOOKINGS_JSON, null) ?: return emptyList()

        return try {
            val listType = object : TypeToken<List<BookingDto>>() {}.type
            gson.fromJson<List<BookingDto>>(cachedJson, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val PREFS_NAME = "my_bookings_cache"
        const val KEY_BOOKINGS_JSON = "bookings_json"
    }
}