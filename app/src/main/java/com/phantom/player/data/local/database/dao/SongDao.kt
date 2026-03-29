package com.phantom.player.data.local.database.dao

import androidx.room.*
import com.phantom.player.data.local.database.entities.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): Song?
    
    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, track")
    fun getSongsByArtist(artist: String): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE album = :album ORDER BY track")
    fun getSongsByAlbum(album: String): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayed DESC")
    fun getFavoriteSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 20): Flow<List<Song>>
    
    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 20): Flow<List<Song>>
    
    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>
    
    @Query("SELECT DISTINCT album FROM songs ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>
    
    @Query("SELECT DISTINCT genre FROM songs WHERE genre IS NOT NULL ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)
    
    @Update
    suspend fun updateSong(song: Song)
    
    @Delete
    suspend fun deleteSong(song: Song)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: String, isFavorite: Boolean)
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<Song>>
}
