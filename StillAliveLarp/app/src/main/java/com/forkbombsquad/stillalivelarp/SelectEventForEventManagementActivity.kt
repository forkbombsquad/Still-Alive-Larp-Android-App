package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope

import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreenBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class SelectEventForEventManagementActivity : NoStatusBarActivity() {

    private lateinit var progressbar: ProgressBar
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_event_for_event_management)
        setupView()
    }

    private fun setupView() {
        progressbar = findViewById(R.id.selecteventformanagement_progressbar)
        layout = findViewById(R.id.selecteventformanagement_layout)

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENTS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (OldDataManager.shared.loadingEvents) {
            progressbar.isGone = false
            layout.isGone = true
        } else {
            progressbar.isGone = true
            layout.isGone = false

            layout.removeAllViews()

            val arrow = NavArrowButtonRedBuildable(this)
            arrow.textView.text = "Add New Event"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 32, 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                OldDataManager.shared.unrelaltedUpdateCallback = {
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENTS), true) {
                        buildView()
                    }
                    buildView()
                }
                val intent = Intent(this, CreateNewEventActivity::class.java)
                startActivity(intent)
            }
            layout.addView(arrow)

            OldDataManager.shared.events.ifLet { events ->
                events.forEachIndexed { index, event ->
                    var view = View(this)
                    if (event.isFinished.toBoolean()) {
                        val arrow = NavArrowButtonBlueBuildable(this)
                        arrow.textView.text = "${event.title} - ${event.date.yyyyMMddToMonthDayYear()}"
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 16)
                        arrow.layoutParams = params
                        arrow.setLoading(false)
                        arrow.setOnClick {
                            onClickEvent(event)
                        }
                        view = arrow
                    } else if (event.isStarted.toBoolean()) {
                        val arrow = NavArrowButtonGreenBuildable(this)
                        arrow.textView.text = "${event.title} - ${event.date.yyyyMMddToMonthDayYear()}"
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 16)
                        arrow.layoutParams = params
                        arrow.setLoading(false)
                        arrow.setOnClick {
                            onClickEvent(event)
                        }
                        view = arrow
                    } else {
                        val arrow = NavArrowButtonBlackBuildable(this)
                        arrow.textView.text = "${event.title} - ${event.date.yyyyMMddToMonthDayYear()}"
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(0, 16, 0, 16)
                        arrow.layoutParams = params
                        arrow.setLoading(false)
                        arrow.setOnClick {
                            onClickEvent(event)
                        }
                        view = arrow
                    }
                    layout.addView(view)
                }
            }
        }
    }

    private fun onClickEvent(event: EventModel) {
        OldDataManager.shared.selectedEvent = event
        OldDataManager.shared.unrelaltedUpdateCallback = {
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENTS), true) {
                buildView()
            }
            buildView()
        }
        val intent = Intent(this, ManageEventActivity::class.java)
        startActivity(intent)
    }
}