package com.example.hugbunadarver2

import FavoritesScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hugbunadarver2.auth.LoginRoute
import com.example.hugbunadarver2.auth.SignUpRoute
import com.example.hugbunadarver2.home.HomeScreen
import com.example.hugbunadarver2.home.HomeViewModel
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.profile.EditProfileRoute
import com.example.hugbunadarver2.profile.ProfileRoute
import com.example.hugbunadarver2.ui.theme.Hugbunadarver2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hugbunadarver2Theme {
                Hugbunadarver2App()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun Hugbunadarver2App() {
    var token by rememberSaveable { mutableStateOf<String?>(null) }
    var showSignUp by rememberSaveable { mutableStateOf(false) }
    var showEditProfile by rememberSaveable { mutableStateOf(false) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val homeVm: HomeViewModel = viewModel()  // einn VM fyrir bæði HOME og FAVORITES

    if (token == null) {
        if (showSignUp) {
            SignUpRoute(
                onSignedUp = { token = it },
                onBackToLogin = { showSignUp = false }
            )
        } else {
            LoginRoute(
                onLoggedIn = { newToken ->
                    ApiClient.setToken(newToken)
                    token = newToken
                },
                onGoToSignUp = { showSignUp = true }
            )
        }
        return
    }

    if (showEditProfile) {
        EditProfileRoute(
            token = token!!,
            currentUsername = "",
            onNavigateBack = {
                showEditProfile = false
                currentDestination = AppDestinations.PROFILE
            }
        )
        return
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> HomeScreen(
                state = homeVm.state,
                onRetry = homeVm::loadMovies,
                onToggleFavorite = homeVm::toggleFavorite
            )
            AppDestinations.FAVORITES -> FavoritesScreen(
                movies = homeVm.state.movies,
                favoriteIds = homeVm.state.favoriteIds,
                onToggleFavorite = homeVm::toggleFavorite
            )
            AppDestinations.PROFILE -> ProfileRoute(
                userId = "currentUserId",
                token = token!!,
                onNavigateToEditProfile = { showEditProfile = true }
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}
