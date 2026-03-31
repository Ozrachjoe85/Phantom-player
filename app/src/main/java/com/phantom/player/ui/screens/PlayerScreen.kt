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
import androidx.compose.ui.text.style.TextAlign
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        // Animated Background with Radial Pulses
        AnimatedBackgroundEffects(isPlaying = isPlaying)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top Spectrum Analyzer
            TopSpectrumBar(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Massive Album Art Display
            MassiveAlbumArtDisplay(
                albumArtPath = currentSong?.albumArtPath,
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Track Info Panel
            TrackInfoPanel(
                title = currentSong?.title ?: "No Track Playing",
                artist = currentSong?.artist ?: "Unknown Artist",
                album = currentSong?.album ?: "Unknown Album"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rainbow Waveform Visualizer
            RainbowWaveformVisualizer(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Bar with Seek
            ProgressBarWithSeek(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Playback Controls
            MassivePlaybackControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipToPrevious() },
                onNext = { viewModel.skipToNext() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Control Row
            BottomControlRow(
                isFavorite = currentSong?.isFavorite ?: false,
                onFavoriteToggle = {
                    currentSong?.let { song ->
                        viewModel.toggleFavorite(song.id, !song.isFavorite)
                    }
                },
                onShuffleToggle = { viewModel.setShuffleMode(true) },
                onRepeatToggle = { viewModel.setRepeatMode(1) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AnimatedBackgroundEffects(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_scale"
    )
    
    if (isPlaying) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Multiple pulsing circles
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (index * 100).dp)
                        .scale(scale * (1f + index * 0.1f))
                        .blur(80.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    when (index % 3) {
                                        0 -> PhantomPurple.copy(alpha = 0.15f)
                                        1 -> PhantomGreen.copy(alpha = 0.15f)
                                        else -> PhantomOrange.copy(alpha = 0.15f)
                                    },
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun TopSpectrumBar(isPlaying: Boolean) {
    val bars = remember { List(32) { mutableStateOf(Random.nextFloat()) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                bars.forEach { it.value = Random.nextFloat() * 0.8f + 0.2f }
                kotlinx.coroutines.delay(50)
            }
        } else {
            bars.forEach { it.value = 0f }
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PhantomBlack)
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / bars.size) * 0.7f
        val spacing = width / bars.size
        
        bars.forEachIndexed { index, heightState ->
            val barHeight = heightState.value * height
            val x = index * spacing
            
            // Color gradient based on height
            val color = when {
                heightState.value > 0.7f -> PhantomOrange
                heightState.value > 0.4f -> PhantomPurple
                else -> PhantomGreen
            }
            
            drawRect(
                color = color,
                topLeft = Offset(x, height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun MassiveAlbumArtDisplay(
    albumArtPath: String?,
    isPlaying: Boolean
) {
    val rotation by rememberInfiniteTransition(label = "art_rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rotating outer ring
        if (isPlaying) {
            Canvas(modifier = Modifier.size(300.dp).rotate(rotation)) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            PhantomPurple,
                            PhantomGreen,
                            PhantomOrange,
                            PhantomPurple
                        )
                    ),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 4f)
                )
            }
        }
        
        // Album art container
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        listOf(PhantomDarkPurple, PhantomBlack)
                    )
                )
                .border(
                    2.dp,
                    if (isPlaying) {
                        Brush.linearGradient(listOf(PhantomPurple, PhantomGreen, PhantomOrange))
                    } else {
                        Brush.linearGradient(listOf(PhantomPurple.copy(alpha = 0.3f), Color.Transparent))
                    },
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtPath != null) {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = PhantomPurple.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun TrackInfoPanel(
    title: String,
    artist: String,
    album: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.4f),
                        SurfaceGlass.copy(alpha = 0.2f),
                        SurfaceGlass.copy(alpha = 0.4f)
                    )
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = PhantomPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = artist.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            color = PhantomGreen,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = album,
            style = MaterialTheme.typography.bodyMedium,
            color = PhantomWhite.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RainbowWaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_progress"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PhantomBlack)
            .border(1.dp, PhantomGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (isPlaying) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val points = 120
            
            for (i in 0 until points) {
                val x = (i.toFloat() / points) * width
                val wave = sin((i + progress * 30) * 0.4).toFloat() * height * 0.4f
                
                // Rainbow gradient based on position
                val hue = (i.toFloat() / points) * 360f
                val color = when {
                    i % 3 == 0 -> PhantomPurple
                    i % 3 == 1 -> PhantomGreen
                    else -> PhantomOrange
                }
                
                drawLine(
                    color = color.copy(alpha = 0.7f),
                    start = Offset(x, centerY - wave),
                    end = Offset(x, centerY + wave),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun ProgressBarWithSeek(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(progress) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGlass.copy(alpha = 0.3f))
            .border(1.dp, PhantomOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Seekable progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PhantomDarkPurple)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragProgress * duration).toLong())
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { _, dragAmount ->
                            dragProgress = (dragProgress + dragAmount / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
                .clickable {
                    // Allow clicking to seek as well
                }
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isDragging) dragProgress else progress)
                    .fillMaxHeight()
                    .blur(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomPurple, PhantomGreen, PhantomOrange)
                        )
                    )
            )
            
            // Solid progress
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isDragging) dragProgress else progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomPurple, PhantomGreen, PhantomOrange)
                        )
                    )
            )
            
            // Seek thumb
            if (isDragging || progress > 0f) {
                val thumbOffset = (if (isDragging) dragProgress else progress) * size.width.toFloat()
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset.dp - 10.dp)
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(PhantomGreen)
                        .border(2.dp, PhantomWhite, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhantomGreen
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhantomOrange
            )
        }
    }
}

