package com.android.noam.face_detector_example

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.Button
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var  takePictureBtn : Button
    private lateinit var textureView : TextureView

    companion object {
        private val ORIENTATIONS = SparseIntArray()
        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
        private val REQUEST_CAMERA_PERMISSION = 200
    }

    private lateinit var cameraID: String
    protected var cameraDevice: CameraDevice? = null
    protected var cameraCaptureSession: CameraCaptureSession? = null
    protected lateinit var captureRequest: CaptureRequest
    protected lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageDimension: Size
    private lateinit var imageReader: ImageReader
    private lateinit var file : File
    private var mflashSupported : Boolean = false
    private var mBackgroundHandler : Handler? = null
    private var mBackgroundThread : HandlerThread? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        takePictureBtn = btn_takepicture!!
        textureView = texture_view!!

        takePictureBtn.setOnClickListener {
            //            takePicture()
        }

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

    @SuppressLint("MissingPermission")
    private fun openCamera() = runWithPermissions(android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.d(TAG, "Opening Camera.")
        try {
            cameraID = manager.cameraIdList.single {
                val charateristics = manager.getCameraCharacteristics(it)
                val facing = charateristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_FRONT
            }
            val frontCamCharacteristics = manager.getCameraCharacteristics(cameraID)
            val map = frontCamCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            manager.openCamera(cameraID, stateCallBack, null)
        }catch (e : CameraAccessException){
            e.printStackTrace()
        }
    }

    protected fun createCameraPreview(){
        try {
            val texture = textureView.surfaceTexture!!
            // Setting Image size to textureView available width.
            texture.setDefaultBufferSize(textureView.width, textureView.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface),  object : CameraCaptureSession.StateCallback(){
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

    private fun updatePreview() {
        if (cameraDevice == null)
            Log.e(TAG,"updatePreview: cameraDevice is null")
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try{
            cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder.build(),null, mBackgroundHandler)
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
