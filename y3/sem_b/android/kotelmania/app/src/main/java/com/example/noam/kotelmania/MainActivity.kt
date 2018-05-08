package com.example.noam.kotelmania

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*


const val TITLE = "title"
const val CONTENT = "content"
const val NEW_NOTE = -1

class MainActivity : Activity() {


    lateinit var listView: ListView
    private var notes: ArrayList<Note> = ArrayList()
    private var noteInProgress: Note? = null
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notes.add(Note("לעשות:", "1. לטפס על הקלימנג'רו\n2. להחזיר ליובל כסף."))
        notes.add(Note("רשימת קניות", "1. בוטנים\n2. אפרסק"))
        notes.add(Note("לעשות:", "1. לטפס על הקלימנג'רו\n2. להחזיר ליובל כסף."))
        notes.add(Note("רשימת קניות", "1. בוטנים\n2. אפרסק"))
        notes.add(Note("לעשות:", "1. לטפס על הקלימנג'רו\n2. להחזיר ליובל כסף."))
        notes.add(Note("רשימת קניות", "1. בוטנים\n2. אפרסק"))
        notes.add(Note("לעשות:", "1. לטפס על הקלימנג'רו\n2. להחזיר ליובל כסף."))
        notes.add(Note("רשימת קניות", "1. בוטנים\n2. אפרסק"))


        listView = findViewById(R.id.listView)
        adapter = NotesAdapter(this, notes)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            launchEditNote(position)
        }

        newNote.setOnClickListener {
            launchEditNote(NEW_NOTE)
        }
    }

    private fun launchEditNote(index: Int) {
        val intent = Intent(this, EditNote::class.java)
        noteInProgress = if (index == NEW_NOTE) {
            Note("", "")
        } else {
            notes.removeAt(index)
        }

        intent.putExtra(TITLE, noteInProgress!!.title)
        intent.putExtra(CONTENT, noteInProgress!!.content)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val title = data.getStringExtra(TITLE)
            val content = data.getStringExtra(CONTENT)
            if (title.isNotBlank() or content.isNotBlank()) {
                noteInProgress!!.title = title
                noteInProgress!!.content = content
                notes.add(0, noteInProgress!!)
            }
        }
    }
}

class NotesAdapter(private var activity: Activity, private var notes: ArrayList<Note>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        lateinit var listItem: RelativeLayout
        listItem = if (convertView == null) {
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.list_item, null) as RelativeLayout
        } else
            convertView as RelativeLayout

        val note: Note = notes[position]
        val title: TextView = listItem.findViewById(R.id.title)
        val content: TextView = listItem.findViewById(R.id.content)
        val status: TextView = listItem.findViewById(R.id.status)
        title.text = note.title
        content.text = note.content
        status.text = note.status
        return listItem
    }

    override fun getItem(position: Int): Any {
        return notes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return notes.size
    }

}