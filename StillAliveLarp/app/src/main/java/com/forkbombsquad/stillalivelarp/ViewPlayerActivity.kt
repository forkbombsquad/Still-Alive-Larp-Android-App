package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.utils.CharacterPanel
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap

class ViewPlayerActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var profileImage: ImageView
    private lateinit var playerStats: NavArrowButtonBlack
    private lateinit var playerAwards: NavArrowButtonBlack
    private lateinit var characterPanel: CharacterPanel

    private lateinit var player: FullPlayerModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_player)
        setupView()
    }

    private fun setupView() {
        player = DataManager.shared.getPassedData(PlayersListActivity::class, DataManagerPassedDataKey.SELECTED_PLAYER)!!

        title = findViewById(R.id.viewplayer_title)
        profileImage = findViewById(R.id.playerview_profileImage)
        playerStats = findViewById(R.id.playerview_stats)
        playerAwards = findViewById(R.id.playerview_viewAwards)
        characterPanel = findViewById(R.id.playerview_charPanel)

        playerStats.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_PLAYER, player)
            val intent = Intent(this, ViewPlayerStatsActivity::class.java)
            startActivity(intent)
        }

        playerAwards.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, player.getAwardsSorted())
            val intent = Intent(this, ViewAwardsActivity::class.java)
            startActivity(intent)
        }

        characterPanel.setOnClicks(
            viewStatsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, player.getActiveCharacter()!!)
                val intent = Intent(this, ViewCharacterStatsActivity::class.java)
                startActivity(intent)
            },
            viewSkillsTreeCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, player.getActiveCharacter()!!)
                val intent = Intent(this, OtherCharacterPersonalNativeSkillTreeActivity::class.java)
                startActivity(intent)
            },
            viewSkillsListCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, player.getActiveCharacter()!!)
                val intent = Intent(this, ViewSkillsActivity::class.java)
                startActivity(intent)
            },
            viewBioCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, player.getActiveCharacter()!!)
                val intent = Intent(this, ViewBioActivity::class.java)
                startActivity(intent)
            },
            viewGearCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, player.getActiveCharacter()!!)
                val intent = Intent(this, ViewGearActivity::class.java)
                startActivity(intent)
            },
            viewAwardsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, player.getActiveCharacter()!!.getAwardsSorted())
                val intent = Intent(this, ViewAwardsActivity::class.java)
                startActivity(intent)
            },
            viewInactiveCharsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, player.getInactiveCharacters())
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewCharacterActivity::class)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "${player.fullName}'s Inactive Characters")
                val intent = Intent(this, CharactersListActivity::class.java)
                startActivity(intent)
            },
            viewPlannedCharsCallback = {
                if (DataManager.shared.playerIsCurrentPlayer(player)) {
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_PLAYER, player)
                    val intent = Intent(this, CharacterPlannerActivity::class.java)
                    startActivity(intent)
                } else {
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, player.getPlannedCharacters())
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewCharacterActivity::class)
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "${player.fullName}'s Planned Characters")
                    val intent = Intent(this, CharactersListActivity::class.java)
                    startActivity(intent)
                }
            }
        )

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, player.fullName)
        player.profileImage.ifLet {
            profileImage.setImageBitmap(it.image.toBitmap())
        }
        characterPanel.setValuesAndHideViews(player.getActiveCharacter(), player)
    }
}