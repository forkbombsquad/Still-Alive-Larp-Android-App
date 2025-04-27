package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.GearCreateModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonListModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddEditGearActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var nameField: TextInputEditText
    private lateinit var desc: TextInputEditText
    private lateinit var submit: LoadingButton
    private lateinit var delete: LoadingButton

    private lateinit var gearType: Spinner
    private lateinit var primarySubtype: Spinner
    private lateinit var secondarySubtype: Spinner

    private var editGear: GearJsonModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_gear)
        setupView()
    }

    private fun setupView() {
        editGear = DataManager.shared.gearToEdit

        title = findViewById(R.id.addgear_title)
        nameField = findViewById(R.id.addgear_name)
        gearType = findViewById(R.id.gearTypeSpinner)
        primarySubtype = findViewById(R.id.gearPrimarySubtypeSpinner)
        secondarySubtype = findViewById(R.id.gearSecondarySubtypeSpinner)
        desc = findViewById(R.id.addgear_desc)
        submit = findViewById(R.id.addgear_submit)
        delete = findViewById(R.id.addgear_delete)

        val gearTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Constants.GearTypes.allTypes)
        gearType.adapter = gearTypeAdapter
        gearType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = Constants.GearTypes.allTypes[position]
                runOnUiThread {
                    val gap = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, getPrimarySubtypeList(item))
                    primarySubtype.adapter = gap
                    val gas = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, getSecondarySubtypeList(item))
                    secondarySubtype.adapter = gas
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        gearType.setSelection(0)
        val gap = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, getPrimarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition]))
        primarySubtype.adapter = gap
        primarySubtype.setSelection(0)
        val gas = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, getSecondarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition]))
        secondarySubtype.adapter = gas
        primarySubtype.setSelection(0)

        submit.setOnClick {
            submit.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                DataManager.shared.selectedChar.ifLet { char ->

                    val gearList = DataManager.shared.selectedCharacterGear ?: arrayOf()
                    val gear = gearList.firstOrNull()
                    val jsonGearList = gear?.jsonModels?.toMutableList() ?: mutableListOf()
                    if (gear != null) {
                        // Add or Edit Gear in existing json
                        var alreadyDeletedFirstMatch = false
                        if (editGear != null) {
                            jsonGearList.removeIf {
                                val prevVal = alreadyDeletedFirstMatch
                                if (it.isEqualTo(editGear!!)) {
                                    alreadyDeletedFirstMatch = true
                                }
                                it.isEqualTo(editGear!!) && !prevVal
                            }
                        }
                        if (secondarySubtype.selectedItem.toString() == Constants.GearSecondarySubtype.primaryFirearm) {
                            // Remove existing primary firearms
                            val prim = jsonGearList.firstOrNull { it.isPrimaryFirearm() }
                            if (prim != null) {
                                val newPrim = prim.duplicateWithEdit(secondarySubtype = Constants.GearSecondarySubtype.none)
                                var alreadyDeletedFirstMatch = false
                                if (editGear != null) {
                                    jsonGearList.removeIf {
                                        val prevVal = alreadyDeletedFirstMatch
                                        if (it.isEqualTo(prim)) {
                                            alreadyDeletedFirstMatch = true
                                        }
                                        it.isEqualTo(editGear!!) && !prevVal
                                    }
                                }
                                jsonGearList.add(newPrim)
                            }
                        }

                        // Create new gear
                        val gjm = GearJsonModel(
                            name = nameField.text.toString(),
                            gearType = gearType.selectedItem.toString(),
                            primarySubtype = primarySubtype.selectedItem.toString(),
                            secondarySubtype = secondarySubtype.selectedItem.toString(),
                            desc = desc.text.toString()
                        )
                        jsonGearList.add(gjm)

                        val gearJsonListModel = GearJsonListModel(jsonGearList.toTypedArray())
                        val toJson: String = globalToJson(gearJsonListModel)

                        val updatedGearModel = GearModel(gear.id, gear.characterId, toJson)
                        DataManager.shared.selectedCharacterGear = arrayOf(updatedGearModel)
                        DataManager.shared.unrelaltedUpdateCallback()
                        finish()
                    } else {
                        // Create New Gear json list
                        val gjm = GearJsonModel(
                            name = nameField.text.toString(),
                            gearType = gearType.selectedItem.toString(),
                            primarySubtype = primarySubtype.selectedItem.toString(),
                            secondarySubtype = secondarySubtype.selectedItem.toString(),
                            desc = desc.text.toString()
                        )
                        jsonGearList.add(gjm)

                        val gearJsonListModel = GearJsonListModel(jsonGearList.toTypedArray())
                        val toJson: String = globalToJson(gearJsonListModel)

                        val newGearModel = GearModel(-1, char.id, toJson)
                        DataManager.shared.selectedCharacterGear = arrayOf(newGearModel)
                        DataManager.shared.unrelaltedUpdateCallback()
                        finish()
                    }
                }
            } else {
                AlertUtils.displayValidationError(this, fieldValidation.getErrorMessages())
                submit.setLoading(false)
            }
        }

        delete.setOnClick {
            delete.setLoading(true)
            DataManager.shared.selectedChar.ifLet { char ->

                val gearList = DataManager.shared.selectedCharacterGear ?: arrayOf()
                val gear = gearList.firstOrNull()
                val jsonGearList = gear?.jsonModels?.toMutableList() ?: mutableListOf()
                if (gear != null) {
                    var alreadyDeletedFirstMatch = false
                    if (editGear != null) {
                        jsonGearList.removeIf {
                            val prevVal = alreadyDeletedFirstMatch
                            if (it.isEqualTo(editGear!!)) {
                                alreadyDeletedFirstMatch = true
                            }
                            it.isEqualTo(editGear!!) && !prevVal
                        }
                    }

                    val gearJsonListModel = GearJsonListModel(jsonGearList.toTypedArray())
                    val toJson: String = globalToJson(gearJsonListModel)

                    val updatedGearModel = GearModel(gear.id, gear.characterId, toJson)
                    DataManager.shared.selectedCharacterGear = arrayOf(updatedGearModel)
                    DataManager.shared.unrelaltedUpdateCallback()
                    finish()
                }
            }
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.selectedChar?.ifLet { char ->
            title.text = "Add Gear For ${char.fullName}"
            editGear.ifLet { eg ->
                title.text = "Edit Gear For ${char.fullName}"

                nameField.setText(eg.name)
                desc.setText(eg.desc)

                gearType.setSelection(Constants.GearTypes.allTypes.indexOf(eg.gearType))
                val ptypes = getPrimarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition])
                val gap = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, ptypes)
                primarySubtype.adapter = gap
                primarySubtype.setSelection(ptypes.indexOf(eg.primarySubtype))

                val stypes = getSecondarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition])
                val gas = ArrayAdapter(this@AddEditGearActivity, android.R.layout.simple_spinner_dropdown_item, stypes)
                secondarySubtype.adapter = gas
                primarySubtype.setSelection(stypes.indexOf(eg.secondarySubtype))
            }
        }
        submit.textView.text = (editGear == null).ternary("Create", "Update")
        delete.isGone = editGear == null
    }

    private fun getPrimarySubtypeList(type: String): List<String> {
        val t = Constants.GearTypes
        val p = Constants.GearPrimarySubtype
        return when (type) {
            t.meleeWeapon -> p.allMeleeTypes
            t.firearm -> p.allFirearmTypes
            t.clothing -> p.allClothingTypes
            t.accessory -> p.allAccessoryTypes
            t.bag -> p.allBagTypes
            t.other -> p.allOtherTypes
            else -> listOf()
        }
    }

    private fun getSecondarySubtypeList(type: String): List<String> {
        val t = Constants.GearTypes
        val s = Constants.GearSecondarySubtype
        return when (type) {
            t.meleeWeapon -> s.allNonFirearmTypes
            t.firearm -> s.allFirearmTypes
            t.clothing -> s.allNonFirearmTypes
            t.accessory -> s.allNonFirearmTypes
            t.bag -> s.allNonFirearmTypes
            t.other -> s.allNonFirearmTypes
            else -> listOf()
        }
    }

    private fun validateFields(): ValidationResult {
        return Validator.validateMultiple(arrayOf(
            ValidationGroup(nameField, ValidationType.GEAR_NAME),
            ValidationGroup(desc, ValidationType.GEAR_DESCRIPTION)
        ))
    }
}