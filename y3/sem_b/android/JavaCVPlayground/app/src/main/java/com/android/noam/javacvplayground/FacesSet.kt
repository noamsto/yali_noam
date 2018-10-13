package com.android.noam.javacvplayground

import android.os.Parcel
import android.os.Parcelable

data class FacesSet(val name: String, val path: String, val peopleCount: Int, val samples: Int,
                    val isNew: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt())

    fun isEmpty() = (peopleCount == 0 || samples == 0)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeInt(peopleCount)
        parcel.writeInt(samples)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FacesSet> {
        override fun createFromParcel(parcel: Parcel): FacesSet {
            return FacesSet(parcel)
        }

        override fun newArray(size: Int): Array<FacesSet?> {
            return arrayOfNulls(size)
        }
    }
}