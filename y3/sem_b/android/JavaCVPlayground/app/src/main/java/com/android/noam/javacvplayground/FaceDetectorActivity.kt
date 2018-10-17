package com.android.noam.javacvplayground

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast

class FaceDetectorActivity : AppCompatActivity() {

    private lateinit var classObj : ClassObj
    private lateinit var eigenFaces: EigenFaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detector)
        classObj = intent.extras.getSerializable(SelectClassActivity.CLASS_OBJ_TAG) as ClassObj
//        eigenFaces = EigenFaces(classObj.path)
//        eigenFaces.readImagesFromDir()


//        doAsync {
//            eigenFaces.trainModel()
//            val id = eigenFaces.runTest()
//            runOnUiThread { showResults(id) }
//        }
    }


    fun showResults(id: Int){
        longToast("Predicated id is $id")
    }

}