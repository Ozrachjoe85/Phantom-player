package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.data.local.database.entities.EqBand
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.EqViewModel
import kotlin.math.abs
import kotlin.math.pow

enum class EqViewMode {
    CURVE, FADERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqScreen(
    viewModel: EqViewModel = hiltViewModel()
) {
    val bands by viewModel.bands.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    var viewMode by remember { mutableStateOf(EqViewMode.CURVE) }
    var autoEqEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.initialize(0)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Control Panel
            EqControlPanel(
                isEnabled = isEnabled,
                onEnabledChange = { viewModel.setEnabled(it) },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                autoEqEnabled = autoEqEnabled,
                onAutoEqChange = { autoEqEnabled = it },
                onReset = { viewModel.resetAllBands() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bands.isNotEmpty()) {
                when (viewMode) {
                    EqViewMode.CURVE -> {
                        FrequencyCurveView(
                            bands = bands,
                            autoEqEnabled = autoEqEnabled,
                            onBandValueChange = { index, value ->
                                viewModel.setBandValue(index, value)
                            }
                        )
                    }
                    EqViewMode.FADERS -> {
                        FadersView(
                            bands = bands,
                            autoEqEnabled = autoEqEnabled,
                            onBandValueChange = { index, value ->
                                viewModel.setBandValue(index, value)
                            }
                        )
                    }
                }
            } else {
                InitializingEqState()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preset Panel
            EqPresetPanel(viewModel = viewModel)
        }
    }
}

@Composable
fun EqControlPanel(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    viewMode: EqViewMode,
    onViewModeChange: (EqViewMode) -> Unit,
    autoEqEnabled: Boolean,
    onAutoEqChange: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.4f),
                        SurfaceGlass.copy(alpha = 0.2f)
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
            .padding(16.dp)
    ) {
        Column(spacing = 12.dp) {
            // Power and Mode Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Power Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "EQUALIZER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = if (isEnabled) PhantomCyan else PhantomWhite.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    PowerSwitch(
                        isOn = isEnabled,
                        onToggle = onEnabledChange
                    )
                }
                
                // Reset Button
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, "Reset", tint = PhantomPink)
                }
            }
            
            // View Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeToggleButton(
                    label = "CURVE",
                    icon = Icons.Default.ShowChart,
                    isSelected = viewMode == EqViewMode.CURVE,
                    onClick = { onViewModeChange(EqViewMode.CURVE) },
                    modifier = Modifier.weight(1f)
                )
                
                ModeToggleButton(
                    label = "FADERS",
                    icon = Icons.Default.Tune,
                    isSelected = viewMode == EqViewMode.FADERS,
                    onClick = { onViewModeChange(EqViewMode.FADERS) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Auto EQ Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (autoEqEnabled) {
                            Brush.horizontalGradient(
                                listOf(PhantomPurple.copy(alpha = 0.3f), PhantomPink.copy(alpha = 0.3f))
                            )
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .border(
                        1.dp,
                        if (autoEqEnabled) PhantomPink else PhantomPurple.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onAutoEqChange(!autoEqEnabled) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        tint = if (autoEqEnabled) PhantomPink else PhantomPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AUTO EQ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (autoEqEnabled) PhantomWhite else PhantomPurple
                    )
                }
                
                Switch(
                    checked = autoEqEnabled,
                    onCheckedChange = onAutoEqChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PhantomPink,
                        checkedTrackColor = PhantomPurple.copy(alpha = 0.5f),
                        uncheckedThumbColor = PhantomPurple,
                        uncheckedTrackColor = PhantomDarkPurple
                    )
                )
            }
        }
    }
}

@Composable
fun PowerSwitch(isOn: Boolean, onToggle: (Boolean) -> Unit) {
    val glowAnimation = rememberInfiniteTransition(label = "power_glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isOn) {
                    Brush.radialGradient(
                        listOf(
                            PhantomCyan.copy(alpha = glowAlpha),
                            PhantomCyan.copy(alpha = 0.3f)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        listOf(
                            PhantomPurple.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                }
            )
            .border(2.dp, if (isOn) PhantomCyan else PhantomPurple.copy(alpha = 0.5f), CircleShape)
            .clickable { onToggle(!isOn) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Power,
            contentDescription = "Power",
            tint = if (isOn) PhantomCyan else PhantomPurple.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ModeToggleButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.3f))
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .border(
                1.dp,
                if (isSelected) PhantomCyan else PhantomPurple.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = if (isSelected) PhantomCyan else PhantomPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (isSelected) PhantomWhite else PhantomPurple
            )
        }
    }
}

