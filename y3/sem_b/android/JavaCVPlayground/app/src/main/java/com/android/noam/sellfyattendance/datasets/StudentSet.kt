package com.android.noam.sellfyattendance.datasets

import java.io.File
import java.io.Serializable

data class StudentSet(val name: String, val dir: File, val id: Int, var samplesCount: Int)
    : Comparable<StudentSet>, Serializable {

    override fun compareTo(other: StudentSet) = id.compareTo(other.id)
}