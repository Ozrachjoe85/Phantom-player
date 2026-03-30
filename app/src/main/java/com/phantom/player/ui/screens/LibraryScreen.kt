package com.phantom.player.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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

enum class LibraryViewMode {
    SONGS, ALBUMS, ARTISTS, PLAYLISTS
}

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val songs by libraryViewModel.songs.collectAsState()
    val isScanning by libraryViewModel.isScanning.collectAsState()
    var viewMode by remember { mutableStateOf(LibraryViewMode.SONGS) }
    var searchQuery by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantomBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Search and View Mode Selector
            LibraryHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                onScanLibrary = { libraryViewModel.scanLibrary() }
            )
            
            // Scan Status Banner
            if (isScanning) {
             ScanStatusBanner(progress = 0.5f)
            }
            
            // Content based on view mode
            when {
                songs.isEmpty() && !isScanning -> {
                    EmptyLibraryState(onScanClick = { libraryViewModel.scanLibrary() })
                }
                else -> {
                    when (viewMode) {
                        LibraryViewMode.SONGS -> {
                            SongsGridView(
                                songs = songs.filter {
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.artist.contains(searchQuery, ignoreCase = true)
                                },
                                onSongClick = { song -> playerViewModel.playSong(song) }
                            )
                        }
                        LibraryViewMode.ALBUMS -> {
                            AlbumsView(songs = songs)
                        }
                        LibraryViewMode.ARTISTS -> {
                            ArtistsView(songs = songs)
                        }
                        LibraryViewMode.PLAYLISTS -> {
                            PlaylistsView()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onScanLibrary: () -> Unit
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
                    listOf(PhantomCyan.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title and Scan Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "LIBRARY",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                ),
                color = PhantomCyan
            )
            
            IconButton(
                onClick = onScanLibrary,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomPurple.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .border(1.dp, PhantomPurple, CircleShape)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Scan Library",
                    tint = PhantomPurple
                )
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
                Text("Search songs, artists...", color = PhantomWhite.copy(alpha = 0.5f))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = PhantomCyan)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = PhantomPurple)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PhantomWhite,
                unfocusedTextColor = PhantomWhite,
                focusedBorderColor = PhantomCyan,
                unfocusedBorderColor = PhantomPurple.copy(alpha = 0.3f),
                cursorColor = PhantomCyan,
                focusedContainerColor = PhantomBlack.copy(alpha = 0.5f),
                unfocusedContainerColor = PhantomBlack.copy(alpha = 0.3f)
            ),
            singleLine = true
        )
        
        // View Mode Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewModePill(
                label = "SONGS",
                isSelected = viewMode == LibraryViewMode.SONGS,
                onClick = { onViewModeChange(LibraryViewMode.SONGS) },
                modifier = Modifier.weight(1f)
            )
            ViewModePill(
                label = "ALBUMS",
                isSelected = viewMode == LibraryViewMode.ALBUMS,
                onClick = { onViewModeChange(LibraryViewMode.ALBUMS) },
                modifier = Modifier.weight(1f)
            )
            ViewModePill(
                label = "ARTISTS",
                isSelected = viewMode == LibraryViewMode.ARTISTS,
                onClick = { onViewModeChange(LibraryViewMode.ARTISTS) },
                modifier = Modifier.weight(1f)
            )
            ViewModePill(
                label = "PLAYLISTS",
                isSelected = viewMode == LibraryViewMode.PLAYLISTS,
                onClick = { onViewModeChange(LibraryViewMode.PLAYLISTS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ViewModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(PhantomCyan.copy(alpha = 0.4f), PhantomPurple.copy(alpha = 0.4f))
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .border(
                1.dp,
                if (isSelected) PhantomCyan else PhantomPurple.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (isSelected) PhantomWhite else PhantomPurple
        )
    }
}

@Composable
fun ScanStatusBanner(progress: Float) {
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
                        PhantomCyan.copy(alpha = 0.2f),
                        PhantomPurple.copy(alpha = 0.2f)
                    )
                )
            )
            .border(1.dp, PhantomCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated scanning icon
            Canvas(modifier = Modifier.size(24.dp).rotate(offset * 360f)) {
                drawCircle(
                    color = PhantomCyan,
                    radius = size.minDimension / 4,
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = PhantomCyan,
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
                    color = PhantomCyan
                )
                Text(
                    "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = PhantomPurple
                )
            }
            
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(32.dp),
                color = PhantomCyan,
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
fun SongsGridView(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Play All Button
        if (songs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.3f))
                        )
                    )
                    .border(1.dp, PhantomCyan, RoundedCornerShape(16.dp))
                    .clickable { onSongClick(songs.first()) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        tint = PhantomCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PLAY ALL (${songs.size} TRACKS)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = PhantomWhite
                    )
                }
            }
        }
        
        // Songs Grid
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
    val glowAnimation = rememberInfiniteTransition(label = "card_glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        PhantomDarkPurple.copy(alpha = 0.5f),
                        PhantomBlack
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        PhantomCyan.copy(alpha = glowAlpha),
                        PhantomPurple.copy(alpha = glowAlpha)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.5f))
                        )
                    )
                    .border(1.dp, PhantomCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
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
                        tint = PhantomCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Song Info
            Text(
                text = song.title.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = PhantomWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.artist.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 0.3.sp
                ),
                color = PhantomPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AlbumsView(songs: List<Song>) {
    val albums = remember(songs) {
        songs.groupBy { it.album }.map { (album, albumSongs) ->
            album to albumSongs
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(albums) { (album, albumSongs) ->
            AlbumCard(
                albumName = album,
                artist = albumSongs.firstOrNull()?.artist ?: "Unknown",
                trackCount = albumSongs.size,
                albumArtPath = albumSongs.firstOrNull()?.albumArtPath
            )
        }
    }
}

@Composable
fun AlbumCard(
    albumName: String,
    artist: String,
    trackCount: Int,
    albumArtPath: String?
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceGlass.copy(alpha = 0.3f),
                        SurfaceGlass.copy(alpha = 0.1f)
                    )
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomCyan.copy(alpha = 0.3f), PhantomPurple.copy(alpha = 0.5f))
                        )
                    )
                    .border(1.dp, PhantomCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (albumArtPath != null) {
                    AsyncImage(
                        model = albumArtPath,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        tint = PhantomCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = albumName.uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = PhantomWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = artist.uppercase(),
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 0.3.sp
                ),
                color = PhantomPurple,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "$trackCount TRACKS",
                style = MaterialTheme.typography.labelSmall,
                color = PhantomCyan.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ArtistsView(songs: List<Song>) {
    val artists = remember(songs) {
        songs.groupBy { it.artist }.map { (artist, artistSongs) ->
            artist to artistSongs
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artists) { (artist, artistSongs) ->
            ArtistCard(
                artistName = artist,
                trackCount = artistSongs.size,
                albumCount = artistSongs.map { it.album }.distinct().size
            )
        }
    }
}

@Composable
fun ArtistCard(
    artistName: String,
    trackCount: Int,
    albumCount: Int
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        PhantomPurple.copy(alpha = 0.3f),
                        PhantomBlack
                    )
                )
            )
            .border(1.dp, PhantomPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { }
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Artist Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(PhantomCyan.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.3f))
                        )
                    )
                    .border(2.dp, PhantomCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = PhantomCyan,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = artistName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = PhantomWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$albumCount ALBUMS",
                style = MaterialTheme.typography.bodySmall,
                color = PhantomPurple
            )
            
            Text(
                text = "$trackCount TRACKS",
                style = MaterialTheme.typography.bodySmall,
                color = PhantomCyan.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlaylistsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.QueueMusic,
                contentDescription = null,
                tint = PhantomPurple,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                "PLAYLISTS COMING SOON",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = PhantomCyan
            )
            
            Text(
                "Create and manage custom playlists",
                style = MaterialTheme.typography.bodyMedium,
                color = PhantomPurple
            )
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
            // Animated radar icon
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
                    color = PhantomCyan.copy(alpha = 0.3f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = PhantomPurple.copy(alpha = 0.3f),
                    radius = size.minDimension / 3,
                    style = Stroke(width = 3f)
                )
                drawLine(
                    color = PhantomCyan,
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
                color = PhantomCyan
            )
            
            Text(
                "Scan your device to find music files",
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 1.sp
                ),
                color = PhantomPurple
            )
            
            // Scan Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PhantomCyan.copy(alpha = 0.5f), PhantomPurple.copy(alpha = 0.5f))
                        )
                    )
                    .border(2.dp, PhantomCyan, RoundedCornerShape(16.dp))
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