@Composable
fun FrequencyCurveView(
    bands: List<EqBand>,
    autoEqEnabled: Boolean,
    onBandValueChange: (Int, Float) -> Unit
) {
    // Simulate Auto EQ target curve
    val autoEqBands = remember {
        listOf(
            EqBand(31, 3.5f),
            EqBand(62, 2.0f),
            EqBand(125, 0f),
            EqBand(250, -1.5f),
            EqBand(500, -0.5f),
            EqBand(1000, 1.0f),
            EqBand(2000, 2.5f),
            EqBand(4000, 1.5f),
            EqBand(8000, 0.5f),
            EqBand(16000, -1.0f)
        )
    }
    
    // Animated transition for Auto EQ
    val animatedBands = bands.mapIndexed { index, band ->
        val targetValue = if (autoEqEnabled && index < autoEqBands.size) {
            autoEqBands[index].value
        } else {
            band.value
        }
        
        val animatedValue by animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(800, easing = FastOutSlowInEasing),
            label = "eq_band_$index"
        )
        
        band.copy(value = animatedValue)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PhantomBlack)
            .border(2.dp, EquipmentMetal, RoundedCornerShape(20.dp))
    ) {
        // Oscilloscope Grid Background
        OscilloscopeGrid()
        
        // Original/Stock EQ Curve (dashed)
        if (autoEqEnabled) {
            FrequencyCurvePath(
                bands = bands,
                color = PhantomPink.copy(alpha = 0.5f),
                isDashed = true,
                label = "ORIGINAL"
            )
        }
        
        // Auto EQ Target Curve (dashed)
        if (autoEqEnabled) {
            FrequencyCurvePath(
                bands = autoEqBands,
                color = PhantomPurple.copy(alpha = 0.6f),
                isDashed = true,
                label = "AUTO EQ TARGET"
            )
        }
        
        // Current EQ Curve (solid, interactive)
        FrequencyCurvePath(
            bands = animatedBands,
            color = PhantomCyan,
            isDashed = false,
            label = "CURRENT EQ",
            onBandValueChange = if (!autoEqEnabled) onBandValueChange else null
        )
        
        // Legend
        CurveLegend(autoEqEnabled = autoEqEnabled)
    }
}

