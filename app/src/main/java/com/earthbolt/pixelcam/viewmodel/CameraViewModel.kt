package com.earthbolt.pixelcam.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.earthbolt.pixelcam.database.PhotoDatabase
import com.earthbolt.pixelcam.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository

    init {
        val photosDao = PhotoDatabase.getDatabase(application).photoDao()
        repository = PhotoRepository(photosDao)
    }

    fun insert(photo: Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(photo)
        }
    }
}
