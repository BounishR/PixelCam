package com.earthbolt.pixelcam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.earthbolt.pixelcam.databinding.ActivityCameraBinding
import com.earthbolt.pixelcam.viewmodel.CameraViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var isFlashlightOn = false
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var capturedImageFile: File
    private lateinit var cameraViewModel: CameraViewModel

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allPermissionsGranted = permissions.all { it.value }
        if (allPermissionsGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionsLauncher.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))

        // Initialize MediaPlayer with the camera click sound
        mediaPlayer = MediaPlayer.create(this, R.raw.camera_click_sound) // Ensure you have a sound file in res/raw

        // Initialize the CameraViewModel
        cameraViewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        binding.buttonCapture.setOnClickListener {
            dimButton(it)
            playCameraClickSound()
            takePhoto()
        }
        binding.buttonOldImages.setOnClickListener { dimButton(it) /* Navigate to old images */ }
        binding.buttonFlashlight.setOnClickListener {
            dimButton(it)
            toggleFlashlight()
        }
        binding.selfie.setOnClickListener {
            dimButton(it)
            toggleCamera()
        }

        // Set touch listener for touch-to-focus
        binding.cameraPreviewView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleFocus(event.x, event.y)
            }
            true
        }
    }

    private fun dimButton(view: View) {
        view.alpha = 0.5f
        view.postDelayed({ view.alpha = 1f }, 200)
    }

    private fun playCameraClickSound() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(0)
        }
        mediaPlayer.start()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        try {
            cameraProvider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val msg = "Photo saved in ${photoFile.absolutePath}"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

                // Save photo to database
                val photo = Photo(
                    id = 0, // Ensure this matches your Photo entity
                    name = photoFile.name,
                    albumName = "Default Album",
                    uri = photoFile.absolutePath,
                    dateTaken = System.currentTimeMillis()
                )
                cameraViewModel.insert(photo)
            }

            private fun Photo(id: Int, name: String, albumName: String, uri: String, dateTaken: Long) {

            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(baseContext, "Error capturing photo: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val fileName = "IMG_$timeStamp"
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    private fun showImagePreview() {
        val intent = Intent(this, ImagePreviewActivity::class.java).apply {
            putExtra("image_path", capturedImageFile.absolutePath)
        }
        startActivity(intent)
    }

    private fun toggleFlashlight() {
        if (::cameraControl.isInitialized) {
            isFlashlightOn = !isFlashlightOn
            cameraControl.enableTorch(isFlashlightOn)
            Toast.makeText(this, if (isFlashlightOn) "Flashlight On" else "Flashlight Off", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindCameraUseCases()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleFocus(x: Float, y: Float) {
        if (::cameraControl.isInitialized) {
            val meteringPoint = binding.cameraPreviewView.meteringPointFactory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(meteringPoint, FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            cameraControl.startFocusAndMetering(action).addListener({
                // Add any additional logic for focus success/failure if needed
                Toast.makeText(this, "Focus complete", Toast.LENGTH_SHORT).show()
                hideFocusIndicator()
            }, ContextCompat.getMainExecutor(this))

            showFocusIndicator()
        }
    }

    private fun showFocusIndicator() {
        binding.focusIndicator.visibility = View.VISIBLE
    }

    private fun hideFocusIndicator() {
        binding.focusIndicator.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        cameraExecutor.shutdown()
    }
}
