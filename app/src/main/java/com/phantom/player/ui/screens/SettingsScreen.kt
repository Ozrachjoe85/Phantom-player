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
import com.phantom.player.ui.theme.*

@Composable
fun SettingsScreen() {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = ElectricBlue
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                SettingsSection(title = "AUDIO")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.GraphicEq,
                    title = "Audio Engine",
                    subtitle = "Maximum buffer size",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.HighQuality,
                    title = "Audio Quality",
                    subtitle = "24-bit / 192kHz",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "PLAYBACK")
            }
            
            item {
                var gapless by remember { mutableStateOf(true) }
                SettingsSwitchItem(
                    icon = Icons.Default.QueueMusic,
                    title = "Gapless Playback",
                    subtitle = "Seamless track transitions",
                    checked = gapless,
                    onCheckedChange = { gapless = it }
                )
            }
            
            item {
                var autoPlay by remember { mutableStateOf(true) }
                SettingsSwitchItem(
                    icon = Icons.Default.PlayCircle,
                    title = "Auto-Play",
                    subtitle = "Continue to next track",
                    checked = autoPlay,
                    onCheckedChange = { autoPlay = it }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "APPEARANCE")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Liquid Metal Holographic",
                    onClick = { }
                )
            }
            
            item {
                var animations by remember { mutableStateOf(true) }
                SettingsSwitchItem(
                    icon = Icons.Default.Animation,
                    title = "Animations",
                    subtitle = "Particle effects & transitions",
                    checked = animations,
                    onCheckedChange = { animations = it }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "LIBRARY")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.FolderOpen,
                    title = "Scan Music",
                    subtitle = "Update library",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Storage",
                    subtitle = "Manage cache & data",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "ABOUT")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "Phantom Player 1.0.0",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        ),
        color = HoloCyan.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x11001233))
            .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            ElectricBlue.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ChromeLight
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HoloCyan.copy(alpha = 0.6f)
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MetallicSilver.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x11001233))
            .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (checked) ElectricBlue.copy(alpha = 0.3f) else Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
                .border(
                    1.dp,
                    if (checked) ElectricBlue else MetallicSilver.copy(alpha = 0.3f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) ElectricBlue else MetallicSilver.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ChromeLight
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HoloCyan.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElectricBlue,
                checkedTrackColor = ElectricBlue.copy(alpha = 0.5f),
                uncheckedThumbColor = MetallicSilver,
                uncheckedTrackColor = Color(0xFF001233)
            )
        )
    }
}
