package com.android.noam.javacvplayground

import android.media.Image
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.R.attr.bitmap
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import android.support.annotation.NonNull
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task


class FaceDetectorWrapper() {

    val TAG = "FaceDetectorWrapper"

    val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.15f)
            .setTrackingEnabled(true)
            .build()

    var detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(options)

    fun detectFromFile(pathToPicture: String){
        var bitmap = BitmapFactory.decodeFile(pathToPicture)

        val image = FirebaseVisionImage.fromBitmap(bitmap)

        val result =
                detector.detectInImage(image)
                        .addOnSuccessListener {
                            // Task completed successfully
                            // ...
                            Log.d(TAG,"Detected ${it.size}")
                        }
                        .addOnFailureListener(
                                object:OnFailureListener {
                                    override fun onFailure(e:Exception) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                })
    }

}