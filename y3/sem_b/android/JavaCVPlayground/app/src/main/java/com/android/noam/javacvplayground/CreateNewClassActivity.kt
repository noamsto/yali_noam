package com.android.noam.javacvplayground

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
import com.android.noam.javacvplayground.ManageStudentsActivity.Companion.STUDENTS_DIR
import com.android.noam.javacvplayground.SelectClassActivity.Companion.CLASS_OBJ_TAG
import com.android.noam.javacvplayground.SelectClassActivity.Companion.CREATE_NEW_CLASS
import kotlinx.android.synthetic.main.activity_create_new_class.*
import kotlinx.android.synthetic.main.list_view_student_item.view.*
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

class CreateNewClassActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CreateNewClassActivity"
    }
    val allStudentList : ArrayList<StudentSet> = ArrayList()
    val selectedStudents : TreeSet<StudentSet> = TreeSet()
    lateinit var samplesDir : File
    lateinit var studentRecyclerAdapter: RecyclerView.Adapter<StudentViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_class)
        samplesDir = intent.extras.getSerializable(STUDENTS_DIR) as File

        val studentRecyclerView: RecyclerView = student_recycler_view
        studentRecyclerAdapter = StudentRecyclerAdapter(this, allStudentList, this)
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.adapter = studentRecyclerAdapter
        readAllStudents()
    }

    private fun readAllStudents() {
        for (studentDir in samplesDir.listFiles().filter { it.isDirectory }){
            if (studentDir == samplesDir )
                continue
            val name = studentDir.name.filter { it.isLetter() }
            val id = studentDir.name.filter { it.isDigit() }.toInt()
            var numOfSamples = studentDir.listFiles().size
            val studentSet = StudentSet(name, studentDir, id, numOfSamples)
            allStudentList.add(studentSet)
        }
        studentRecyclerAdapter.notifyDataSetChanged()
    }


    fun setSelected(currentItem: StudentSet, view: View) {
        if (currentItem in selectedStudents){
            selectedStudents.remove(currentItem)
            view.setBackgroundColor(Color.WHITE)
        }
        else{
            selectedStudents.add(currentItem)
            view.setBackgroundColor(Color.GREEN)
        }
        studentRecyclerAdapter.notifyDataSetChanged()
    }

    fun submitClass(view: View){
        if (class_name.text.isBlank()){
            toast("Please fill Class name.")
            return
        }
        val result = Intent()
        result.putExtra(CLASS_OBJ_TAG, ClassObj(class_name.text.toString(),selectedStudents.size,
                selectedStudents))
        setResult(CREATE_NEW_CLASS, result)
        this.finish()
    }

}

class StudentRecyclerAdapter(private val context: Context,
                             private val studentSetList: ArrayList<StudentSet>,
                             private val createNewClassActivity: CreateNewClassActivity) : RecyclerView.Adapter<StudentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        return StudentViewHolder(LayoutInflater.from(context).inflate(R.layout.list_view_student_item, parent, false))
    }

    override fun getItemCount() = studentSetList.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val studentSet = studentSetList[position]
        holder.createNewClassActivity = createNewClassActivity
        holder.currentItem = studentSet
        holder.studentName.text = studentSet.name
        holder.samplesCount.text = studentSet.samplesCount.toString()

    }
}
class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val studentName = view.student_name!!
    val samplesCount = view.num_of_samples!!
    lateinit var currentItem: StudentSet
    lateinit var createNewClassActivity: CreateNewClassActivity

    init {
        view.setOnClickListener {
            createNewClassActivity.setSelected(currentItem, view)
        }
    }
}

