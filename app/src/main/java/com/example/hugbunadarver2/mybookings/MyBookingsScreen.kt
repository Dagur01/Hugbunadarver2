package com.example.hugbunadarver2.mybookings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MyBookingsRoute() {
    val vm: MyBookingsViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.loadBookings(context)
    }

    MyBookingsScreen(
        state = vm.state,
        onRetry = { vm.loadBookings(context) },
        onCancelBooking = { bookingId -> vm.cancelBooking(context, bookingId) },
        onShowQr = { bookingId -> vm.showBookingQr(context, bookingId) },
        onHideQr = vm::hideBookingQr
    )
}

@Composable
fun MyBookingsScreen(
    state: MyBookingsState,
    onRetry: () -> Unit,
    onCancelBooking: (Long) -> Unit,
    onShowQr: (Long) -> Unit,
    onHideQr: () -> Unit
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
                if (state.isOfflineData) {
                    item {
                        Text(
                            text = "Offline mode: showing saved bookings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

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

                            val isCurrentQr = state.selectedQrBookingId == booking.bookingid

                            if (isCurrentQr && state.qrLoading) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (isCurrentQr && state.qrBitmap != null) {
                                Image(
                                    bitmap = state.qrBitmap.asImageBitmap(),
                                    contentDescription = "Booking QR",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isCurrentQr && state.qrBitmap != null) {
                                    OutlinedButton(onClick = onHideQr) {
                                        Text("Hide QR")
                                    }
                                } else {
                                    OutlinedButton(onClick = { onShowQr(booking.bookingid) }) {
                                        Text("Show QR")
                                    }
                                }

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