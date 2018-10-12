package com.android.noam.javacvplayground

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import kotlinx.android.synthetic.main.activity_pick_faces.*
import kotlinx.android.synthetic.main.grid_item.view.*
import java.io.File


class PickFacesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        const val FACE_SET_TAG = "FaceSetTag"
    }

    private val TAG = "PickFacesActivity"

    private val faceSets: ArrayList<FacesSet> = ArrayList()
    private lateinit var facesSetAdapter: FacesSetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_faces)

        val facesSetGrid: GridView = SetsGrid
        facesSetGrid.onItemClickListener = this
        facesSetAdapter = FacesSetAdapter(this, faceSets)
        facesSetGrid.adapter = facesSetAdapter
        swipeLayout.setOnRefreshListener {
            faceSets.clear()
            readFacesSets()
            swipeLayout.isRefreshing = false

        }
        readFacesSets()
    }

    private fun getPublicPicturesStorageDir(): File? {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Faces")
        if (!file.mkdirs()) {
            Log.d(TAG, "Directory not created")
        }
        return file
    }

    private fun readFacesSets() {
        val picDir = getPublicPicturesStorageDir()
        if (picDir != null) {
            Log.d(TAG, "Searching for all faces sets in ${picDir.absolutePath}")
            for (facesSet in picDir.listFiles()) {
                var numOfSamples = 0
                var peopleCount = 0
                facesSet.walkTopDown().forEach {
                    if (it.parentFile != facesSet && it.extension.matches("""pgm|jpg|bmp|png""".toRegex())) {
                        numOfSamples++
                    }
                    if (it != facesSet && it.isDirectory)
                        peopleCount++
                }
                val faceSet = FacesSet(facesSet.nameWithoutExtension, facesSet.absolutePath, peopleCount,
                        numOfSamples)
                faceSets.add(faceSet)
                Log.d(TAG, "Found ${faceSet.name}, path: ${faceSet.path}, " +
                        "consisting of ${faceSet.peopleCount} Peoples and  ${faceSet.samples} samples")
            }
        }
        facesSetAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val faceDetectorIntent = Intent(this, FaceDetectorActivity::class.java)
        faceDetectorIntent.putExtra(FACE_SET_TAG, faceSets[position])
        startActivity(faceDetectorIntent)
    }

    class FacesSetAdapter(private val activity: Activity, private val facesSets: ArrayList<FacesSet>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val setGridItem = if (convertView != null) {
                convertView
            } else {
                val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(R.layout.grid_item, null)
            }
            val faceSet = facesSets[position]
            setGridItem.setName.text = faceSet.name
            setGridItem.peopleCount.text = "Peoples: ${faceSet.peopleCount}"
            setGridItem.facesCount.text = "Samples: ${faceSet.samples}"
            return setGridItem
        }

        override fun getItem(position: Int): Any = facesSets[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getCount(): Int = facesSets.size
    }
}

data class FacesSet(val name: String, val path: String, val peopleCount: Int, val samples: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt())

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

