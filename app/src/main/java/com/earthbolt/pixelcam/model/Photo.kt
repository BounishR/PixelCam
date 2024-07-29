package com.earthbolt.pixelcam.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val albumName: String,
    val uri: String,
    val dateTaken: Long
)
