package com.phantom.player.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.phantom.player.data.local.database.entities.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun scanLocalMusic(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val filePath = it.getString(dataColumn) ?: continue
                val albumId = it.getLong(albumIdColumn)
                val dateAdded = it.getLong(dateAddedColumn) * 1000
                val year = it.getInt(yearColumn)
                val track = it.getInt(trackColumn)
                
                // Extract embedded album art from file
                val albumArtPath = extractAlbumArt(filePath) ?: run {
                    // Fallback to MediaStore album art
                    val albumArtUri = Uri.parse("content://media/external/audio/albumart")
                    Uri.withAppendedPath(albumArtUri, albumId.toString()).toString()
                }
                
                val song = Song(
                    id = generateSongId(filePath),
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    filePath = filePath,
                    albumArtPath = albumArtPath,
                    dateAdded = dateAdded,
                    year = if (year > 0) year else null,
                    track = if (track > 0) track else null
                )
                
                songs.add(song)
            }
        }
        
        songs
    }
    
    private fun extractAlbumArt(filePath: String): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            retriever.release()
            
            if (art != null) {
                // Save album art to cache directory
                val cacheDir = File(context.cacheDir, "album_art")
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
                
                val artFile = File(cacheDir, "${generateSongId(filePath)}.jpg")
                artFile.writeBytes(art)
                artFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generateSongId(filePath: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(filePath.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            filePath.hashCode().toString()
        }
    }
}
