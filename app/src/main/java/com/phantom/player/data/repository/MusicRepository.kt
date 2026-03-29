package com.phantom.player.data.repository

import com.phantom.player.data.local.MediaScanner
import com.phantom.player.data.local.database.dao.SongDao
import com.phantom.player.data.local.database.entities.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val mediaScanner: MediaScanner
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    
    fun getSongsByArtist(artist: String): Flow<List<Song>> = 
        songDao.getSongsByArtist(artist)
    
    fun getSongsByAlbum(album: String): Flow<List<Song>> = 
        songDao.getSongsByAlbum(album)
    
    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()
    
    fun getRecentlyAdded(limit: Int = 20): Flow<List<Song>> = 
        songDao.getRecentlyAdded(limit)
    
    fun getMostPlayed(limit: Int = 20): Flow<List<Song>> = 
        songDao.getMostPlayed(limit)
    
    fun getAllArtists(): Flow<List<String>> = songDao.getAllArtists()
    
    fun getAllAlbums(): Flow<List<String>> = songDao.getAllAlbums()
    
    fun getAllGenres(): Flow<List<String>> = songDao.getAllGenres()
    
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    
    suspend fun getSongById(songId: String): Song? = songDao.getSongById(songId)
    
    suspend fun scanAndImportMusic(): Result<Int> {
        return try {
            val songs = mediaScanner.scanLocalMusic()
            songDao.insertSongs(songs)
            Result.success(songs.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSong(song: Song) = songDao.updateSong(song)
    
    suspend fun incrementPlayCount(songId: String) = 
        songDao.incrementPlayCount(songId)
    
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean) = 
        songDao.updateFavorite(songId, isFavorite)
    
    suspend fun deleteSong(song: Song) = songDao.deleteSong(song)
    
    suspend fun deleteAllSongs() = songDao.deleteAllSongs()
}
