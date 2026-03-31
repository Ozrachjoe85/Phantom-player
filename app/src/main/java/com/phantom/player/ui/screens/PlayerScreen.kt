package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.PlayerViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    var swipeOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        // Cyberpunk grid background
        CyberpunkGrid(isPlaying = isPlaying)
        
        // Pulsing radial glow
        PulsingRadialGlow(isPlaying = isPlaying)
        
        // Animated particles
        AnimatedBackgroundParticles(isPlaying = isPlaying)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // HERO: Album Art with Holographic Frame (40% of screen)
            HolographicAlbumArt(
                albumArtPath = currentSong?.albumArtPath,
                isPlaying = isPlaying,
                onSwipeLeft = { viewModel.skipToNext() },
                onSwipeRight = { viewModel.skipToPrevious() },
                onTap = { /* Navigate to library */ },
                onDoubleTap = {
                    currentSong?.let { song ->
                        viewModel.toggleFavorite(song.id, !song.isFavorite)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Track info with cyberpunk glitch effect
            CyberpunkTrackInfo(
                title = currentSong?.title ?: "NO SIGNAL DETECTED",
                artist = currentSong?.artist ?: "UNKNOWN SOURCE",
                album = currentSong?.album ?: "NULL REFERENCE",
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Real-time spectrum analyzer
            CyberpunkSpectrumAnalyzer(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar with neon glow
            NeonProgressBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // PRIMARY CONTROLS: 96dp Play, 64dp Skip (as per research)
            CyberpunkPrimaryControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipToPrevious() },
                onNext = { viewModel.skipToNext() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // SECONDARY CONTROLS: 48dp buttons
            CyberpunkSecondaryControls(
                isFavorite = currentSong?.isFavorite ?: false,
                onFavoriteToggle = {
                    currentSong?.let { song ->
                        viewModel.toggleFavorite(song.id, !song.isFavorite)
                    }
                },
                onEqClick = { /* Navigate to EQ */ },
                onShuffleClick = { /* Toggle shuffle */ },
                onRepeatClick = { /* Toggle repeat */ }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ============================================================================
// CYBERPUNK GRID BACKGROUND
// ============================================================================
@Composable
fun CyberpunkGrid(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_offset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSpacing = 50f
        val lineAlpha = if (isPlaying) 0.15f else 0.08f
        
        // Vertical lines
        var x = gridOffset % gridSpacing
        while (x < size.width) {
            drawLine(
                color = PhantomPurple.copy(alpha = lineAlpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }
        
        // Horizontal lines (perspective effect)
        var y = size.height - (gridOffset % gridSpacing)
        var lineWidth = 1f
        while (y > size.height * 0.3f) {
            val alpha = lineAlpha * (y / size.height)
            drawLine(
                color = PhantomPurple.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = lineWidth
            )
            y -= gridSpacing
            lineWidth += 0.2f
        }
    }
}

// ============================================================================
// HOLOGRAPHIC ALBUM ART (Interactive - 96dp touch targets)
// ============================================================================
@Composable
fun HolographicAlbumArt(
    albumArtPath: String?,
    isPlaying: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onTap: () -> Unit,
    onDoubleTap: () -> Unit
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "album_scale"
    )
    
    val rotation by rememberInfiniteTransition(label = "holo_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Holographic ring
        Canvas(modifier = Modifier
            .fillMaxSize()
            .rotate(rotation)
        ) {
            val radius = size.minDimension / 2
            
            // Outer ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        PhantomPurple,
                        PhantomOrange,
                        PhantomPurple,
                        PhantomOrange,
                        PhantomPurple
                    )
                ),
                radius = radius,
                style = Stroke(width = 4f)
            )
            
            // Middle ring
            drawCircle(
                color = PhantomPurple.copy(alpha = 0.3f),
                radius = radius - 10f,
                style = Stroke(width = 2f)
            )
            
            // Scan line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        PhantomOrange,
                        Color.Transparent
                    )
                ),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 3f
            )
        }
        
        // Album artwork
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .scale(scaleAnim)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(PhantomPurple, PhantomOrange)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                swipeOffset > 100f -> onSwipeRight()
                                swipeOffset < -100f -> onSwipeLeft()
                            }
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffset += dragAmount
                        }
                    )
                }
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        onDoubleTap()
                    } else {
                        onTap()
                    }
                    lastTapTime = currentTime
                }
        ) {
            if (albumArtPath != null) {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Cyberpunk placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PhantomPurple.copy(alpha = 0.3f),
                                    PhantomBlack
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = PhantomPurple.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Holographic overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PhantomPurple.copy(alpha = 0.1f),
                            Color.Transparent,
                            PhantomOrange.copy(alpha = 0.1f)
                        )
                    )
                )
            }
        }
    }
}

