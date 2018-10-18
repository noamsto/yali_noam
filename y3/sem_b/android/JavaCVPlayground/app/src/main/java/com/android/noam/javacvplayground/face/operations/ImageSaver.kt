package com.android.noam.javacvplayground.face.operations

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
        private val detectFaceFilureListener : OnFailureListener

) : Runnable, OnSuccessListener<List<FirebaseVisionFace>>, OnFailureListener {
    private val scaleFactor = 100 //face will be (scaleFactor)x(scaleFactor)
    private lateinit var bitMapImage: Bitmap
    private lateinit var croppedFace: Bitmap
    private lateinit var scaledFace: Bitmap
    private val compWithNull = Comparator<Float?> { p0, p1 ->
        if (p0 == null)
            if (p1 == null)
                0
            else
                1
        else
            if (p1== null)
                -1
            else
                (p0 - p1).toInt()
    }

    override fun run() {
        convertToBmpAndRotate()
        FaceDetect(bitMapImage, this, this)
    }

    override fun onFailure(p0: Exception) {
        Log.d(TAG, "Failed To detect a Face.")
        image.close()
    }

    override fun onSuccess(visionFacesList: List<FirebaseVisionFace>) {
        // Task completed successfully
        // ...
        if (visionFacesList.isEmpty()) {
            Log.d(TAG, "No Face Detected.")
            image.close()
            return
        }
        visionFacesList.forEach {
            Log.d(TAG, "Detected Face: in this bounds ${it.boundingBox}")
            cropFace(it)
        }

    }

    private fun cropFace(firebaseVisionFace: FirebaseVisionFace){
        val rightEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
        val leftEye = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
        val rightEar = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)
        val leftEar = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
        val rightCheek = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        val leftCheek = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK)
        val bottomMouth = firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.BOTTOM_MOUTH)

        if (leftEye == null || rightEye == null || bottomMouth == null){
            image.close()
            bitMapImage.recycle()
            detectFaceFilureListener.onFailure(java.lang.Exception("Couldn't detect your eyes."))
            return
        }

        var minX = arrayOf(rightEye.position.x,  rightEar?.position?.x, rightCheek?.position?.x).minWith(compWithNull)!!.toInt()
        var maxX = arrayOf(leftEye.position.x, leftEar?.position?.x, leftCheek?.position?.x).minWith(compWithNull)!!.toInt()
        var minY = arrayOf(
                rightEye.position?.y, rightEar?.position?.y, leftEar?.position?.y,
                leftEye.position?.y).minWith(compWithNull)!!.toInt()
        var maxY = bottomMouth.position.y.toInt()

        val eyeDist = (leftEye.position.x - rightEye.position.x).toInt()
        minX -= eyeDist / 2
        minY -= (eyeDist * 1.5).toInt()
        maxX += eyeDist/2
        maxY += eyeDist/2
        val width = maxX - minX
        val height = maxY - minY
        minY = max(0, minY)
        minX = max(0, minX)
        if (minY + height >= bitMapImage.height || minX + width >= bitMapImage.width) {
            image.close()
            bitMapImage.recycle()
            detectFaceFilureListener.onFailure(java.lang.Exception("Move further from the camera."))
            return
        }

        Log.d(TAG, "Cropping image to top left: $minX, $minY and width:$width, height:$height")
        croppedFace = Bitmap.createBitmap(bitMapImage,minX, minY, width, height)
        verifyAndSaveFace()
        return
    }

    private fun verifyAndSaveFace() {
        FaceDetect(croppedFace, OnSuccessListener {
            it.forEach {
                val leftEye = it.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
                val rightEye = it.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
                if (leftEye == null || rightEye == null )
                {
                    croppedFace.recycle()
                    bitMapImage.recycle()
                    image.close()
                    detectFaceFilureListener.onFailure(java.lang.Exception("Face detection failed."))
                }else if (croppedFace.width - leftEye.position.x  - rightEye.position.x >
                        croppedFace.width/10){
                    croppedFace.recycle()
                    bitMapImage.recycle()
                    image.close()
                    detectFaceFilureListener.onFailure(java.lang.Exception("Face detectionFailed"))
                }else{
                    scaledFace = Bitmap.createScaledBitmap(croppedFace, scaleFactor, scaleFactor, false)
                    croppedFaceViewer.setImageBitmap(scaledFace)
                    writeToFile()
                    detectFaceSuccessListener.onSuccess("Face cropped and saved.")
                }
            }
        }, OnFailureListener {
            image.close()
            croppedFace.recycle()
            bitMapImage.recycle()
            detectFaceFilureListener.onFailure(it)
            Log.d(TAG, "Face detection Error ${it.message}")
        } )
    }

    private fun convertToBmpAndRotate() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        bitMapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitMapImage = rotateImage(bitMapImage, rotation)
    }

    private fun writeToFile() {
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
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }


    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val mirrorMatrix = Matrix()
        mirrorMatrix.preScale(-1.0f, 1.0f)
        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, rotateMatrix, true)
        val mirroredImg = Bitmap.createBitmap(rotatedImg, 0, 0, rotatedImg.width, rotatedImg.height, mirrorMatrix, true)
        img.recycle()
        rotatedImg.recycle()
        return mirroredImg
    }

    companion object {
        /**
         * Tag for the [Log].
         */
        private const val TAG = "ImageSaver"
    }
}
