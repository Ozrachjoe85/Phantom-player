package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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

enum class SortOption {
    TITLE_ASC, TITLE_DESC,
    ARTIST_ASC, ARTIST_DESC,
    DATE_ADDED_ASC, DATE_ADDED_DESC,
    DURATION_ASC, DURATION_DESC
}

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val songs by libraryViewModel.songs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.DATE_ADDED_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Search and Sort
            LibraryHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onScanLibrary = { libraryViewModel.scanLibrary() },
                songCount = songs.size,
                sortOption = sortOption,
                onSortClick = { showSortMenu = true }
            )
            
            // Scan Status Banner
            if (isScanning) {
                ScanStatusBanner()
            }
            
            // Content
            when {
                songs.isEmpty() && !isScanning -> {
                    EmptyLibraryState(onScanClick = { libraryViewModel.scanLibrary() })
                }
                else -> {
                    val filteredAndSortedSongs = songs
                        .filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true) ||
                            it.album.contains(searchQuery, ignoreCase = true)
                        }
                        .let { list ->
                            when (sortOption) {
                                SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                                SortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
                                SortOption.ARTIST_ASC -> list.sortedBy { it.artist.lowercase() }
                                SortOption.ARTIST_DESC -> list.sortedByDescending { it.artist.lowercase() }
                                SortOption.DATE_ADDED_ASC -> list.sortedBy { it.dateAdded }
                                SortOption.DATE_ADDED_DESC -> list.sortedByDescending { it.dateAdded }
                                SortOption.DURATION_ASC -> list.sortedBy { it.duration }
                                SortOption.DURATION_DESC -> list.sortedByDescending { it.duration }
                            }
                        }
                    
                    SongsListView(
                        songs = filteredAndSortedSongs,
                        currentSong = currentSong,
                        onSongClick = { song, index ->
                            playerViewModel.setPlaylist(filteredAndSortedSongs, index)
                            playerViewModel.play(song)
                        }
                    )
                }
            }
        }
        
        // Sort Menu Dropdown
        if (showSortMenu) {
            SortMenuDialog(
                currentSort = sortOption,
                onSortSelected = { 
                    sortOption = it
                    showSortMenu = false
                },
                onDismiss = { showSortMenu = false }
            )
        }
    }
}

@Composable
fun LibraryHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onScanLibrary: () -> Unit,
    songCount: Int,
    sortOption: SortOption,
    onSortClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
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
                    listOf(PhantomPurple.copy(alpha = 0.5f), PhantomGreen.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Row with Scan and Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "LIBRARY",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    ),
                    color = PhantomPurple
                )
                Text(
                    "$songCount TRACKS • ${sortOption.displayName()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 1.sp
                    ),
                    color = PhantomGreen
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sort button
                IconButton(
                    onClick = onSortClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PhantomGreen.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(1.dp, PhantomGreen, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = PhantomGreen
                    )
                }
                
                // Scan button
                IconButton(
                    onClick = onScanLibrary,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(PhantomOrange.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                        .border(1.dp, PhantomOrange, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Scan Library",
                        tint = PhantomOrange
                    )
                }
            }
        }
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = {
                Text("Search songs, artists, albums...", color = PhantomWhite.copy(alpha = 0.5f))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = PhantomPurple)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = PhantomGreen)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PhantomWhite,
                unfocusedTextColor = PhantomWhite,
                focusedBorderColor = PhantomPurple,
                unfocusedBorderColor = PhantomGreen.copy(alpha = 0.3f),
                cursorColor = PhantomPurple,
                focusedContainerColor = PhantomBlack.copy(alpha = 0.5f),
                unfocusedContainerColor = PhantomBlack.copy(alpha = 0.3f)
            ),
            singleLine = true
        )
    }
}

@Composable
fun SortMenuDialog(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PhantomDarkPurple)
                .border(2.dp, PhantomPurple, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .clickable(onClick = {}) // Prevent click-through
        ) {
            Text(
                "SORT BY",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomPurple,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            SortOption.values().forEach { option ->
                SortMenuItem(
                    label = option.displayName(),
                    isSelected = option == currentSort,
                    onClick = { onSortSelected(option) }
                )
            }
        }
    }
}

