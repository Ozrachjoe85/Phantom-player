package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.PlayerViewModel
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Album Art with Glass Effect
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PhantomDarkPurple, PhantomMidPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            currentSong?.albumArtPath?.let { artPath ->
                AsyncImage(
                    model = artPath,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "No Album Art",
                modifier = Modifier.size(120.dp),
                tint = PhantomCyan.copy(alpha = 0.3f)
            )
            
            // Glass overlay with neon border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // LED-style Track Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentSong?.let { song ->
                Text(
                    text = song.title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = PhantomCyan
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${song.artist.uppercase()} • ${song.album.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = PhantomPurple
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: Text(
                text = "NO SONG LOADED",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = PhantomCyan.copy(alpha = 0.5f)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Waveform Visualizer
        WaveformVisualizer(isPlaying = isPlaying)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Spectrum Analyzer
        SpectrumAnalyzer(isPlaying = isPlaying)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                onValueChange = { value ->
                    viewModel.seekTo((value * duration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = PhantomCyan,
                    activeTrackColor = PhantomCyan,
                    inactiveTrackColor = PhantomDarkPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PhantomPurple,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PhantomPurple,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // VU Meters
        VUMeters(isPlaying = isPlaying)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.skipToPrevious() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp),
                    tint = PhantomCyan
                )
            }
            
            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PhantomCyan
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                    tint = PhantomBlack
                )
            }
            
            IconButton(
                onClick = { viewModel.skipToNext() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp),
                    tint = PhantomCyan
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Additional Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* Toggle shuffle */ }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = PhantomPurple
                )
            }
            
            IconButton(onClick = { 
                currentSong?.let { song ->
                    viewModel.toggleFavorite(song.id, !song.isFavorite)
                }
            }) {
                Icon(
                    imageVector = if (currentSong?.isFavorite == true) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = "Favorite",
                    tint = if (currentSong?.isFavorite == true) PhantomPink else PhantomPurple
                )
            }
            
            IconButton(onClick = { /* Toggle repeat */ }) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = PhantomPurple
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun WaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_progress"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val points = 50
        
        for (i in 0 until points) {
            val x = (i.toFloat() / points) * width
            val progress = if (isPlaying) animatedProgress else 0f
            val amplitude = if (isPlaying) {
                sin((i + progress * 10) * 0.5f) * 20f
            } else {
                0f
            }
            
            drawLine(
                color = PhantomCyan,
                start = Offset(x, centerY - amplitude),
                end = Offset(x, centerY + amplitude),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SpectrumAnalyzer(isPlaying: Boolean) {
    val bars = remember { List(16) { mutableStateOf(Random.nextFloat()) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                bars.forEach { it.value = Random.nextFloat() }
                kotlinx.coroutines.delay(100)
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { heightState ->
            val height by animateFloatAsState(
                targetValue = if (isPlaying) heightState.value else 0f,
                animationSpec = tween(100),
                label = "spectrum_bar"
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(height)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PhantomPink, PhantomPurple, PhantomCyan)
                        )
                    )
            )
        }
    }
}

@Composable
fun VUMeters(isPlaying: Boolean) {
    val leftLevel = remember { mutableStateOf(0.7f) }
    val rightLevel = remember { mutableStateOf(0.65f) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                leftLevel.value = 0.5f + Random.nextFloat() * 0.5f
                rightLevel.value = 0.5f + Random.nextFloat() * 0.5f
                kotlinx.coroutines.delay(50)
            }
        } else {
            leftLevel.value = 0f
            rightLevel.value = 0f
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VUMeter(
            label = "L",
            level = leftLevel.value
        )
        
        VUMeter(
            label = "R",
            level = rightLevel.value
        )
    }
}

@Composable
fun VUMeter(label: String, level: Float) {
    val animatedLevel by animateFloatAsState(
        targetValue = level,
        animationSpec = tween(50),
        label = "vu_level"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PhantomDarkPurple)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedLevel)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PhantomCyan, PhantomPurple, PhantomPink)
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PhantomCyan,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
