package com.android.noam.javacvplayground

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.bytedeco.javacpp.opencv_face

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var face : opencv_face.EigenFaceRecognizer



    }
}
