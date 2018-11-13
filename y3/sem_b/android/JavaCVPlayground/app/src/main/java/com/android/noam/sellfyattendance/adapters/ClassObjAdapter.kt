package com.android.noam.sellfyattendance.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.noam.sellfyattendance.R
import com.android.noam.sellfyattendance.datasets.ClassObj
import kotlinx.android.synthetic.main.card_view.view.*
import java.util.ArrayList

class ClassObjAdapter(private val context: Context,
                      private val classesList: ArrayList<ClassObj>,
                      private val classesMarkForDelete : ArrayList<ClassObj>,
                      private val listenerActivity: OnLongShortClickListener) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view, parent, false))
    }

    override fun getItemCount() = classesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val classObj = classesList[position]
        if (classObj in classesMarkForDelete)
            holder.itemView.setBackgroundColor(Color.RED)
        else
            holder.itemView.setBackgroundColor(Color.GRAY)
        holder.listenerActivity = listenerActivity
        holder.currentItem = classObj
        holder.setName.text = classObj.name
        val numOfStudents = classObj.studentList.size
        holder.peopleCount.text = if ( !classObj.isNew ) {
            holder.peopleCount.visibility = View.VISIBLE
            "Students: $numOfStudents"
        } else {
            holder.peopleCount.visibility = View.GONE
            ""
        }
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val setName = view.setName!!
    val peopleCount = view.peopleCount!!
    lateinit var currentItem: ClassObj
    lateinit var listenerActivity: OnLongShortClickListener

    init {
        view.setOnClickListener {
            listenerActivity.onShortClickListener(currentItem)
        }
        view.setOnLongClickListener{
            listenerActivity.onLongClickListener(currentItem)
        }
    }
}

interface OnLongShortClickListener{
    fun onLongClickListener(classObj: ClassObj) : Boolean
    fun onShortClickListener(classObj: ClassObj)
}
