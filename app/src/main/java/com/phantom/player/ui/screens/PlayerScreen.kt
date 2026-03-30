package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        // Animated Background Particles
        AnimatedBackgroundParticles(isPlaying = isPlaying)
        
        // Pulsing Radial Glow
        PulsingRadialGlow(isPlaying = isPlaying)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album Art with Holographic Effect
            HolographicAlbumArt(
                albumArtPath = currentSong?.albumArtPath,
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Track Info with Glitch Effect
            GlitchTrackInfo(
                title = currentSong?.title ?: "NO SIGNAL",
                artist = currentSong?.artist ?: "UNKNOWN SOURCE",
                album = currentSong?.album ?: "NULL",
                isPlaying = isPlaying
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Waveform Visualizer - More Intense
            IntenseWaveformVisualizer(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 3D Spectrum Analyzer
            ThreeDSpectrumAnalyzer(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Neon Progress Bar with Time Display
            NeonProgressBar(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { viewModel.seekTo(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // VU Meters - Enhanced
            EnhancedVUMeters(isPlaying = isPlaying)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Playback Controls - Holographic Style
            HolographicPlaybackControls(
                isPlaying = isPlaying,
                onPlayPause = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipToPrevious() },
                onNext = { viewModel.skipToNext() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Secondary Controls
            SecondaryControls(
                isFavorite = currentSong?.isFavorite ?: false,
                onFavoriteToggle = {
                    currentSong?.let { song ->
                        viewModel.toggleFavorite(song.id, !song.isFavorite)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnimatedBackgroundParticles(isPlaying: Boolean) {
    val particles = remember {
        List(30) {
            ParticleState(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.2f,
                size = Random.nextFloat() * 4f + 2f
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
            particles.forEach { particle ->
                val x = (particle.x + time * particle.speed) % 1f
                val y = particle.y
                
                drawCircle(
                    color = PhantomCyan.copy(alpha = 0.3f),
                    radius = particle.size,
                    center = Offset(x * size.width, y * size.height)
                )
            }
        }
    }
}

data class ParticleState(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)

@Composable
fun PulsingRadialGlow(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
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
            modifier = Modifier
                .fillMaxSize(),
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
                                PhantomCyan.copy(alpha = alpha),
                                PhantomPurple.copy(alpha = alpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun HolographicAlbumArt(
    albumArtPath: String?,
    isPlaying: Boolean
) {
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(320.dp)
            .scale(scaleAnim),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Holographic Ring
        Canvas(
            modifier = Modifier
                .size(340.dp)
                .rotate(rotation)
        ) {
            val ringCount = 3
            for (i in 0 until ringCount) {
                val radius = size.minDimension / 2 - (i * 20f)
                drawCircle(
                    color = when (i % 3) {
                        0 -> PhantomCyan.copy(alpha = 0.3f)
                        1 -> PhantomPurple.copy(alpha = 0.3f)
                        else -> PhantomPink.copy(alpha = 0.3f)
                    },
                    radius = radius,
                    style = Stroke(width = 2f)
                )
            }
            
            // Scan lines
            for (angle in 0..360 step 30) {
                val rad = Math.toRadians(angle.toDouble())
                val startX = center.x + cos(rad).toFloat() * (size.minDimension / 2 - 60f)
                val startY = center.y + sin(rad).toFloat() * (size.minDimension / 2 - 60f)
                val endX = center.x + cos(rad).toFloat() * (size.minDimension / 2)
                val endY = center.y + sin(rad).toFloat() * (size.minDimension / 2)
                
                drawLine(
                    color = PhantomCyan.copy(alpha = 0.2f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1f
                )
            }
        }
        
        // Album Art Core
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PhantomDarkPurple,
                            PhantomBlack
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PhantomCyan,
                            PhantomPurple,
                            PhantomPink,
                            PhantomCyan
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            albumArtPath?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Holographic overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PhantomCyan.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    PhantomPurple.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            } ?: Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "No Album Art",
                modifier = Modifier.size(120.dp),
                tint = PhantomCyan.copy(alpha = 0.3f)
            )
        }
        
        // Corner accents
        CornerAccents()
    }
}

@Composable
fun BoxScope.CornerAccents() {
    val corners = listOf(
        Alignment.TopStart to 0f,
        Alignment.TopEnd to 90f,
        Alignment.BottomEnd to 180f,
        Alignment.BottomStart to 270f
    )
    
    corners.forEach { (alignment, rotation) ->
        Canvas(
            modifier = Modifier
                .size(40.dp)
                .align(alignment)
                .rotate(rotation)
        ) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.3f)
                moveTo(size.width, 0f)
                lineTo(size.width * 0.7f, 0f)
            }
            drawPath(
                path = path,
                color = PhantomCyan,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun GlitchTrackInfo(
    title: String,
    artist: String,
    album: String,
    isPlaying: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.3f),
                        SurfaceGlass.copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(PhantomCyan.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        // Title with scan line effect
        Box {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomCyan,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (isPlaying) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = PhantomPink.copy(alpha = 0.3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(x = 2.dp, y = 1.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Artist
        Text(
            text = "◢ ${artist.uppercase()} ◣",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            ),
            color = PhantomPurple,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Album
        Text(
            text = "［ ${album.uppercase()} ］",
            style = MaterialTheme.typography.bodyMedium.copy(
                letterSpacing = 1.sp
            ),
            color = PhantomWhite.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IntenseWaveformVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_progress"
    )
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PhantomBlack)
            .border(1.dp, PhantomCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val points = 100
        
        for (i in 0 until points) {
            val x = (i.toFloat() / points) * width
            val progress = if (isPlaying) animatedProgress else 0f
            
            val amplitude = if (isPlaying) {
                (sin((i + progress * 20) * 0.3).toFloat() * height * 0.4f *
                        (0.5f + Random.nextFloat() * 0.5f))
            } else {
                0f
            }
            
            // Outer glow
            drawLine(
                color = PhantomCyan.copy(alpha = 0.2f),
                start = Offset(x, centerY - amplitude - 2f),
                end = Offset(x, centerY + amplitude + 2f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            
            // Main line
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PhantomCyan,
                        PhantomPurple,
                        PhantomPink
                    )
                ),
                start = Offset(x, centerY - amplitude),
                end = Offset(x, centerY + amplitude),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ThreeDSpectrumAnalyzer(isPlaying: Boolean) {
    val bars = remember { List(24) { mutableStateOf(Random.nextFloat()) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                bars.forEach { it.value = Random.nextFloat() * 0.7f + 0.3f }
                kotlinx.coroutines.delay(50)
            }
        } else {
            bars.forEach { it.value = 0f }
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PhantomBlack, PhantomDarkPurple.copy(alpha = 0.5f))
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / bars.size
        val perspective = 0.7f
        
        bars.forEachIndexed { index, heightState ->
            val animatedHeight by animateFloatAsState(
                targetValue = if (isPlaying) heightState.value else 0f,
                animationSpec = tween(50),
                label = "bar_$index"
            )
            
            val x = index * barWidth
            val barHeight = animatedHeight * height
            
            // 3D perspective effect
            val bottomWidth = barWidth * perspective
            val perspectiveOffset = (barWidth - bottomWidth) / 2
            
            // Shadow
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(x + perspectiveOffset + 2f, height - 2f),
                size = androidx.compose.ui.geometry.Size(bottomWidth, 2f)
            )
            
            // 3D Bar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PhantomCyan,
                        PhantomPurple,
                        PhantomPink
                    )
                ),
                topLeft = Offset(x + perspectiveOffset, height - barHeight),
                size = androidx.compose.ui.geometry.Size(bottomWidth, barHeight)
            )
            
            // Top face
            val topWidth = barWidth * 0.8f
            drawRect(
                color = PhantomCyan.copy(alpha = 0.8f),
                topLeft = Offset(x + (barWidth - topWidth) / 2, height - barHeight - 3f),
                size = androidx.compose.ui.geometry.Size(topWidth, 3f)
            )
            
            // Highlight edge
            drawLine(
                color = PhantomCyan.copy(alpha = 0.5f),
                start = Offset(x + perspectiveOffset, height - barHeight),
                end = Offset(x + perspectiveOffset, height),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun NeonProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.2f),
                        SurfaceGlass.copy(alpha = 0.1f)
                    )
                )
            )
            .border(1.dp, PhantomCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Progress bar with glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(PhantomDarkPurple)
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .blur(8.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomCyan, PhantomPurple, PhantomPink)
                        )
                    )
            )
            
            // Solid bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomCyan, PhantomPurple, PhantomPink)
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Time display
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
                color = PhantomCyan
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhantomPurple
            )
        }
    }
}

@Composable
fun EnhancedVUMeters(isPlaying: Boolean) {
    val leftLevel = remember { mutableStateOf(0.7f) }
    val rightLevel = remember { mutableStateOf(0.65f) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                leftLevel.value = 0.4f + Random.nextFloat() * 0.6f
                rightLevel.value = 0.4f + Random.nextFloat() * 0.6f
                kotlinx.coroutines.delay(30)
            }
        } else {
            leftLevel.value = 0f
            rightLevel.value = 0f
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PhantomBlack)
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VUMeterChannel(
            label = "L",
            level = leftLevel.value,
            color = PhantomCyan
        )
        
        VUMeterChannel(
            label = "R",
            level = rightLevel.value,
            color = PhantomPink
        )
    }
}

@Composable
fun VUMeterChannel(label: String, level: Float, color: Color) {
    val animatedLevel by animateFloatAsState(
        targetValue = level,
        animationSpec = tween(30),
        label = "vu_level"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = color
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PhantomDarkPurple)
        ) {
            // Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedLevel)
                    .fillMaxHeight()
                    .blur(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.5f), color)
                        )
                    )
            )
            
            // Solid fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedLevel)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
            
            // Peak indicator
            if (animatedLevel > 0.8f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedLevel)
                        .fillMaxHeight()
                        .background(PhantomWhite.copy(alpha = 0.5f))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${(animatedLevel * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun HolographicPlaybackControls(
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
        label = "button_scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.4f),
                        SurfaceGlass.copy(alpha = 0.2f),
                        SurfaceGlass.copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    listOf(PhantomCyan, PhantomPurple, PhantomPink, PhantomCyan)
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        HolographicButton(
            icon = Icons.Default.SkipPrevious,
            buttonSize = 64.dp,
            iconSize = 32.dp,
            color = PhantomPurple,
            onClick = onPrevious
        )
        
        // Play/Pause Button - HUGE
        Box(
            modifier = Modifier.scale(scaleAnim)
        ) {
            HolographicButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                buttonSize = 96.dp,
                iconSize = 48.dp,
                color = PhantomCyan,
                onClick = onPlayPause,
                isPrimary = true
            )
        }
        
        // Next Button
        HolographicButton(
            icon = Icons.Default.SkipNext,
            buttonSize = 64.dp,
            iconSize = 32.dp,
            color = PhantomPink,
            onClick = onNext
        )
    }
}

@Composable
fun HolographicButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
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
        modifier = Modifier.size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        // Rotating ring
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = size.minDimension / 2,
                style = Stroke(width = 2f)
            )
            
            // Scan line effect
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = center,
                end = Offset(center.x + size.minDimension / 2, center.y),
                strokeWidth = 2f
            )
        }
        
        // Button core
        Box(
            modifier = Modifier
                .size(buttonSize * 0.85f)
                .clip(CircleShape)
                .background(
                    if (isPrimary) {
                        Brush.radialGradient(
                            listOf(
                                color,
                                color.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                color.copy(alpha = 0.4f),
                                color.copy(alpha = 0.2f)
                            )
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    color = color,
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = if (isPrimary) PhantomBlack else color
            )
        }
    }
}

@Composable
fun SecondaryControls(
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SecondaryButton(
            icon = Icons.Default.Shuffle,
            color = PhantomPurple,
            onClick = { }
        )
        
        SecondaryButton(
            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            color = if (isFavorite) PhantomPink else PhantomPurple,
            onClick = onFavoriteToggle
        )
        
        SecondaryButton(
            icon = Icons.Default.Repeat,
            color = PhantomPurple,
            onClick = { }
        )
        
        SecondaryButton(
            icon = Icons.Default.QueueMusic,
            color = PhantomCyan,
            onClick = { }
        )
    }
}

@Composable
fun SecondaryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
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
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
