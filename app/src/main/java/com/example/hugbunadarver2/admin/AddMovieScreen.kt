package com.example.hugbunadarver2.admin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
@Composable
fun AddMovieScreen(
    state: AdminState,
    onTitleChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onAgeRatingChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Add Movie", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.loading
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.genre,
            onValueChange = onGenreChange,
            label = { Text("Genre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.loading
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.ageRating,
            onValueChange = onAgeRatingChange,
            label = { Text("Age Rating") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !state.loading
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.duration,
            onValueChange = onDurationChange,
            label = { Text("Duration") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !state.loading
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCancel,
                enabled = !state.loading
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSubmit,
                enabled = !state.loading
            ) {
                Text("Submit")
            }
        }
        if (state.loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
