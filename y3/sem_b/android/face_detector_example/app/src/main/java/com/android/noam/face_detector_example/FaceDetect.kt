package com.android.noam.face_detector_example

import android.media.Image
import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata




class FaceDetect(var rotationCompensation: Int, val mediaImage:Image) {

    val TAG = "FaceDetect"
    var fBImage : FirebaseVisionImage

    init {
        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        when (rotationCompensation) {
            0 -> rotationCompensation = FirebaseVisionImageMetadata.ROTATION_0
            90 -> rotationCompensation = FirebaseVisionImageMetadata.ROTATION_90
            180 -> rotationCompensation = FirebaseVisionImageMetadata.ROTATION_180
            270 -> rotationCompensation = FirebaseVisionImageMetadata.ROTATION_270
            else -> {
                rotationCompensation = FirebaseVisionImageMetadata.ROTATION_0
                Log.e(TAG, "Bad rotation value: $rotationCompensation")
            }
        }
        fBImage = FirebaseVisionImage.fromMediaImage(mediaImage, rotationCompensation)
    }

    var options = FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.15f)
            .setTrackingEnabled(true)
            .build()



}