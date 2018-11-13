package com.android.noam.sellfyattendance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.noam.sellfyattendance.ManageStudentsActivity.Companion.STUDENTS_DIR
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.CLASS_LIST_TAG
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.CLASS_OBJ_TAG
import com.android.noam.sellfyattendance.SelectClassActivity.Companion.EDIT_CLASS
import com.android.noam.sellfyattendance.datasets.ClassObj
import com.android.noam.sellfyattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.activity_edit_class.*
import kotlinx.android.synthetic.main.list_view_studentset_item.view.*
import org.bytedeco.javacpp.RealSense
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
    private lateinit var studentRecyclerAdapter: RecyclerView.Adapter<StudentViewHolder>
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

        val studentRecyclerView: RecyclerView = student_recycler_view
        studentRecyclerAdapter = StudentRecyclerAdapter(this.baseContext, allStudentList, selectedStudents, this)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.adapter = studentRecyclerAdapter
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
        studentRecyclerAdapter.notifyDataSetChanged()
    }


    fun setSelected(currentItem: StudentSet, view: View) {
        if (currentItem in selectedStudents)
            selectedStudents.remove(currentItem)
        else
            selectedStudents.add(currentItem)
        studentRecyclerAdapter.notifyDataSetChanged()
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

class StudentRecyclerAdapter(private val context: Context,
                             private val studentSetList: ArrayList<StudentSet>,
                             private val selectedStudents: TreeSet<StudentSet>,
                             private val createNewClassActivity: CreateNewClassActivity) :
        RecyclerView.Adapter<StudentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        return StudentViewHolder(LayoutInflater.from(context).inflate(
                R.layout.list_view_studentset_item, parent, false))
    }

    override fun getItemCount() = studentSetList.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val studentSet = studentSetList[position]
        holder.createNewClassActivity = createNewClassActivity
        holder.currentItem = studentSet
        holder.studentName.text = studentSet.name
        holder.samplesCount.text = studentSet.samplesCount.toString()
        holder.studentID.text = studentSet.id.toString()
        if (studentSet in selectedStudents)
            holder.itemView.setBackgroundColor(Color.GRAY)
        else
            holder.itemView.setBackgroundColor(Color.DKGRAY)

    }
}
class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val studentName = view.student_name!!
    val samplesCount = view.num_of_samples!!
    val studentID = view.student_id!!
    lateinit var currentItem: StudentSet
    lateinit var createNewClassActivity: CreateNewClassActivity
    init {
        view.setOnClickListener {
            createNewClassActivity.setSelected(currentItem, view)
        }
    }
}

