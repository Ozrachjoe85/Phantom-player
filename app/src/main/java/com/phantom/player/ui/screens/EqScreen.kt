package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.ui.viewmodel.EqViewModel
import kotlin.math.abs

// RETRO-FUTURISTIC 70s/80s COLOR PALETTE
private val RetroOrange = Color(0xFFFF6B35)  // 70s orange
private val RetroGreen = Color(0xFF00FF88)   // CRT green
private val RetroAmber = Color(0xFFFFB000)   // Amber display
private val RetroBlue = Color(0xFF00B4D8)    // Electric blue
private val RetroRed = Color(0xFFFF006E)     // Analog red
private val RetroBlack = Color(0xFF0A0A0A)   // Deep black
private val RetroGray = Color(0xFF2A2A2A)    // Dark gray
private val RetroGlow = Color(0xFF39FF14)    // Neon glow

enum class EqViewMode {
    CURVE,    // Frequency response curve
    MIXER     // Individual fader sliders
}

@Composable
fun EqScreen(
    viewModel: EqViewModel = hiltViewModel()
) {
    val currentBands by viewModel.currentBands.collectAsState()
    val originalMastering by viewModel.originalMastering.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    val isAutoEqActive by viewModel.isAutoEqActive.collectAsState()
    val hasSongProfile by viewModel.hasSongProfile.collectAsState()
    
    var viewMode by remember { mutableStateOf(EqViewMode.CURVE) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with retro VU meter styling
            RetroHeader(
                isEnabled = isEnabled,
                onEnabledChange = { viewModel.setEnabled(it) },
                isAutoEqActive = isAutoEqActive,
                onAutoEqToggle = { viewModel.toggleAutoEQ() },
                hasSongProfile = hasSongProfile,
                onSaveProfile = { viewModel.saveSongProfile() },
                onDeleteProfile = { viewModel.deleteSongProfile() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View mode toggle
            RetroViewModeToggle(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Main EQ display
            when (viewMode) {
                EqViewMode.CURVE -> {
                    RetroCurveView(
                        currentBands = currentBands,
                        originalMastering = originalMastering,
                        onBandChange = { index, value ->
                            viewModel.setBandValue(index, value)
                        }
                    )
                }
                EqViewMode.MIXER -> {
                    RetroMixerView(
                        currentBands = currentBands,
                        originalMastering = originalMastering,
                        onBandChange = { index, value ->
                            viewModel.setBandValue(index, value)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RetroHeader(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    isAutoEqActive: Boolean,
    onAutoEqToggle: () -> Unit,
    hasSongProfile: Boolean,
    onSaveProfile: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    Column {
        // Title with 70s styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EQUALIZER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = RetroOrange
            )
            
            // Power switch
            RetroSwitch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange,
                label = "POWER"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Control panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(RetroGray)
                .border(2.dp, RetroOrange, RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Auto EQ button
            RetroButton(
                text = "AUTO",
                isActive = isAutoEqActive,
                onClick = onAutoEqToggle,
                color = RetroGreen
            )
            
            // Save profile button
            RetroButton(
                text = "SAVE",
                isActive = hasSongProfile,
                onClick = onSaveProfile,
                color = RetroAmber
            )
            
            // Delete profile button
            if (hasSongProfile) {
                RetroButton(
                    text = "DELETE",
                    isActive = false,
                    onClick = onDeleteProfile,
                    color = RetroRed
                )
            }
        }
    }
}

@Composable
fun RetroSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(RetroGray)
            .border(1.dp, if (checked) RetroGreen else Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (checked) RetroGreen else Color.Gray
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .size(32.dp, 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (checked) RetroGreen.copy(alpha = 0.3f) else Color.DarkGray)
                .border(1.dp, if (checked) RetroGreen else Color.Gray, RoundedCornerShape(10.dp))
                .pointerInput(Unit) {
                    detectTapGestures { onCheckedChange(!checked) }
                },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (checked) RetroGreen else Color.Gray)
            )
        }
    }
}

@Composable
fun RetroButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) color.copy(alpha = 0.3f) else RetroGray)
            .border(
                2.dp,
                if (isActive) color else Color.Gray,
                RoundedCornerShape(4.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = if (isActive) color else Color.Gray
        )
    }
}

@Composable
fun RetroViewModeToggle(
    viewMode: EqViewMode,
    onViewModeChange: (EqViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(RetroGray)
            .border(2.dp, RetroAmber, RoundedCornerShape(4.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(EqViewMode.CURVE to "CURVE", EqViewMode.MIXER to "MIXER").forEach { (mode, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (viewMode == mode) RetroAmber.copy(alpha = 0.3f) else Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures { onViewModeChange(mode) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = if (viewMode == mode) RetroAmber else Color.Gray
                )
            }
        }
    }
}

@Composable
fun RetroCurveView(
    currentBands: List<EqBand>,
    originalMastering: List<EqBand>,
    onBandChange: (Int, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0A0A))
            .border(3.dp, RetroGreen, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val spacing = width / (currentBands.size - 1)
            
            // Draw grid lines (retro oscilloscope style)
            for (i in 0..10) {
                val y = (i / 10f) * height
                drawLine(
                    color = RetroGreen.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            
            for (i in currentBands.indices) {
                val x = i * spacing
                drawLine(
                    color = RetroGreen.copy(alpha = 0.1f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }
            
            // Draw zero line (original mastering baseline)
            drawLine(
                color = RetroAmber.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            
            // GHOST OVERLAY - Original mastering (always flat at center)
            val ghostPath = Path()
            ghostPath.moveTo(0f, centerY)
            for (i in originalMastering.indices) {
                val x = i * spacing
                val normalizedValue = originalMastering[i].value / 12f
                val y = centerY - (normalizedValue * (height / 2) * 0.9f)
                if (i == 0) ghostPath.moveTo(x, y) else ghostPath.lineTo(x, y)
            }
            
            drawPath(
                path = ghostPath,
                color = RetroGray.copy(alpha = 0.6f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            
            // Current EQ curve with glow effect
            val currentPath = Path()
            for (i in currentBands.indices) {
                val x = i * spacing
                val normalizedValue = currentBands[i].value / 12f
                val y = centerY - (normalizedValue * (height / 2) * 0.9f)
                if (i == 0) currentPath.moveTo(x, y) else currentPath.lineTo(x, y)
            }
            
            // Glow
            drawPath(
                path = currentPath,
                color = RetroGreen.copy(alpha = 0.4f),
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
            
            // Solid line
            drawPath(
                path = currentPath,
                color = RetroGreen,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            
            // Draw points
            currentBands.forEachIndexed { index, band ->
                val x = index * spacing
                val normalizedValue = band.value / 12f
                val y = centerY - (normalizedValue * (height / 2) * 0.9f)
                
                // Glow
                drawCircle(
                    color = RetroGreen.copy(alpha = 0.5f),
                    radius = 12f,
                    center = Offset(x, y)
                )
                
                // Point
                drawCircle(
                    color = RetroGreen,
                    radius = 6f,
                    center = Offset(x, y)
                )
                
                // Frequency label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(255, 176, 0)
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText("${band.frequency}", x, height - 10f, paint)
                }
                
                // dB value label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(0, 255, 136)
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(String.format("%+.1f", band.value), x, y - 20f, paint)
                }
            }
        }
    }
}

@Composable
fun RetroMixerView(
    currentBands: List<EqBand>,
    originalMastering: List<EqBand>,
    onBandChange: (Int, Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(RetroBlack)
            .border(3.dp, RetroOrange, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        currentBands.forEachIndexed { index, band ->
            RetroFader(
                frequency = band.frequency,
                value = band.value,
                ghostValue = originalMastering.getOrNull(index)?.value ?: 0f,
                onValueChange = { newValue ->
                    onBandChange(index, newValue)
                }
            )
        }
    }
}

@Composable
fun RetroFader(
    frequency: Int,
    value: Float,
    ghostValue: Float,
    onValueChange: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .width(32.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // dB value display
        Text(
            text = String.format("%+.0f", value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isDragging) RetroGlow else RetroGreen,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Fader track
        Box(
            modifier = Modifier
                .width(32.dp)
                .weight(1f)
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .background(RetroGray)
                    .border(1.dp, Color.Gray)
            )
            
            // Ghost fader position (original mastering)
            val ghostNormalized = (ghostValue + 12f) / 24f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.Center)
                    .offset(y = (-ghostNormalized * 100).dp)
                    .background(RetroGray.copy(alpha = 0.5f))
                    .border(1.dp, Color.DarkGray)
            )
            
            // Current fader position
            val normalized = (value + 12f) / 24f
            
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val maxHeightPx = constraints.maxHeight.toFloat()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.Center)
                        .offset(y = (-(normalized - 0.5f) * maxHeightPx / density).dp)
                        .background(
                            if (isDragging) RetroGlow else RetroOrange,
                            RoundedCornerShape(2.dp)
                        )
                        .border(2.dp, if (isDragging) Color.White else RetroAmber, RoundedCornerShape(2.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = { isDragging = false }
                            ) { change, dragAmount ->
                                change.consume()
                                val delta = -(dragAmount.y / maxHeightPx) * 24f
                                val newValue = (value + delta).coerceIn(-12f, 12f)
                                onValueChange(newValue)
                            }
                        }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency label
        Text(
            text = when {
                frequency >= 1000 -> "${frequency / 1000}k"
                else -> "$frequency"
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = RetroAmber,
            textAlign = TextAlign.Center
        )
    }
}
