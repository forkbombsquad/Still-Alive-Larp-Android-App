package com.forkbombsquad.stillalivelarp.views.account.admin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.views.shared.CharactersListActivity
import com.forkbombsquad.stillalivelarp.views.shared.EventsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.PlayersListActivity
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel

import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.ButtonTypePressed
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.MessageInput
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
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
        event = DataManager.shared.getPassedData(listOf(EventsListActivity::class, MyAccountFragment::class), DataManagerPassedDataKey.SELECTED_EVENT)!!

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
            // TODO only commenting out these lines for testing. Put them back.
//            val updateEventRequest = AdminService.UpdateEvent()
//            lifecycleScope.launch {
//                updateEventRequest.successfulResponse(UpdateModelSP(event)).ifLet({ _ ->
                    if (starting) {
                        // Event is being started for first time - simple success
                        AlertUtils.displaySuccessMessage(this@ManageEventActivity, "Event Started!") { _, _ ->
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    } else {
                        // Event is being finished - show pre-finish dialog chain
                        promptForRaffleAward()
                    }
//                }, {
//                    startFinishButton.setLoading(false)
//                })
//            }
        }

        buildView()
    }

    private fun promptForRaffleAward() {
        AlertUtils.displayMessage(
            this,
            "Award Raffle Winner?",
            "Players: Xp, Free Tier 1 Skils, Prestige Points\nCharacters: Infection, Ammo, Materials",
            arrayOf(
                AlertButton("Character", { _, _ ->
                    launchCharacterAwardSelection()
                }, ButtonType.POSITIVE),
                AlertButton("Continue", { _, _ ->
                    finishEventFlowPromptForFood()
                }, ButtonType.NEUTRAL),
                AlertButton("Player", { _, _ ->
                    launchPlayerAwardSelection()
                }, ButtonType.NEGATIVE),
            )
        )
    }

    private fun launchCharacterAwardSelection() {
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.STANDARD))
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardCharacterActivity::class)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Character to Award")
        DataManager.shared.setUpdateCallback(this::class) {
            // After award is given, loop back to raffle prompt
            this@ManageEventActivity.promptForRaffleAward()
        }
        val intent = Intent(this, CharactersListActivity::class.java)
        startActivity(intent)
    }

    private fun launchPlayerAwardSelection() {
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.PLAYER_LIST, DataManager.shared.players)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, AwardPlayerActivity::class)
        DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "Select Player To Award")
        DataManager.shared.setUpdateCallback(this::class) {
            // After award is given, loop back to raffle prompt
            this@ManageEventActivity.promptForRaffleAward()
        }
        val intent = Intent(this, PlayersListActivity::class.java)
        startActivity(intent)
    }

    private fun finishEventFlowPromptForFood() {
        AlertUtils.displayMessageWithInputs(
            this,
            "How Much Food Was Collected?",
                listOf(
                    MessageInput(
                    "food",
                    TextView(this).apply {
                        text = "Food Required To Meed Threshold: TODO"
                    },
                    EditText(this).apply {
                        hint = "Food Collected"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    },
                    null,
                    null),
                ),
                "Submit",
                "Skip") { messageOutput ->
            if (messageOutput.buttonPressed == ButtonTypePressed.POSITIVE) {
                (messageOutput.getValuesForKey("food")?.editTextValue ?: "0").toIntOrNull().ifLet({ food ->
                    // TODO do food calc
                    // And other stuff like adding resoureces to commander davis (after clearing existing ones)
                    // ALlow it to be customized so we can change the nubmer without app update
                  finishEventFlow()
                }, {
                    AlertUtils.displayOkMessage(this, "Must enter a number!", "Please") { _, _ ->
                        finishEventFlowPromptForFood()
                    }
                })
            } else {
                finishEventFlow()
            }
        }
    }

    private fun finishEventFlow() {
        AlertUtils.displaySuccessMessage(this, "${event.title} Finished!") { _, _ ->
            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
            DataManager.shared.closeActiviesToClose()
            finish()
        }
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