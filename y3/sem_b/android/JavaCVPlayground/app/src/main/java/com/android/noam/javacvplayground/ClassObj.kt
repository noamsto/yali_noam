package com.android.noam.javacvplayground

import java.io.Serializable
import java.util.*

data class ClassObj(val name: String, var numOfStudent: Int,
                    val studentList: SortedSet<StudentSet> = TreeSet(),
                    var isNew: Boolean = false) : Serializable{

    fun addStudent(newStudentSet : StudentSet) = studentList.add(newStudentSet)
    fun isEmpty() = studentList.isEmpty()
}