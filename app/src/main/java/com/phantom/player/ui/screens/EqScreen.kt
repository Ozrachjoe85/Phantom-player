package com.phantom.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phantom.player.ui.viewmodel.EqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqScreen(
    viewModel: EqViewModel = hiltViewModel()
) {
    val bands by viewModel.bands.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Reset Button
            OutlinedButton(
                onClick = { viewModel.resetAllBands() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Bands")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // EQ Bands
            if (bands.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(bands) { index, band ->
                        EqBandControl(
                            frequency = band.frequency,
                            value = band.value,
                            onValueChange = { newValue ->
                                viewModel.setBandValue(index, newValue)
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Equalizer not initialized",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EqBandControl(
    frequency: Int,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatFrequency(frequency),
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = String.format("%.1f dB", value),
                style = MaterialTheme.typography.labelMedium
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -12f..12f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatFrequency(frequency: Int): String {
    return if (frequency >= 1000) {
        "${frequency / 1000}kHz"
    } else {
        "${frequency}Hz"
    }
}
