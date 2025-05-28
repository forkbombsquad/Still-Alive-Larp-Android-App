package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService

import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.KeyValuePickerView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AwardPlayerActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var awardType: KeyValuePickerView
    private lateinit var amount: TextInputEditText
    private lateinit var reason: TextInputEditText
    private lateinit var submitButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_award_player)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.awardplayer_title)
        awardType = findViewById(R.id.awardplayer_awardTypeKVPicker)
        amount = findViewById(R.id.awardplayer_amount)
        reason = findViewById(R.id.awardplayer_reason)
        submitButton = findViewById(R.id.awardplayer_submitButton)

        val awardTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Xp", "Prestige Points", "Free Tier-1 Skills"))
        awardType.valuePickerView.adapter = awardTypeAdapter
        awardType.valuePickerView.setSelection(0)

        submitButton.setOnClick {
            OldDataManager.shared.selectedPlayer.ifLet { player ->
                submitButton.setLoading(true)
                val awardCreateModel = AwardCreateModel.createPlayerAward(
                    player = player,
                    awardType = getAwardType(),
                    reason = reason.text.toString(),
                    amount = amount.text.toString()
                )
                val awardPlayerRequest = AdminService.AwardPlayer()
                lifecycleScope.launch {
                    awardPlayerRequest.successfulResponse(AwardCreateSP(awardCreateModel)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@AwardPlayerActivity, "Successfully Awarded ${player.fullName}!") { _, _ ->
                            OldDataManager.shared.activityToClose?.finish()
                            finish()
                        }
                    }, {
                        submitButton.setLoading(false)
                    })
                }
            }
        }

        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.selectedPlayer.ifLet {
            title.text = "Give Award To ${it.fullName}"
        }
    }

    private fun getAwardType(): AwardPlayerType {
        return when (awardType.valuePickerView.selectedItemPosition) {
            0 -> AwardPlayerType.XP
            1 -> AwardPlayerType.PRESTIGEPOINTS
            2 -> AwardPlayerType.FREETIER1SKILLS
            else -> AwardPlayerType.XP
        }
    }
}