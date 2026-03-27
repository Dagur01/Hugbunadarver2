package com.example.hugbunadarver2.friends

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hugbunadarver2.home.Movie

@Composable
fun MovieInvitationsRoute(
    movie: Movie? = null,
    onBack: () -> Unit
) {
    val vm: FriendsViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.loadSentMovieInvitations()
        vm.loadReceivedMovieInvitations()
    }

    MovieInvitationsScreen(
        state = vm.state,
        movie = movie,
        onInviteEmailChange = vm::onInviteEmailChange,
        onSendInvite = {
            movie?.let { vm.sendMovieInvitation(it.movieId) }
        },
        onAcceptInvite = vm::acceptMovieInvitation,
        onRejectInvite = vm::rejectMovieInvitation,
        onRetry = {
            vm.loadSentMovieInvitations()
            vm.loadReceivedMovieInvitations()
        },
        onBack = onBack
    )
}

@Composable
fun MovieInvitationsScreen(
    state: FriendsState,
    movie: Movie?,
    onInviteEmailChange: (String) -> Unit,
    onSendInvite: () -> Unit,
    onAcceptInvite: (Long) -> Unit,
    onRejectInvite: (Long) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = if (movie != null) "Invite friend to ${movie.title}" else "Movie Invitations",
            style = MaterialTheme.typography.headlineSmall
        )

        if (movie != null) {
            OutlinedTextField(
                value = state.inviteEmailInput,
                onValueChange = onInviteEmailChange,
                label = { Text("Friend email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = onSendInvite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send movie invitation")
            }
        }

        state.successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Received") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sent") }
            )
        }

        if (state.loading) {
            CircularProgressIndicator()
        } else {
            if (selectedTab == 0) {
                if (state.receivedMovieInvitations.isEmpty()) {
                    Text("No received invitations")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.receivedMovieInvitations, key = { it.id }) { invitation ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Movie ID: ${invitation.movieId}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text("From: ${invitation.inviter.email}")
                                    Text("Status: ${invitation.status}")

                                    Spacer(modifier = Modifier.padding(6.dp))

                                    if (invitation.status == "SENT") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onAcceptInvite(invitation.id) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Accept")
                                            }

                                            Button(
                                                onClick = { onRejectInvite(invitation.id) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (state.sentMovieInvitations.isEmpty()) {
                    Text("No sent invitations")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.sentMovieInvitations, key = { it.id }) { invitation ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Movie ID: ${invitation.movieId}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text("To: ${invitation.invitee.email}")
                                    Text("Status: ${invitation.status}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}