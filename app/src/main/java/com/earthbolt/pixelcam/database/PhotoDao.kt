package com.earthbolt.pixelcam.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.earthbolt.pixelcam.model.Photo

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo)

    @Query("SELECT * FROM photos WHERE albumName = :albumName ORDER BY dateTaken DESC")
    fun getPhotosByAlbum(albumName: String): LiveData<List<Photo>>

    @Query("SELECT * FROM photos ORDER BY dateTaken DESC")
    fun getAllPhotos(): LiveData<List<Photo>>
}
