package com.android.noam.sellfyattendance.adapters

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.android.noam.sellfyattendance.R
import com.android.noam.sellfyattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.list_view_student_item.view.*

class StudentsViewAdapter(private val activity: Activity, private val studentsList: ArrayList<StudentSet>) : BaseAdapter() {
    override fun getItem(p0: Int) = studentsList[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getCount() = studentsList.size

    override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {
        val studentListItem = if (p1 != null) {
            p1
        } else {
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_view_student_item, parent, false)
        }
        val student = studentsList[p0]
        studentListItem.student_name.text = student.name
        if (studentListItem.cropped_face_view.drawable != null)
            (studentListItem.cropped_face_view.drawable as BitmapDrawable).bitmap.recycle()
        if(student.dir.listFiles().isNotEmpty()){
            val pic = student.dir.listFiles().random()
            val bitmap = BitmapFactory.decodeFile(pic.absolutePath)
            studentListItem.cropped_face_view.setImageBitmap(bitmap)
        }
        return studentListItem
    }
}
