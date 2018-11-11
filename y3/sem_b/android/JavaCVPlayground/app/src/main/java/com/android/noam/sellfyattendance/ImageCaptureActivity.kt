package com.android.noam.sellfyattendance

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.noam.sellfyattendance.ManageStudentsActivity.Companion.CURRENT_STUDENT_DIR
import com.android.noam.sellfyattendance.camera.Camera2Fragment
import com.android.noam.sellfyattendance.camera.Camera2Fragment.OnCameraFragmentInteractionListener
import com.android.noam.sellfyattendance.face.operations.BmpOperations
import com.android.noam.sellfyattendance.face.operations.FaceDetector
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_image_capture.*
import org.jetbrains.anko.toast
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class ImageCaptureActivity : AppCompatActivity(), OnCameraFragmentInteractionListener,
        OnSuccessListener<Bitmap>, OnFailureListener {


    companion object {
        private const val TAG = "ImageCaptureActivity"
    }

    private lateinit var picFile : File
    private lateinit var studentDir : File
    private var faceInd = 0
    private val shouldThrottle = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.camera_frag_container, Camera2Fragment.newInstance())
                .commit()

        studentDir = intent.extras.get(CURRENT_STUDENT_DIR) as File
        if (! studentDir.exists() )
            studentDir.mkdir()
        if (!studentDir.listFiles().isEmpty())
            faceInd = studentDir.listFiles().map { it.nameWithoutExtension.toInt()}.max()!! + 1
        picFile = File(studentDir, "$faceInd.jpg")
    }

    override fun onImageAvailable(image: Image, rotationValue: Int) {
        if (shouldThrottle.get()){
            toast("Still processing previous image.")
            image.close()
            return
        }
        shouldThrottle.set(true)
        FaceDetector(image, rotationValue, this, this).start()
    }

    override fun onFailure(p0: Exception) {
        shouldThrottle.set(false)
        toast(p0.message.toString())
    }

    override fun onSuccess(croppedFace: Bitmap) {
        shouldThrottle.set(false)
        if (croppedFaceView.drawable != null)
            (croppedFaceView.drawable as BitmapDrawable).bitmap.recycle()
        croppedFaceView.setImageBitmap(croppedFace)
        BmpOperations.writeBmpToFile(croppedFace, picFile)
        toast("Saved face under:${picFile.absolutePath}")
        picFile = File(studentDir, "${++faceInd}.jpg")
    }
}
