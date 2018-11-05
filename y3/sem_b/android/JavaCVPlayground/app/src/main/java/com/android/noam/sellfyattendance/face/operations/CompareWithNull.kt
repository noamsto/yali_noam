package com.android.noam.sellfyattendance.face.operations

class CompareWithNull : Comparator<Float?> {
    override fun compare(p0: Float?, p1: Float?): Int {
        return if (p0 == null)
            if (p1 == null)
                0
            else
                1
        else
            if ( p1== null )
                -1
            else
                (p0 - p1).toInt()
    }
}