package com.android.noam.faceDetectorPart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

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
        private val detectFaceSuccessListener: OnSuccessListener<String>

) : Runnable, OnSuccessListener<List<FirebaseVisionFace>>, OnFailureListener {
    private val scaleFactor = 120 //face will be (scaleFactor)x(scaleFactor)
    private lateinit var bitMapImage: Bitmap
    private lateinit var croppedFace: Bitmap
    private lateinit var scaledFace: Bitmap

    override fun run() {
        convertToBmpAndRotate()
        FaceDetect(bitMapImage, this, this)
    }

    override fun onFailure(p0: Exception) {
        Log.d(TAG, "Failed To detect a Face.")
        image.close()
    }

    override fun onSuccess(p0: List<FirebaseVisionFace>) {
        // Task completed successfully
        // ...
        if (p0.isEmpty()) {
            Log.d(TAG, "No Face Detected.")
            image.close()
            return
        }
        p0.forEach {
            Log.d(TAG, "Detected Face: in this bounds ${it.boundingBox}")
            val bounds = it.boundingBox
            if( (bounds.left + bounds.width() > bitMapImage.width) || (bounds.top + bounds.height() > bitMapImage.height) ){
                Log.d(TAG,  "Face detection failed.")
                image.close()
                return
            }
            croppedFace = Bitmap.createBitmap(bitMapImage, max(bounds.left, 0), max(0, bounds.top), bounds.width(), bounds.height())
            scaledFace = Bitmap.createScaledBitmap(croppedFace, scaleFactor, scaleFactor, false)
            croppedFaceViewer.setImageBitmap(scaledFace)
            writeToFile()
            detectFaceSuccessListener.onSuccess("Face cropped and saved.")
            return
        }

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
