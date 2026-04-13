package com.example.hugbunadarver2.theater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.network.MovieHallDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TheaterWithDistance(
    val hall: MovieHallDto,
    val distanceMeters: Float?
)

class TheaterMapViewModel : ViewModel() {

    private val mutableTheaters = MutableStateFlow<List<TheaterWithDistance>>(emptyList())
    val theaters: StateFlow<List<TheaterWithDistance>> = mutableTheaters.asStateFlow()

    private val mutableIsLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = mutableIsLoading.asStateFlow()

    private val mutableError = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = mutableError.asStateFlow()

    private var rawHalls: List<MovieHallDto> = emptyList()

    init {
        loadTheaters()
    }

    fun reload() = loadTheaters()

    private fun loadTheaters() {
        viewModelScope.launch {
            mutableIsLoading.value = true
            mutableError.value = null
            try {
                val response = ApiClient.api.getMovieHalls()
                if (response.isSuccessful) {
                    rawHalls = (response.body() ?: emptyList()).filter { hall ->
                        hall.latitude != null && hall.longitude != null
                    }
                    mutableTheaters.value = rawHalls.map { TheaterWithDistance(it, null) }
                } else {
                    mutableError.value = "Failed to load theaters (${response.code()})"
                }
            } catch (e: Exception) {
                mutableError.value = "Network error: ${e.message}"
            } finally {
                mutableIsLoading.value = false
            }
        }
    }

    fun updateUserLocation(lat: Double, lng: Double) {
        mutableTheaters.value = rawHalls.map { hall ->
            val result = FloatArray(1)
            android.location.Location.distanceBetween(
                lat, lng,
                hall.latitude!!, hall.longitude!!,
                result
            )
            TheaterWithDistance(hall, result[0])
        }.sortedBy { it.distanceMeters }
    }
}