@Composable
fun BoxScope.OscilloscopeGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Horizontal grid lines
        for (i in 0..4) {
            val y = (height / 4) * i
            val alpha = if (i == 2) 0.4f else 0.15f
            drawLine(
                color = PhantomCyan.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = if (i == 2) 2f else 1f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 10f)
                )
            )
        }
        
        // Vertical grid lines
        for (i in 0..10) {
            val x = (width / 10) * i
            drawLine(
                color = PhantomPurple.copy(alpha = 0.1f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 10f)
                )
            )
        }
    }
}
        
        // dB labels
        val dbLabels = listOf("+12dB", "+6dB", "0dB", "-6dB", "-12dB")
        dbLabels.forEachIndexed { index, label ->
            drawContext.canvas.nativeCanvas.apply {
                val y = (height / 4) * index
                drawText(
                    label,
                    16f,
                    y + 5f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#9D4EDD")
                        textSize = 24f
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

@Composable
fun BoxScope.FrequencyCurvePath(
    bands: List<EqBand>,
    color: Color,
    isDashed: Boolean,
    label: String,
    onBandValueChange: ((Int, Float) -> Unit)? = null
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 60.dp, end = 20.dp, top = 20.dp, bottom = 40.dp)
            .then(
                if (onBandValueChange != null) {
                    Modifier.pointerInput(bands) {
                        detectDragGestures { change, _ ->
                            val x = change.position.x
                            val y = change.position.y
                            
                            val index = ((x / size.width) * bands.size).toInt().coerceIn(0, bands.size - 1)
                            val dbValue = 12f - ((y / size.height) * 24f)
                            val clampedValue = dbValue.coerceIn(-12f, 12f)
                            
                            onBandValueChange(index, clampedValue)
                        }
                    }
                } else Modifier
            )
    ) {
        if (bands.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val stepX = width / (bands.size - 1)
        
        // Draw filled area under curve
        val path = Path().apply {
            moveTo(0f, height)
            
            bands.forEachIndexed { index, band ->
                val x = index * stepX
                val normalizedValue = (band.value + 12f) / 24f
                val y = height - (normalizedValue * height)
                
                if (index == 0) {
                    lineTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            
            lineTo(width, height)
            close()
        }
        
        // Fill gradient
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )
        
        // Draw curve line
        val curvePath = Path().apply {
            bands.forEachIndexed { index, band ->
                val x = index * stepX
                val normalizedValue = (band.value + 12f) / 24f
                val y = height - (normalizedValue * height)
                
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        drawPath(
            path = curvePath,
            color = color,
            style = Stroke(
                width = 3f,
                cap = StrokeCap.Round,
                pathEffect = if (isDashed) {
                    androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        intervals = floatArrayOf(15f, 10f)
                    )
                } else null
            )
        )
        
        // Draw control points (only for current EQ)
        if (!isDashed && onBandValueChange != null) {
            bands.forEachIndexed { index, band ->
                val x = index * stepX
                val normalizedValue = (band.value + 12f) / 24f
                val y = height - (normalizedValue * height)
                
                // Outer glow
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = 12f,
                    center = Offset(x, y)
                )
                
                // Inner dot
                drawCircle(
                    color = color,
                    radius = 6f,
                    center = Offset(x, y)
                )
                
                // Center dot
                drawCircle(
                    color = PhantomBlack,
                    radius = 3f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun BoxScope.CurveLegend(autoEqEnabled: Boolean) {
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PhantomBlack.copy(alpha = 0.7f))
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        LegendItem("CURRENT EQ", PhantomCyan, false)
        if (autoEqEnabled) {
            LegendItem("AUTO TARGET", PhantomPurple, true)
            LegendItem("ORIGINAL", PhantomPink, true)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, isDashed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 20.dp, height = 2.dp)) {
            if (isDashed) {
                drawLine(
                    color = color,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        intervals = floatArrayOf(5f, 5f)
                    )
                )
            } else {
                drawLine(
                    color = color,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 3f
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp
            ),
            color = color
        )
    }
}

@Composable
fun FadersView(
    bands: List<EqBand>,
    autoEqEnabled: Boolean,
    onBandValueChange: (Int, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(EquipmentMetal, PhantomBlack)
                )
            )
            .border(2.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bands.forEachIndexed { index, band ->
                EqFader(
                    frequency = band.frequency,
                    value = band.value,
                    autoEqEnabled = autoEqEnabled,
                    onValueChange = { value ->
                        if (!autoEqEnabled) {
                            onBandValueChange(index, value)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EqFader(
    frequency: Int,
    value: Float,
    autoEqEnabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(32.dp)
    ) {
        // Value Display
        Text(
            text = String.format("%+.1f", value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = PhantomCyan,
            modifier = Modifier.height(20.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Fader Channel
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PhantomBlack)
                .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            // Center line (0dB)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.Center)
                    .background(PhantomPurple.copy(alpha = 0.5f))
            )
            
            // Fill indicator
            val normalizedValue = (value + 12f) / 24f
            val fillHeight = normalizedValue.coerceIn(0f, 1f)
            
            if (fillHeight > 0.5f) {
                // Above center - fill from center up
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight((fillHeight - 0.5f) * 2f)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    PhantomCyan.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                // Below center - fill from center down
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight((0.5f - fillHeight) * 2f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    PhantomPink.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }
            
            // Fader Knob
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(40.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = ((1f - normalizedValue) * 160).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PhantomCyan.copy(alpha = 0.5f),
                                PhantomPurple.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .border(1.dp, PhantomCyan, RoundedCornerShape(4.dp))
                    .pointerInput(Unit) {
                        if (!autoEqEnabled) {
                            detectDragGestures { change, dragAmount ->
                                val newValue = value - (dragAmount.y / 10f)
                                onValueChange(newValue.coerceIn(-12f, 12f))
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .background(PhantomCyan)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Frequency Label
        Text(
            text = formatFrequency(frequency),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = PhantomPurple
        )
    }
}

@Composable
fun EqPresetPanel(viewModel: EqViewModel) {
    val presets = listOf("BASS BOOST", "VOCAL", "TREBLE", "FLAT", "ROCK", "POP")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EquipmentMetal)
            .border(1.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                "PRESETS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomCyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(3).forEach { preset ->
                    PresetButton(
                        label = preset,
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.drop(3).forEach { preset ->
                    PresetButton(
                        label = preset,
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PhantomDarkPurple.copy(alpha = 0.5f),
                        PhantomMidPurple.copy(alpha = 0.3f)
                    )
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = PhantomWhite
        )
    }
}

@Composable
fun InitializingEqState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = PhantomCyan,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "INITIALIZING EQUALIZER...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomCyan.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatFrequency(frequency: Int): String {
    return if (frequency >= 1000) {
        "${frequency / 1000}k"
    } else {
        "$frequency"
    }
}


