package com.example.hugbunadarver2.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FriendsRoute(
    onOpenRequests: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val vm: FriendsViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.loadFriends()
    }

    FriendsScreen(
        state = vm.state,
        onEmailChange = vm::onEmailInputChange,
        onSendRequest = vm::sendFriendRequest,
        onRetry = vm::loadFriends,
        onOpenRequests = onOpenRequests,
        onOpenProfile = onOpenProfile
    )
}

@Composable
fun FriendsScreen(
    state: FriendsState,
    onEmailChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    onRetry: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Friends", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.emailInput,
            onValueChange = onEmailChange,
            label = { Text("Friend email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = onSendRequest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send friend request")
        }

        Button(
            onClick = onOpenRequests,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View friend requests")
        }

        state.successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Text("My friends", style = MaterialTheme.typography.titleMedium)

        when {
            state.loading -> {
                CircularProgressIndicator()
            }

            state.friends.isEmpty() -> {
                Text("No friends yet")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.friends) { email ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProfile(email) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text(
                                    text = "Tap to view profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}