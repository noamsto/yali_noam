package com.android.noam.sellfyattendance.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.android.noam.sellfyattendance.R
import com.android.noam.sellfyattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.list_view_studentset_item.view.*

class StudentsSetAdapter(private val activity: Activity,
                         private val studentsList: ArrayList<StudentSet>,
                         private val studentToMark: Iterable<StudentSet>? = null,
                         private val markColor: Int = Color.RED,
                         private val defaultColor: Int = Color.GRAY) : BaseAdapter() {

    override fun getItem(p0: Int) = studentsList[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getCount() = studentsList.size

    @SuppressLint("SetTextI18n")
    override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {
        val studentListItem = if (p1 != null) {
            p1
        } else {
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_view_studentset_item, parent, false)
        }
        val student = studentsList[p0]
        if (studentToMark != null) {
            if (student in studentToMark)
                studentListItem.setBackgroundColor(markColor)
            else
                studentListItem.setBackgroundColor(defaultColor)
        }
        studentListItem.student_name.text = student.name
        studentListItem.num_of_samples.text = "${student.samplesCount} Samples"
        studentListItem.student_id.text = "ID: ${student.id}"
        return studentListItem
    }
}
