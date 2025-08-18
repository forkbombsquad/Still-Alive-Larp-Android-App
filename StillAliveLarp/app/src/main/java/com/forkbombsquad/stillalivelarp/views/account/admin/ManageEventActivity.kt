package com.forkbombsquad.stillalivelarp.views.account.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.EventsListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel

import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import kotlinx.coroutines.launch

class ManageEventActivity : NoStatusBarActivity() {

    private lateinit var viewTitle: TextView
    private lateinit var title: KeyValueView
    private lateinit var date: KeyValueView
    private lateinit var startTime: KeyValueView
    private lateinit var endTime: KeyValueView
    private lateinit var isStarted: KeyValueView
    private lateinit var isFinished: KeyValueView
    private lateinit var description: KeyValueView
    private lateinit var edit: NavArrowButtonRed
    private lateinit var viewAttendees: NavArrowButtonBlue
    private lateinit var startFinishButton: LoadingButton

    private lateinit var event: FullEventModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event)
        setupView()
    }

    private fun setupView() {
        event = DataManager.shared.getPassedData(EventsListActivity::class, DataManagerPassedDataKey.SELECTED_EVENT)!!

        viewTitle = findViewById(R.id.manageevent_viewtitle)
        title = findViewById(R.id.manageevent_title)
        date = findViewById(R.id.manageevent_date)
        startTime = findViewById(R.id.manageevent_startTime)
        endTime = findViewById(R.id.manageevent_endTime)
        isStarted = findViewById(R.id.manageevent_isStarted)
        isFinished = findViewById(R.id.manageevent_isFinished)
        description = findViewById(R.id.manageevent_description)
        edit = findViewById(R.id.manageevent_edit)
        viewAttendees = findViewById(R.id.manageevent_viewAttendees)
        startFinishButton = findViewById(R.id.manageevent_startFinishButton)

        edit.setOnClick {
            DataManager.shared.addActivityToClose(this, false)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_EVENT, event)
            val intent = Intent(this, EditEventActivity::class.java)
            startActivity(intent)
        }

        viewAttendees.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_EVENT, event)
            val intent = Intent(this, ViewEventAttendeesActivity::class.java)
            startActivity(intent)
        }

        startFinishButton.setOnClick {
            startFinishButton.setLoading(true)
            var starting = false
            if (!event.isStarted) {
                event.isStarted = true
                starting = true
            } else {
                event.isFinished = true
            }
            val updateEventRequest = AdminService.UpdateEvent()
            lifecycleScope.launch {
                updateEventRequest.successfulResponse(UpdateModelSP(event)).ifLet({ _ ->
                    AlertUtils.displaySuccessMessage(this@ManageEventActivity, starting.ternary("Event Started!", "Event Finished!")) { _, _ ->
                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                        DataManager.shared.closeActiviesToClose()
                        finish()
                    }
                }, {
                    startFinishButton.setLoading(false)
                })
            }
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(viewTitle, "Manage Event")
        title.set(event.title)
        date.set(event.date.yyyyMMddToMonthDayYear())
        startTime.set(event.startTime)
        endTime.set(event.endTime)
        isStarted.set(event.isStarted.toString())
        isFinished.set(event.isFinished.toString())
        description.set(event.description)

        if (!event.isFinished) {
            startFinishButton.isGone = false
            startFinishButton.set(event.isStarted.ternary("Finish Event", "Start Event"))
        } else {
            startFinishButton.isGone = true
        }

        if (DataManager.shared.offlineMode) {
            startFinishButton.isGone = true
            edit.isGone = true
        }
    }
}