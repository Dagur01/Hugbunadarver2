package com.example.hugbunadarver2

import FavoritesScreen
import android.os.Bundle
import android.util.Base64
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.hugbunadarver2.auth.SessionManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hugbunadarver2.admin.AdminRoute
import com.example.hugbunadarver2.auth.LoginRoute
import com.example.hugbunadarver2.auth.SignUpRoute
import com.example.hugbunadarver2.booking.BookingRoute
import com.example.hugbunadarver2.home.HomeScreen
import com.example.hugbunadarver2.home.HomeViewModel
import com.example.hugbunadarver2.network.ApiClient
import com.example.hugbunadarver2.profile.EditProfileRoute
import com.example.hugbunadarver2.profile.ProfileRoute
import com.example.hugbunadarver2.ui.theme.Hugbunadarver2Theme
import org.json.JSONObject
import com.example.hugbunadarver2.booking.BookingRoute
import com.example.hugbunadarver2.home.Movie
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import com.example.hugbunadarver2.mybookings.MyBookingsRoute
import com.example.hugbunadarver2.friends.FriendProfileRoute
import com.example.hugbunadarver2.friends.FriendRequestsRoute
import com.example.hugbunadarver2.friends.FriendsRoute
import com.example.hugbunadarver2.friends.MovieInvitationsRoute
import com.example.hugbunadarver2.theater.TheaterMapScreen
import com.example.hugbunadarver2.theater.TheaterMapViewModel




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
    val context = LocalContext.current
    var token by rememberSaveable {
        val saved = SessionManager.getToken(context)
        if (saved != null) ApiClient.setToken(saved)
        mutableStateOf(saved)
    }
    var showSignUp by rememberSaveable { mutableStateOf(false) }
    var showEditProfile by rememberSaveable { mutableStateOf(false) }
    var authUiResetKey by rememberSaveable { mutableStateOf(0) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val homeVm: HomeViewModel = viewModel()
    val theaterVm: TheaterMapViewModel = viewModel()
    var selectedMovieForBooking by remember { mutableStateOf<com.example.hugbunadarver2.home.Movie?>(null) }
    var showFriendRequests by rememberSaveable { mutableStateOf(false) }
    var selectedFriendEmail by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMovieForInvitation by remember { mutableStateOf<Movie?>(null) }


    if (selectedMovieForBooking != null) {
        BookingRoute(
            movie = selectedMovieForBooking!!,
            onBack = { selectedMovieForBooking = null }
        )
        return
    }

    if (token == null) {
        if (showSignUp) {
            SignUpRoute(
                onSignedUp = { newToken ->
                    ApiClient.setToken(newToken)
                    SessionManager.saveToken(context, newToken)
                    token = newToken

                    homeVm.loadMovies()
                    homeVm.loadFavorites()
                },
                onBackToLogin = { showSignUp = false },
                resetKey = authUiResetKey
            )
        } else {
            LoginRoute(
                onLoggedIn = { newToken ->
                    ApiClient.setToken(newToken)
                    SessionManager.saveToken(context, newToken)
                    token = newToken

                    homeVm.loadMovies()
                    homeVm.loadFavorites()
                },
                onGoToSignUp = { showSignUp = true },
                resetKey = authUiResetKey
            )
        }
        return
    }

    val isAdmin = extractRoleFromToken(token!!) == "ADMIN"

    LaunchedEffect(isAdmin, currentDestination) {
        if (!isAdmin && currentDestination == AppDestinations.ADMIN) {
            currentDestination = AppDestinations.HOME
        }
        if (currentDestination == AppDestinations.FAVORITES) {
            homeVm.loadFavorites()
        }
        if (currentDestination == AppDestinations.THEATER_MAP) {
            theaterVm.reload()
        }
    }

    if (showEditProfile) {
        EditProfileRoute(
            token = token!!,
            currentUsername = "",
            onNavigateBack = {
                showEditProfile = false
                currentDestination = AppDestinations.PROFILE
            },
            onLogout = {
                SessionManager.clearToken(context)
                token = null
                ApiClient.clearToken()
                showSignUp = false
                showEditProfile = false
                currentDestination = AppDestinations.HOME
                authUiResetKey++
            }
        )
        return
    }

    val visibleDestinations = if (isAdmin) {
        listOf(
            AppDestinations.HOME,
            AppDestinations.FAVORITES,
            AppDestinations.PROFILE,
            AppDestinations.MY_BOOKINGS,
            AppDestinations.FRIENDS,
            AppDestinations.THEATER_MAP,
            AppDestinations.ADMIN
        )
    } else {
        listOf(
            AppDestinations.HOME,
            AppDestinations.FAVORITES,
            AppDestinations.MY_BOOKINGS,
            AppDestinations.FRIENDS,
            AppDestinations.THEATER_MAP,
            AppDestinations.PROFILE
        )
    }

    if (showFriendRequests) {
        FriendRequestsRoute(
            onBack = { showFriendRequests = false }
        )
        return
    }

    if (selectedFriendEmail != null) {
        FriendProfileRoute(
            email = selectedFriendEmail!!,
            onBack = { selectedFriendEmail = null }
        )
        return
    }

    if (selectedMovieForInvitation != null) {
        MovieInvitationsRoute(
            movie = selectedMovieForInvitation!!,
            onBack = { selectedMovieForInvitation = null }
        )
        return
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            visibleDestinations.forEach {
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
                onToggleFavorite = homeVm::toggleFavorite,
                onFilterGenre = homeVm::loadMoviesByGenre,
                onBookMovie = { movie ->
                    selectedMovieForBooking = movie
                },
                onInviteFriend = { movie ->
                    selectedMovieForInvitation = movie
                }
            )
            AppDestinations.FAVORITES -> FavoritesScreen(
                movies = homeVm.state.movies,
                favoriteIds = homeVm.state.favoriteIds,
                onToggleFavorite = homeVm::toggleFavorite
            )
            AppDestinations.PROFILE -> ProfileRoute(
                userId = "currentUserId",
                token = token!!,
                onNavigateToEditProfile = { showEditProfile = true },
                onLogout = {
                    SessionManager.clearToken(context)
                    token = null
                    ApiClient.clearToken()
                    showSignUp = false
                    currentDestination = AppDestinations.HOME
                    authUiResetKey++
                }
            )

            AppDestinations.FRIENDS -> FriendsRoute(
                onOpenRequests = { showFriendRequests = true },
                onOpenProfile = { email -> selectedFriendEmail = email }
            )
            AppDestinations.ADMIN -> {
                if (isAdmin) {
                    AdminRoute()
                } else {
                    HomeScreen(
                        state = homeVm.state,
                        onRetry = homeVm::loadMovies,
                        onToggleFavorite = homeVm::toggleFavorite,
                        onFilterGenre = homeVm::loadMoviesByGenre,
                        onBookMovie = { movie ->
                            selectedMovieForBooking = movie
                        },
                        onInviteFriend = { movie ->
                            selectedMovieForInvitation = movie
                        }
                    )
                }
            }
            AppDestinations.MY_BOOKINGS -> MyBookingsRoute()
            AppDestinations.THEATER_MAP -> TheaterMapScreen(viewModel = theaterVm)
        }
    }
}

private fun extractRoleFromToken(token: String): String {
    return try {
        val parts = token.split(".")
        if (parts.size < 2) return "USER"

        val payload = parts[1]
        val decoded = Base64.decode(
            payload,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val json = JSONObject(String(decoded))

        when (json.optString("role", "USER").uppercase()) {
            "ADMIN" -> "ADMIN"
            else -> "USER"
        }
    } catch (_: Exception) {
        "USER"
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    MY_BOOKINGS("Bookings", Icons.Default.List),
    PROFILE("Profile", Icons.Default.AccountBox),
    FRIENDS("Friends", Icons.Default.AccountBox),
    THEATER_MAP("Theaters", Icons.Default.LocationOn),
    ADMIN("Admin", Icons.Default.AccountBox),
}
