package com.forkbombsquad.stillalivelarp.views.shared

import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class ViewPlayerStatsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
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
    private lateinit var playerId: KeyValueView

    private lateinit var player: FullPlayerModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_player_stats)
        setupView()
    }

    private fun setupView() {
        player = DataManager.shared.getPassedData(listOf(ViewPlayerActivity::class, MyAccountFragment::class), DataManagerPassedDataKey.SELECTED_PLAYER)!!

        title = findViewById(R.id.playerstats_title)
        name = findViewById(R.id.playerstats_name)
        email = findViewById(R.id.playerstats_email)
        startDate = findViewById(R.id.playerstats_startdate)
        xp = findViewById(R.id.playerstats_xp)
        ft1s = findViewById(R.id.playerstats_ft1s)
        pp = findViewById(R.id.playerstats_pp)
        totalEvents = findViewById(R.id.playerstats_totalEvents)
        npcEvents = findViewById(R.id.playerstats_totalNpcEvents)
        lastEvent = findViewById(R.id.playerstats_lastEvent)
        admin = findViewById(R.id.playerstats_admin)
        playerId = findViewById(R.id.playerstats_playerId)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Player Stats")

        name.set(player.fullName)
        email.set(player.username)
        startDate.set(player.startDate.yyyyMMddToMonthDayYear())
        xp.set("${player.experience} xp")
        ft1s.set("${player.freeTier1Skills} FT1S")
        pp.set("${player.prestigePoints} pp")
        totalEvents.set(player.numEventsAttended.toString())
        npcEvents.set(player.numNpcEventsAttended.toString())
        lastEvent.set(player.lastCheckIn.isNotEmpty().ternary(player.lastCheckIn.yyyyMMddToMonthDayYear(), ""))
        admin.set(player.isAdmin.toString().uppercase())
        playerId.set(player.id.toString())

        if (DataManager.shared.getCurrentPlayer()?.isAdmin == false) {
            admin.isGone = true
            email.isGone = !DataManager.shared.playerIsCurrentPlayer(player)
            playerId.isGone = !DataManager.shared.playerIsCurrentPlayer(player)
        } else {
            admin.isGone = false
            email.isGone = false
            playerId.isGone = false
        }

    }
}