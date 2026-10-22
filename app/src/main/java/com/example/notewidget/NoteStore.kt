package com.example.notewidget

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Note(val id: String, val text: String)

object NoteStore {
    private const val PREFS = "notes"
    private const val KEY = "data"

    fun load(context: Context): List<Note> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Note(o.getString("id"), o.getString("text"))
        }
    }

    private fun save(context: Context, notes: List<Note>) {
        val arr = JSONArray()
        notes.forEach { arr.put(JSONObject().put("id", it.id).put("text", it.text)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun get(context: Context, id: String): Note? = load(context).firstOrNull { it.id == id }

    fun upsert(context: Context, id: String?, text: String) {
        val notes = load(context).toMutableList()
        val i = notes.indexOfFirst { it.id == id }
        if (i >= 0) notes[i] = Note(notes[i].id, text)
        else notes.add(0, Note(UUID.randomUUID().toString(), text))
        save(context, notes)
    }

    fun delete(context: Context, id: String) {
        save(context, load(context).filter { it.id != id })
    }
}
