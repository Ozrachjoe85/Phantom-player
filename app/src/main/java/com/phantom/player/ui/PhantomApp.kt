package com.phantom.player.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phantom.player.ui.screens.EqScreen
import com.phantom.player.ui.screens.LibraryScreen
import com.phantom.player.ui.screens.PlayerScreen
import com.phantom.player.ui.screens.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Player : Screen("player", "Player", Icons.Default.PlayCircle)
    object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    object Equalizer : Screen("equalizer", "EQ", Icons.Default.GraphicEq)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhantomApp() {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Player,
        Screen.Library,
        Screen.Equalizer,
        Screen.Settings
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Player.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Player.route) { PlayerScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Equalizer.route) { EqScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
