package com.phantom.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.phantom.player.ui.viewmodel.LibraryViewModel
import com.phantom.player.ui.viewmodel.PlayerViewModel
import com.phantom.player.ui.viewmodel.EqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavHostController,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    eqViewModel: EqViewModel = hiltViewModel()
) {
    val songs by libraryViewModel.songs.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library (${songs.size} songs)") })
        }
    ) { padding ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No music found. Add music to your device.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(songs) { song ->
                    ListItem(
                        headlineContent = { Text(song.title) },
                        supportingContent = { Text(song.artist) },
                        modifier = Modifier.clickable {
                            // Play the song
                            playerViewModel.playSong(song)
                            
                            // Update EQ for this song
                            eqViewModel.setCurrentSong(song.id)
                            
                            // Navigate to player
                            navController.navigate("player")
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
