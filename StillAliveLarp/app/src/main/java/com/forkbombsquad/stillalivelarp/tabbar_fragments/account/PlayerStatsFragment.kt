package com.forkbombsquad.stillalivelarp.tabbar_fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class PlayerStatsFragment : Fragment() {

    private lateinit var name: KeyValueView
    private lateinit var email: KeyValueView
    private lateinit var startDate: KeyValueView
    private lateinit var xp: KeyValueView
    private lateinit var ft1s: KeyValueView
    private lateinit var pp: KeyValueView
    private lateinit var totalEvents: KeyValueView
    private lateinit var npcEvents: KeyValueView
    private lateinit var lastEvent: KeyValueView
    private lateinit var admin: KeyValueView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_player_stats, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        name = v.findViewById(R.id.playerstats_name)
        email = v.findViewById(R.id.playerstats_email)
        startDate = v.findViewById(R.id.playerstats_startdate)
        xp = v.findViewById(R.id.playerstats_xp)
        ft1s = v.findViewById(R.id.playerstats_ft1s)
        pp = v.findViewById(R.id.playerstats_pp)
        totalEvents = v.findViewById(R.id.playerstats_totalEvents)
        npcEvents = v.findViewById(R.id.playerstats_totalNpcEvents)
        lastEvent = v.findViewById(R.id.playerstats_lastEvent)
        admin = v.findViewById(R.id.playerstats_admin)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.selectedPlayer.ifLet({
            name.isGone = false
            email.isGone = it.id != DataManager.shared.player?.id
            startDate.isGone = false
            xp.isGone = false
            ft1s.isGone = false
            pp.isGone = false
            totalEvents.isGone = false
            npcEvents.isGone = false
            lastEvent.isGone = false
            admin.isGone = !it.isAdmin.toBoolean() && it.id == DataManager.shared.player?.id

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
            admin.set(it.isAdmin)
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
            admin.isGone = true
        })
    }

    companion object {
        fun newInstance() = PlayerStatsFragment()
    }
}