package com.android.noam.sellfyattendance.datasets

import java.io.Serializable
import java.util.*

data class ClassObj(val name: String, var numOfStudent: Int,
                    val studentList: SortedSet<StudentSet> = TreeSet(),
                    var isNew: Boolean = false) : Serializable, Comparable<ClassObj> {
    override fun compareTo(other: ClassObj) = name.compareTo(other.name)

    fun isEmpty() = studentList.isEmpty()
}