// ============================================================================
// CYBERPUNK TRACK INFO with Glitch Effect
// ============================================================================
@Composable
fun CyberpunkTrackInfo(
    title: String,
    artist: String,
    album: String,
    isPlaying: Boolean
) {
    val glitchOffset by rememberInfiniteTransition(label = "glitch").animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 2f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitch_offset"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title with glitch effect
        Box {
            // Glitch layers (RGB split)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = PhantomOrange.copy(alpha = 0.3f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = (-glitchOffset).dp, y = glitchOffset.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = PhantomPurple.copy(alpha = 0.3f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = glitchOffset.dp, y = (-glitchOffset).dp)
            )
            // Main text
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = PhantomWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Artist
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = PhantomPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Album
        Text(
            text = album,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = PhantomWhite.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// CYBERPUNK SPECTRUM ANALYZER
// ============================================================================
@Composable
fun CyberpunkSpectrumAnalyzer(isPlaying: Boolean) {
    val bars = remember { List(32) { Random.nextFloat() } }
    val animatedBars = bars.map { initialHeight ->
        rememberInfiniteTransition(label = "bar").animateFloat(
            initialValue = initialHeight,
            targetValue = if (isPlaying) Random.nextFloat() else 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (100..300).random(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_height"
        )
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0A0A))
            .border(2.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val barWidth = size.width / bars.size
        val maxHeight = size.height
        
        bars.forEachIndexed { index, _ ->
            val barHeight = animatedBars[index].value * maxHeight
            val x = index * barWidth
            
            // Glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PhantomOrange.copy(alpha = 0.5f),
                        PhantomPurple.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(x + barWidth * 0.2f, maxHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, barHeight)
            )
            
            // Bar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PhantomOrange,
                        PhantomPurple
                    )
                ),
                topLeft = Offset(x + barWidth * 0.3f, maxHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.4f, barHeight)
            )
        }
    }
}

// ============================================================================
// NEON PROGRESS BAR (Scrubable)
// ============================================================================
@Composable
fun NeonProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
    
    Column {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PhantomWhite.copy(alpha = 0.1f))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        val seekPosition = (change.position.x / size.width * duration).toLong()
                        onSeek(seekPosition.coerceIn(0, duration))
                    }
                }
        ) {
            // Progress fill with glow
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PhantomPurple,
                                PhantomOrange
                            )
                        )
                    )
                    .blur(4.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PhantomPurple,
                                PhantomOrange
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = PhantomOrange
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = PhantomPurple
            )
        }
    }
}

// ============================================================================
// PRIMARY CONTROLS (96dp Play, 64dp Skip)
// ============================================================================
@Composable
fun CyberpunkPrimaryControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "play_scale"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous (64dp)
        CyberpunkButton(
            icon = Icons.Default.SkipPrevious,
            size = 64.dp,
            color = PhantomPurple,
            onClick = onPrevious
        )
        
        // Play/Pause (96dp) - PRIMARY
        Box(modifier = Modifier.scale(scaleAnim)) {
            CyberpunkButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                size = 96.dp,
                color = PhantomOrange,
                onClick = onPlayPause,
                isPrimary = true
            )
        }
        
        // Next (64dp)
        CyberpunkButton(
            icon = Icons.Default.SkipNext,
            size = 64.dp,
            color = PhantomPurple,
            onClick = onNext
        )
    }
}

// ============================================================================
// SECONDARY CONTROLS (48dp buttons)
// ============================================================================
@Composable
fun CyberpunkSecondaryControls(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onEqClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CyberpunkIconButton(
            icon = Icons.Default.Shuffle,
            size = 48.dp,
            color = PhantomPurple,
            onClick = onShuffleClick
        )
        
        CyberpunkIconButton(
            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            size = 48.dp,
            color = if (isFavorite) PhantomOrange else PhantomPurple,
            onClick = onFavoriteToggle
        )
        
        CyberpunkIconButton(
            icon = Icons.Default.Equalizer,
            size = 48.dp,
            color = PhantomOrange,
            onClick = onEqClick
        )
        
        CyberpunkIconButton(
            icon = Icons.Default.Repeat,
            size = 48.dp,
            color = PhantomPurple,
            onClick = onRepeatClick
        )
    }
}

// ============================================================================
// CYBERPUNK BUTTON (Holographic with rotating ring)
// ============================================================================
@Composable
fun CyberpunkButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    val rotation by rememberInfiniteTransition(label = "button_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Rotating holographic ring
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = size.toPx() / 2,
                style = Stroke(width = 2f)
            )
            
            // Scan line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, color, Color.Transparent)
                ),
                start = Offset(0f, size.toPx() / 2),
                end = Offset(size.toPx(), size.toPx() / 2),
                strokeWidth = 2f
            )
        }
        
        // Button core
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .clip(CircleShape)
                .background(
                    if (isPrimary) {
                        Brush.radialGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.4f),
                                color.copy(alpha = 0.2f)
                            )
                        )
                    }
                )
                .border(2.dp, color, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.5f),
                tint = if (isPrimary) PhantomBlack else color
            )
        }
    }
}

// ============================================================================
// CYBERPUNK ICON BUTTON (Secondary controls)
// ============================================================================
@Composable
fun CyberpunkIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// ============================================================================
// UTILITIES
// ============================================================================
@Composable
fun PulsingRadialGlow(isPlaying: Boolean) {
    val scale by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    
    val alpha by rememberInfiniteTransition(label = "glow_alpha").animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    if (isPlaying) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = 100.dp)
                    .scale(scale)
                    .blur(100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PhantomPurple.copy(alpha = alpha),
                                PhantomOrange.copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AnimatedBackgroundParticles(isPlaying: Boolean) {
    val particles = remember {
        List(20) {
            Triple(
                Random.nextFloat(),
                Random.nextFloat(),
                Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )
    
    if (isPlaying) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { (x, y, speed) ->
                val particleX = (x + time * speed) % 1f
                val particleY = y
                
                drawCircle(
                    color = PhantomOrange.copy(alpha = 0.3f),
                    radius = 3f,
                    center = Offset(particleX * size.width, particleY * size.height)
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
