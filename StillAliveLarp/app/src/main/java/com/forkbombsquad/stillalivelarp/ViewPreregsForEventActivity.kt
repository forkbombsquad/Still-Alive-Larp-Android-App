package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.PreregCell
import com.forkbombsquad.stillalivelarp.utils.getRegNumbers
import com.forkbombsquad.stillalivelarp.utils.ifLet

class ViewPreregsForEventActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout
    private lateinit var title: TextView
    private lateinit var loading: ProgressBar

    private lateinit var premium: KeyValueView
    private lateinit var basic: KeyValueView
    private lateinit var free: KeyValueView
    private lateinit var notAttending: KeyValueView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_preregs_for_event)
        setupView()
    }

    private fun setupView() {
        layout = findViewById(R.id.viewpreregview_layout)
        title = findViewById(R.id.viewpreregview_title)
        loading = findViewById(R.id.viewpreregview_progressbar)

        premium = findViewById(R.id.viewpreregview_premium)
        basic = findViewById(R.id.viewpreregview_basic)
        free = findViewById(R.id.viewpreregview_free)
        notAttending = findViewById(R.id.viewpreregview_notAttending)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLAYERS, DataManagerType.ALL_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        loading.isGone = false
        DataManager.shared.selectedEvent.ifLet({ event ->
            title.text = "Pre-Registration For\n${event.title}"

            if (DataManager.shared.loadingAllCharacters || DataManager.shared.loadingAllPlayers) {
                loading.isGone = false

            } else {
                loading.isGone = true
                val count = DataManager.shared.eventPreregs[event.id]?.count() ?: 0
                if (count > 0) {
                    DataManager.shared.eventPreregs[event.id].ifLet { preregs ->

                        preregs.forEach {
                            val preregCell = PreregCell(this)

                            preregCell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            preregCell.setPadding(8, 16, 8, 16)
                            preregCell.set(it)

                            layout.addView(preregCell)
                        }
                        val regNums = preregs.getRegNumbers()
                        premium.isGone = false
                        premium.set("${regNums.premium} Total (${regNums.premiumNpc} NPCs)")

                        basic.isGone = false
                        basic.set("${regNums.basic} Total (${regNums.basicNpc} NPCs)")

                        free.isGone = false
                        free.set("${regNums.free} Total (All Are NPCs)")

                        notAttending.isGone = false
                        notAttending.set("${regNums.notAttending}")
                    }
                } else {
                    premium.isGone = true
                    basic.isGone = true
                    free.isGone = true
                    notAttending.isGone = true

                    val textView = TextView(this)

                    textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    textView.setPadding(8, 16, 8, 16)
                    textView.text = "There are no Pre-Registrations for this event yet"

                    layout.addView(textView)
                }
            }

        }, {
            title.text = "Error"
        })
    }
}