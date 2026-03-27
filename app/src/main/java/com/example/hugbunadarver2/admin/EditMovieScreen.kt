package com.example.hugbunadarver2.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMovieScreen(
    state: EditMovieState,
    onTitleChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onAgeRatingChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onNowShowingChange: (Boolean) -> Unit,
    onUploadPoster: (Uri) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val genres = listOf("Action", "Adventure", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller")
    var genreExpanded by remember { mutableStateOf(false) }
    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri ->
        selectedUri?.let(onUploadPoster)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Edit Movie", style = MaterialTheme.typography.headlineSmall)
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

        ExposedDropdownMenuBox(
            expanded = genreExpanded,
            onExpandedChange = { genreExpanded = !genreExpanded }
        ) {
            OutlinedTextField(
                value = state.genre,
                onValueChange = {},
                readOnly = true,
                label = { Text("Genre") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                enabled = !state.loading
            )

            DropdownMenu(
                expanded = genreExpanded,
                onDismissRequest = { genreExpanded = false }
            ) {
                genres.forEach { genre ->
                    DropdownMenuItem(
                        text = { Text(genre) },
                        onClick = {
                            onGenreChange(genre)
                            genreExpanded = false
                        }
                    )
                }
            }
        }

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

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Now Showing", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.nowShowing,
                onCheckedChange = onNowShowingChange,
                enabled = !state.loading
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { posterPickerLauncher.launch("image/*") },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Poster")
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !state.loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSubmit,
                enabled = !state.loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Changes")
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

