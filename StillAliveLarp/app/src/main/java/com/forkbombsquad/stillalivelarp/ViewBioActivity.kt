package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment

import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.ifLet

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
        edit.isGone = !(DataManager.shared.playerIsCurrentPlayer(character.id) && character.isAlive) || DataManager.shared.offlineMode || character.characterType() != CharacterType.STANDARD
        text.text = character.bio
    }
}