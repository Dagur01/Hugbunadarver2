package com.example.hugbunadarver2.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditProfileRoute(
    token: String,
    currentUsername: String,
    onNavigateBack: () -> Unit
) {
    val vm: ProfileViewModel = viewModel()

    EditProfileScreen(
        currentUsername = currentUsername,
        loading = vm.state.loading,
        error = vm.state.error,
        onSave = { newUsername ->
            vm.updateUsername(token, newUsername, onNavigateBack)
        },
        onCancel = onNavigateBack
    )
}

@Composable
fun EditProfileScreen(
    currentUsername: String,
    loading: Boolean,
    error: String?,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            enabled = !loading,
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCancel,
                enabled = !loading
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { onSave(username) },
                enabled = !loading && username.isNotBlank()
            ) {
                Text("Save")
            }
        }

        if (loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        error?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}