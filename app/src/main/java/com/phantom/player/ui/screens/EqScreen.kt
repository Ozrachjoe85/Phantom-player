package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import com.phantom.player.ui.viewmodel.EqViewModel
import com.phantom.player.data.repository.EqBand
import kotlin.math.abs

// Cyberpunk Retro Color Palette
private val CyberGreen = Color(0xFF00FF88)
private val CyberOrange = Color(0xFFFF6B35)
private val CyberAmber = Color(0xFFFFB000)
private val CyberRed = Color(0xFFFF1744)
private val CyberBlue = Color(0xFF00D9FF)
private val CyberPurple = Color(0xFF9D4EDD)
private val GhostGray = Color(0xFF606060)
private val StudioBlack = Color(0xFF0A0A0A)

@Composable
fun EqScreen(
    viewModel: EqViewModel = hiltViewModel()
) {
    val currentBands by viewModel.currentBands.collectAsState()
    val isAutoEqActive by viewModel.isAutoEqActive.collectAsState()
    val originalMastering = remember { List(10) { EqBand(0, 0f, 1.0f) } }
    
    var selectedView by remember { mutableStateOf(ViewMode.CURVE) }
    var isPowerOn by remember { mutableStateOf(true) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBlack)
    ) {
        // Cyberpunk grid background
        CyberpunkEqGrid()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // HEADER: Power, Auto EQ, Save, Delete
            CyberpunkEqHeader(
                isPowerOn = isPowerOn,
                isAutoEqActive = isAutoEqActive,
                onPowerToggle = { isPowerOn = !isPowerOn },
                onAutoEqToggle = { viewModel.toggleAutoEQ() },
                onSave = { viewModel.saveSongProfile() },
                onDelete = { viewModel.deleteSongProfile() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // MAIN VISUALIZATION AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isPowerOn) {
                    when (selectedView) {
                        ViewMode.CURVE -> {
                            CyberpunkCurveView(
                                currentBands = currentBands,
                                originalMastering = originalMastering,
                                isAutoEqActive = isAutoEqActive,
                                onBandChange = { index, value ->
                                    viewModel.setBandValue(index, value)
                                }
                            )
                        }
                        ViewMode.MIXER -> {
                            CyberpunkMixerView(
                                currentBands = currentBands,
                                originalMastering = originalMastering,
                                isAutoEqActive = isAutoEqActive,
                                onBandChange = { index, value ->
                                    viewModel.setBandValue(index, value)
                                }
                            )
                        }
                    }
                } else {
                    PowerOffDisplay()
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // VIEW MODE TOGGLE
            CyberpunkViewToggle(
                selectedView = selectedView,
                onViewChange = { selectedView = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

enum class ViewMode {
    CURVE, MIXER
}

// ============================================================================
// CYBERPUNK EQ GRID BACKGROUND
// ============================================================================
@Composable
fun CyberpunkEqGrid() {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_grid")
    val scanline by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Grid lines
        for (i in 0..20) {
            val y = (i / 20f) * size.height
            drawLine(
                color = CyberPurple.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
        
        for (i in 0..10) {
            val x = (i / 10f) * size.width
            drawLine(
                color = CyberPurple.copy(alpha = 0.1f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
        }
        
        // Animated scanline
        val scanY = scanline * size.height
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyberGreen.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                startY = scanY - 50f,
                endY = scanY + 50f
            ),
            start = Offset(0f, scanY),
            end = Offset(size.width, scanY),
            strokeWidth = 2f
        )
    }
}

// ============================================================================
// HEADER: Power, Auto EQ, Save, Delete
// ============================================================================
@Composable
fun CyberpunkEqHeader(
    isPowerOn: Boolean,
    isAutoEqActive: Boolean,
    onPowerToggle: () -> Unit,
    onAutoEqToggle: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF0A0A0A)
                    )
                )
            )
            .border(2.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // POWER
        CyberpunkHeaderButton(
            label = "POWER",
            isActive = isPowerOn,
            activeColor = CyberRed,
            onClick = onPowerToggle
        )
        
        // AUTO EQ
        CyberpunkHeaderButton(
            label = "AUTO EQ",
            isActive = isAutoEqActive,
            activeColor = CyberGreen,
            onClick = onAutoEqToggle
        )
        
        // SAVE
        CyberpunkHeaderButton(
            label = "SAVE",
            isActive = false,
            activeColor = CyberAmber,
            onClick = onSave
        )
        
        // DELETE
        CyberpunkHeaderButton(
            label = "DELETE",
            isActive = false,
            activeColor = CyberRed,
            onClick = onDelete
        )
    }
}

@Composable
fun CyberpunkHeaderButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // LED indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        activeColor.copy(alpha = glowAlpha)
                    } else {
                        GhostGray.copy(alpha = 0.3f)
                    }
                )
                .border(
                    1.dp,
                    if (isActive) activeColor else GhostGray.copy(alpha = 0.5f),
                    CircleShape
                )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            ),
            color = if (isActive) activeColor else GhostGray,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// CURVE VIEW: Oscilloscope-style with ghost overlay
