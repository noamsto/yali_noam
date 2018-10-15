package com.android.noam.javacvplayground

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_create_new_set.*
import kotlinx.android.synthetic.main.list_view_student_item.view.*
import org.jetbrains.anko.*
import java.io.File

class CreateNewSetActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        const val ROOT_DIR_TAG = "ROOT_DIR_TAG"
        const val TAG = "CreateNewSetActivity"
    }
//    var studentSetList : ArrayList<StudentSet> = ArrayList()
    lateinit var rootDir : File
    lateinit var classDir : File
    lateinit var listViewAdapter: BaseAdapter
    var oldClassName = ""

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_set)
        rootDir = intent.extras.getSerializable(ROOT_DIR_TAG) as File
//
//        student_list_view.onItemClickListener = this
////        listViewAdapter = StudentsSetAdapter(this, studentSetList)
//        student_list_view.adapter = listViewAdapter
//        student_list_view.itemsCanFocus = false

    }
//
//    fun submitNewStudent(view: View){
//        val newStudentName = student_name_ET.text.toString()
//        if (newStudentName.isNotBlank()){
//            createNewStudentDir(newStudentName)
//            student_name_ET.setText("")
//        }
//    }
//
//    private fun createNewStudentDir(newStudentName: String) {
//        val studentID = studentSetList.lastIndex.plus(1)
//        val studentDir = File(classDir, "$newStudentName$studentID")
//        if (!studentDir.exists()){
//            Log.d(TAG, "Creating New StudentSet dir: ${studentDir.absolutePath}" )
//            studentDir.mkdir()
//            studentSetList.add(StudentSet(newStudentName))
//            listViewAdapter.notifyDataSetChanged()
//        }else{
//            toast("StudentSet dir:${studentDir.absolutePath} already exists.")
//        }
//    }
//
//    fun submitClassName(view: View){
//        val newClassName = class_name.text.toString()
//        if (newClassName.isNotBlank())
//        {
//            if (oldClassName != newClassName)
//            changeOrCreateClassDir(oldClassName, newClassName)
//            oldClassName = newClassName
//            new_student_area.visibility = View.VISIBLE
//        }else
//            toast("Class name Cant be empty.")
//    }
//
//    private fun changeOrCreateClassDir(oldName: String, newName : String) {
//        val oldDir = File(rootDir, oldName)
//        val newDir = File(rootDir, newName)
//        if (!newDir.exists() && (oldName.isBlank() || !oldDir.exists())){
//            Log.d(TAG,"Creating new class dir: ${newDir.absolutePath}")
//            newDir.mkdir()
//            student_list_view.itemsCanFocus = true
//        }else if ( !newDir.exists() ){
//            Log.d(TAG, "Renaming class dir from:${oldDir.absolutePath} to old")
//            oldDir.renameTo(newDir)
//        }else{
//            toast("Can't create Class, This Class Already Exists!")
//            return
//        }
//        classDir = newDir
//    }
//
//
//    class StudentsSetAdapter(private val activity: Activity, private val studentsList: ArrayList<StudentSet>) : BaseAdapter() {
//        override fun getItem(p0: Int) = studentsList[p0]
//
//        override fun getItemId(p0: Int) = p0.toLong()
//
//        override fun getCount() = studentsList.size
//
//        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
//            val studentListItem = if (p1 != null) {
//                p1
//            } else {
//                val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                inflater.inflate(R.layout.list_view_student_item, null)
//            }
//            val student = studentsList[p0]
//            studentListItem.student_name.text = student.name
//            studentListItem.num_of_samples.text = "${student.samplesCount} Samples"
//            return studentListItem
//        }
//    }
//
//    data class StudentSet (val name: String, var samplesCount : Int = 0)
}

