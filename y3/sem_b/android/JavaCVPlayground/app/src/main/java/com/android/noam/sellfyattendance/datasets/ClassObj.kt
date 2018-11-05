package com.android.noam.sellfyattendance.datasets

import java.io.Serializable
import java.util.*

data class ClassObj(val name: String, var numOfStudent: Int,
                    val studentList: SortedSet<StudentSet> = TreeSet(),
                    var isNew: Boolean = false) : Serializable{
    fun isEmpty() = studentList.isEmpty()
}