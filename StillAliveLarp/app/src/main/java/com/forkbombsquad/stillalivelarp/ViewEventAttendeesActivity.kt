package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.ternary

class ViewEventAttendeesActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var playersLayout: LinearLayout
    private lateinit var npcLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event_attendees)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.eventattendees_eventName)
        playersLayout = findViewById(R.id.eventattendees_players)
        npcLayout = findViewById(R.id.eventattendees_npcs)

        buildView()
    }

    private fun buildView() {
        playersLayout.removeAllViews()
        npcLayout.removeAllViews()

        val players = OldDataManager.shared.allPlayers ?: listOf()
        val event = OldDataManager.shared.selectedEvent!!
        val eventAttendees = OldDataManager.shared.eventAttendeesForEvent ?: listOf()

        title.text = event.title

        for (attendee in eventAttendees) {
            val kvView = KeyValueViewBuildable(this)
            kvView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            kvView.set(players.firstOrNull { it.id == attendee.playerId }?.fullName ?: "", (attendee.isCheckedIn.lowercase() == "true").ternary("CHECKED IN", "Checked Out"))
            if (attendee.asNpc.lowercase() == "true") {
                npcLayout.addView(kvView)
            } else {
                playersLayout.addView(kvView)
            }
        }
    }

    override fun onBackPressed() {
        OldDataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}