package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.CharacterService

import com.forkbombsquad.stillalivelarp.services.utils.CharacterSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditBioActivity : NoStatusBarActivity() {

    private lateinit var text: TextInputEditText
    private lateinit var updateButton: LoadingButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bio)
        setupView()
    }

    private fun setupView() {
        text = findViewById(R.id.editbio_text)
        updateButton = findViewById(R.id.editbio_update)

        updateButton.setOnClick {
            OldDataManager.shared.character.ifLet({
                updateButton.setLoading(true)
                val char = it.getBaseModel()
                char.bio = text.text.toString()
                char.approvedBio = "FALSE"
                val updateBioRequest = CharacterService.UpdateCharacterBio()
                lifecycleScope.launch {
                    updateBioRequest.successfulResponse(CharacterSP(char)).ifLet({
                        AlertUtils.displayOkMessage(this@EditBioActivity, "Success", "${char.fullName}'s bio was updated!") { _, _ ->
                            finish()
                        }
                        OldDataManager.shared.unrelaltedUpdateCallback()
                    }, {
                        updateButton.setLoading(false)
                    })
                }
            },{})
        }

        buildView()
    }

    private fun buildView() {
        text.hint = "Bio\n(Optional, but if your bio is approved, you will earn 1 additional experience)"
        text.setText(OldDataManager.shared.character?.bio ?: "")
    }

    override fun onBackPressed() {
        OldDataManager.shared.unrelaltedUpdateCallback()
        super.onBackPressed()
    }
}