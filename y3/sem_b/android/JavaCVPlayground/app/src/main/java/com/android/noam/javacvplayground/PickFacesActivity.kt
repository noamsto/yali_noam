package com.android.noam.javacvplayground

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import kotlinx.android.synthetic.main.activity_pick_faces.*
import kotlinx.android.synthetic.main.grid_item.view.*
import java.io.File


data class FacesSet(val name: String, val path:String, val peopleCount: Int, val samples: Int)

class PickFacesActivity : AppCompatActivity() {

    private val TAG = "PickFacesActivity"
    private val faceSets : ArrayList<FacesSet> = ArrayList()
    private lateinit var  facesSetAdapter : FacesSetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_faces)

        val facesSetGrid: GridView = SetsGrid
        facesSetAdapter = FacesSetAdapter(this, faceSets)
        facesSetGrid.adapter = facesSetAdapter
        swipeLayout.setOnRefreshListener {
            faceSets.clear()
            readFacesSets()
            swipeLayout.isRefreshing = false

        }
        readFacesSets()
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun getPublicPicturesStorageDir(): File? {
        // Get the directory for the user's public pictures directory.
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Faces")
        if (!file.mkdirs()) {
            Log.d(TAG, "Directory not created")
        }
        return file
    }

    fun readFacesSets(){
        val picDir = getPublicPicturesStorageDir()
        if (picDir != null) {
            Log.d(TAG, "Searching for all faces sets in ${picDir.absolutePath}")
            for (facesSet in picDir.listFiles()){
                var numOfSamples = 0
                var peopleCount = -1
                facesSet.walkTopDown().forEach {
                    if (it.extension.matches(Regex.fromLiteral("pgm"))){
                        numOfSamples++
                    }
                    if (it.isDirectory)
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


    class  FacesSetAdapter(private val activity: Activity, private val facesSets: ArrayList<FacesSet> ) : BaseAdapter(){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val setGridItem = if (convertView != null){
                convertView
            }else {
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


