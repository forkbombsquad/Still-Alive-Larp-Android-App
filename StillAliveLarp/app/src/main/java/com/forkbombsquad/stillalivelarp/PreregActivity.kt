package com.forkbombsquad.stillalivelarp

import android.media.metrics.Event
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.EventPreregService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.EventModel
import com.forkbombsquad.stillalivelarp.services.models.EventPreregCreateModel
import com.forkbombsquad.stillalivelarp.services.models.EventPreregModel
import com.forkbombsquad.stillalivelarp.services.models.EventRegType
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import kotlinx.coroutines.launch

class PreregActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var loading: ProgressBar
    private lateinit var dataLayout: LinearLayout
    private lateinit var player: KeyValueView
    private lateinit var character: KeyValuePickerView
    private lateinit var event: KeyValueView
    private lateinit var entryType: KeyValuePickerView
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prereg)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.prereg_title)
        loading = findViewById(R.id.prereg_loading)
        dataLayout = findViewById(R.id.prereg_dataLayout)
        player = findViewById(R.id.prereg_player)
        character = findViewById(R.id.prereg_character)
        event = findViewById(R.id.prereg_event)
        entryType = findViewById(R.id.prereg_entryType)
        submit = findViewById(R.id.prereg_submit)

        val regTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Not Attending", "Free Entry", "Basic Donation Tier ($15)", "Premium Donation Tier ($25 or more)"))
        entryType.valuePickerView.adapter = regTypeAdapter
        entryType.valuePickerView.setSelection(2)

        entryType.valuePickerView.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // If free entry is selected, delesect the character.
                if (position == 1) {
                    character.valuePickerView.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        submit.setOnClick {
            submit.setLoading(true)
            val player = DataManager.shared.player
            val char = DataManager.shared.character
            val event = DataManager.shared.currentEvent
            val existingPrereg = getExistingPrereg(event)

            existingPrereg.ifLet({
                // Update

                var charId = char?.id
                if (character.valuePickerView.selectedItemPosition == 0) {
                    charId = null
                }

                val preregUpdate = EventPreregModel(
                    id = it.id,
                    playerId = it.playerId,
                    characterId = charId,
                    eventId = it.eventId,
                    regType = getRegType()
                )

                val preregUpdateRequest = EventPreregService.UpdatePrereg()
                lifecycleScope.launch {
                    preregUpdateRequest.successfulResponse(UpdateModelSP(preregUpdate)).ifLet({
                        AlertUtils.displayOkMessage(this@PreregActivity, "Preregistration Updated Successfully", "") { _, _ ->
                            finish()
                        }
                        DataManager.shared.unrelaltedUpdateCallback()
                    }, {
                        submit.setLoading(false)
                    })
                }
            }, {
                // Submit new

                var charId = char?.id
                if (character.valuePickerView.selectedItemPosition == 0) {
                    charId = null
                }

                val preregCreate = EventPreregCreateModel(
                    playerId = player?.id ?: -1,
                    characterId = charId,
                    eventId = event?.id ?: -1,
                    regType = getRegType()
                )

                val preregPlayerRequest = EventPreregService.PreregPlayer()
                lifecycleScope.launch {
                    preregPlayerRequest.successfulResponse(CreateModelSP(preregCreate)).ifLet({
                        AlertUtils.displayOkMessage(this@PreregActivity, "Preregistration Successful!", "") { _, _ ->
                            finish()
                        }
                        DataManager.shared.unrelaltedUpdateCallback()
                    }, {
                        submit.setLoading(false)
                    })
                }
            })

        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.EVENT_PREREGS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        val load = DataManager.shared.loadingPlayer || DataManager.shared.loadingCharacter || DataManager.shared.loadingEventPreregs || DataManager.shared.selectedEvent == null
        loading.isGone = !load
        dataLayout.isGone = load

        DataManager.shared.character.ifLet({ char ->
            val charSelectionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("NPC", char.fullName))
            character.valuePickerView.adapter = charSelectionAdapter
            character.valuePickerView.setSelection(1)
        }, {
            val charSelectionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("NPC"))
            character.valuePickerView.adapter = charSelectionAdapter
            character.valuePickerView.setSelection(0)
        })

        DataManager.shared.player.ifLet {
            player.set(it.fullName)
        }

        DataManager.shared.selectedEvent.ifLet { ev ->
            event.set(ev.title)
            getExistingPrereg(ev).ifLet({ existingPrereg ->
                title.text = "Update Preregistration"
                submit.textView.text = "Update"
                character.valuePickerView.setSelection((existingPrereg.getCharId() != null).ternary(1, 0))

                var entrySelect = when (existingPrereg.eventRegType()) {
                    EventRegType.NOT_PREREGED -> 0
                    EventRegType.FREE -> 1
                    EventRegType.BASIC -> 2
                    EventRegType.PREMIUM -> 3
                }
                entryType.valuePickerView.setSelection(entrySelect)

            }, {
                title.text = "Preregistration"
                submit.textView.text = "Submit"
            })
        }
    }

    private fun getExistingPrereg(ev: EventModel?): EventPreregModel? {
        ev?.let { event ->
            DataManager.shared.eventPreregs[event.id]?.let {
                return it.firstOrNull { prereg -> prereg.playerId == (DataManager.shared.player?.id ?: -1) }
            } ?: run {
                return null
            }
        } ?: run {
            return null
        }
    }

    private fun getRegType(): EventRegType {
        return when (entryType.valuePickerView.selectedItemPosition) {
            0 -> EventRegType.NOT_PREREGED
            1 -> EventRegType.FREE
            2 -> EventRegType.BASIC
            3 -> EventRegType.PREMIUM
            else -> EventRegType.NOT_PREREGED
        }
    }
}