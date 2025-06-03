package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.services.models.CharacterCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.tabbar_fragments.HomeFragment
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ValidationGroup
import com.forkbombsquad.stillalivelarp.utils.ValidationType
import com.forkbombsquad.stillalivelarp.utils.Validator
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddFormatted
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateCharacterActivity : NoStatusBarActivity() {

    private lateinit var bioView: EditText
    private lateinit var nameView: EditText
    private lateinit var submitButton: LoadingButton

    private var player: FullPlayerModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_character)
        setupView()
    }

    private fun setupView() {
        DataManager.shared.load(lifecycleScope) {
            bioView = findViewById(R.id.bio)
            nameView = findViewById(R.id.fullName)
            submitButton = findViewById(R.id.submitButton)

            player = DataManager.shared.getCurrentPlayer()

            bioView.hint = "Bio (Optional, but if your bio is approved, you will earn 1 additional experience. You always have the option to add a bio later on and earn 1xp then as well.)"

            submitButton.setOnClick {
                val validationResult = Validator.validateMultiple(arrayOf(ValidationGroup(nameView, ValidationType.FULL_NAME)))
                if (!validationResult.hasError) {
                    submitButton.setLoading(true)
                    val request = CharacterService.CreateCharacter()
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
                                    playerId = player?.id ?: -1,
                                    characterTypeId = CharacterType.STANDARD.id
                                )
                            )
                        ).ifLet({
                            DataManager.shared.callUpdateCallback(HomeFragment::class)
                            AlertUtils.displayOkMessage(this@CreateCharacterActivity, "Success!","Character named ${it.fullName} created!") { _, _ ->
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
}