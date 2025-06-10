package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageNPCActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var manageStats: NavArrowButtonBlack
    private lateinit var manageSkills: NavArrowButtonBlack

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_npcactivity)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(NPCListActivity::class, DataManagerPassedDataKey.CHARACTER_LIST)!!

        title = findViewById(R.id.mannpc_title)
        manageStats = findViewById(R.id.mannpc_managestats)
        manageSkills = findViewById(R.id.mannpc_manageskills)

        manageStats.setOnClick {
            manageStats.setLoading(true)
            manageSkills.setLoading(true)
            AlertUtils.displayMessageWithInputs(
                context = this,
                title = "Adjust ${character.fullName} Values",
                editTexts = mapOf(
                    Pair("Bullets", EditText(this).apply {
                        setText(character.bullets.toString())
                        hint = "Bullets"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }),
                    Pair("Infection", EditText(this).apply {
                        setText(character.infection)
                        hint = "Infection Rating"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    })
                ),
                checkboxes = mapOf(
                    Pair("Alive", CheckBox(this).apply {
                        text = "Is Alive?"
                        isChecked = character.isAlive
                    })
                ),
            ) { response ->

                val updateCharRequest = AdminService.UpdateCharacter()

                val charUpdate = character.baseModel()
                charUpdate.bullets = response["Bullets"] ?: "0"
                charUpdate.infection = response["Infection"] ?: "0"
                charUpdate.isAlive = response["Alive"]?.uppercase() ?: "FALSE"

                lifecycleScope.launch {
                    updateCharRequest.successfulResponse(UpdateModelSP(charUpdate)).ifLet({
                        DataManager.shared.load(lifecycleScope) {
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            AlertUtils.displaySuccessMessage(this@ManageNPCActivity, "Updated ${character.fullName}!") { _, _ -> }
                            manageStats.setLoading(false)
                            manageSkills.setLoading(false)
                        }
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@ManageNPCActivity)
                        manageStats.setLoading(false)
                        manageSkills.setLoading(false)
                    })
                }
            }
        }

        manageSkills.setOnClick {
            manageSkills.setLoading(true)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            DataManager.shared.setUpdateCallback(this::class) {
                DataManager.shared.load(lifecycleScope) {
                    reload()
                }
            }
            val intent = Intent(this, ViewSkillsActivity::class.java)
            startActivity(intent)
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Manage NPC\n${character.fullName}")

        manageSkills.setLoading(DataManager.shared.loading)
        manageStats.setLoading(DataManager.shared.loading)
    }
}