package com.android.noam.javacvplayground

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val WRITE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun pickFacesSet(view: View) =runWithPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
        val pickFacesIntent = Intent(this, PickFacesActivity::class.java)
        startActivity(pickFacesIntent)

    }
}


