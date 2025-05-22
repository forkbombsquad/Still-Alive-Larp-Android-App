package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class SelectPlayerToChangePassActivity : NoStatusBarActivity() {

    private lateinit var progressbar: ProgressBar
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_player_to_change_pass)
        setupView()
    }

    private fun setupView() {
        progressbar = findViewById(R.id.selectplayertochangepass_progressbar)
        layout = findViewById(R.id.selectplayertochangepass_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_PLAYERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (OldDataManager.shared.loadingAllPlayers) {
            progressbar.isGone = false
            layout.isGone = true
        } else {
            progressbar.isGone = true
            layout.isGone = false

            layout.removeAllViews()

            OldDataManager.shared.allPlayers.ifLet { players ->
                players.alphabetized().forEachIndexed { index, player ->
                    val arrow = NavArrowButtonBlackBuildable(this)
                    arrow.textView.text = player.fullName
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                    arrow.layoutParams = params
                    arrow.setLoading(false)
                    arrow.setOnClick {
                        OldDataManager.shared.selectedPlayer = player
                        OldDataManager.shared.activityToClose = this
                        val intent = Intent(this, ChangePlayerPasswordActivity::class.java)
                        startActivity(intent)
                    }
                    layout.addView(arrow)
                }
            }
        }
    }
}