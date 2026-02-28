package com.example.hugbunadarver2.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun EditProfileRoute(
    token: String,
    currentUsername: String,
    onNavigateBack: () -> Unit
) {
    val vm: ProfileViewModel = viewModel()
    val context = LocalContext.current

    EditProfileScreen(
        currentUsername = currentUsername,
        loading = vm.state.loading,
        error = vm.state.error,
        onSave = { newUsername ->
            vm.updateUsername(token, newUsername) {
                onNavigateBack()
            }
        },
        onUploadPicture = { uri ->
            vm.uploadProfilePicture(token, uri, context) {
                onNavigateBack()
            }
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
    onUploadPicture: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            onUploadPicture(it)
        }
    }

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

        // Profile Picture
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable(enabled = !loading) {
                    imagePickerLauncher.launch("image/*")
                },
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            enabled = !loading
        ) {
            Text("Change Picture")
        }

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