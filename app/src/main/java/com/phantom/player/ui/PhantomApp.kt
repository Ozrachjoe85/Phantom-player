package com.phantom.player.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.phantom.player.ui.screens.*
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.PlayerViewModel
import kotlin.math.cos
import kotlin.math.sin

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Player : Screen("player", "PLAYER", Icons.Default.Album)
    object Library : Screen("library", "LIBRARY", Icons.Default.LibraryMusic)
    object Eq : Screen("eq", "EQ", Icons.Default.Equalizer)
    object Settings : Screen("settings", "SETTINGS", Icons.Default.Settings)
}

@Composable
fun PhantomApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val currentSong by playerViewModel.currentSong.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        // Background ambient effects
        AmbientBackgroundEffect()
        
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
            
            // Mini player bar (when not on player screen)
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute != Screen.Player.route && currentSong != null) {
                MiniPlayerBar(
                    song = currentSong,
                    playerViewModel = playerViewModel,
                    onExpand = {
                        navController.navigate(Screen.Player.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            // Bottom Navigation
            HolographicBottomNavigation(navController = navController)
        }
    }
}

@Composable
fun AmbientBackgroundEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )
    
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center1 = Offset(
            size.width * 0.3f + cos(Math.toRadians(offset1.toDouble())).toFloat() * 100f,
            size.height * 0.3f + sin(Math.toRadians(offset1.toDouble())).toFloat() * 100f
        )
        
        val center2 = Offset(
            size.width * 0.7f + cos(Math.toRadians(offset2.toDouble())).toFloat() * 100f,
            size.height * 0.7f + sin(Math.toRadians(offset2.toDouble())).toFloat() * 100f
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PhantomPurple.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = center1,
                radius = 400f
            ),
            center = center1,
            radius = 400f
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PhantomPurple.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = center2,
                radius = 400f
            ),
            center = center2,
            radius = 400f
        )
    }
}

@Composable
fun MiniPlayerBar(
    song: com.phantom.player.data.local.database.entities.Song?,
    playerViewModel: PlayerViewModel,
    onExpand: () -> Unit
) {
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        PhantomBlack.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SurfaceGlass.copy(alpha = 0.4f),
                            SurfaceGlass.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(PhantomPurple.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onExpand)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art thumbnail
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(PhantomPurple.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.5f))
                            )
                        )
                        .border(1.dp, PhantomPurple, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (song?.albumArtPath != null) {
                        AsyncImage(
                            model = song.albumArtPath,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PhantomPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Song info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song?.title?.uppercase() ?: "NO TRACK",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = PhantomPurple,
                        maxLines = 1
                    )
                    Text(
                        text = song?.artist?.uppercase() ?: "UNKNOWN",
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 0.5.sp
                        ),
                        color = PhantomPurple,
                        maxLines = 1
                    )
                }
                
                // Play/Pause button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PhantomPurple.copy(alpha = 0.4f), PhantomPurple.copy(alpha = 0.2f))
                            )
                        )
                        .border(2.dp, PhantomPurple, CircleShape)
                        .clickable { playerViewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PhantomPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HolographicBottomNavigation(navController: NavController) {
    val items = listOf(
        Screen.Player,
        Screen.Library,
        Screen.Eq,
        Screen.Settings
    )
    
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        PhantomBlack.copy(alpha = 0.98f)
                    )
                )
            )
    ) {
        // Top border glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            PhantomPurple.copy(alpha = 0.5f),
                            PhantomPurple.copy(alpha = 0.5f),
                            PhantomOrange.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationButton(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
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
}

@Composable
fun NavigationButton(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rotation by rememberInfiniteTransition(label = "nav_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scaleValue by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating ring (only when selected)
        if (isSelected) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
            ) {
                drawCircle(
                    color = PhantomPurple.copy(alpha = 0.3f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 2f)
                )
                
                // Scan line
                drawLine(
                    color = PhantomPurple.copy(alpha = 0.5f),
                    start = center,
                    end = Offset(center.x + size.width / 2, center.y),
                    strokeWidth = 2f
                )
            }
        }
        
        // Button core
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.radialGradient(
                            listOf(
                                PhantomPurple.copy(alpha = 0.4f),
                                PhantomPurple.copy(alpha = 0.2f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                PhantomPurple.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    }
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PhantomPurple else PhantomPurple.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .scale(scaleValue)
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = if (isSelected) PhantomPurple else PhantomPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = screen.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PhantomPurple,
                    fontSize = 8.sp
                )
            }
        }
    }
}
