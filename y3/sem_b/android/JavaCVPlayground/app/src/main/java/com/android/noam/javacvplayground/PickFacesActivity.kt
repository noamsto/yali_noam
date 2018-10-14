package com.android.noam.javacvplayground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.noam.javacvplayground.CreateNewSetActivity.Companion.ROOT_DIR_TAG
import com.livinglifetechway.k4kotlin.longToast
import kotlinx.android.synthetic.main.activity_pick_faces.*
import kotlinx.android.synthetic.main.card_view.view.*
import org.jetbrains.anko.toast
import java.io.File


class PickFacesActivity : AppCompatActivity() {

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
        val faceSetRecycler: RecyclerView = class_recycler_view
        facesSetAdapter = FacesSetAdapter(this, faceSets, this)
        faceSetRecycler.layoutManager = GridLayoutManager(this,3)
        faceSetRecycler.adapter = facesSetAdapter




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

    fun setSelected ( faceSet : FacesSet) {
        if (faceSet.isNew) {
            toast("Let's Create a new Set.")
            val createNewSetIntent = Intent(this, CreateNewSetActivity::class.java)
            createNewSetIntent.putExtra(ROOT_DIR_TAG, facesDir)
            startActivity(createNewSetIntent)
            return
        }
        if (faceSet.isEmpty()){
            toast("FaceSet is Empty, please fill it.")
            return
        }
        val faceDetectorIntent = Intent(this, FaceDetectorActivity::class.java)
        faceDetectorIntent.putExtra(FACE_SET_TAG, faceSet)
        startActivity(faceDetectorIntent)
    }

    class FacesSetAdapter(private val context: Context,
                          private val facesSets: ArrayList<FacesSet>,
                          private val pickFacesActivity: PickFacesActivity) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view,parent ,false))
        }

        override fun getItemCount() = facesSets.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val faceSet = facesSets[position]
            holder.pickFacesActivity = pickFacesActivity
            holder.currentItem = faceSet
            holder.setName.text = faceSet.name
            holder.peopleCount.text = if (faceSet.peopleCount != 0){
                holder.peopleCount.visibility = View.VISIBLE
                "Peoples: ${faceSet.peopleCount}"
            }else{
                holder.peopleCount.visibility = View.INVISIBLE
                ""
            }
            holder.facesCount.text = if ( faceSet.samples != 0 ) {
                holder.facesCount.visibility = View.VISIBLE
                "Samples: ${faceSet.samples}"
            }else {
                holder.facesCount.visibility = View.INVISIBLE
                ""
            }
        }

//        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//            val setGridItem = if (convertView != null) {
//                convertView
//            } else {
//                val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//                inflater.inflate(R.layout.card_view, null)
//            }
//
//            val faceSet = facesSets[position]
//            setGridItem.setName.text = faceSet.name
//
//            setGridItem.peopleCount.text = if (faceSet.peopleCount != 0){
//                setGridItem.peopleCount.visibility = View.VISIBLE
//                "Peoples: ${faceSet.peopleCount}"
//            }else{
//                setGridItem.peopleCount.visibility = View.INVISIBLE
//                ""
//            }
//            setGridItem.facesCount.text = if ( faceSet.samples != 0 ) {
//                setGridItem.facesCount.visibility = View.VISIBLE
//                "Samples: ${faceSet.samples}"
//            }else {
//                setGridItem.facesCount.visibility = View.INVISIBLE
//                ""
//            }
//            return setGridItem
//        }

    }

}

class ViewHolder(view: View): RecyclerView.ViewHolder(view){
    val setName = view.setName
    val peopleCount = view.peopleCount
    val facesCount = view.facesCount
    lateinit var currentItem : FacesSet
    lateinit var pickFacesActivity: PickFacesActivity
    init {
        view.setOnClickListener {
            pickFacesActivity.setSelected(currentItem)
        }
    }
}


