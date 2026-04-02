package com.phantom.player.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phantom.player.ui.screens.*

@Composable
fun PhantomApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNav(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "library",
            modifier = Modifier.padding(padding)
        ) {
            composable("library") { LibraryScreen() }
            composable("player") { PlayerScreen() }
            composable("eq") { EqScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route
    
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.LibraryMusic, "Library") },
            label = { Text("Library") },
            selected = route == "library",
            onClick = { navController.navigate("library") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PlayCircle, "Player") },
            label = { Text("Player") },
            selected = route == "player",
            onClick = { navController.navigate("player") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.GraphicEq, "EQ") },
            label = { Text("EQ") },
            selected = route == "eq",
            onClick = { navController.navigate("eq") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, "Settings") },
            label = { Text("Settings") },
            selected = route == "settings",
            onClick = { navController.navigate("settings") }
        )
    }
}
