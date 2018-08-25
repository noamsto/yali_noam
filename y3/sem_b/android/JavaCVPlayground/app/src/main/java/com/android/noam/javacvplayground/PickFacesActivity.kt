package com.android.noam.javacvplayground

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileFilter


data class FacesSet(val name: String, val path:String, val samples: Int)

class PickFacesActivity : AppCompatActivity() {

    private val TAG = "PickFacesActivity"
    private val faceSets : ArrayList<FacesSet> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_faces)

        readFacesSets()
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }


    fun getPublicPicturesStorageDir(): File? {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Faces")
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        return file
    }

    fun readFacesSets(){
        val picDir = getPublicPicturesStorageDir()
        if (picDir != null) {
            Log.d(TAG, "Searching for all faces sets in ${picDir.absolutePath}")
            for (facesSet in picDir.listFiles()){
                var numOfSamples = 0
                facesSet.walkTopDown().forEach {
                    if (it.extension.matches(Regex.fromLiteral("pgm"))){
                        numOfSamples++
                    }
                }
//                        facesSet.listFiles(FileFilter { it.isDirectory })
//                samplesDirs.forEach {
//                    numOfSamples += it.listFiles(FileFilter {
//                        it.extension.matches(Regex.fromLiteral("pgm|jpg|bmp"))
//                    }).size
//                }
                val faceSet = FacesSet(facesSet.nameWithoutExtension, facesSet.absolutePath,
                        numOfSamples)
                faceSets.add(faceSet)
                Log.d(TAG, "Found ${faceSet.name}, path: ${faceSet.path}, " +
                        "with ${faceSet.samples} samples")
            }
        }
    }
}


