package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment

import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.views.account.EditBioActivity

class ViewBioActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var edit: NavArrowButtonGreen
    private lateinit var text: TextView

    private lateinit var character: FullCharacterModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bio)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(listOf(MyAccountFragment::class, ViewPlayerActivity::class, ViewNPCStuffActivity::class, ViewCharacterActivity::class), DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.bio_title)
        edit = findViewById(R.id.bio_edit)
        text = findViewById(R.id.bio_text)

        edit.setOnClick {
            DataManager.shared.setUpdateCallback(this::class) {
                character = DataManager.shared.getCharacter(character.id)!!
                buildView()
            }
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            val intent = Intent(this, EditBioActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}'s Bio")
        var showEdit = DataManager.shared.playerIsCurrentPlayer(character.playerId) && character.isAlive && character.characterType() == CharacterType.STANDARD
        if (DataManager.shared.offlineMode) {
            showEdit = false
        }
        edit.isGone = !showEdit
        text.text = character.bio
    }
}