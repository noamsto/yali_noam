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
import com.livinglifetechway.k4kotlin.longToast
import kotlinx.android.synthetic.main.activity_pick_faces.*
import kotlinx.android.synthetic.main.grid_item.view.*
import java.io.File
import android.support.v4.content.res.TypedArrayUtils.getResourceId
import android.content.res.TypedArray
import android.support.v7.widget.TintTypedArray.obtainStyledAttributes
import com.android.noam.javacvplayground.CreateNewSetActivity.Companion.ROOT_DIR_TAG
import org.jetbrains.anko.backgroundResource


class PickFacesActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    companion object {
        const val FACE_SET_TAG = "FaceSetTag"
        private const val TAG = "PickFacesActivity"
    }
    private val faceSets: ArrayList<FacesSet> = ArrayList()
    private lateinit var facesSetAdapter: FacesSetAdapter
    private lateinit var facesDir : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_faces)
        facesDir = getPublicPicturesStorageDir()
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

    private fun getPublicPicturesStorageDir(): File {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Faces")
        if (!file.mkdirs()) {
            Log.d(TAG, "Directory not created")
        }
        return file
    }

    private fun readFacesSets() {
        facesDir = getPublicPicturesStorageDir()
        if (facesDir != null) {
            Log.d(TAG, "Searching for all faces sets in ${facesDir.absolutePath}")
            for (facesSet in facesDir.listFiles()) {
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
        val newSet = FacesSet("Create new set.", "", 0,0, isNew = true)
        faceSets.add(newSet)
        facesSetAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val faceDetectorIntent = Intent(this, FaceDetectorActivity::class.java)
        val faceSet = faceSets[position]
        if (faceSet.isNew) {
            longToast("Let's Create a new Set.")
            val createNewSetIntent = Intent(this, CreateNewSetActivity::class.java)
            createNewSetIntent.putExtra(ROOT_DIR_TAG, facesDir)
            startActivity(createNewSetIntent)
            return
        }
        if (faceSet.isEmpty()){
            longToast("FaceSet is Empty, please fill it.")
            return
        }
        faceDetectorIntent.putExtra(FACE_SET_TAG, faceSet)
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

            setGridItem.peopleCount.text = if (faceSet.peopleCount != 0){
                setGridItem.peopleCount.visibility = View.VISIBLE
                "Peoples: ${faceSet.peopleCount}"
            }else{
                setGridItem.peopleCount.visibility = View.INVISIBLE
                ""
            }
            setGridItem.facesCount.text = if ( faceSet.samples != 0 ) {
                setGridItem.facesCount.visibility = View.VISIBLE
                "Samples: ${faceSet.samples}"
            }else {
                setGridItem.facesCount.visibility = View.INVISIBLE
                ""
            }
            return setGridItem
        }

        override fun getItem(position: Int): Any = facesSets[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getCount(): Int = facesSets.size
    }
}



