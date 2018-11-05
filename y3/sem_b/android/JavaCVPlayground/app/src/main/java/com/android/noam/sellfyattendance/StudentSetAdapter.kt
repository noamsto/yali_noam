package com.android.noam.sellfyattendance

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.android.noam.sellfyattendance.datasets.StudentSet
import kotlinx.android.synthetic.main.list_view_student_item.view.*

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
