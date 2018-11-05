package com.android.noam.sellfyattendance.face.operations

import android.graphics.Bitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions


class FireBaseFaceDetectorWrapper {
    private val detector : FirebaseVisionFaceDetector
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setMinFaceSize(0.30f)
            .build()!!
    init {
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)
    }


    fun detectFace(mediaImage : Bitmap, successListener: OnSuccessListener<List<FirebaseVisionFace>>,
                   failureListener: OnFailureListener){
         val fBImage : FirebaseVisionImage = FirebaseVisionImage.fromBitmap(mediaImage)

        detector.detectInImage(fBImage).addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener)
    }
}