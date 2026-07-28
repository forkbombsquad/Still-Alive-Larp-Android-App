package com.forkbombsquad.stillalivelarp.views.home

import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager

import com.forkbombsquad.stillalivelarp.services.models.CharacterCreateModel
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.utils.CharacterCreateSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.CharacterArmor
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
                if (!validationResult.hasError && player != null) {
                    submitButton.setLoading(true)
                    player!!.createCharacter(lifecycleScope, nameView.text.toString().trim(), bioView.text.toString().trim()) { newCharacter ->
                        if (newCharacter != null) {
                            DataManager.shared.callUpdateCallback(HomeFragment::class)
                            AlertUtils.displayOkMessage(this@CreateCharacterActivity, "Success!","Character named ${newCharacter.fullName} created!") { _, _ ->
                                finish()
                            }
                        } else {
                            submitButton.setLoading(false)
                        }
                    }
                } else {
                    AlertUtils.displayOkMessage(this, "Validation Error(s)", validationResult.getErrorMessages())
                }
            }
        }
    }
}