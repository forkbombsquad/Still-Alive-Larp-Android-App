package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.GearJsonListModel
import com.forkbombsquad.stillalivelarp.services.models.GearJsonModel
import com.forkbombsquad.stillalivelarp.services.models.GearModel
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationResult
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.globalToJson
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.google.android.material.textfield.TextInputEditText

class AddEditGearFromBarcodeActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var nameField: TextInputEditText
    private lateinit var desc: TextInputEditText
    private lateinit var submit: LoadingButton
    private lateinit var delete: LoadingButton

    private lateinit var gearType: Spinner
    private lateinit var primarySubtype: Spinner
    private lateinit var secondarySubtype: Spinner

    private lateinit var limitTitle: TextView
    private lateinit var limitDesc: TextView
    private lateinit var classTitle: TextView
    private lateinit var classDesc: TextView

    private var editGear: GearJsonModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_gear_from_barcode)
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

        limitTitle = findViewById(R.id.gearLimitTitle)
        limitDesc = findViewById(R.id.gearLimitDesc)
        classTitle = findViewById(R.id.gearClassificationTitle)
        classDesc = findViewById(R.id.gearClassificationDesc)

        val gearTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Constants.GearTypes.allTypes)
        gearType.adapter = gearTypeAdapter
        gearType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = Constants.GearTypes.allTypes[position]
                runOnUiThread {
                    val gap = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, getPrimarySubtypeList(item))
                    primarySubtype.adapter = gap
                    val gas = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, getSecondarySubtypeList(item))
                    secondarySubtype.adapter = gas
                    this@AddEditGearFromBarcodeActivity.updateLimitAndClassText()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        gearType.setSelection(0)
        val gap = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, getPrimarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition]))
        primarySubtype.adapter = gap
        primarySubtype.setSelection(0)
        val gas = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, getSecondarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition]))
        secondarySubtype.adapter = gas
        primarySubtype.setSelection(0)

        submit.setOnClick {
            submit.setLoading(true)
            val fieldValidation = validateFields()
            if (!fieldValidation.hasError) {
                DataManager.shared.playerCheckInModel?.character.ifLet { char ->

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
                        DataManager.shared.playerCheckInModel?.gear = updatedGearModel
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
                        DataManager.shared.playerCheckInModel?.gear = newGearModel
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
            DataManager.shared.playerCheckInModel?.character.ifLet { char ->

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
                    DataManager.shared.playerCheckInModel?.gear = updatedGearModel
                    DataManager.shared.unrelaltedUpdateCallback()
                    finish()
                }
            }
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.playerCheckInModel?.character?.ifLet { char ->
            title.text = "Add Gear For ${char.fullName}"
            editGear.ifLet { eg ->
                title.text = "Edit Gear For ${char.fullName}"

                nameField.setText(eg.name)
                desc.setText(eg.desc)

                gearType.setSelection(Constants.GearTypes.allTypes.indexOf(eg.gearType))
                val ptypes = getPrimarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition])
                val gap = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, ptypes)
                primarySubtype.adapter = gap
                primarySubtype.setSelection(ptypes.indexOf(eg.primarySubtype))

                val stypes = getSecondarySubtypeList(Constants.GearTypes.allTypes[gearType.selectedItemPosition])
                val gas = ArrayAdapter(this@AddEditGearFromBarcodeActivity, android.R.layout.simple_spinner_dropdown_item, stypes)
                secondarySubtype.adapter = gas
                primarySubtype.setSelection(stypes.indexOf(eg.secondarySubtype))
            }
        }
        submit.textView.text = (editGear == null).ternary("Create", "Update")
        delete.isGone = editGear == null

        updateLimitAndClassText()

    }

    private fun updateLimitAndClassText() {
        getNewCharacterLimitString().ifLet({ limit ->
            limitDesc.text = limit
            limitTitle.isGone = false
            limitDesc.isGone = false
        }, {
            limitTitle.isGone = true
            limitDesc.isGone = true
        })
        getClassificaitonString().ifLet({ classificaiton ->
            classDesc.text = classificaiton
            classTitle.isGone = false
            classDesc.isGone = false
        }, {
            classTitle.isGone = true
            classDesc.isGone = true
        })
    }

    private fun getNewCharacterLimitString(): String? {
        val type = gearType.selectedItem.toString()
        when (type) {
            Constants.GearTypes.meleeWeapon, Constants.GearTypes.firearm -> {
                return "Up to 2 of each type you're proficient with"
            }
            Constants.GearTypes.clothing -> {
                return "1 Mechanically Advantageous piece of Clothing\nNO LIMIT ON: regular clothing"
            }
            Constants.GearTypes.accessory -> {
                return "2 Mechancially Advangageous Accessories (such as flashlights or holsters)\nNO LIMIT ON: non-advantageous accessories (such as safety glasses, sunglasses, belts, masks, headbands, gloves, phones, watches, etc)"
            }
            Constants.GearTypes.bag -> {
                return "3 Small Bags OR 1 Medium Bag and 2 Small Bags OR 1 Large Bag and 1 Small Bag"
            }
        }
        return null
    }

    private fun getClassificaitonString(): String? {
        val type = gearType.selectedItem.toString()
        when (type) {
            Constants.GearTypes.meleeWeapon -> {
                return "Super Light: Coreless\nLight: 22.99\" (57.3cm) or shorter\nMedium: 23\" - 43.99\" (57.4cm - 111.7cm)\nHeavy: 44\" (111.8cm) or longer"
            }
            Constants.GearTypes.firearm -> {
                return "+1 per magazine\n+1 more than 5 bullets\n+1 more than 10 bullets\n+1 more than 15 bullets\n+1 Semi-Auto\n+2 Auto\nRivals or Rockets = Military Grade\n\nLight: 0\nMedium: 1\nHeavy: 2\nAdvanced: 3+\nMilitary Grade: Shoots Rivals or Rockets"
            }
            Constants.GearTypes.clothing, Constants.GearTypes.accessory -> {
                return null
            }
            Constants.GearTypes.bag -> {
                return "Small: 0.5L (30.5cu in) or less\nMedium: 0.5L - 5L (30.5cu in - 305.1cu in)\nLarge: 5L - 25L (305.1cu in - 1,525.6cu in)\nExtra Large: 25L (1,525.6cu in) or more"
            }
        }
        return null
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