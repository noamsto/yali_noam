package com.android.noam.sellfyattendance.face.operations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.media.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BmpOperations {

    companion object {
        private const val TAG = "BmpOperations"

        @JvmStatic
        fun writeBmpToFile(bmp : Bitmap, file: File) {
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(file).apply {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, this)
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
        }

        fun writeBmpToTmpFile(bmp: Bitmap, context: Context): File {
            Log.d(TAG,"writeToFile start")
            val tmpFile = File(context.cacheDir, "tmp.jpg")
            writeBmpToFile(bmp, tmpFile)
            return tmpFile
        }
    }

    fun convertToBmpAndRotate(image: Image, rotation: Int ): Bitmap {
        Log.d(TAG,"convertToBmpAndRotate start")
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var bitMapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitMapImage = rotateImage(bitMapImage, rotation)
        bitMapImage = Bitmap.createScaledBitmap(bitMapImage,
                (bitMapImage.width * 0.5).toInt(), (bitMapImage.height * 0.5).toInt(), true)
        Log.d(TAG,"convertToBmpAndRotate end")
        return bitMapImage
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        Log.d(TAG,"rotateImage start")
        val mirrorMatrix = Matrix()
        mirrorMatrix.preScale(-1.0f, 1.0f)
        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, rotateMatrix, true)
        val mirroredImg = Bitmap.createBitmap(rotatedImg, 0, 0, rotatedImg.width, rotatedImg.height, mirrorMatrix, true)
        img.recycle()
        rotatedImg.recycle()
        Log.d(TAG,"rotateImage end")
        return mirroredImg
    }
}