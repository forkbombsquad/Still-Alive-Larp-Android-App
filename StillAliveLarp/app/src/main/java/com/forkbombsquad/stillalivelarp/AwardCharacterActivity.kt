package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardCharType
import com.forkbombsquad.stillalivelarp.utils.KeyValuePickerView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AwardCharacterActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var awardType: KeyValuePickerView
    private lateinit var awardSecondaryType: KeyValuePickerView
    private lateinit var amount: TextInputEditText
    private lateinit var reason: TextInputEditText
    private lateinit var submitButton: LoadingButton

    private lateinit var secondaryMaterialAdapter: ArrayAdapter<String>
    private lateinit var secondaryAmmoAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_award_character)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.awardchar_title)
        awardType = findViewById(R.id.awardchar_awardTypeKVPicker)
        awardSecondaryType = findViewById(R.id.awardchar_awardSecondaryTypeKVPicker)
        amount = findViewById(R.id.awardchar_amount)
        reason = findViewById(R.id.awardchar_reason)
        submitButton = findViewById(R.id.awardchar_submitButton)

        val awardTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Material", "Ammo", "Infection"))
        awardType.valuePickerView.adapter = awardTypeAdapter
        awardType.valuePickerView.setSelection(0)

        secondaryMaterialAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Casing", "Wood", "Cloth", "Metal", "Tech", "Medical"))
        secondaryAmmoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Bullet", "Mega", "Rival", "Rocket"))

        awardSecondaryType.valuePickerView.adapter = secondaryMaterialAdapter
        awardSecondaryType.valuePickerView.setSelection(0)

        awardType.valuePickerView.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (awardType.valuePickerView.selectedItemPosition) {
                    0 -> {
                        awardSecondaryType.isGone = false
                        awardSecondaryType.valuePickerView.setSelection(0)
                        awardSecondaryType.valuePickerView.adapter = secondaryMaterialAdapter
                        awardSecondaryType.valuePickerView.setSelection(0)
                    }
                    1 -> {
                        awardSecondaryType.isGone = false
                        awardSecondaryType.valuePickerView.setSelection(0)
                        awardSecondaryType.valuePickerView.adapter = secondaryAmmoAdapter
                        awardSecondaryType.valuePickerView.setSelection(0)
                    }
                    2 -> {
                        awardSecondaryType.isGone = true
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        submitButton.setOnClick {
            OldDataManager.shared.selectedChar.ifLet { character ->
                submitButton.setLoading(true)
                val awardCreateModel = AwardCreateModel.createCharacterAward(
                    char = character,
                    awardType = getAwardType(),
                    reason = reason.text.toString(),
                    amount = amount.text.toString()
                )
                val awardCharRequest = AdminService.AwardCharacter()
                lifecycleScope.launch {
                    awardCharRequest.successfulResponse(AwardCreateSP(awardCreateModel)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@AwardCharacterActivity, "Successfully Awarded ${character.fullName}!") { _, _ ->
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
        OldDataManager.shared.selectedChar.ifLet {
            title.text = "Give Award To ${it.fullName}"
        }
    }

    private fun getAwardType(): AwardCharType {
        return when (awardType.valuePickerView.selectedItemPosition) {
            0 -> {
                when(awardSecondaryType.valuePickerView.selectedItemPosition) {
                    0 -> AwardCharType.MATERIALCASINGS
                    1 -> AwardCharType.MATERIALWOOD
                    2 -> AwardCharType.MATERIALCLOTH
                    3 -> AwardCharType.MATERIALMETAL
                    4 -> AwardCharType.MATERIALTECH
                    5 -> AwardCharType.MATERIALMED
                    else -> AwardCharType.MATERIALCASINGS
                }
            }
            1 -> {
                when(awardSecondaryType.valuePickerView.selectedItemPosition) {
                    0 -> AwardCharType.AMMOBULLET
                    1 -> AwardCharType.AMMOMEGA
                    2 -> AwardCharType.AMMORIVAL
                    3 -> AwardCharType.AMMOROCKET
                    else -> AwardCharType.AMMOBULLET
                }
            }
            2 -> AwardCharType.INFECTION
            else -> AwardCharType.INFECTION
        }
    }
}