package com.phantom.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    var gaplessPlayback by remember { mutableStateOf(true) }
    var globalEq by remember { mutableStateOf(true) }
    var fadeInDuration by remember { mutableStateOf(2f) }
    var fadeOutDuration by remember { mutableStateOf(2f) }
    var replayGain by remember { mutableStateOf(false) }
    var headphoneMode by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            item {
                AppInfoHeader()
            }
            
            // Playback Section
            item {
                SettingsSection(title = "PLAYBACK") {
                    SettingToggle(
                        icon = Icons.Default.SkipNext,
                        title = "Gapless Playback",
                        description = "Seamless transition between tracks",
                        checked = gaplessPlayback,
                        onCheckedChange = { gaplessPlayback = it }
                    )
                    
                    SettingSlider(
                        icon = Icons.Default.MusicNote,
                        title = "Fade In Duration",
                        description = "${fadeInDuration.toInt()} seconds",
                        value = fadeInDuration,
                        onValueChange = { fadeInDuration = it },
                        valueRange = 0f..5f
                    )
                    
                    SettingSlider(
                        icon = Icons.Default.MusicNote,
                        title = "Fade Out Duration",
                        description = "${fadeOutDuration.toInt()} seconds",
                        value = fadeOutDuration,
                        onValueChange = { fadeOutDuration = it },
                        valueRange = 0f..5f
                    )
                    
                    SettingToggle(
                        icon = Icons.Default.Equalizer,
                        title = "Replay Gain",
                        description = "Normalize volume across tracks",
                        checked = replayGain,
                        onCheckedChange = { replayGain = it }
                    )
                }
            }
            
            // Audio Section
            item {
                SettingsSection(title = "AUDIO") {
                    SettingToggle(
                        icon = Icons.Default.Tune,
                        title = "Global EQ Control",
                        description = "Apply EQ to all audio sources",
                        checked = globalEq,
                        onCheckedChange = { globalEq = it }
                    )
                    
                    SettingToggle(
                        icon = Icons.Default.Headphones,
                        title = "Headphone Mode",
                        description = "Optimize audio for headphones",
                        checked = headphoneMode,
                        onCheckedChange = { headphoneMode = it }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.AudioFile,
                        title = "Audio Buffer Size",
                        description = "Normal (Default)",
                        onClick = { }
                    )
                }
            }
            
            // Library Section
            item {
                SettingsSection(title = "LIBRARY") {
                    SettingButton(
                        icon = Icons.Default.Refresh,
                        title = "Rescan Library",
                        description = "Scan device for new music files",
                        onClick = { libraryViewModel.scanLibrary() }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.FolderOpen,
                        title = "Library Folders",
                        description = "Manage scanned folders",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Delete,
                        title = "Clear Library Cache",
                        description = "Remove cached album art and metadata",
                        onClick = { },
                        isDestructive = true
                    )
                }
            }
            
            // Interface Section
            item {
                SettingsSection(title = "INTERFACE") {
                    SettingButton(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        description = "Cyberpunk (Default)",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Speed,
                        title = "Animations",
                        description = "Enabled",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Fullscreen,
                        title = "Now Playing View",
                        description = "Full Screen",
                        onClick = { }
                    )
                }
            }
            
            // Advanced Section
            item {
                SettingsSection(title = "ADVANCED") {
                    SettingButton(
                        icon = Icons.Default.Storage,
                        title = "Cache Management",
                        description = "125 MB used",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.DataUsage,
                        title = "Network Settings",
                        description = "Album art quality, streaming",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Build,
                        title = "Developer Options",
                        description = "Debug logs, performance stats",
                        onClick = { }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(title = "ABOUT") {
                    SettingButton(
                        icon = Icons.Default.Info,
                        title = "Version",
                        description = "Phantom Player 1.0.0",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Code,
                        title = "Open Source Licenses",
                        description = "View third-party software",
                        onClick = { }
                    )
                    
                    SettingButton(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        description = "Data collection and usage",
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun AppInfoHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PhantomCyan.copy(alpha = 0.2f),
                        PhantomPurple.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(PhantomCyan.copy(alpha = 0.6f), PhantomPurple.copy(alpha = 0.4f))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Icon Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomCyan, PhantomPurple)
                        )
                    )
                    .border(3.dp, PhantomCyan, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = PhantomBlack,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "PHANTOM PLAYER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                ),
                color = PhantomCyan
            )
            
            Text(
                "NEXT-GEN AUDIO EXPERIENCE",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                ),
                color = PhantomPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 1.sp
                ),
                color = PhantomWhite.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = PhantomCyan,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        
        Box(
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
                    PhantomPurple.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingToggle(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(
                        listOf(
                            PhantomCyan.copy(alpha = 0.15f),
                            PhantomPurple.copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable { onCheckedChange(!checked) }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) PhantomCyan else PhantomPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = PhantomWhite
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 0.3.sp
                    ),
                    color = PhantomPurple.copy(alpha = 0.8f)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PhantomCyan,
                    checkedTrackColor = PhantomPurple.copy(alpha = 0.5f),
                    uncheckedThumbColor = PhantomPurple.copy(alpha = 0.6f),
                    uncheckedTrackColor = PhantomDarkPurple
                )
            )
        }
    }
}

@Composable
fun SettingSlider(
    icon: ImageVector,
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PhantomPurple.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PhantomPurple.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = PhantomWhite
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 0.3.sp
                        ),
                        color = PhantomCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = PhantomCyan,
                    activeTrackColor = PhantomCyan,
                    inactiveTrackColor = PhantomPurple.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp)
            )
        }
    }
}

@Composable
fun SettingButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDestructive) {
                    Brush.horizontalGradient(
                        listOf(
                            PhantomPink.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) {
                    PhantomPink.copy(alpha = 0.8f)
                } else {
                    PhantomPurple.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isDestructive) PhantomPink else PhantomWhite
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 0.3.sp
                    ),
                    color = PhantomPurple.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PhantomPurple.copy(alpha = 0.5f)
            )
        }
    }
}
