package com.android.noam.sellfyattendance.face.operations

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.android.noam.sellfyattendance.comparators.CompareWithNull
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import org.jetbrains.anko.doAsync
import kotlin.math.max

internal class FaceDetector(
        private val image: Image,
        private val rotation: Int,
        private val detectFaceSuccessListener: OnSuccessListener<Bitmap>,
        private val detectFaceFailureListener : OnFailureListener

) : Thread() {
    private lateinit var bitMapImage: Bitmap
    private lateinit var croppedFace: Bitmap
    private lateinit var scaledFace: Bitmap
    private val bmpTools = BmpOperations()
    private val faceDetector = FireBaseFaceDetectorWrapper()

    override fun run() {
        bitMapImage = bmpTools.convertToBmpAndRotate(image, rotation)
        this.faceDetector.detectFace(bitMapImage,
                OnSuccessListener {
                    Log.d(TAG, "run onSuccess")
                    if (it.isEmpty())
                    {
                        detectFaceFailureListener
                        bitMapImage.recycle()

                    }else{
                        it.forEach {
                            Log.d(TAG,"run OnSuccess")
                            Log.i(TAG, "Detected Face: in this bounds ${it.boundingBox}")
                            processFace(it)
                        }
                    }
                    faceDetector.close()
                }, OnFailureListener {
            Log.d(TAG, "run onFailure")
            bitMapImage.recycle()
            faceDetector.close()
        })
        image.close()
    }

    private fun processFace(firebaseVisionFace: FirebaseVisionFace){
        Log.d(TAG,"processFace start")

        val rightEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
        val leftEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
        val bottomMouth = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)

        if (leftEye == null || rightEye == null){
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(java.lang.Exception("Couldn't detect your eyes."))
            return
        }
        if (bottomMouth == null){
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(java.lang.Exception("Couldn't detect your mouth."))
            return
        }
        var minX = rightEye.position.x.toInt()
        var maxX = leftEye.position.x.toInt()
        var minY = arrayOf(rightEye.position.y, leftEye.position.y).
                minWith(CompareWithNull())!!.toInt()
        var maxY = bottomMouth.position.y.toInt()

        val eyeDist = (leftEye.position.x - rightEye.position.x).toInt()
        minX -= eyeDist / 2
        minY -= eyeDist
        maxX += eyeDist / 2
        maxY += eyeDist / 2
        val width = maxX - minX
        val height = maxY - minY
        minY = max(0, minY)
        minX = max(0, minX)
        if (minY + height >= bitMapImage.height || minX + width >= bitMapImage.width) {
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(Exception("Move further from the camera."))
            return
        }

        Log.i(TAG, "Cropping image to top left: $minX, $minY and width:$width, height:$height")
        croppedFace = Bitmap.createBitmap(bitMapImage,minX, minY, width, height)
        verifyAndRecognizeFace()
        Log.d(TAG,"processFace end")
        return
    }

    private fun verifyAndRecognizeFace() {
        Log.d(TAG,"verifyAndRecognizeFace start")
        this.faceDetector.detectFace(croppedFace, OnSuccessListener {
            Log.d(TAG, "verifyAndRecognizeFace onSuccess")
            Log.d(TAG, "Detected ${it.size} faces")
            if (it.isEmpty()){
                croppedFace.recycle()
                bitMapImage.recycle()
                detectFaceFailureListener.onFailure(java.lang.Exception("Please Try Again"))
            }else{
                it.forEach {
                    val leftEye = it.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
                    val rightEye = it.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
                    if (leftEye == null || rightEye == null )
                    {
                        croppedFace.recycle()
                        bitMapImage.recycle()
                        detectFaceFailureListener.onFailure(Exception("Face detection failed."))
                    }else if (croppedFace.width - leftEye.position.x  - rightEye.position.x >
                            croppedFace.width/5){
                        croppedFace.recycle()
                        bitMapImage.recycle()
                        detectFaceFailureListener.onFailure(java.lang.Exception("Face detectionFailed"))
                    }else{
                        scaledFace = Bitmap.createScaledBitmap(croppedFace, SCALE_WIDTH, SCALE_HEIGHT,
                                false)
                        detectFaceSuccessListener.onSuccess(scaledFace)
                    }
                }
            }

            faceDetector.close()
        }, OnFailureListener {
            Log.d(TAG, "verifyAndRecognizeFace onFailure")
            croppedFace.recycle()
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(it)
            Log.d(TAG, "Face detection Error ${it.message}")
            faceDetector.close()
        })
        Log.d(TAG,"verifyAndRecognizeFace end")
    }

    companion object {
        /**
         * Tag for the [Log].
         */
        private const val TAG = "FaceDetector"
        const val SCALE_HEIGHT = 420 //face will be (SCALE_FACTOR)x(SCALE_FACTOR)
        const val SCALE_WIDTH = 260 //face will be (SCALE_FACTOR)x(SCALE_FACTOR)
    }
}
