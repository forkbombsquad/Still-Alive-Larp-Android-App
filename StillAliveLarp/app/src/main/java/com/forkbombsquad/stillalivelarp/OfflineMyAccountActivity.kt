package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.managers.SkillManager
import com.forkbombsquad.stillalivelarp.utils.*

class OfflineMyAccountActivity : NoStatusBarActivity() {

    private lateinit var playerNameText: TextView

    private lateinit var allSkillsNav: NavArrowButtonBlack
    private lateinit var skillTreeNav: NavArrowButtonBlack
    private lateinit var skillTreeDarkNav: NavArrowButtonBlack
    private lateinit var treatingWoundsNav: NavArrowButtonBlack

    private lateinit var playerStatsNav: NavArrowButtonBlack
    private lateinit var charStatsNav: NavArrowButtonBlack
    private lateinit var skillViewNav: NavArrowButtonBlack
    private lateinit var bioNav: NavArrowButtonBlack
    private lateinit var gearNav: NavArrowButtonBlack
    private lateinit var rulesNav: NavArrowButtonBlack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_my_account)
        setupView()
    }

    private fun setupView() {

        allSkillsNav = findViewById(R.id.myaccountoffline_viewAllSkillsNavArrow)
        skillTreeNav = findViewById(R.id.myaccountoffline_skillTreeArrow)
        skillTreeDarkNav = findViewById(R.id.myaccountoffline_skillTreeDarkArrow)
        treatingWoundsNav = findViewById(R.id.myaccountoffline_treatingWoundsArrow)

        playerNameText = findViewById(R.id.myaccountoffline_playerName)
        playerStatsNav = findViewById(R.id.myaccountoffline_playerStatsNavArrow)
        charStatsNav = findViewById(R.id.myaccountoffline_characterStatsNavArrow)
        skillViewNav = findViewById(R.id.myaccountoffline_viewSkillsNavArrow)
        bioNav = findViewById(R.id.myaccountoffline_bioNavArrow)
        gearNav = findViewById(R.id.myaccountoffline_gearNavArrow)
        rulesNav = findViewById(R.id.myaccountoffline_rulesNavArrow)

        allSkillsNav.setOnClick {
            allSkillsNav.setLoading(true)
            DataManager.shared.unrelaltedUpdateCallback = {
                allSkillsNav.setLoading(false)
            }
            val intent = Intent(this, OfflineViewAllSkillsActivity::class.java)
            startActivity(intent)
        }
        skillTreeNav.setOnClick {
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE.key)
            val intent = Intent(this, SAImageViewActivity::class.java)
            startActivity(intent)
        }
        skillTreeDarkNav.setOnClick {
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK.key)
            val intent = Intent(this, SAImageViewActivity::class.java)
            startActivity(intent)
        }
        treatingWoundsNav.setOnClick {
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS.key)
            val intent = Intent(this, SAImageViewActivity::class.java)
            startActivity(intent)
        }

        playerStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            DataManager.shared.selectedPlayer = SharedPrefsManager.shared.getPlayer()
            val intent = Intent(this, OfflinePlayerStatsActivity::class.java)
            startActivity(intent)
        }
        charStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            DataManager.shared.selectedPlayer = SharedPrefsManager.shared.getPlayer()
            DataManager.shared.charForSelectedPlayer = SharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineCharacterStatsActivity::class.java)
            startActivity(intent)
        }
        skillViewNav.setOnClick {
            // Set info in data manager so that things populate correctly
            DataManager.shared.selectedPlayer = SharedPrefsManager.shared.getPlayer()
            DataManager.shared.charForSelectedPlayer = SharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineViewSkillsActivity::class.java)
            startActivity(intent)
        }
        bioNav.setOnClick {
            // Set info in data manager so that things populate correctly
            DataManager.shared.selectedPlayer = SharedPrefsManager.shared.getPlayer()
            DataManager.shared.charForSelectedPlayer = SharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineViewBioActivity::class.java)
            startActivity(intent)
        }
        gearNav.setOnClick {
            // Set info in data manager so that things populate correctly
            DataManager.shared.selectedChar = SharedPrefsManager.shared.getCharacter()?.getBaseModel()
            val intent = Intent(this, OfflineViewGearActivity::class.java)
            startActivity(intent)
        }
        rulesNav.setOnClick {
            // Set info in data manager so that things populate correctly
            rulesNav.setLoading(true)
            DataManager.shared.unrelaltedUpdateCallback = {
                rulesNav.setLoading(false)
            }
            DataManager.shared.rulebook = RulebookManager.shared.getOfflineVersion()
            val intent = Intent(this, ViewRulesActivity::class.java)
            startActivity(intent)
        }
        
        buildView()
    }

    private fun buildView() {
        val player = SharedPrefsManager.shared.getPlayer()
        val character = SharedPrefsManager.shared.getCharacter()
        val rulebook = RulebookManager.shared.getOfflineVersion()
        player.ifLet({
            playerNameText.text = it.fullName.underline()
        }, {
            playerNameText.text = ""
        })

        playerStatsNav.isGone = player == null

        charStatsNav.isGone = character == null
        bioNav.isGone = character == null || !(character.approvedBio.toBoolean())
        gearNav.isGone = character == null
        rulesNav.isGone = rulebook == null

        allSkillsNav.isGone = SkillManager.shared.getSkillsOffline().isEmpty()

        skillTreeNav.isGone = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE.key) == null
        skillTreeDarkNav.isGone = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK.key) == null
        treatingWoundsNav.isGone = SharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS.key) == null

    }
}