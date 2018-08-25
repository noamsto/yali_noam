package com.android.noam.javacvplayground

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.content.Intent
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val WRITE_PERMISSION_CODE = 101
    private val PICK_DIR_CODE = 102

    val pickFaces = PickFacesActivity()
    lateinit var facesDir: String
    var csvName: String = "att_faces.csv"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        } else{
//            doWork()
            val pickFacesIntent = Intent(this, PickFacesActivity::class.java)
            startActivity(pickFacesIntent)
        }
    }


    fun requestPermissions() {
        Log.d(TAG, "Requesting Permission")

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION_CODE)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {
            Log.d(TAG, "External Write Permission Granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            WRITE_PERMISSION_CODE -> doWork()
        }

    }


    private fun pickDirectory() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(Intent.createChooser(i, "Choose directory"), PICK_DIR_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_DIR_CODE -> {
                facesDir = data?.data!!.path
                Log.d(TAG, "Selected path is $facesDir")
                facesDir = facesDir.removePrefix("/tree/primary:")
                facesDir = "/sdcard/$facesDir"
//                facesDir = facesDir.substring(facesDir.indexOf("/storage"))
                Log.d(TAG, "faces path is $facesDir")
                secondWork()
            }
        }

    }


    fun secondWork() {

        val eigenFacesPrep = EigenFacesPrep(csvName, facesDir)
        val dialog = indeterminateProgressDialog("Reading all the training images","Please Wait"  )
        {
        }
        dialog.show()
        eigenFacesPrep.readImagesFromDir()
//        dialog.dismiss()
//        doAsync {
//            //Execute all the lon running tasks here
//            dialog.show()
//            eigenFacesPrep.readImagesFromDir()
//            dialog.dismiss()
//        }


//        val testsample_path = "$facesDir/11_7.pgm"
//        eigenFacesPrep.predict_image(testsample_path)
    }


    fun doWork() {
        pickDirectory()


    }
}


