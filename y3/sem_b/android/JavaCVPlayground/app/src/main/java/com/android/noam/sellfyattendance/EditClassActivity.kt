package com.android.noam.sellfyattendance

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.android.noam.sellfyattendance.ManageStudentsActivity.Companion.STUDENTS_DIR
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.CLASS_LIST_TAG
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.CLASS_OBJ_TAG
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.EDIT_CLASS
import com.android.noam.sellfyattendance.adapters.StudentsSetAdapter
import com.android.noam.sellfyattendance.datasets.ClassObj
import com.android.noam.sellfyattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.activity_edit_class.*
import org.jetbrains.anko.toast
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNCHECKED_CAST")
class CreateNewClassActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CreateNewClassActivity"
    }
    private val allStudentList : ArrayList<StudentSet> = ArrayList()
    private val selectedStudents : TreeSet<StudentSet> = TreeSet()
    private lateinit var samplesDir : File
    private lateinit var studentListViewAdapter : StudentsSetAdapter
    private lateinit var oldClass: ClassObj
    private lateinit var classes : ArrayList<ClassObj>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_class)
        oldClass = intent.extras.get(CLASS_OBJ_TAG) as ClassObj
        classes = intent.extras.get(CLASS_LIST_TAG) as ArrayList<ClassObj>
        selectedStudents.addAll(oldClass.studentList)
        if (!oldClass.isNew){
            class_name.setText(oldClass.name)
            confirm_edit_class_btn.text = getString(R.string.confirm_btn)
        }
        samplesDir = intent.extras.getSerializable(STUDENTS_DIR) as File
        studentListViewAdapter = StudentsSetAdapter(this, allStudentList, selectedStudents,
                markColor = Color.parseColor("#2e7d32"))
        student_list_view.adapter = studentListViewAdapter
        student_list_view.setOnItemClickListener { parent, view, position, id ->
            setSelected(allStudentList[position], view)
        }
        readAllStudents()
    }

    private fun readAllStudents() {
        for (studentDir in samplesDir.listFiles().filter { it.isDirectory }){
            if (studentDir == samplesDir )
                continue
            val name = studentDir.name.filter { it.isLetter() || it.isWhitespace()}
            val id = studentDir.name.filter { it.isDigit() }.toInt()
            val numOfSamples = studentDir.listFiles().size
            val studentSet = StudentSet(name, studentDir, id, numOfSamples)
            allStudentList.add(studentSet)
        }
        studentListViewAdapter.notifyDataSetChanged()
    }


    private fun setSelected(currentItem: StudentSet, view: View) {
        if (currentItem in selectedStudents)
            selectedStudents.remove(currentItem)
        else
            selectedStudents.add(currentItem)
        studentListViewAdapter.notifyDataSetChanged()
    }

    @Suppress("UNUSED_PARAMETER")
    fun submitClass(view: View){
        if (class_name.text.isBlank()){
            toast("Please fill Class name.")
            return
        }else{
            classes.forEach {
                if (!it.isNew  && it.name == class_name.text.toString()) {
                    toast("Class already exists!")
                    return
                }
            }
        }
        val result = Intent()
        result.putExtra(CLASS_OBJ_TAG, ClassObj(class_name.text.toString(), selectedStudents.size,
                selectedStudents))
        setResult(EDIT_CLASS, result)
        this.finish()
    }
}
