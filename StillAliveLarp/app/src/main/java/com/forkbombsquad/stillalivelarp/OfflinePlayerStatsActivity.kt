package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.core.view.isGone

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class OfflinePlayerStatsActivity : NoStatusBarActivity() {

    private lateinit var name: KeyValueView
    private lateinit var email: KeyValueView
    private lateinit var startDate: KeyValueView
    private lateinit var xp: KeyValueView
    private lateinit var ft1s: KeyValueView
    private lateinit var pp: KeyValueView
    private lateinit var totalEvents: KeyValueView
    private lateinit var npcEvents: KeyValueView
    private lateinit var lastEvent: KeyValueView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_player_stats)
        setupView()
    }

    private fun setupView() {
        name = findViewById(R.id.offlineplayerstats_name)
        email = findViewById(R.id.offlineplayerstats_email)
        startDate = findViewById(R.id.offlineplayerstats_startdate)
        xp = findViewById(R.id.offlineplayerstats_xp)
        ft1s = findViewById(R.id.offlineplayerstats_ft1s)
        pp = findViewById(R.id.offlineplayerstats_pp)
        totalEvents = findViewById(R.id.offlineplayerstats_totalEvents)
        npcEvents = findViewById(R.id.offlineplayerstats_totalNpcEvents)
        lastEvent = findViewById(R.id.offlineplayerstats_lastEvent)

        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.selectedPlayer.ifLet({
            name.isGone = false
            email.isGone = false
            startDate.isGone = false
            xp.isGone = false
            ft1s.isGone = false
            pp.isGone = false
            totalEvents.isGone = false
            npcEvents.isGone = false
            lastEvent.isGone = false

            name.set(it.fullName)
            email.set(it.username)
            startDate.set(it.startDate.yyyyMMddToMonthDayYear())
            xp.set(it.experience)
            ft1s.set(it.freeTier1Skills)
            pp.set(it.prestigePoints)
            totalEvents.set(it.numEventsAttended)
            npcEvents.set(it.numNpcEventsAttended)
            var lastCheckIn = it.lastCheckIn
            if (lastCheckIn.isNotEmpty()) {
                lastCheckIn = lastCheckIn.yyyyMMddToMonthDayYear()
            }
            lastEvent.set(lastCheckIn)
        }, {
            name.isGone = true
            email.isGone = true
            startDate.isGone = true
            xp.isGone = true
            ft1s.isGone = true
            pp.isGone = true
            totalEvents.isGone = true
            npcEvents.isGone = true
            lastEvent.isGone = true
        })
    }
}