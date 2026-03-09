package com.example.hugbunadarver2.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdminRoute() {
    var showAddMovie by rememberSaveable { mutableStateOf(false) }
    val vm: AdminViewModel = viewModel()

    if (showAddMovie) {
        AddMovieScreen(
            state = vm.state,
            onTitleChange = vm::onTitleChange,
            onGenreChange = vm::onGenreChange,
            onAgeRatingChange = vm::onAgeRatingChange,
            onDurationChange = vm::onDurationChange,
            onSubmit = {
                vm.submitMovie {
                    showAddMovie = false
                }
            },
            onCancel = {
                vm.clearError()
                showAddMovie = false
            }
        )
        return
    }

    AdminScreen(
        successMessage = vm.state.successMessage,
        onAddMovieClick = {
            vm.clearSuccess()
            showAddMovie = true
        }
    )
}

@Composable
fun AdminScreen(
    successMessage: String?,
    onAddMovieClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Admin View",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Only admins can see this tab.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onAddMovieClick) {
            Text("Add Movie")
        }

        successMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
