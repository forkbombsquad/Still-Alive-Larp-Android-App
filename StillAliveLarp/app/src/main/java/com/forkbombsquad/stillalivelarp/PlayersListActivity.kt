package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ternary

class PlayersListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var innerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players_list)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.playerslist_title)
        innerLayout = findViewById(R.id.playerslist_innerlayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "All Players")
        innerLayout.removeAllViews()
        DataManager.shared.players.alphabetized().forEachIndexed { index, player ->
            val arrow = NavArrowButtonBlackBuildable(this)
            arrow.textView.text = "${player.fullName}${player.isAdmin.ternary(" (Staff)", "")}"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_PLAYER, player)
                val intent = Intent(this, ViewPlayerActivity::class.java)
                startActivity(intent)
            }
            innerLayout.addView(arrow)
        }
    }
}