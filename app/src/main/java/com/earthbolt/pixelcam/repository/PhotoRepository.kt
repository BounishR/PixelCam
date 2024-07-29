package com.earthbolt.pixelcam.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.earthbolt.pixelcam.database.PhotoDao
import com.earthbolt.pixelcam.model.Photo

class PhotoRepository(private val photoDao: PhotoDao) {

    @WorkerThread
    suspend fun insert(photo: Photo) {
        photoDao.insert(photo)
    }

    fun getPhotosByAlbum(albumName: String): LiveData<List<Photo>> {
        return photoDao.getPhotosByAlbum(albumName)
    }

    fun getAllPhotos(): LiveData<List<Photo>> {
        return photoDao.getAllPhotos()
    }
}
