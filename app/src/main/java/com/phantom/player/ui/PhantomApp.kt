package com.phantom.player.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phantom.player.ui.screens.*
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.PlayerViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Player : Screen("player", "PLAYER", Icons.Default.Album)
    object Library : Screen("library", "LIBRARY", Icons.Default.LibraryMusic)
    object Eq : Screen("eq", "EQ", Icons.Default.Equalizer)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
}

@Composable
fun PhantomApp() {
    val navController = rememberNavController()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF000814),
                        Color(0xFF001233),
                        Color(0xFF000814)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Player.route
                ) {
                    composable(Screen.Player.route) { PlayerScreen() }
                    composable(Screen.Library.route) { LibraryScreen() }
                    composable(Screen.Eq.route) { EqScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
            
            // Liquid Metal Navigation Bar
            LiquidMetalNavigationBar(navController = navController)
        }
    }
}

@Composable
fun LiquidMetalNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val screens = listOf(
        Screen.Player,
        Screen.Library,
        Screen.Eq,
        Screen.Settings
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0x44001233),
                        Color(0x33000814),
                        Color(0x44001233)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(
                        ElectricBlue.copy(alpha = 0.5f),
                        HoloCyan.copy(alpha = 0.5f),
                        ElectricBlue.copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        screens.forEach { screen ->
            NavigationItem(
                screen = screen,
                isSelected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Player.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.radialGradient(
                            listOf(
                                ElectricBlue,
                                ElectricBlue.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
                .border(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) ElectricBlue else MetallicSilver.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glow effect when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(12.dp)
                        .background(ElectricBlue.copy(alpha = 0.6f), CircleShape)
                )
            }
            
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = if (isSelected) Color(0xFF000814) else HoloCyan,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = screen.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            color = if (isSelected) ElectricBlue else MetallicSilver.copy(alpha = 0.7f)
        )
    }
}