// ============================================================================
@Composable
fun CyberpunkCurveView(
    currentBands: List<EqBand>,
    originalMastering: List<EqBand>,
    isAutoEqActive: Boolean,
    onBandChange: (Int, Float) -> Unit
) {
    // Animate bands when Auto EQ activates
    val animatedBands = currentBands.mapIndexed { index, band ->
        val targetValue by animateFloatAsState(
            targetValue = band.value,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "band_$index"
        )
        EqBand(band.frequency, targetValue, band.q)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioBlack)
            .border(3.dp, CyberGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val spacing = width / (currentBands.size - 1)
            
            // Draw grid (oscilloscope style)
            for (i in 0..12) {
                val y = (i / 12f) * height
                val alpha = if (i == 6) 0.3f else 0.1f  // Center line brighter
                drawLine(
                    color = CyberGreen.copy(alpha = alpha),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = if (i == 6) 2f else 1f
                )
            }
            
            for (i in currentBands.indices) {
                val x = i * spacing
                drawLine(
                    color = CyberGreen.copy(alpha = 0.1f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }
            
            // GHOST OVERLAY: Original mastering (flat at 0dB)
            val ghostPath = Path()
            ghostPath.moveTo(0f, centerY)
            ghostPath.lineTo(width, centerY)
            
            drawPath(
                path = ghostPath,
                color = GhostGray.copy(alpha = 0.6f),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f)
                    )
                )
            )
            
            // CURRENT EQ CURVE with glow
            val currentPath = Path()
            animatedBands.forEachIndexed { index, band ->
                val x = index * spacing
                val normalizedValue = band.value / 12f
                val y = centerY - (normalizedValue * (height / 2) * 0.9f)
                
                if (index == 0) {
                    currentPath.moveTo(x, y)
                } else {
                    // Bezier curve for smooth transitions
                    val prevX = (index - 1) * spacing
                    val prevBand = animatedBands[index - 1]
                    val prevNormalized = prevBand.value / 12f
                    val prevY = centerY - (prevNormalized * (height / 2) * 0.9f)
                    
                    val controlX1 = prevX + spacing * 0.5f
                    val controlX2 = x - spacing * 0.5f
                    
                    currentPath.cubicTo(
                        controlX1, prevY,
                        controlX2, y,
                        x, y
                    )
                }
            }
            
            // Glow layer
            drawPath(
                path = currentPath,
                color = CyberGreen.copy(alpha = 0.4f),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
            
            // Solid curve
            drawPath(
                path = currentPath,
                color = CyberGreen,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            
            // Draw control points
            animatedBands.forEachIndexed { index, band ->
                val x = index * spacing
                val normalizedValue = band.value / 12f
                val y = centerY - (normalizedValue * (height / 2) * 0.9f)
                
                // Outer glow
                drawCircle(
                    color = CyberGreen.copy(alpha = 0.5f),
                    radius = 16f,
                    center = Offset(x, y)
                )
                
                // Inner point
                drawCircle(
                    color = CyberGreen,
                    radius = 8f,
                    center = Offset(x, y)
                )
                
                // Center dot
                drawCircle(
                    color = StudioBlack,
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }
        
        // Frequency labels at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            currentBands.forEach { band ->
                val freqText = when {
                    band.frequency < 1000 -> "${band.frequency}"
                    else -> "${band.frequency / 1000}k"
                }
                Text(
                    text = freqText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CyberAmber,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// MIXER VIEW: Studio faders with animated ghost positions
// ============================================================================
@Composable
fun CyberpunkMixerView(
    currentBands: List<EqBand>,
    originalMastering: List<EqBand>,
    isAutoEqActive: Boolean,
    onBandChange: (Int, Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioBlack)
            .border(3.dp, CyberOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        currentBands.forEachIndexed { index, band ->
            CyberpunkFader(
                frequency = band.frequency,
                value = band.value,
                ghostValue = 0f,  // Original mastering at 0dB
                isAutoEqActive = isAutoEqActive,
                onValueChange = { newValue ->
                    onBandChange(index, newValue)
                }
            )
        }
    }
}

@Composable
fun CyberpunkFader(
    frequency: Int,
    value: Float,
    ghostValue: Float,
    isAutoEqActive: Boolean,
    onValueChange: (Float) -> Unit
) {
    // Animate fader when Auto EQ activates
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fader_$frequency"
    )
    
    val normalized = (animatedValue + 12f) / 24f  // -12 to +12 → 0 to 1
    val ghostNormalized = (ghostValue + 12f) / 24f
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(32.dp)
    ) {
        // dB value display
        Text(
            text = String.format("%+.1f", animatedValue),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = if (animatedValue > 0) CyberOrange else if (animatedValue < 0) CyberBlue else CyberGreen,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Fader track
        Box(
            modifier = Modifier
                .width(8.dp)
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, CyberOrange.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        val delta = -dragAmount / size.height * 24f
                        val newValue = (value + delta).coerceIn(-12f, 12f)
                        onValueChange(newValue)
                    }
                }
        ) {
            // Ghost position indicator (0dB line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopStart)
                    .offset(y = ((1f - ghostNormalized) * this@Box.constraints.maxHeight / density.density).dp)
                    .background(GhostGray.copy(alpha = 0.6f))
            )
            
            // Current fader knob
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.TopStart)
                    .offset(y = ((1f - normalized) * (this@Box.constraints.maxHeight - 24.dp.toPx()) / density.density).dp)
            ) {
                // Knob glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(CyberOrange.copy(alpha = 0.5f))
                )
                
                // Knob body
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyberOrange,
                                    CyberOrange.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .border(1.dp, CyberAmber, CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency label
        val freqText = when {
            frequency < 1000 -> "$frequency"
            else -> "${frequency / 1000}k"
        }
        Text(
            text = freqText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = CyberAmber,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// VIEW MODE TOGGLE
// ============================================================================
@Composable
fun CyberpunkViewToggle(
    selectedView: ViewMode,
    onViewChange: (ViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .border(2.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ViewMode.values().forEach { mode ->
            val isSelected = selectedView == mode
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CyberOrange,
                                    CyberPurple
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            )
                        }
                    )
                    .clickable { onViewChange(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    ),
                    color = if (isSelected) StudioBlack else GhostGray
                )
            }
        }
    }
}

// ============================================================================
// POWER OFF DISPLAY
// ============================================================================
@Composable
fun PowerOffDisplay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(StudioBlack)
            .border(3.dp, GhostGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SYSTEM OFFLINE",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            ),
            color = GhostGray.copy(alpha = 0.3f)
        )
    }
}
