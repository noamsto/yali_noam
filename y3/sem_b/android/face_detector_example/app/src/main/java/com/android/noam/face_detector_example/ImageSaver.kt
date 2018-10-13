package com.android.noam.face_detector_example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.absoluteValue

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

        private val rotation: Int
) : Runnable, OnSuccessListener<List<FirebaseVisionFace>>, OnFailureListener {
    private val scaleFactor = 120 //face will be (scaleFactor)x(scaleFactor)
    lateinit var bitMapImage: Bitmap
    lateinit var croppedFace: Bitmap
    lateinit var scaledFace: Bitmap

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
            croppedFace = Bitmap.createBitmap(bitMapImage, bounds.left.absoluteValue, bounds.top.absoluteValue, bounds.width(), bounds.height())
            scaledFace = Bitmap.createScaledBitmap(croppedFace, scaleFactor, scaleFactor, false)
            writeToFile()
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
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    companion object {
        /**
         * Tag for the [Log].
         */
        private val TAG = "ImageSaver"
    }
}
