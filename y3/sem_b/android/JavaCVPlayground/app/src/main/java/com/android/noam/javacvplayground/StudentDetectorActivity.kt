package com.android.noam.javacvplayground

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.android.noam.javacvplayground.face.operations.FaceRecognizer
import com.android.noam.javacvplayground.face.operations.ImageSaver
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_student_detector.*
import org.bytedeco.javacpp.opencv_core
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class StudentDetectorActivity : AppCompatActivity(), OnModelReadyListener, OnSuccessListener<Bitmap>, OnFailureListener {



    private lateinit var classObj : ClassObj
    private lateinit var eigenFaces: EigenFaces
    private val modelReady = AtomicBoolean(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detector)

        btn_sign_me.setOnClickListener{
            captureStillPicture(it)
        }

        textureView = texture_view!!



        classObj = intent.extras.getSerializable(SelectClassActivity.CLASS_OBJ_TAG) as ClassObj


        eigenFaces = EigenFaces(classObj.studentList, this)


        doAsync {
            eigenFaces.readAllStudentsFaces()
            eigenFaces.trainModel()
        }
    }

    override fun onModelReady() {
        modelReady.set(true)
    }

    override fun onFailure(p0: Exception) {
        toast(p0.message.toString())
    }

    override fun onSuccess(p0: Bitmap) {
        if (!modelReady.get()){
            toast("Recognition model not ready yet!")
            return
        }

        val tmpImg = ImageSaver.saveTmpImg(p0, this)
        val studentId = eigenFaces.predictImage(tmpImg.absolutePath)

        val student = classObj.studentList.single { it.id == studentId }
        predicted_student_id.text = student.id.toString()
        predicted_student_name.text = student.name
    }


    private lateinit var textureView : AutoFitTextureView

    companion object {
        private const val TAG = "StudentDetectorActivity"
        private const val WIDTH = 920
        private val ORIENTATIONS = SparseIntArray()
        private val shouldThrottle = AtomicBoolean(false)
        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private lateinit var cameraID: String
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private lateinit var captureRequest: CaptureRequest
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageDimension: Size
    private lateinit var imageReader: ImageReader
    private var mBackgroundHandler : Handler? = null
    private var mBackgroundThread : HandlerThread? = null
    private var rotationValue = 0


    private val textureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }

    }

    private val stateCallBack = object : CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice?) {
            Log.d(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            Log.e(TAG, "Error $error occurred")
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun startBackgroundThread(){
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread(){
        mBackgroundThread!!.quitSafely()
        try{
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        }catch ( e : InterruptedException ){
            e.printStackTrace()
        }
    }

    private var sensorOrientation = 0

    @SuppressLint("MissingPermission")
    private fun openCamera() = runWithPermissions(android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.d(TAG, "openCamera start")
        try {
            cameraID = manager.cameraIdList.single {
                val characteristics = manager.getCameraCharacteristics(it)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_FRONT
            }
            val frontCamCharacteristics = manager.getCameraCharacteristics(cameraID)
            val map = frontCamCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            sensorOrientation = frontCamCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            manager.openCamera(cameraID, stateCallBack, null)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
        Log.d(TAG, "openCamera end")
    }

    private fun createCameraPreview(){
        try {
            val texture = textureView.surfaceTexture!!
            // Setting Image size to textureView available width.
            texture.setDefaultBufferSize(textureView.width, textureView.height)
            textureView.setAspectRatio(textureView.width,textureView.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            val aspectRatio = textureView.width/textureView.height.toFloat()
            val width = WIDTH
            val height = width/aspectRatio
            Log.i(TAG, "Imreader dimensions: width:$width, height:$height")

            imageReader = ImageReader.newInstance(width, height.toInt(),
                    ImageFormat.JPEG, /*maxImages*/ 5).apply {
                setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler)
            }
            cameraDevice!!.createCaptureSession(Arrays.asList(surface, imageReader.surface),  object : CameraCaptureSession.StateCallback(){
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    Log.e(TAG, "onConfigureFailed: failed here.")
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    if (cameraDevice == null)
                        return
                    cameraCaptureSession = session
                    updatePreview()
                }
            }, mBackgroundHandler )
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still mediaImage is ready to be saved.
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        Log.d(TAG, "onImageAvailableListener start")
        Log.d(TAG, "image add")
        mBackgroundHandler?.post(
                FaceRecognizer(it.acquireNextImage(), rotationValue,
                        this, this))
        Log.d(TAG, "onImageAvailableListener end")
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     */
    @Suppress("UNUSED_PARAMETER")
    private fun captureStillPicture(view: View) {
        if (shouldThrottle.get()){
            toast("Still working on previous picture.")
            return
        }
        shouldThrottle.set(true)
        Log.d(TAG, "captureStillPicture start" )
        try {
            if (cameraDevice == null) return
            val rotation = windowManager.defaultDisplay.rotation

            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = cameraDevice?.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader.surface)

                // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
                // We have to take that into account and rotate JPEG properly.
                // For devices with orientation of 90, we return our mapping from ORIENTATIONS.
                // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
                rotationValue = (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360
                set(CaptureRequest.JPEG_ORIENTATION, rotationValue)

                // Use the same AE and AF modes as the preview.
                set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {
                    cameraCaptureSession?.setRepeatingRequest(captureRequest, null,
                            mBackgroundHandler)
                    shouldThrottle.set(false)
                }
            }
            cameraCaptureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        Log.d(TAG, "captureStillPicture end" )
    }

    private fun updatePreview() {
        if (cameraDevice == null)
            Log.e(TAG,"updatePreview: cameraDevice is null")
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try{
            captureRequest = captureRequestBuilder.build()
            cameraCaptureSession?.setRepeatingRequest(captureRequest,null, mBackgroundHandler)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "OnResume")
        startBackgroundThread()
        if( textureView.isAvailable){
            openCamera()
        }else{
            textureView.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        stopBackgroundThread()
        super.onPause()
    }

}