@Composable
fun SortMenuItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(PhantomPurple.copy(alpha = 0.3f), PhantomGreen.copy(alpha = 0.3f))
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = PhantomGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) PhantomGreen else PhantomWhite
        )
    }
}

fun SortOption.displayName(): String = when (this) {
    SortOption.TITLE_ASC -> "Title (A-Z)"
    SortOption.TITLE_DESC -> "Title (Z-A)"
    SortOption.ARTIST_ASC -> "Artist (A-Z)"
    SortOption.ARTIST_DESC -> "Artist (Z-A)"
    SortOption.DATE_ADDED_ASC -> "Date Added (Oldest First)"
    SortOption.DATE_ADDED_DESC -> "Date Added (Newest First)"
    SortOption.DURATION_ASC -> "Duration (Shortest First)"
    SortOption.DURATION_DESC -> "Duration (Longest First)"
}

@Composable
fun ScanStatusBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PhantomPurple.copy(alpha = 0.2f),
                        PhantomGreen.copy(alpha = 0.2f)
                    )
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Canvas(modifier = Modifier.size(24.dp).rotate(offset * 360f)) {
                drawCircle(
                    color = PhantomPurple,
                    radius = size.minDimension / 4,
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = PhantomPurple,
                    start = center,
                    end = Offset(center.x + size.width / 2, center.y),
                    strokeWidth = 3f
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SCANNING LIBRARY...",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = PhantomPurple
                )
                Text(
                    "Finding music files",
                    style = MaterialTheme.typography.bodySmall,
                    color = PhantomGreen
                )
            }
            
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = PhantomPurple,
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun SongsListView(
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song, Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(songs) { index, song ->
            SongListItem(
                song = song,
                isPlaying = currentSong?.id == song.id,
                onClick = { onSongClick(song, index) }
            )
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val glowAnimation = rememberInfiniteTransition(label = "item_glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val borderColor = if (isPlaying) {
        Brush.horizontalGradient(
            listOf(
                PhantomPurple.copy(alpha = glowAlpha),
                PhantomGreen.copy(alpha = glowAlpha)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                PhantomPurple.copy(alpha = 0.2f),
                PhantomGreen.copy(alpha = 0.2f)
            )
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) {
                    Brush.horizontalGradient(
                        listOf(
                            PhantomDarkPurple.copy(alpha = 0.5f),
                            PhantomBlack
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            SurfaceGlass.copy(alpha = 0.3f),
                            SurfaceGlass.copy(alpha = 0.1f)
                        )
                    )
                }
            )
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            PhantomPurple.copy(alpha = 0.3f),
                            PhantomGreen.copy(alpha = 0.3f)
                        )
                    )
                )
                .border(1.dp, if (isPlaying) PhantomGreen else PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtPath != null) {
                AsyncImage(
                    model = song.albumArtPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isPlaying) PhantomGreen else PhantomPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PhantomGreen.copy(alpha = 0.2f))
                )
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Now Playing",
                    tint = PhantomGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // Song Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = if (isPlaying) PhantomGreen else PhantomWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.3.sp
                ),
                color = if (isPlaying) PhantomPurple else PhantomGreen.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.album,
                style = MaterialTheme.typography.bodySmall,
                color = PhantomWhite.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Duration
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = if (isPlaying) PhantomOrange else PhantomPurple
            )
            
            if (isPlaying) {
                Text(
                    text = "• PLAYING •",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = PhantomGreen
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryState(onScanClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "empty")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Canvas(modifier = Modifier.size(120.dp).rotate(rotation)) {
                drawCircle(
                    color = PhantomPurple.copy(alpha = 0.3f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = PhantomGreen.copy(alpha = 0.3f),
                    radius = size.minDimension / 3,
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = PhantomPurple,
                    start = center,
                    end = Offset(center.x + size.width / 2, center.y),
                    strokeWidth = 3f
                )
            }
            
            Text(
                "NO MUSIC FOUND",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                ),
                color = PhantomPurple
            )
            
            Text(
                "Scan your device to find music files",
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 1.sp
                ),
                color = PhantomGreen
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomPurple.copy(alpha = 0.5f), PhantomOrange.copy(alpha = 0.5f))
                        )
                    )
                    .border(2.dp, PhantomPurple, RoundedCornerShape(16.dp))
                    .clickable(onClick = onScanClick)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = PhantomWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "SCAN LIBRARY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = PhantomWhite
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
