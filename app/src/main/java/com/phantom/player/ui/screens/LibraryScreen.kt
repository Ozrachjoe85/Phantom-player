package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.ui.theme.*
import com.phantom.player.ui.viewmodel.LibraryViewModel
import com.phantom.player.ui.viewmodel.PlayerViewModel

enum class LibraryView {
    SONGS, ALBUMS, ARTISTS, PLAYLISTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val songs by libraryViewModel.songs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val scanStatus by libraryViewModel.scanStatus.collectAsState()
    
    var currentView by remember { mutableStateOf(LibraryView.SONGS) }
    var searchQuery by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Glass Panel with Search and View Selector
            GlassPanelHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                currentView = currentView,
                onViewChange = { currentView = it },
                onScanClick = { libraryViewModel.scanLibrary() },
                isScanning = isScanning
            )
            
            // Scan Status Banner
            scanStatus?.let { status ->
                ScanStatusBanner(
                    status = status,
                    isScanning = isScanning,
                    onDismiss = { libraryViewModel.clearScanStatus() }
                )
            }
            
            // Content Area
            when (currentView) {
                LibraryView.SONGS -> SongsView(
                    songs = songs.filter { 
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
                    },
                    onSongClick = { song -> playerViewModel.play(song) },
                    onPlayAll = { playerViewModel.setPlaylist(songs) }
                )
                LibraryView.ALBUMS -> AlbumsView(songs = songs)
                LibraryView.ARTISTS -> ArtistsView(songs = songs)
                LibraryView.PLAYLISTS -> PlaylistsView()
            }
        }
        
        // Empty State
        if (songs.isEmpty() && !isScanning) {
            EmptyLibraryState(
                onScanClick = { libraryViewModel.scanLibrary() }
            )
        }
    }
}

@Composable
fun GlassPanelHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentView: LibraryView,
    onViewChange: (LibraryView) -> Unit,
    onScanClick: () -> Unit,
    isScanning: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SurfaceGlass.copy(alpha = 0.4f),
                        SurfaceGlass.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(PhantomCyan.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(spacing = 12.dp) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "SEARCH LIBRARY...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PhantomCyan.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = PhantomCyan)
                },
                trailingIcon = {
                    IconButton(onClick = onScanClick, enabled = !isScanning) {
                        Icon(
                            if (isScanning) Icons.Default.Refresh else Icons.Default.LibraryMusic,
                            "Scan",
                            tint = PhantomPurple
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PhantomCyan,
                    unfocusedBorderColor = PhantomPurple.copy(alpha = 0.5f),
                    focusedTextColor = PhantomWhite,
                    unfocusedTextColor = PhantomWhite,
                    cursorColor = PhantomCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // View Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LibraryView.values().forEach { view ->
                    ViewPillButton(
                        label = view.name,
                        isSelected = currentView == view,
                        onClick = { onViewChange(view) }
                    )
                }
            }
        }
    }
}

@Composable
fun ViewPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(listOf(PhantomCyan, PhantomPurple))
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) PhantomCyan else PhantomPurple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (isSelected) PhantomBlack else PhantomCyan
        )
    }
}

@Composable
fun SongsView(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Play All Button
        if (songs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.3f))
                        )
                    )
                    .border(1.dp, PhantomCyan, RoundedCornerShape(12.dp))
                    .clickable(onClick = onPlayAll)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = PhantomCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PLAY ALL ${songs.size} TRACKS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = PhantomCyan
                    )
                }
            }
        }
        
        // Song Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                SongCard(song = song, onClick = { onSongClick(song) })
            }
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(EquipmentMetal)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        PhantomCyan.copy(alpha = glowAlpha),
                        PhantomPurple.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Album Art Background
        song.albumArtPath?.let { path ->
            AsyncImage(
                model = path,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PhantomBlack.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }
        
        // Song Info Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = song.title.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = PhantomCyan,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 0.5.sp
                ),
                color = PhantomPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Play Icon on Hover/Press
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "Play",
                tint = PhantomCyan.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AlbumsView(songs: List<Song>) {
    val albums = songs.groupBy { it.album }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(albums.entries.toList()) { (album, albumSongs) ->
            AlbumCard(
                albumName = album,
                artistName = albumSongs.firstOrNull()?.artist ?: "Unknown",
                trackCount = albumSongs.size,
                albumArt = albumSongs.firstOrNull()?.albumArtPath
            )
        }
    }
}

@Composable
fun AlbumCard(
    albumName: String,
    artistName: String,
    trackCount: Int,
    albumArt: String?
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PhantomDarkPurple, PhantomBlack)
                )
            )
            .border(2.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { }
    ) {
        albumArt?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, PhantomBlack.copy(alpha = 0.95f))
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                albumName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PhantomCyan,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                artistName.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = PhantomPurple,
                maxLines = 1
            )
            Text(
                "$trackCount TRACKS",
                style = MaterialTheme.typography.labelSmall,
                color = PhantomPink.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ArtistsView(songs: List<Song>) {
    val artists = songs.groupBy { it.artist }
    
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artists.entries.toList()) { (artist, artistSongs) ->
            ArtistCard(
                artistName = artist,
                trackCount = artistSongs.size,
                albumCount = artistSongs.map { it.album }.distinct().size
            )
        }
    }
}

@Composable
fun ArtistCard(artistName: String, trackCount: Int, albumCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.3f),
                        SurfaceGlass.copy(alpha = 0.1f)
                    )
                )
            )
            .border(1.dp, PhantomCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.5f))
                        )
                    )
                    .border(2.dp, PhantomCyan, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    artistName.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PhantomCyan
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    artistName.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PhantomWhite,
                    maxLines = 1
                )
                Text(
                    "$albumCount ALBUMS • $trackCount TRACKS",
                    style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.5.sp),
                    color = PhantomPurple
                )
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = PhantomCyan)
        }
    }
}

@Composable
fun PlaylistsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "PLAYLISTS COMING SOON",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = PhantomCyan.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ScanStatusBanner(
    status: String,
    isScanning: Boolean,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(PhantomPurple.copy(alpha = 0.3f), PhantomPink.copy(alpha = 0.3f))
                )
            )
            .border(1.dp, PhantomPink, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = PhantomCyan
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                status.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhantomWhite,
                modifier = Modifier.weight(1f)
            )
            if (!isScanning) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = PhantomPink)
                }
            }
        }
    }
}

@Composable
fun EmptyLibraryState(onScanClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PhantomCyan.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "NO MUSIC DETECTED",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomCyan.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PhantomCyan,
                    contentColor = PhantomBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SCAN LIBRARY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

