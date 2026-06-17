package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.database.AppDatabase
import com.example.data.repository.LinkSaaSRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PublicProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SaaSViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = LinkSaaSRepository(db.userDao(), db.linkDao())
            val factory = SaaSViewModel.Factory(application, repository)
            val viewModel: SaaSViewModel = viewModel(factory = factory)

            val isDarkTheme by viewModel.isGlobalDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkTheme) Color(0xFF09090B) else Color(0xFFF4F4F5)
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }

    @Composable
    fun AppNavigation(viewModel: SaaSViewModel) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "auth"
        ) {
            // Private Auth screen (Route 1)
            composable("auth") {
                AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Private Admin Dashboard / Editor workspace (Route 2)
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToPublic = { username ->
                        navController.navigate("public/$username")
                    },
                    onSignout = {
                        navController.navigate("auth") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }

            // Public profile screen / dynamic handle slug fetcher (Route 3)
            composable(
                route = "public/{username}",
                arguments = listOf(
                    navArgument("username") {
                        type = NavType.StringType
                        defaultValue = "skthouhid"
                    }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: "skthouhid"
                PublicProfileScreen(
                    username = username,
                    viewModel = viewModel,
                    onBackToLogin = {
                        navController.navigate("auth") {
                            popUpTo("public/{username}") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
