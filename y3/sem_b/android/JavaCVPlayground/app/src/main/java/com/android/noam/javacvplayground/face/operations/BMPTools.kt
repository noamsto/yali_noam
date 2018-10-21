package com.android.noam.javacvplayground.face.operations

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.media.Image


class BMPTools {

    companion object {
        private const val TAG = "BMPTools"
    }

    fun convertToBmpAndRotate(image: Image, rotation: Int ): Bitmap {
        Log.d(TAG,"convertToBmpAndRotate start")
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var bitMapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitMapImage = rotateImage(bitMapImage, rotation)
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