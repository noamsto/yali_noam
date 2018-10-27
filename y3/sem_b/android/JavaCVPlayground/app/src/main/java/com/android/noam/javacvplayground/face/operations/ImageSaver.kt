package com.android.noam.javacvplayground.face.operations

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Saves a JPEG [Image] into the specified [File].
 */
internal class ImageSaver(
        /**
         * The JPEG mediaImage
         */
        private val image: Image,

        /**
         * The file we save the mediaImage into.
         */
        private val file: File,
        private val rotation: Int,
        private val croppedFaceViewer : ImageView,
        private val detectFaceSuccessListener: OnSuccessListener<String>,
        private val detectFaceFailureListener : OnFailureListener

) : Runnable {
    private lateinit var bitMapImage: Bitmap
    private lateinit var croppedFace: Bitmap
    private lateinit var scaledFace: Bitmap
    private val bmpTools = BMPTools()
    private val faceDetector = FaceDetect()
    private val shouldThrottle = AtomicBoolean(false)

    override fun run() {
        if (shouldThrottle.get()){
            image.close()
            detectFaceFailureListener.onFailure(Exception("Still working on previous picture."))
            return
        }
        bitMapImage = bmpTools.convertToBmpAndRotate(image, rotation)
        this.faceDetector.detectFace(bitMapImage,
                OnSuccessListener {
                    it.forEach {
                        Log.d(TAG,"run OnSuccess")
                        Log.i(TAG, "Detected Face: in this bounds ${it.boundingBox}")
                        processFace(it)
                    }
                }, OnFailureListener {
            Log.d(TAG, "run OnFailure")
            Log.i(TAG, "Failed To detect a Face.")
            bitMapImage.recycle()
        })
        image.close()
    }

    private fun processFace(firebaseVisionFace: FirebaseVisionFace){
        Log.d(TAG,"processFace start")

        val rightEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
        val leftEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
        val rightEar = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)
        val leftEar = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
        val rightCheek = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        val leftCheek = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK)
        val bottomMouth = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)

        if (leftEye == null || rightEye == null || bottomMouth == null){
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(java.lang.Exception("Couldn't detect your eyes."))
            return
        }

        var minX = arrayOf(rightEye.position.x,  rightEar?.position?.x, rightCheek?.position?.x).minWith(CompareWithNull())!!.toInt()
        var maxX = arrayOf(leftEye.position.x, leftEar?.position?.x, leftCheek?.position?.x).minWith(CompareWithNull())!!.toInt()
        var minY = arrayOf(
                rightEye.position?.y, rightEar?.position?.y, leftEar?.position?.y,
                leftEye.position?.y).minWith(CompareWithNull())!!.toInt()
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
        verifyAndSaveFace()
        Log.d(TAG,"processFace end")
        return
    }

    private fun verifyAndSaveFace() {
        Log.d(TAG,"verifyAndSaveFace start")
        this.faceDetector.detectFace(croppedFace, OnSuccessListener {
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
                    scaledFace = Bitmap.createScaledBitmap(croppedFace, SCALE_WIDTH, SCALE_HEIGHT, false)
                    croppedFaceViewer.setImageBitmap(scaledFace)
                    writeToFile()
                    detectFaceSuccessListener.onSuccess("Face cropped and saved.")
                }
                shouldThrottle.set(false)
            }
        }, OnFailureListener {
            croppedFace.recycle()
            bitMapImage.recycle()
            detectFaceFailureListener.onFailure(it)
            Log.e(TAG, "Face detection Error ${it.message}")
            shouldThrottle.set(false)
        } )
        shouldThrottle.set(true)
        Log.d(TAG,"verifyAndSaveFace end")
    }

    private fun writeToFile() {
        Log.d(TAG,"writeToFile start")
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                scaledFace.compress(Bitmap.CompressFormat.JPEG, 100, this)
                flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            croppedFace.recycle()
            bitMapImage.recycle()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
        Log.d(TAG,"writeToFile end")
    }


    companion object {
        /**
         * Tag for the [Log].
         */
        private const val TAG = "ImageSaver"
        const val SCALE_HEIGHT = 420 //face will be (SCALE_FACTOR)x(SCALE_FACTOR)
        const val SCALE_WIDTH = 260 //face will be (SCALE_FACTOR)x(SCALE_FACTOR)

        fun saveTmpImg(faceImg: Bitmap, context: Context): File {
            Log.d(TAG,"writeToFile start")
            var output: FileOutputStream? = null
            val tmpFile = File(context.cacheDir, "tmp.txt")
            try {
                output = FileOutputStream(tmpFile).apply {
                    faceImg.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    flush()
                }
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            } finally {
                output?.let {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        Log.e(TAG, e.toString())
                    }
                }
            }
            Log.d(TAG,"writeToFile end")
            return tmpFile
        }
    }
}
