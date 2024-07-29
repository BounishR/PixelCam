package com.earthbolt.pixelcam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthbolt.pixelcam.model.Photo
import com.earthbolt.pixelcam.repository.PhotoRepository
import kotlinx.coroutines.launch

class PhotoViewModel(private val repository: PhotoRepository) : ViewModel() {

    fun insert(photo: Photo) = viewModelScope.launch {
        repository.insert(photo)
    }

    fun getPhotosByAlbum(albumName: String): LiveData<List<Photo>> {
        return repository.getPhotosByAlbum(albumName)
    }

    fun getAllPhotos(): LiveData<List<Photo>> {
        return repository.getAllPhotos()
    }
}
