package com.forkbombsquad.stillalivelarp.views.account

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.views.shared.ViewBioActivity
import com.forkbombsquad.stillalivelarp.services.CharacterService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.services.utils.CharacterSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditBioActivity : NoStatusBarActivity() {

    private lateinit var text: TextInputEditText
    private lateinit var updateButton: LoadingButton

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bio)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(ViewBioActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!
        text = findViewById(R.id.editbio_text)
        updateButton = findViewById(R.id.editbio_update)

        updateButton.setOnClick {
            updateButton.setLoading(true)
            val char = character.baseModel()
            char.bio = text.text.toString()
            char.approvedBio = "FALSE"
            val updateBioRequest = CharacterService.UpdateCharacterBio()
            lifecycleScope.launch {
                updateBioRequest.successfulResponse(CharacterSP(char)).ifLet({ _ ->
                    DataManager.shared.load(lifecycleScope) {
                        DataManager.shared.callUpdateCallback(ViewBioActivity::class)
                        AlertUtils.displayOkMessage(this@EditBioActivity, "Success", "${char.fullName}'s bio was updated!") { _, _ ->
                            finish()
                        }
                    }
                }, {
                    updateButton.setLoading(false)
                })
            }
        }

        buildView()
    }

    private fun buildView() {
        text.hint = "Bio\n(Optional, but if your bio is approved, you will earn 1 additional experience)"
        text.setText(character.bio)
    }

}