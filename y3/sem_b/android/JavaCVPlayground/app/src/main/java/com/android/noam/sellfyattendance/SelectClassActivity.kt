package com.android.noam.sellfyattendance

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.android.noam.sellfyattendance.MainActivity.Companion.APP_DIR_NAME
import com.android.noam.sellfyattendance.ManageStudentsActivity.Companion.STUDENTS_DIR
import com.android.noam.sellfyattendance.adapters.ClassObjAdapter
import com.android.noam.sellfyattendance.adapters.OnLongShortClickListener
import com.android.noam.sellfyattendance.datasets.ClassObj
import com.android.noam.sellfyattendance.datasets.StudentSet
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_select_class.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class SelectClassActivity : AppCompatActivity(), OnLongShortClickListener {

    companion object {
        private const val TAG = "SelectClassActivity"
        private const val CLASS_FILE_NAME = "classes.dat"
        const val CLASS_OBJ_TAG = "CLASS_OBJ_TAG"
        const val CLASS_LIST_TAG = "CLASS_LIST_TAG"
        const val EDIT_CLASS = 101

    }

    private val classes: ArrayList<ClassObj> = ArrayList()
    private val classesMarkForDelete: ArrayList<ClassObj> = ArrayList()
    private lateinit var classesAdapter: ClassObjAdapter
    private lateinit var samplesDir: File
    private lateinit var preEditClass: ClassObj
    private var deleteMode = false

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
        classesAdapter = ClassObjAdapter(this, classes, classesMarkForDelete,  this)
        faceSetRecycler.layoutManager = GridLayoutManager(this, 3)
        faceSetRecycler.adapter = classesAdapter
        restoreSavedClassData()
        checkbox_delete_mode.setOnCheckedChangeListener { buttonView, isChecked ->
            deleteMode = isChecked
            if (isChecked)
                confirm_delete_btn.visibility = View.VISIBLE
            else{
                confirm_delete_btn.visibility = View.GONE
                classesMarkForDelete.clear()
            }
            classesAdapter.notifyDataSetChanged()
        }
        confirm_delete_btn.setOnClickListener {
            deleteSelectedClasses()
        }
    }


    override fun onStart() {
        super.onStart()
        updateClasses()
    }

    override fun onStop() {
        super.onStop()
        saveClassesData()
    }

    private fun saveClassesData(){
        if (classes.isNotEmpty())
            if (classes.last().isNew)
                classes.removeAt(classes.lastIndex)
        val classesFile = File(filesDir, CLASS_FILE_NAME)
        if (classes.isEmpty()){
            if (classesFile.exists())
                classesFile.delete()
            return
        }
        val fileOutputStream = FileOutputStream(classesFile)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)
        objectOutputStream.writeObject(classes)
        objectOutputStream.close()
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreSavedClassData(){
        val classesFile = File(filesDir, CLASS_FILE_NAME)
        if (!classesFile.exists())
            return
        val fileInputStream = FileInputStream(classesFile)
        val objInputStream = ObjectInputStream(fileInputStream)
        classes.clear()
        try {
            classes.addAll(objInputStream.readObject() as ArrayList<ClassObj>)
        }catch (e: Exception){
            Log.e(TAG, e.message)
            classesFile.delete()
            return
        }finally {
            objInputStream.close()
        }
    }

    private fun updateClasses() {
        val invalidStudenSet = ArrayList<StudentSet>()
        for (classObj in classes ) {
            var numOfSamples = 0
            for (studentSet in classObj.studentList){
                if (!studentSet.dir.exists()){
                    invalidStudenSet.add(studentSet)
                    continue
                }
                studentSet.dir.walkTopDown().forEach {
                    Log.d(TAG, "Reading from Student dir: ${studentSet.dir.path}")
                    if (it.parentFile != studentSet.dir && it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                        numOfSamples++
                    }
                }
                studentSet.samplesCount = numOfSamples
            }
            // Remove students with no valid dir
            classObj.studentList.removeAll(invalidStudenSet)
            invalidStudenSet.clear()
        }
        if (classes.isEmpty() || !classes.last().isNew)
            classes.add(ClassObj("new", 0, TreeSet(), true))
        classesAdapter.notifyDataSetChanged()
    }

    private fun classSelected(currentItem: ClassObj)  = runWithPermissions(android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    {
        when {
            currentItem.isNew -> editClass(currentItem)
            currentItem.isEmpty() -> toast("FaceSet is Empty, please fill it.")
            else -> {
                val studentAttendanceActivity = Intent(this, StudentAttendanceActivity::class.java)
                studentAttendanceActivity.putExtra(CLASS_OBJ_TAG, currentItem)
                startActivity(studentAttendanceActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode){
            EDIT_CLASS -> {
                classes.add(0, data!!.getSerializableExtra(CLASS_OBJ_TAG)!! as ClassObj)
            }
            else -> {
                if (!preEditClass.isNew)
                    classes.add(0, preEditClass)
            }
        }
        if (preEditClass.isNew)
            classes.add(preEditClass)
        classesAdapter.notifyDataSetChanged()
    }

    override fun onShortClickListener(classObj: ClassObj) {
        if (deleteMode){
            if (classObj.isNew)
                return
            if (classObj in classesMarkForDelete)
                classesMarkForDelete.remove(classObj)
            else{
                classesMarkForDelete.add(classObj)
            }
            classesAdapter.notifyDataSetChanged()
            return
        }
        classSelected(classObj)
    }

    override fun onLongClickListener(classObj: ClassObj): Boolean {
        if (deleteMode){
            longToast("Can enter Edit class mode when delete mode is active.")
            return true
        }
        if (!classObj.isNew)
            editClass(classObj)
        return true
    }

    private fun editClass(currentItem: ClassObj): Boolean {
        preEditClass = currentItem
        classes.remove(currentItem)
        val createNewClassIntent = Intent(this, CreateNewClassActivity::class.java)
        createNewClassIntent.putExtra(STUDENTS_DIR, samplesDir)
        createNewClassIntent.putExtra(CLASS_OBJ_TAG, currentItem)
        createNewClassIntent.putExtra(CLASS_LIST_TAG, classes)
        this.startActivityForResult(createNewClassIntent, EDIT_CLASS)
        return true
    }

    private fun deleteSelectedClasses(){
        classesMarkForDelete.forEach {
            classes.remove(it)
        }
        checkbox_delete_mode.isChecked = false
        classesAdapter.notifyDataSetChanged()
    }
}



