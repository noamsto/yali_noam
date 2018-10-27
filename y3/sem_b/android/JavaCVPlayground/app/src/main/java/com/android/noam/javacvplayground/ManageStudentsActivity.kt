package com.android.noam.javacvplayground

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.BaseAdapter
import com.android.noam.javacvplayground.face.operations.ImageCaptureActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_manage_students.*
import org.jetbrains.anko.toast
import java.io.File

class ManageStudentsActivity : AppCompatActivity(), AdapterView.OnItemClickListener{


    companion object {
        const val TAG = "ManageStudentsActivity"
        const val STUDENTS_DIR = "samples"
        const val CURRENT_STUDENT_DIR = "CURRENT_STUDENT_DIR"
    }

    var studentSetList: ArrayList<StudentSet> = ArrayList()
    private lateinit var rootDir: File
    lateinit var samplesDir: File
    lateinit var studentSetAdapter: BaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)
        rootDir = intent.getSerializableExtra(MainActivity.APP_DIR_NAME)!! as File
        studentSetAdapter = StudentsSetAdapter(this, studentSetList)
        student_list_view.adapter = studentSetAdapter
        student_list_view.onItemClickListener = this
        initSamplesDir(rootDir)
    }

    private fun initSamplesDir(rootDir: File) {
        samplesDir = File(rootDir, STUDENTS_DIR)
        if (!samplesDir.exists()) {
            Log.d(CreateNewClassActivity.TAG, "Creating New StudentSet dir: ${samplesDir.absolutePath}")
            samplesDir.mkdir()
        }
    }


    override fun onPostResume() {
        super.onPostResume()
        studentSetList.clear()
        readStudentSets()
    }

    private fun readStudentSets() {

        Log.d(TAG, "Searching for all faces sets in ${samplesDir.absolutePath}")
        for (studentDir in samplesDir.listFiles()) {
            var numOfSamples = 0
            studentDir.listFiles().forEach {
                if (it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                    numOfSamples += 1
                }
            }
            val studentSet = StudentSet(
                    studentDir.nameWithoutExtension.filter { it.isLetter() || it.isWhitespace() },
                    studentDir, studentDir.nameWithoutExtension.filter { it.isDigit() }.toInt() ,
                    numOfSamples)
            studentSetList.add(studentSet)
            Log.d(TAG, "Found ${studentSet.name}, path: ${studentDir.path}, " +
                    "id: ${studentSet.id} and  ${studentSet.samplesCount} samples")
        }

        studentSetAdapter.notifyDataSetChanged()
    }
    fun submitNewStudent(view: View) {
        val newStudentName = student_name_ET.text.toString()
        if (newStudentName.isNotBlank()) {
            createNewStudentDir(newStudentName)
            student_name_ET.setText("")
        }
    }
    private fun createNewStudentDir(newStudentName: String) {
        val studentID = studentSetList.lastIndex.plus(1)
        val studentDir = File(samplesDir, "$newStudentName$studentID")
        if (!studentDir.exists()) {
            Log.d(CreateNewClassActivity.TAG, "Creating New StudentSet dir: ${studentDir.absolutePath}")
            studentDir.mkdir()
            studentSetList.add(StudentSet(newStudentName, studentDir, studentID, 0))
            studentSetAdapter.notifyDataSetChanged()
        } else {
            toast("StudentSet dir:${studentDir.absolutePath} already exists.")
        }
    }

    private fun captureSelfies(studentSet: StudentSet) = runWithPermissions(
            android.Manifest.permission.CAMERA ) {
        val imageCaptureIntent = Intent(this, ImageCaptureActivity::class.java)
        imageCaptureIntent.putExtra(CURRENT_STUDENT_DIR, studentSet.dir)
        startActivity(imageCaptureIntent)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        val selectedStudentSet = studentSetList[p2]
        captureSelfies(selectedStudentSet)
    }


}
