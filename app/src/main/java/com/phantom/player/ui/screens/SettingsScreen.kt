package com.phantom.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Info Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Phantom Player",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Library Section
            Text(
                text = "Library",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Rescan Library") },
                supportingContent = { Text("Scan device for new music files") },
                leadingContent = {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            ListItem(
                headlineContent = { Text("Clear Library") },
                supportingContent = { Text("Remove all songs from database") },
                leadingContent = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Audio Section
            Text(
                text = "Audio",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Gapless Playback") },
                supportingContent = { Text("Seamless transition between tracks") },
                leadingContent = {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                },
                trailingContent = {
                    Switch(checked = true, onCheckedChange = {})
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            ListItem(
                headlineContent = { Text("Fade Duration") },
                supportingContent = { Text("Crossfade time: 2 seconds") },
                leadingContent = {
                    Icon(Icons.Default.Timer, contentDescription = null)
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
