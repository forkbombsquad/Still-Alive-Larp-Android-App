package com.forkbombsquad.stillalivelarp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.GearService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.models.primaryWeapon
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class RegisterPrimaryWeaponActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout
    private lateinit var weaponName: TextInputEditText
    private lateinit var ammo: TextInputEditText
    private lateinit var submitButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_primary_weapon)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.primweapon_title)
        progressBar = findViewById(R.id.primweapon_progressbar)
        layout = findViewById(R.id.primweapon_layout)
        weaponName = findViewById(R.id.primweapon_weaponName)
        ammo = findViewById(R.id.primweapon_ammoAmountAndType)
        submitButton = findViewById(R.id.primweapon_submitButton)

        submitButton.setOnClick {
            submitButton.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                DataManager.shared.selectedChar.ifLet { char ->
                    DataManager.shared.selectedCharacterGear?.primaryWeapon().ifLet({ primaryWeapon ->
                        // Edit
                        val gearModel = GearModel(id = primaryWeapon.id, characterId = char.id, type = Constants.Gear.primaryWeapon, name = weaponName.text.toString(), description = ammo.text.toString())
                        val editRequest = AdminService.UpdateGear()
                        lifecycleScope.launch {
                            editRequest.successfulResponse(UpdateModelSP(gearModel)).ifLet({ gear ->
                                AlertUtils.displaySuccessMessage(this@RegisterPrimaryWeaponActivity, "${gear.name} registered as Primary Weapon for ${char.fullName}") { _, _ ->
                                    DataManager.shared.activityToClose?.finish()
                                    finish()
                                }
                            }, {
                                submitButton.setLoading(false)
                            })
                        }
                    }, {
                        // Create
                        val createGearModel = GearCreateModel(characterId = char.id, type = Constants.Gear.primaryWeapon, name = weaponName.text.toString(), description = ammo.text.toString())
                        val createRequest = AdminService.CreateGear()
                        lifecycleScope.launch {
                            createRequest.successfulResponse(CreateModelSP(createGearModel)).ifLet({ gear ->
                                AlertUtils.displaySuccessMessage(this@RegisterPrimaryWeaponActivity, "${gear.name} registered as Primary Weapon for ${char.fullName}") { _, _ ->
                                    DataManager.shared.activityToClose?.finish()
                                    finish()
                                }
                            }, {
                                submitButton.setLoading(false)
                            })
                        }
                    })
                }
            } else {
                AlertUtils.displayValidationError(this, fieldValidation.getErrorMessages())
                submitButton.setLoading(false)
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SELECTED_CHARACTER_GEAR), forceDownloadIfApplicable = true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        val charName = ("For ${DataManager.shared.selectedChar?.fullName ?: ""}")

        if (DataManager.shared.loadingSelectedCharacterGear) {
            progressBar.isGone = false
            layout.isGone = true
            title.text = "Loading Primary Weapon Registration $charName"
        } else {
            progressBar.isGone = true
            layout.isGone = false
            DataManager.shared.selectedCharacterGear?.primaryWeapon().ifLet({ primWeapon ->
                title.text = "Edit Primary Weapon $charName"
                weaponName.setText(primWeapon.name)
                ammo.setText(primWeapon.description)
            }, {
                title.text = "Register Primary Weapon $charName"
            })
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(weaponName, ValidationType.PRIMARY_WEAPON_NAME),
            ValidationGroup(ammo, ValidationType.PRIMARY_WEAPON_AMMO)
        ))
    }
}