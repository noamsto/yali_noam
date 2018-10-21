package com.android.noam.javacvplayground


import android.util.Log
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face.EigenFaceRecognizer
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import java.io.FileFilter
import java.nio.IntBuffer
import java.util.*


class EigenFaces(private val selectedStudents : SortedSet<StudentSet>, private val onModelReadyListener: OnModelReadyListener) {

    private val TAG = "EigenFaces"

    private var images = MatVector()
    private var labels = Vector<Int>()
    private val eigenFaceRecognizer: EigenFaceRecognizer = EigenFaceRecognizer.create()
    private lateinit var testImage : Mat
    private var maxConfidence = 0.0

    private var imHeight: Int = 0
    private var imWidth: Int = 0


    fun readAllStudentsFaces() {
        var image: Mat? = null
        var label = 0
        selectedStudents.forEach {
            label = it.name.filter { it.isDigit() }.toInt()
            it.dir.listFiles(FileFilter { it.name.matches("""pgm|jpg|bmp|png""".toRegex()) })
                    .forEach {
                        image = opencv_imgcodecs.imread(it.path, CV_LOAD_IMAGE_GRAYSCALE)
                        images.push_back(image)
                        labels.addElement(label)
                        Log.d(TAG, "Read ${it.path} with Label $label")
                    }
        }
        testImage = image!!
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

    private fun calcMaxConfidence() {

        if (eigenFaceRecognizer.empty()) {
            Log.e(TAG, "Called predict without training model.")
            return
        }
        val confidence = DoublePointer(1)
        val label = IntPointer(1)
        try {
            eigenFaceRecognizer.predict(testImage, label, confidence)
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message)
            return
        }
        Log.d(TAG, "Max Confidence value is: ${confidence.get(0)}")
        maxConfidence = confidence.get(0)
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
        calcMaxConfidence()
        onModelReadyListener.onModelReady()
        return true
    }
}

interface  OnModelReadyListener{
    fun onModelReady()
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



