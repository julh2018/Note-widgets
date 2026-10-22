package com.example.notewidget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val NoteIdKey = ActionParameters.Key<String>("id")

private val ink = ColorProvider(Color(0xFF212121))
private val red = ColorProvider(Color(0xFFC62828))

class NotesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val notes = NoteStore.load(context)
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(Color(0xFFFFF8E1)))
                    .cornerRadius(16.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "নোট",
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(color = ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    )
                    Text(
                        "＋",
                        modifier = GlanceModifier.padding(8.dp)
                            .clickable(actionStartActivity<EditNoteActivity>()),
                        style = TextStyle(color = ink, fontSize = 24.sp)
                    )
                }
                if (notes.isEmpty()) {
                    Text(
                        "＋ চেপে নতুন নোট লিখুন",
                        modifier = GlanceModifier.padding(8.dp),
                        style = TextStyle(color = ink, fontSize = 14.sp)
                    )
                } else {
                    LazyColumn {
                        items(notes, itemId = { it.id.hashCode().toLong() }) { note ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    note.text,
                                    maxLines = 3,
                                    modifier = GlanceModifier.defaultWeight().padding(4.dp)
                                        .clickable(
                                            actionStartActivity<EditNoteActivity>(
                                                actionParametersOf(NoteIdKey to note.id)
                                            )
                                        ),
                                    style = TextStyle(color = ink, fontSize = 15.sp)
                                )
                                Text(
                                    "✕",
                                    modifier = GlanceModifier.padding(8.dp)
                                        .clickable(
                                            actionRunCallback<DeleteNoteCallback>(
                                                actionParametersOf(NoteIdKey to note.id)
                                            )
                                        ),
                                    style = TextStyle(color = red, fontSize = 18.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class DeleteNoteCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val id = parameters[NoteIdKey] ?: return
        NoteStore.delete(context, id)
        NotesWidget().updateAll(context)
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}
