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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

enum class AdminPage {
    MAIN,
    ADD_MOVIE,
    SELECT_MOVIE_TO_EDIT,
    EDIT_MOVIE
}

@Composable
fun AdminRoute() {
    var currentPage by rememberSaveable { mutableStateOf(AdminPage.MAIN) }
    val vm: AdminViewModel = viewModel()

    when (currentPage) {
        AdminPage.ADD_MOVIE -> {
            AddMovieScreen(
                state = vm.state,
                onTitleChange = vm::onTitleChange,
                onGenreChange = vm::onGenreChange,
                onAgeRatingChange = vm::onAgeRatingChange,
                onDurationChange = vm::onDurationChange,
                onSubmit = {
                    vm.submitMovie {
                        currentPage = AdminPage.MAIN
                    }
                },
                onCancel = {
                    vm.clearError()
                    currentPage = AdminPage.MAIN
                }
            )
        }

        AdminPage.SELECT_MOVIE_TO_EDIT -> {
            LaunchedEffect(Unit) {
                vm.loadMoviesForSelection()
            }

            SelectMovieScreen(
                movies = vm.movieListState.movies,
                loading = vm.movieListState.loading,
                error = vm.movieListState.error,
                onMovieSelected = { movie ->
                    vm.selectMovieForEdit(movie)
                    currentPage = AdminPage.EDIT_MOVIE
                },
                onCancel = {
                    currentPage = AdminPage.MAIN
                }
            )
        }

        AdminPage.EDIT_MOVIE -> {
            EditMovieScreen(
                state = vm.editMovieState,
                onTitleChange = vm::onEditTitleChange,
                onGenreChange = vm::onEditGenreChange,
                onAgeRatingChange = vm::onEditAgeRatingChange,
                onDurationChange = vm::onEditDurationChange,
                onNowShowingChange = vm::onEditNowShowingChange,
                onSubmit = {
                    vm.submitMovieEdit {
                        currentPage = AdminPage.MAIN
                    }
                },
                onCancel = {
                    vm.clearEditError()
                    currentPage = AdminPage.MAIN
                }
            )
        }

        AdminPage.MAIN -> {
            AdminScreen(
                successMessage = vm.state.successMessage,
                onAddMovieClick = {
                    vm.clearSuccess()
                    currentPage = AdminPage.ADD_MOVIE
                },
                onEditMovieClick = {
                    vm.clearSuccess()
                    currentPage = AdminPage.SELECT_MOVIE_TO_EDIT
                }
            )
        }
    }
}

@Composable
fun AdminScreen(
    successMessage: String?,
    onAddMovieClick: () -> Unit,
    onEditMovieClick: () -> Unit
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

        Spacer(Modifier.height(8.dp))

        Button(onClick = onEditMovieClick) {
            Text("Edit Movie")
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
