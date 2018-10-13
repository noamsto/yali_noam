package com.android.noam.faceDetectorPart

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val captureFace = Intent(this, ImageCapture::class.java)
        startActivity(captureFace)
    }
}
