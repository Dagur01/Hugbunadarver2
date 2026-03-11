package com.example.hugbunadarver2.mybookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MyBookingsRoute() {
    val vm: MyBookingsViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.loadBookings()
    }

    MyBookingsScreen(
        state = vm.state,
        onRetry = vm::loadBookings,
        onCancelBooking = vm::cancelBooking
    )
}

@Composable
fun MyBookingsScreen(
    state: MyBookingsState,
    onRetry: () -> Unit,
    onCancelBooking: (Long) -> Unit
) {
    when {
        state.loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }

        state.bookings.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No bookings yet")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.bookings,
                    key = { it.bookingid }
                ) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = booking.movie.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.padding(4.dp))

                            Text("Hall: ${booking.movieHall.name}")
                            Text("Location: ${booking.movieHall.location}")
                            Text("Seat: Row ${booking.seat.rowNumber}, Seat ${booking.seat.seatNumber}")
                            Text("Screening: ${booking.screening.screeningTime}")

                            booking.discountCode?.let { code ->
                                Text(
                                    "Discount: $code (${booking.discountPercent ?: 0}%)"
                                )
                            }

                            Spacer(modifier = Modifier.padding(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onCancelBooking(booking.bookingid) }
                                ) {
                                    Text("Cancel booking")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}