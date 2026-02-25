package com.example.hugbunadarver2

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.hugbunadarver2.auth.LoginRoute
import com.example.hugbunadarver2.ui.theme.Hugbunadarver2Theme
import com.example.hugbunadarver2.profile.ProfileRoute
import com.example.hugbunadarver2.profile.EditProfileRoute




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
    var showEditProfile by rememberSaveable { mutableStateOf(false) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    if (token == null) {
        LoginRoute(onLoggedIn = { token = it })
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
            AppDestinations.HOME -> {
                Text("Home Screen")
            }
            AppDestinations.FAVORITES -> {
                Text("Favorites Screen")
            }
            AppDestinations.PROFILE -> {
                ProfileRoute(
                    userId = "currentUserId",
                    token = token!!,
                    onNavigateToEditProfile = { showEditProfile = true }
                )
            }
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Hugbunadarver2Theme {
        Greeting("Android")
    }

}

