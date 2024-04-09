package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.EventRegType
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.PreregCell
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

                        var premiums = 0
                        var premiumNpcs = 0
                        var basics = 0
                        var basicNpcs = 0
                        var nots = 0
                        var frees = 0

                        preregs.forEach {
                            val preregCell = PreregCell(this)

                            preregCell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            preregCell.setPadding(8, 16, 8, 16)
                            preregCell.set(it)

                            layout.addView(preregCell)

                            when (it.eventRegType()) {
                                EventRegType.PREMIUM -> {
                                    premiums++
                                    if (it.getCharId() == null) {
                                        premiumNpcs++
                                    }
                                }
                                EventRegType.BASIC -> {
                                    basics++
                                    if (it.getCharId() == null) {
                                        basicNpcs++
                                    }
                                }
                                EventRegType.FREE -> {
                                    frees++
                                }
                                EventRegType.NOT_PREREGED -> nots++
                            }
                        }
                        premium.isGone = false
                        premium.set("$premiums Total ($premiumNpcs NPCs)")

                        basic.isGone = false
                        basic.set("$basics Total ($basicNpcs NPCs)")

                        free.isGone = false
                        free.set("$frees Total (All Are NPCs)")

                        notAttending.isGone = false
                        notAttending.set("$nots")
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