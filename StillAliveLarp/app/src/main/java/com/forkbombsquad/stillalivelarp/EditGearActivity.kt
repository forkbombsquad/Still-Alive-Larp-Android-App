package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var type: TextInputEditText
    private lateinit var nameField: TextInputEditText
    private lateinit var desc: TextInputEditText
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.editgear_title)
        type = findViewById(R.id.editgear_type)
        nameField = findViewById(R.id.editgear_name)
        desc = findViewById(R.id.editgear_desc)
        submit = findViewById(R.id.editgear_submit)

        submit.setOnClick {
            submit.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                DataManager.shared.selectedGear.ifLet { gear ->
                    val request = AdminService.UpdateGear()
                    lifecycleScope.launch {
                        val editedGear = GearModel(id = gear.id, characterId = gear.characterId, type = type.text.toString(), name = nameField.text.toString(), description = desc.text.toString())
                        request.successfulResponse(UpdateModelSP(editedGear)).ifLet({ egear ->
                            AlertUtils.displaySuccessMessage(this@EditGearActivity, "${egear.name} gear edited for ${DataManager.shared.selectedChar?.fullName ?: ""}") { _, _ ->
                                DataManager.shared.unrelaltedUpdateCallback()
                                finish()
                            }
                        }, {
                            submit.setLoading(false)
                        })
                    }
                }
            } else {
                AlertUtils.displayValidationError(this, fieldValidation.getErrorMessages())
                submit.setLoading(false)
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), forceDownloadIfApplicable = true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.selectedChar?.ifLet { char ->
            title.text = "Edit Gear For ${char.fullName}"
        }
        DataManager.shared.selectedGear?.ifLet { gear ->
            type.setText(gear.type)
            nameField.setText(gear.name)
            desc.setText(gear.description)
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(type, ValidationType.GEAR_TYPE),
            ValidationGroup(nameField, ValidationType.GEAR_NAME),
            ValidationGroup(desc, ValidationType.GEAR_DESCRIPTION)
        ))
    }
}