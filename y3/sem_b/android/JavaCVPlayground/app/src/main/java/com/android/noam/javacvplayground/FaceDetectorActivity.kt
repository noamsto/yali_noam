package com.android.noam.javacvplayground

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.jetbrains.anko.doAsync

class FaceDetectorActivity : AppCompatActivity() {

    lateinit var faceSet : FacesSet
    lateinit var eigenFaces: EigenFaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detector)
        faceSet = intent.extras.getParcelable(PickFacesActivity.FACE_SET_TAG)
        eigenFaces = EigenFaces(faceSet.path)
        eigenFaces.readImagesFromDir()
        doAsync {
            eigenFaces.trainModel()
            eigenFaces.runTest()
        }

    }


}
