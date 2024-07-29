package com.earthbolt.pixelcam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.earthbolt.pixelcam.databinding.ActivityImagePreviewBinding

class ImagePreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImagePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra("image_path") ?: ""

        // Load the image using Glide
        Glide.with(this)
            .load(imagePath)
            .into(binding.imagePreview)

        binding.buttonKeep.setOnClickListener {
            // Handle keep button click, navigate to next activity or save the image
        }

        binding.buttonTakeMore.setOnClickListener {
            // Handle take more button click, navigate back to CameraActivity or reset the preview
        }

        binding.buttonDelete.setOnClickListener {
            // Handle delete button click, delete the image and navigate back to CameraActivity
        }
    }
}
