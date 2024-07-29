package com.earthbolt.pixelcam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.earthbolt.pixelcam.CameraActivity
import com.earthbolt.pixelcam.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds delay
    }
}
