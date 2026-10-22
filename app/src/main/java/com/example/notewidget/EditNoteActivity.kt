package com.example.notewidget

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class EditNoteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getStringExtra("id")
        val existing = noteId?.let { NoteStore.get(this, it) }
        val pad = (16 * resources.displayMetrics.density).toInt()

        val input = EditText(this).apply {
            setText(existing?.text ?: "")
            hint = "নোট লিখুন…"
            minLines = 4
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSelection(text.length)
        }

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun addButton(label: String, onClick: () -> Unit) {
            row.addView(
                Button(this).apply { text = label; setOnClickListener { onClick() } },
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
        }

        if (existing != null) {
            addButton("মুছুন") {
                NoteStore.delete(this, existing.id)
                refreshAndFinish()
            }
        }
        addButton("বাতিল") { finish() }
        addButton("সেভ") {
            val t = input.text.toString().trim()
            if (t.isNotEmpty()) NoteStore.upsert(this, existing?.id, t)
            refreshAndFinish()
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            addView(input)
            addView(row)
        })
    }

    private fun refreshAndFinish() {
        val app = applicationContext
        MainScope().launch { NotesWidget().updateAll(app) }
        finish()
    }
}
