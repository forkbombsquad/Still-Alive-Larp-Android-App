package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import com.forkbombsquad.stillalivelarp.views.shared.HiddenNPCListActivity
import com.forkbombsquad.stillalivelarp.views.account.admin.AdminPanelActivity
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.reflect.KClass

class CreateNPCorHiddenCharacterActivity : NoStatusBarActivity() {

    private lateinit var bioView: EditText
    private lateinit var nameView: EditText
    private lateinit var submitButton: LoadingButton
    private lateinit var titleView: TextView

    private var isHiddenCharacter: Boolean = false

    private val sourceClasses: List<KClass<*>> = listOf(HiddenNPCListActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_npc_or_hidden_character)
        setupView()
    }

    private fun setupView() {
        isHiddenCharacter = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.IS_HIDDEN_CHARACTER) ?: false
        bioView = findViewById(R.id.bio)
        nameView = findViewById(R.id.fullName)
        submitButton = findViewById(R.id.submitButton)
        titleView = findViewById(R.id.createNpcTitle)

        DataManager.shared.setTitleTextPotentiallyOffline(titleView, isHiddenCharacter.ternary("Create Hidden NPC", "Create NPC"))

        submitButton.setOnClick {
            val validationResult = Validator.validateMultiple(arrayOf(ValidationGroup(nameView, ValidationType.FULL_NAME)))
            if (!validationResult.hasError) {
                submitButton.setLoading(true)
                val request = CharacterService.CreatePlannedCharacter()
                lifecycleScope.launch {
                    request.successfulResponse(
                        CharacterCreateSP(
                            CharacterCreateModel(
                                fullName = nameView.text.toString(),
                                startDate = LocalDate.now().yyyyMMddFormatted(),
                                isAlive = "TRUE",
                                deathDate = "",
                                infection = "0",
                                bio = bioView.text.toString().trim(),
                                approvedBio = "FALSE",
                                bullets = "20",
                                megas = "0",
                                rivals = "0",
                                rockets = "0",
                                bulletCasings = "0",
                                clothSupplies = "0",
                                woodSupplies = "0",
                                metalSupplies = "0",
                                techSupplies = "0",
                                medicalSupplies = "0",
                                armor = CharacterArmor.NONE.text,
                                unshakableResolveUses = "0",
                                mysteriousStrangerUses = "0",
                                playerId = Constants.SpecificCharacterIds.commanderDavis,
                                characterTypeId = isHiddenCharacter.ternary(CharacterType.HIDDEN.id, CharacterType.NPC.id)
                            )
                        )
                    ).ifLet({
                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                        AlertUtils.displayOkMessage(this@CreateNPCorHiddenCharacterActivity, "Success!","${if (isHiddenCharacter) "Hidden NPC" else "NPC"} named ${it.fullName} created!") { _, _ ->
                            finish()
                        }
                    }, {
                        submitButton.setLoading(false)
                    })
                }
            } else {
                AlertUtils.displayOkMessage(this, "Validation Error(s)", validationResult.getErrorMessages())
            }
        }
    }
}