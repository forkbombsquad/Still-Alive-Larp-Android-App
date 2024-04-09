package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.models.primaryWeapon
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var type: TextInputEditText
    private lateinit var nameField: TextInputEditText
    private lateinit var desc: TextInputEditText
    private lateinit var submit: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_gear)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.addgear_title)
        type = findViewById(R.id.addgear_type)
        nameField = findViewById(R.id.addgear_name)
        desc = findViewById(R.id.addgear_desc)
        submit = findViewById(R.id.addgear_submit)

        submit.setOnClick {
            submit.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                DataManager.shared.selectedChar.ifLet { char ->
                    val request = AdminService.CreateGear()
                    lifecycleScope.launch {
                        val createGear = GearCreateModel(characterId = char.id, type = type.text.toString(), name = nameField.text.toString(), description = desc.text.toString())
                        request.successfulResponse(CreateModelSP(createGear)).ifLet({ newGear ->
                            AlertUtils.displaySuccessMessage(this@AddGearActivity, "${newGear.name} gear created for ${char.fullName}") { _, _ ->
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
            title.text = "Add Gear For ${char.fullName}"
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