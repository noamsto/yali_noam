package com.android.noam.javacvplayground


import android.util.Log
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face.EigenFaceRecognizer
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import org.jetbrains.anko.doAsync
import java.io.File
import java.lang.RuntimeException
import java.nio.IntBuffer
import java.util.*


class EigenFaces(private val facesDirPath: String) {

    private val TAG = "EigenFaces"

    private var images = MatVector()
    private var labels = Vector<Int>()
    private val eigenFaceRecognizer: EigenFaceRecognizer = EigenFaceRecognizer.create()


    private var imHeight: Int = 0
    private var imWidth: Int = 0


    fun readImagesFromDir() {
        var image: Mat
        var label: Int = -1
        val facesDir = File(facesDirPath)
        facesDir.walkTopDown().forEach {
            if (it.isDirectory && it != facesDir) {
                label = it.name.filter { it.isDigit() }.toInt() - 1
            } else if (it.parentFile != facesDir &&
                    it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                image = opencv_imgcodecs.imread(it.path, CV_LOAD_IMAGE_GRAYSCALE)
                images.push_back(image)
                labels.addElement(label)
                Log.d(TAG, "Read ${it.path} with Label $label")
            }
        }
        imHeight = images[0].arrayHeight()
        imWidth = images[0].arrayWidth()
    }

    fun predictImage(img_path: String): Int {

        if (eigenFaceRecognizer.empty()) {
            Log.e(TAG, "Called predict without training model.")
            return -1
        }
        val testSample = imread(img_path, CV_LOAD_IMAGE_GRAYSCALE)
        val confidence = DoublePointer(1)
        val label = IntPointer(1)
        try {
            eigenFaceRecognizer.predict(testSample, label, confidence)
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message)
            return -1
        }
        val predictedLabel = label[0]
        Log.d(TAG, "predicted $predictedLabel, Confidence value: ${confidence.get(0)}")
        return predictedLabel
    }

    fun trainModel(): Boolean {
        if (images.empty() || labels.isEmpty()) {
            Log.e(TAG, "Called train model without reading any images or labels.")
            return false
        }

        val labelMat = Mat(images.size().toInt(), 1, CV_32SC1)
        val intBuffer = labelMat.createBuffer<IntBuffer>()

        for (i in 0 until labels.size - 1) {
            intBuffer.put(i, labels[i])
        }
        Log.d(TAG, "Training model with ${images.size()} samples and ${labels.max()?.plus(1)} labels. ")
        eigenFaceRecognizer.train(images, labelMat)
        Log.d(TAG, "Training Finished. ")
        return true
    }

    fun runTest() {
        val testSamplePath = "$facesDirPath/11_7.pgm"
        predictImage(testSamplePath)
    }

}

// maybe wont be needed
//    fun norm_0_255 (src : Mat) : Mat{
//        val dst : Mat = Mat()
//        when (src.channels()){
//            0 -> opencv_core.normalize(src ,dst ,0.0 ,255.0 , NORM_MINMAX, CV_8UC1, src)
//            3 -> opencv_core.normalize(src ,dst ,0.0 ,255.0 , NORM_MINMAX, CV_8UC3, src)
//            else -> src.copyTo(dst)
//        }
//        return dst
//    }



