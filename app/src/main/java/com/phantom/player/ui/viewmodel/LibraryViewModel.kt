package com.phantom.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantom.player.data.local.database.entities.Song
import com.phantom.player.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    val songs: StateFlow<List<Song>> = musicRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val artists: StateFlow<List<String>> = musicRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val albums: StateFlow<List<String>> = musicRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val genres: StateFlow<List<String>> = musicRepository.getAllGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _scanStatus = MutableStateFlow<String?>(null)
    val scanStatus: StateFlow<String?> = _scanStatus.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val searchResults: StateFlow<List<Song>> = musicRepository.searchSongs("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun scanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanStatus.value = "Scanning music files..."
            
            val result = musicRepository.scanAndImportMusic()
            
            result.onSuccess { count ->
                _scanStatus.value = "Found $count songs"
            }.onFailure { error ->
                _scanStatus.value = "Error: ${error.message}"
            }
            
            _isScanning.value = false
        }
    }
    
    fun clearScanStatus() {
        _scanStatus.value = null
    }
    
    fun searchSongs(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            musicRepository.searchSongs(query)
        }
    }
    
    fun getSongsByArtist(artist: String): StateFlow<List<Song>> {
        return musicRepository.getSongsByArtist(artist)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getSongsByAlbum(album: String): StateFlow<List<Song>> {
        return musicRepository.getSongsByAlbum(album)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getFavoriteSongs(): StateFlow<List<Song>> {
        return musicRepository.getFavoriteSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getRecentlyAdded(): StateFlow<List<Song>> {
        return musicRepository.getRecentlyAdded()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun getMostPlayed(): StateFlow<List<Song>> {
        return musicRepository.getMostPlayed()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