@Composable
fun MassivePlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "play_scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.5f),
                        SurfaceGlass.copy(alpha = 0.3f),
                        SurfaceGlass.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(PhantomPurple, PhantomGreen, PhantomOrange, PhantomPurple)
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(PhantomPurple.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.2f))
                    )
                )
                .border(2.dp, PhantomPurple, CircleShape)
                .clickable(onClick = onPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = PhantomPurple,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // MASSIVE Play/Pause button
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(scaleAnim)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        if (isPlaying) {
                            listOf(PhantomGreen, PhantomGreen.copy(alpha = 0.6f))
                        } else {
                            listOf(PhantomPurple, PhantomPurple.copy(alpha = 0.6f))
                        }
                    )
                )
                .border(
                    3.dp,
                    if (isPlaying) PhantomGreen else PhantomPurple,
                    CircleShape
                )
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = PhantomBlack,
                modifier = Modifier.size(56.dp)
            )
        }
        
        // Next button
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(PhantomOrange.copy(alpha = 0.5f), PhantomOrange.copy(alpha = 0.2f))
                    )
                )
                .border(2.dp, PhantomOrange, CircleShape)
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = PhantomOrange,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun BottomControlRow(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = onShuffleToggle,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceGlass.copy(alpha = 0.3f))
                .border(1.dp, PhantomPurple.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Shuffle, null, tint = PhantomPurple)
        }
        
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isFavorite) {
                        Brush.radialGradient(listOf(PhantomOrange.copy(alpha = 0.5f), Color.Transparent))
                    } else {
                        Brush.radialGradient(listOf(SurfaceGlass.copy(alpha = 0.3f), Color.Transparent))
                    }
                )
                .border(1.dp, if (isFavorite) PhantomOrange else PhantomGreen.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                null,
                tint = if (isFavorite) PhantomOrange else PhantomGreen
            )
        }
        
        IconButton(
            onClick = onRepeatToggle,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceGlass.copy(alpha = 0.3f))
                .border(1.dp, PhantomGreen.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Repeat, null, tint = PhantomGreen)
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
