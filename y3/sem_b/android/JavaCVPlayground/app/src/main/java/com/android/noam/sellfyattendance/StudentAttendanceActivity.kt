package com.android.noam.sellfyattendance

import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class StudentAttendanceActivity : AppCompatActivity(), Camera2Fragment.OnCameraFragmentInteractionListener {
    @SuppressLint("LongLogTag")
    override fun onImageAvailable(image: Image, rotationValue: Int) {
        Log.d(TAG, "OnImageAvailable!.")
        image.close()
    }

    override fun onCreate(savedInstanceState: Bundle?) { savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attandence)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.camera_frag_container, Camera2Fragment.newInstance())
                .commit()
    }
    companion object {
        private const val TAG = "StudentAttendanceActivity"
    }
}
