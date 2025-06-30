package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel

import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.ternary

class ViewEventAttendeesActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var playersLayout: LinearLayout
    private lateinit var npcLayout: LinearLayout

    private lateinit var event: FullEventModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event_attendees)
        setupView()
    }

    private fun setupView() {
        event = DataManager.shared.getPassedData(ManageEventActivity::class, DataManagerPassedDataKey.SELECTED_EVENT)!!

        title = findViewById(R.id.eventattendees_eventName)
        playersLayout = findViewById(R.id.eventattendees_players)
        npcLayout = findViewById(R.id.eventattendees_npcs)

        buildView()
    }

    private fun buildView() {
        playersLayout.removeAllViews()
        npcLayout.removeAllViews()

        title.text = event.title

        for (attendee in event.attendees) {
            val kvView = KeyValueViewBuildable(this)
            kvView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            kvView.set(DataManager.shared.players.firstOrNull { it.id == attendee.playerId }?.fullName ?: "", (attendee.isCheckedIn.lowercase() == "true").ternary("CHECKED IN", "Checked Out"))
            if (attendee.asNpc.lowercase() == "true") {
                npcLayout.addView(kvView)
            } else {
                playersLayout.addView(kvView)
            }
        }
    }
}