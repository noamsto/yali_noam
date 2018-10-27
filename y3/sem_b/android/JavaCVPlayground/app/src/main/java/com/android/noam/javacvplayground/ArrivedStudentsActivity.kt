package com.android.noam.javacvplayground

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import com.android.noam.javacvplayground.StudentDetectorActivity.Companion.ARRIVED_STUDENTS_LIST
import kotlinx.android.synthetic.main.activity_arrived_students.*


class ArrivedStudentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arrived_students)

        val arrivedStudents = intent.getSerializableExtra(ARRIVED_STUDENTS_LIST) as ArrayList<StudentSet>
        val adapter = StudentsSetAdapter(this,arrivedStudents)
        arrived_student_list_view.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        NavUtils.navigateUpFromSameTask(this)
        super.onBackPressed()
    }
}
