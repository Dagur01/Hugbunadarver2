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
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FriendRequestsRoute(
    onBack: () -> Unit
) {
    val vm: FriendsViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.loadPendingRequests()
    }

    FriendRequestsScreen(
        state = vm.state,
        onAccept = vm::acceptFriendRequest,
        onReject = vm::rejectFriendRequest,
        onBack = onBack,
        onRetry = vm::loadPendingRequests
    )
}

@Composable
fun FriendRequestsScreen(
    state: FriendsState,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Text("Friend Requests", style = MaterialTheme.typography.headlineSmall)

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }

        when {
            state.loading -> {
                CircularProgressIndicator()
            }

            state.pendingRequests.isEmpty() -> {
                Text("No pending friend requests")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.pendingRequests, key = { it.id }) { request ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = request.fromUser.email,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                request.fromUser.username?.let {
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.padding(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAccept(request.id) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept")
                                    }

                                    Button(
                                        onClick = { onReject(request.id) },
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
    }
}