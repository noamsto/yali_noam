package com.android.noam.sellfyattendance.face.operations

import android.graphics.Bitmap
import com.google.android.gms.tasks.*
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
            .setMinFaceSize(0.25f)
            .build()!!
    init {
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)
    }


    fun detectFace(mediaImage : Bitmap, successListener: OnSuccessListener<List<FirebaseVisionFace>>,
                   failureListener: OnFailureListener): Task<MutableList<FirebaseVisionFace>> {
         val fBImage : FirebaseVisionImage = FirebaseVisionImage.fromBitmap(mediaImage)

        return detector.detectInImage(fBImage).addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener)
    }

    fun close() = detector.close()
}