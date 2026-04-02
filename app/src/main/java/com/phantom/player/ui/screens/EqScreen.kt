package com.phantom.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.ui.viewmodel.EqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqScreen(
    viewModel: EqViewModel = hiltViewModel()
) {
    val bands by viewModel.currentBands.collectAsState()
    val isAutoEqActive by viewModel.isAutoEqActive.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                actions = {
                    Switch(
                        checked = isAutoEqActive,
                        onCheckedChange = { viewModel.toggleAutoEq() }
                    )
                    Text("Auto EQ")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            bands.forEach { band ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${band.frequency}Hz")
                    Slider(
                        value = band.value,
                        onValueChange = { newValue ->
                            viewModel.setBandValue(band.frequency, newValue)
                        },
                        valueRange = -12f..12f,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                    Text(String.format("%+.1f dB", band.value))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSongProfile() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                
                Button(
                    onClick = { viewModel.resetToFlat() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
