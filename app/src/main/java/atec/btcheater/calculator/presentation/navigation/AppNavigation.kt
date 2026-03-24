package atec.btcheater.calculator.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import atec.btcheater.calculator.presentation.screens.difficulty.DifficultyScreen
import atec.btcheater.calculator.presentation.screens.home.HomeScreen
import atec.btcheater.calculator.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Difficulty : Screen("difficulty")
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.Home, Icons.Default.Home, "Dashboard"),
        Triple(Screen.Difficulty, Icons.Default.ShowChart, "Difficulty"),
        Triple(Screen.Settings, Icons.Default.Settings, "Einstellungen")
    )

    androidx.compose.material3.Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (screen, icon, label) ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Screen.Difficulty.route) {
                DifficultyScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
