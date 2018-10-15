package com.android.noam.javacvplayground

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_create_new_set.*
import kotlinx.android.synthetic.main.activity_manage_students.*
import kotlinx.android.synthetic.main.list_view_student_item.view.*
import org.jetbrains.anko.toast
import java.io.File

class ManageStudentsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CreateNewSetActivity"
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
        initSamplesDir(rootDir)
    }

    private fun initSamplesDir(rootDir: File) {
        samplesDir = File(rootDir, "Samples")
        if (!samplesDir.exists()) {
            Log.d(CreateNewSetActivity.TAG, "Creating New StudentSet dir: ${samplesDir.absolutePath}")
            samplesDir.mkdir()
        }
        readStudentSets()
    }
    private fun readStudentSets() {
        if (samplesDir != null) {
            Log.d(TAG, "Searching for all faces sets in ${samplesDir.absolutePath}")
            for (studentDir in samplesDir.listFiles()) {
                var numOfSamples = 0
                studentDir.walkTopDown().forEach {
                    if (it.parentFile != studentDir && it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                        numOfSamples++
                    }
                }
                val studentSet = StudentSet(
                        studentDir.nameWithoutExtension.filter { it.isLetter() },
                        studentDir, studentSetList.lastIndex, numOfSamples)
                studentSetList.add(studentSet)
                Log.d(TAG, "Found ${studentSet.name}, path: ${studentDir.path}, " +
                        "id: ${studentSet.id} and  ${studentSet.samplesCount} samples")
            }
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

    fun createNewStudentDir(newStudentName: String) {
        val studentID = studentSetList.lastIndex.plus(1)
        val studentDir = File(samplesDir, "$newStudentName$studentID")
        if (!studentDir.exists()) {
            Log.d(CreateNewSetActivity.TAG, "Creating New StudentSet dir: ${studentDir.absolutePath}")
            studentDir.mkdir()
            studentSetList.add(StudentSet(newStudentName, studentDir, studentID, 0))
            studentSetAdapter.notifyDataSetChanged()
        } else {
            toast("StudentSet dir:${studentDir.absolutePath} already exists.")
        }
    }


    class StudentsSetAdapter(private val activity: Activity, private val studentsList: ArrayList<StudentSet>) : BaseAdapter() {
        override fun getItem(p0: Int) = studentsList[p0]

        override fun getItemId(p0: Int) = p0.toLong()

        override fun getCount() = studentsList.size

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val studentListItem = if (p1 != null) {
                p1
            } else {
                val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(R.layout.list_view_student_item, null)
            }
            val student = studentsList[p0]
            studentListItem.student_name.text = student.name
            studentListItem.num_of_samples.text = "${student.samplesCount} Samples"
            return studentListItem
        }
    }
}
