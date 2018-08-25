package com.android.noam.javacvplayground


import android.util.Log
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face.EigenFaceRecognizer
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import java.io.File
import java.lang.RuntimeException
import java.nio.IntBuffer
import java.util.*


class EigenFacesPrep(csvFileName: String, private val facesDirPath: String) {

    private val TAG = "EigenFacesPrep"

    private var images = MatVector()
    private var labels = Vector<Int>()
    private val eigenFaceRecognizer: EigenFaceRecognizer = EigenFaceRecognizer.create()


    private var imHeight: Int = 0
    private var imWidth: Int = 0

    init {
        if (File("$facesDirPath/$csvFileName").exists())
            Log.d(TAG, "Successfully opened $csvFileName")
        else
            Log.d(TAG, "Failed to open $csvFileName")
    }


    fun readImagesFromDir() {
        var image: Mat
        var label: Int
        val facesDir = File(facesDirPath)
        for (sampleDir in facesDir.listFiles()) {
            if (!sampleDir.isDirectory)
                continue
            for (sample in sampleDir.listFiles()) {
                if (sample.extension != "pgm")
                    continue
                image = opencv_imgcodecs.imread(sample.path, CV_LOAD_IMAGE_GRAYSCALE)
                label = sampleDir.name.substringAfter("s").toInt() - 1
                images.push_back(image)
                labels.addElement(label)
                Log.d(TAG, "Read ${sample.path} with Label $label")
            }
        }
        imHeight = images[0].arrayHeight()
        imWidth = images[0].arrayWidth()
    }

    fun predict_image(img_path: String): Int {

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
        val predictedLabel = label.get(0)
        Log.d(TAG, "predicted $predictedLabel, Confidence value: ${confidence.get(0)}")
        return predictedLabel
    }

    fun train_model(): Boolean {
        if (images.empty() || labels.isEmpty()) {
            Log.e(TAG, "Called train model without reading any images or labels.")
            return false
        }

        val labelMat = Mat(images.size().toInt(), 1, CV_32SC1)
        val intBuffer = labelMat.createBuffer<IntBuffer>()

        for (i in 0 until labels.size - 1) {
            intBuffer.put(i, labels[i])
        }
        Log.d(TAG, "Training model with ${images.size()} samples and ${labelMat.size()} labels. ")
        eigenFaceRecognizer.train(images, labelMat)
        return true
    }

    fun runTest() {
        val testsample_path = "$facesDirPath/11_7.pgm"
        readImagesFromDir()
        train_model()
        predict_image(testsample_path)
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


}
