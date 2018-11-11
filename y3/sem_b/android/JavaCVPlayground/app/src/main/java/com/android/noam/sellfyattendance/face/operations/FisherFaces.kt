package com.android.noam.sellfyattendance.face.operations


import android.util.Log
import com.android.noam.sellfyattendance.datasets.StudentSet
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.javacpp.opencv_core.*
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer
import org.bytedeco.javacpp.opencv_imgcodecs
import org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE
import org.bytedeco.javacpp.opencv_imgcodecs.imread
import java.io.FileFilter
import java.nio.IntBuffer
import java.util.*


class FisherFaces(private val selectedStudents : SortedSet<StudentSet>, private val onModelReadyListener: OnModelReadyListener) {

    companion object {
        private const val TAG = "FisherFaces"
        private const val THRESHOLD = 2100.0
    }

    private var images = MatVector()
    private var labels = Vector<Int>()
    private val fisherFaceRecognizer: FisherFaceRecognizer = FisherFaceRecognizer.create()
    private var imHeight: Int = 0
    private var imWidth: Int = 0


    init {
        fisherFaceRecognizer.threshold = THRESHOLD
    }

    @Suppress("NestedLambdaShadowedImplicitParameter")
    fun readAllStudentsFaces() {
        var image: Mat? = null
        var label: Int
        selectedStudents.forEach {
            label = it.id
            it.dir.listFiles(FileFilter {
                it.extension.matches("""pgm|jpg|bmp|png""".toRegex()) })
                    .forEach {
                        image = opencv_imgcodecs.imread(it.path, CV_LOAD_IMAGE_GRAYSCALE)
                        images.push_back(image)
                        labels.addElement(label)
                        Log.d(TAG, "Read ${it.path} with Label $label")
                    }
        }
        imHeight = images[0].arrayHeight()
        imWidth = images[0].arrayWidth()
    }

    private fun predictLabel(img: Mat): Int{
        val confidence = DoublePointer(1)
        val label = IntPointer(1)
        try {
            fisherFaceRecognizer.predict(img, label, confidence)
        } catch (e: RuntimeException) {
            Log.e(TAG, e.message)
            return -1
        }
        val predictedLabel = label[0]
        Log.d(TAG, "predicted $predictedLabel, Confidence value: ${confidence.get(0)}")
        return predictedLabel
    }

    fun predictImage(img_path: String): Int {

        if (fisherFaceRecognizer.empty()) {
            Log.e(TAG, "Called predict without training model.")
            return -1
        }
        val testSample = imread(img_path, CV_LOAD_IMAGE_GRAYSCALE)
        return predictLabel(testSample)
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
        fisherFaceRecognizer.train(images, labelMat)
        Log.d(TAG, "Training Finished. ")
        onModelReadyListener.onModelReady()
        return true
    }
}

interface  OnModelReadyListener{
    fun onModelReady()
}



