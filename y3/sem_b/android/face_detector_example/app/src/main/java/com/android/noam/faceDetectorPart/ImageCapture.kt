package com.android.noam.faceDetectorPart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import com.google.android.gms.tasks.OnSuccessListener
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_image_capture.*
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

class ImageCapture : AppCompatActivity(), OnSuccessListener<String> {
    override fun onSuccess(p0: String?) {
        toast("Saved face under:${picFile.absolutePath}")
        picFile = File(sampleDir, "${++faceInd}.jpg")
    }

    private lateinit var  takePictureBtn : Button
    private lateinit var textureView : TextureView

    companion object {
        private const val TAG = "ImageCapture"
        private val ORIENTATIONS = SparseIntArray()
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
    private lateinit var picFile : File
    private lateinit var sampleDir : File
    private var faceInd = 0
    private var mBackgroundHandler : Handler? = null
    private var mBackgroundThread : HandlerThread? = null
    private var rotationValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)

        takePictureBtn = btn_takepicture!!
        textureView = texture_view!!

        takePictureBtn.setOnClickListener {
            captureStillPicture(it)
        }

        val facesDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Faces")



        val yaliNoamDir = File(facesDir, "YaliNoam" )
        if (! yaliNoamDir.exists() )
            yaliNoamDir.mkdir()

        sampleDir = File(yaliNoamDir, "Gal3")
        if (! sampleDir.exists() )
            sampleDir.mkdir()
        picFile = File(sampleDir, "$faceInd.jpg")
    }

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
        Log.d(TAG, "Opening Camera.")
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
    }

    private fun createCameraPreview(){
        try {
            val texture = textureView.surfaceTexture!!
            // Setting Image size to textureView available width.
            texture.setDefaultBufferSize(textureView.width, textureView.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            val aspectRatio = imageDimension.width/imageDimension.height
            val width = 480
            imageReader = ImageReader.newInstance(width, width/aspectRatio,
                    ImageFormat.JPEG, /*maxImages*/ 10).apply {
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
        mBackgroundHandler?.post(ImageSaver(it.acquireNextImage(), picFile, rotationValue, croppedFaceView, this))
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     */
    @Suppress("UNUSED_PARAMETER")
    private fun captureStillPicture(view: View) {
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
                    Log.d(TAG, picFile.toString())
                    cameraCaptureSession?.setRepeatingRequest(captureRequest, null,
                            mBackgroundHandler)
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
