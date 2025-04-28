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
import com.forkbombsquad.stillalivelarp.services.managers.CharacterManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.AwardCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterModel
import com.forkbombsquad.stillalivelarp.services.utils.AwardCreateSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.AwardPlayerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageNPCActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var manageStats: NavArrowButtonBlack
    private lateinit var manageSkills: NavArrowButtonBlack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_npcactivity)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.mannpc_title)
        manageStats = findViewById(R.id.mannpc_managestats)
        manageSkills = findViewById(R.id.mannpc_manageskills)

        manageStats.setOnClick {
            manageStats.setLoading(true)
            manageSkills.setLoading(true)
            AlertUtils.displayMessageWithInputs(
                context = this,
                title = "Adjust ${DataManager.shared.selectedNPCCharacter?.fullName ?: "NPC"} Values",
                editTexts = mapOf(
                    Pair("Bullets", EditText(this).apply {
                        setText(DataManager.shared.selectedNPCCharacter?.bullets ?: "")
                        hint = "Bullets"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }),
                    Pair("Infection", EditText(this).apply {
                        setText(DataManager.shared.selectedNPCCharacter?.infection ?: "0")
                        hint = "Infection Rating"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    })
                ),
                checkboxes = mapOf(
                    Pair("Alive", CheckBox(this).apply {
                        text = "Is Alive?"
                        isChecked = DataManager.shared.selectedNPCCharacter?.isAlive.toBoolean()
                    })
                ),
            ) { response ->

                val updateCharRequest = AdminService.UpdateCharacter()

                var charUpdate = DataManager.shared.selectedNPCCharacter!!.getBaseModel()
                charUpdate.bullets = response["Bullets"] ?: "0"
                charUpdate.infection = response["Infection"] ?: "0"
                charUpdate.isAlive = response["Alive"]?.uppercase() ?: "FALSE"

                lifecycleScope.launch {
                    updateCharRequest.successfulResponse(UpdateModelSP(charUpdate)).ifLet({
                        CharacterManager.shared.fetchFullCharacter(lifecycleScope, it.id) { fcm ->
                            DataManager.shared.selectedNPCCharacter = fcm
                            DataManager.shared.unrelaltedUpdateCallback()
                            AlertUtils.displaySuccessMessage(this@ManageNPCActivity, "Updated NPC!") { _, _ -> }
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
            CharacterManager.shared.fetchFullCharacter(lifecycleScope, DataManager.shared.selectedNPCCharacter!!.id) { fullCharacter ->
                DataManager.shared.selectedPlannedCharacter = fullCharacter
                val request = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                lifecycleScope.launch {
                    request.successfulResponse(IdSP(DataManager.shared.selectedNPCCharacter!!.id)).ifLet { charSkills ->
                        DataManager.shared.selectedPlannedCharacterCharSkills = charSkills.charSkills.toList()
                        val intent = Intent(this@ManageNPCActivity, NPCSkillListActivity::class.java)
                        manageSkills.setLoading(false)
                        startActivity(intent)
                    }
                }
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "Manage NPC\n" + (DataManager.shared.selectedNPCCharacter?.fullName ?: "")
    }
}