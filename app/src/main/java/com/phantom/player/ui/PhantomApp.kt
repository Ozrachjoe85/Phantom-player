package com.phantom.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phantom.player.ui.screens.*
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.*

/**
 * Phantom Player - Main App Composable with Bottom Navigation
 */
@Composable
fun PhantomApp() {
    val navController = rememberNavController()
    
    Scaffold(
        containerColor = PhantomBlack,
        bottomBar = {
            PhantomBottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PhantomBlack)
        ) {
            PhantomNavHost(navController = navController)
        }
    }
}

/**
 * Bottom Navigation Bar - Cyberpunk Themed
 */
@Composable
fun PhantomBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val items = listOf(
        BottomNavItem.Library,
        BottomNavItem.Player,
        BottomNavItem.EQ,
        BottomNavItem.Settings
    )
    
    NavigationBar(
        containerColor = SurfaceGlass,
        contentColor = PhantomWhite,
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(if (selected) 28.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PhantomOrange,
                    selectedTextColor = PhantomOrange,
                    unselectedIconColor = PhantomWhite.copy(alpha = 0.6f),
                    unselectedTextColor = PhantomWhite.copy(alpha = 0.6f),
                    indicatorColor = PhantomPurple.copy(alpha = 0.3f)
                )
            )
        }
    }
}

/**
 * Navigation Host - Routes to Screens
 */
@Composable
fun PhantomNavHost(navController: NavHostController) {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val eqViewModel: EqViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Library.route
    ) {
        composable(BottomNavItem.Library.route) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    navController.navigate(BottomNavItem.Player.route)
                }
            )
        }
        
        composable(BottomNavItem.Player.route) {
            PlayerScreen(
                viewModel = playerViewModel,
                onNavigateToEQ = {
                    navController.navigate(BottomNavItem.EQ.route)
                }
            )
        }
        
        composable(BottomNavItem.EQ.route) {
            EqScreen(
                viewModel = eqViewModel
            )
        }
        
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }
    }
}

/**
 * Bottom Navigation Items
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    object Library : BottomNavItem(
        route = "library",
        icon = Icons.Default.LibraryMusic,
        title = "Library"
    )
    
    object Player : BottomNavItem(
        route = "player",
        icon = Icons.Default.Album,
        title = "Player"
    )
    
    object EQ : BottomNavItem(
        route = "eq",
        icon = Icons.Default.GraphicEq,
        title = "EQ"
    )
    
    object Settings : BottomNavItem(
        route = "settings",
        icon = Icons.Default.Settings,
        title = "Settings"
    )
}
