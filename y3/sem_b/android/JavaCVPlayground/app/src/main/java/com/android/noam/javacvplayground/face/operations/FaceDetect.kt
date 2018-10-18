package com.android.noam.javacvplayground.face.operations

import android.graphics.Bitmap
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions


class FaceDetect(mediaImage : Bitmap, successListener: OnSuccessListener<List<FirebaseVisionFace>>,
                 failureListener: OnFailureListener) {
    private var fBImage : FirebaseVisionImage = FirebaseVisionImage.fromBitmap(mediaImage)
    private val detector : FirebaseVisionFaceDetector
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setMinFaceSize(0.15f)
            .setTrackingEnabled(false)
            .build()!!
    init {
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options)
        detector.detectInImage(fBImage).addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener)
    }
}