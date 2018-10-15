package com.android.noam.javacvplayground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.noam.javacvplayground.MainActivity.Companion.APP_DIR_NAME
import com.android.noam.javacvplayground.ManageStudentsActivity.Companion.STUDENTS_DIR
import kotlinx.android.synthetic.main.activity_select_class.*
import kotlinx.android.synthetic.main.card_view.view.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File
import java.util.*


class SelectClassActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SelectClassActivity"
        const val CLASS_OBJ_TAG = "CLASS_OBJ_TAG"
        const val CREATE_NEW_CLASS = 100
    }

    private val classes: ArrayList<ClassObj> = ArrayList()
    private lateinit var classesAdapter: ClassesAdapter
    private lateinit var samplesDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_class)

        val rootDir  = intent.getSerializableExtra(APP_DIR_NAME) as File
        samplesDir = File(rootDir, ManageStudentsActivity.STUDENTS_DIR)
        if ( ! samplesDir.exists()){
            longToast("Please add Students before selecting classes.")
            finish()
        }
        val faceSetRecycler: RecyclerView = class_recycler_view
        classesAdapter = ClassesAdapter(this, classes, this)
        faceSetRecycler.layoutManager = GridLayoutManager(this, 3)
        faceSetRecycler.adapter = classesAdapter

        swipeLayout.setOnRefreshListener {
            updateClasses()
            swipeLayout.isRefreshing = false
        }
        updateClasses()
    }


    private fun updateClasses() {
        for (classObj in classes ) {
            var numOfSamples = 0
            for (studentSet in classObj.studentList){
                studentSet.dir.walkTopDown().forEach {
                    Log.d(TAG, "Reading from Student dir: ${studentSet.dir.path}")
                    if (it.parentFile != studentSet.dir && it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                        numOfSamples++
                    }
                }
                studentSet.samplesCount = numOfSamples
            }
        }
        classes.add(ClassObj("new",0, TreeSet(), true))
        classesAdapter.notifyDataSetChanged()
    }

    fun setSelected(classObj: ClassObj) {
        if (classObj.isNew) {
            toast("Let's Create a new Set.")
            val createNewClassIntent = Intent(this, CreateNewClassActivity::class.java)
            createNewClassIntent.putExtra(STUDENTS_DIR, samplesDir)
            createNewClassIntent.putExtra(CLASS_OBJ_TAG, classObj)
            this.startActivityForResult(createNewClassIntent, CREATE_NEW_CLASS)
            return
        }
        if (classObj.isEmpty()) {
            toast("FaceSet is Empty, please fill it.")
            return
        }
        val faceDetectorIntent = Intent(this, FaceDetectorActivity::class.java)
        faceDetectorIntent.putExtra(CLASS_OBJ_TAG, classObj)
        startActivity(faceDetectorIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode){
            CREATE_NEW_CLASS -> {
                classes.add(data!!.getSerializableExtra(CLASS_OBJ_TAG)!! as ClassObj)
                classesAdapter.notifyDataSetChanged()
            }
        }
    }

    class ClassesAdapter(private val context: Context,
                         private val classesList: ArrayList<ClassObj>,
                         private val SelectClassActivity: SelectClassActivity) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view, parent, false))
        }

        override fun getItemCount() = classesList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val classObj = classesList[position]
            holder.selectClassActivity = SelectClassActivity
            holder.currentItem = classObj
            holder.setName.text = classObj.name
            val numOfStudents = classObj.studentList.size
            holder.peopleCount.text = if ( !classObj.isNew ) {
                holder.peopleCount.visibility = View.VISIBLE
                "Students: $numOfStudents"
            } else {
                holder.peopleCount.visibility = View.GONE
                ""
            }
        }
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val setName = view.setName!!
    val peopleCount = view.peopleCount!!
    lateinit var currentItem: ClassObj
    lateinit var selectClassActivity: SelectClassActivity

    init {
        view.setOnClickListener {
            selectClassActivity.setSelected(currentItem)
        }
    }
}


