package com.example.hugbunadarver2.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hugbunadarver2.home.Movie


@Composable
fun BookingRoute(
    movie: Movie,
    onBack: () -> Unit = {},
    onInviteFriend: (Movie) -> Unit = {}
) {
    val vm: BookingViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.loadInitialData()
    }

    BookingScreen(
        movie = movie,
        state = vm.state,
        onSelectHall = vm::selectHall,
        onSelectScreening = vm::selectScreening,
        onSelectSeat = vm::selectSeat,
        onDiscountCodeChange = vm::onDiscountCodeChange,
        onBook = { vm.createBooking(movie, onSuccess = onBack) },
        onInviteFriend = { onInviteFriend(movie) }
    )
}

@Composable
fun BookingScreen(
    movie: Movie,
    state: BookingState,
    onSelectHall: (Long) -> Unit,
    onSelectScreening: (Long) -> Unit,
    onSelectSeat: (Long) -> Unit,
    onDiscountCodeChange: (String) -> Unit,
    onBook: () -> Unit,
    onInviteFriend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Book: ${movie.title}", style = MaterialTheme.typography.headlineSmall)

        Text("Choose hall")
        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            state.halls.forEach { hall ->
                FilterChip(
                    selected = state.selectedHallId == hall.movieHallId,
                    onClick = { onSelectHall(hall.movieHallId) },
                    label = { Text(hall.name) }
                )
            }
        }

        Text("Choose screening")
        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            state.screenings.forEach { screening ->
                FilterChip(
                    selected = state.selectedScreeningId == screening.id,
                    onClick = { onSelectScreening(screening.id) },
                    label = { Text(screening.screeningTime) }
                )
            }
        }


        Text("Choose seat")
        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            state.seats.forEach { seat ->
                val isBooked = state.bookedSeatIds.contains(seat.seatId)
                FilterChip(
                    selected = state.selectedSeatId == seat.seatId,
                    onClick = { if (!isBooked) onSelectSeat(seat.seatId) },
                    enabled = !isBooked,
                    label = { Text("R${seat.rowNumber} S${seat.seatNumber}") }
                )
            }
        }

        OutlinedTextField(
            value = state.discountCode,
            onValueChange = onDiscountCodeChange,
            label = { Text("Discount code (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onBook,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text("Create booking")
        }


        if (state.loading) {
            CircularProgressIndicator()
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        state.successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}