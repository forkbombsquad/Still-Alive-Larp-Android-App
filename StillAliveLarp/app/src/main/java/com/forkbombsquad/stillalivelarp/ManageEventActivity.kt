package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import kotlinx.coroutines.launch

class ManageEventActivity : NoStatusBarActivity() {

    private lateinit var title: KeyValueView
    private lateinit var date: KeyValueView
    private lateinit var startTime: KeyValueView
    private lateinit var endTime: KeyValueView
    private lateinit var isStarted: KeyValueView
    private lateinit var isFinished: KeyValueView
    private lateinit var description: KeyValueView
    private lateinit var edit: NavArrowButtonRed
    private lateinit var startFinishButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.manageevent_title)
        date = findViewById(R.id.manageevent_date)
        startTime = findViewById(R.id.manageevent_startTime)
        endTime = findViewById(R.id.manageevent_endTime)
        isStarted = findViewById(R.id.manageevent_isStarted)
        isFinished = findViewById(R.id.manageevent_isFinished)
        description = findViewById(R.id.manageevent_description)
        edit = findViewById(R.id.manageevent_edit)
        startFinishButton = findViewById(R.id.manageevent_startFinishButton)

        edit.setOnClick {
            DataManager.shared.activityToClose = this
            val intent = Intent(this, EditEventActivity::class.java)
            startActivity(intent)
        }

        startFinishButton.setOnClick {
            DataManager.shared.selectedEvent.ifLet { event ->
                startFinishButton.setLoading(true)
                var starting = false
                if (!event.isStarted.toBoolean()) {
                    event.isStarted = "TRUE"
                    starting = true
                } else {
                    event.isFinished = "TRUE"
                }
                val updateEventRequest = AdminService.UpdateEvent()
                lifecycleScope.launch {
                    updateEventRequest.successfulResponse(UpdateModelSP(event)).ifLet({ _ ->
                        DataManager.shared.unrelaltedUpdateCallback()
                        AlertUtils.displaySuccessMessage(this@ManageEventActivity, starting.ternary("Event Started!", "Event Finished!")) { _, _ ->
                            finish()
                        }
                    }, {
                        startFinishButton.setLoading(false)
                    })
                }
            }
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.selectedEvent.ifLet { event ->
            title.set(event.title)
            date.set(event.date.yyyyMMddToMonthDayYear())
            startTime.set(event.startTime)
            endTime.set(event.endTime)
            isStarted.set(event.isStarted)
            isFinished.set(event.isFinished)
            description.set(event.description)

            if (!event.isFinished.toBoolean()) {
                startFinishButton.isGone = false
                startFinishButton.set(event.isStarted.toBoolean().ternary("Finish Event", "Start Event"))
            } else {
                startFinishButton.isGone = true
            }
        }
    }
}