package com.android.noam.sellfyattendance

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.android.noam.sellfyattendance.camera.Camera2Fragment
import com.android.noam.sellfyattendance.camera.Camera2Fragment.OnCameraFragmentInteractionListener
import com.android.noam.sellfyattendance.datasets.ClassObj
import com.android.noam.sellfyattendance.datasets.StudentSet
import com.android.noam.sellfyattendance.face.operations.BmpOperations
import com.android.noam.sellfyattendance.face.operations.FaceDetector
import com.android.noam.sellfyattendance.face.operations.FisherFaces
import com.android.noam.sellfyattendance.face.operations.OnModelReadyListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_student_attandence.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.concurrent.atomic.AtomicBoolean

class StudentAttendanceActivity : AppCompatActivity(), OnCameraFragmentInteractionListener,
        OnModelReadyListener, OnSuccessListener<Bitmap>, OnFailureListener {

    private val modelReady = AtomicBoolean(false)
    private val shouldThrottle = AtomicBoolean(false)
    private val arrivedStudents = ArrayList<StudentSet>()
    private lateinit var classObj: ClassObj
    private lateinit var fisherFaces: FisherFaces


    override fun onImageAvailable(image: Image, rotationValue: Int) {
        if (shouldThrottle.get()){
            toast("Still processing previous image.")
            image.close()
            return
        }
        shouldThrottle.set(true)
        Log.d(TAG, "OnImageAvailable!.")
        FaceDetector(image, rotationValue, this, this).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) { savedInstanceState
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attandence)
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .add(R.id.camera_frag_container, Camera2Fragment.newInstance())
                .commit()

        classObj = intent.extras.getSerializable(SelectClassActivity.CLASS_OBJ_TAG) as ClassObj
        fisherFaces = FisherFaces(classObj.studentList, this)
        doAsync {
            fisherFaces.readAllStudentsFaces()
            fisherFaces.trainModel()
        }
    }


    override fun onModelReady() {
        modelReady.set(true)
    }


    fun finishSigning(view: View) {
        val arrivedStudentsIntent = Intent(this, ArrivedStudentsActivity::class.java)
        arrivedStudentsIntent.putExtra(ARRIVED_STUDENTS_LIST, arrivedStudents)
        startActivity(arrivedStudentsIntent)

    }

    fun fixLastAttendance(view: View) {
        arrivedStudents.removeAt(arrivedStudents.lastIndex)
        btn_fix.visibility = View.GONE
    }

    override fun onFailure(p0: Exception) {
        Log.d(TAG, "Failed to detect Face.")
        toast(p0.message.toString())
        shouldThrottle.set(false)
    }

    override fun onSuccess(bmp: Bitmap) {
        shouldThrottle.set(false)
        if (!modelReady.get()) {
            toast("Recognition model not ready yet!")
            return
        }
        Log.d(TAG, "Detected Face successfully.")
        val tmpImgFile = BmpOperations.writeBmpToTmpFile(bmp, this)
        bmp.recycle()
        val studentId = fisherFaces.predictImage(tmpImgFile.absolutePath)
        if (studentId == -1) {
            onFailure(java.lang.Exception("I'm not sure, please try again."))
            return
        }
        val student = classObj.studentList.single { it.id == studentId }
        predicted_student_id.text = student.id.toString()
        predicted_student_name.text = student.name
        if (!arrivedStudents.contains(student)) {
            arrivedStudents.add(student)
            if (btn_fix.visibility == View.GONE) {
                btn_fix.visibility = View.VISIBLE
                doAsync {
                    Thread.sleep(4000)
                    runOnUiThread {
                        btn_fix.visibility = View.GONE
                    }
                }
            }
        } else {
            toast("${student.name} is Already Registered :)")
            btn_fix.visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "StudentAttnActivity"
        const val ARRIVED_STUDENTS_LIST = "arrived_student_list"
    }
}
