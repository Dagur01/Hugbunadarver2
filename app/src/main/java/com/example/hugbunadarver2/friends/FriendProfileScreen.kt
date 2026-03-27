package com.example.hugbunadarver2.friends

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FriendProfileRoute(
    email: String,
    onBack: () -> Unit
) {
    val vm: FriendsViewModel = viewModel()

    LaunchedEffect(email) {
        vm.loadFriendProfile(email)
    }

    FriendProfileScreen(
        state = vm.state,
        onBack = onBack
    )
}

@Composable
fun FriendProfileScreen(
    state: FriendsState,
    onBack: () -> Unit
) {
    val profile = state.selectedProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        when {
            state.loading -> {
                CircularProgressIndicator()
            }

            state.error != null -> {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            profile == null -> {
                Text("No profile loaded")
            }

            else -> {
                val profileBitmap = profile.profilePictureBase64?.let { base64 ->
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap.asImageBitmap(),
                        contentDescription = "Friend profile picture",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = profile.username ?: "No username",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (profile.isFriend) "You are friends" else "Not friends",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleMedium
                )

                if (profile.friends.isEmpty()) {
                    Text("No friends visible")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profile.friends) { friendEmail ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = friendEmail,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}