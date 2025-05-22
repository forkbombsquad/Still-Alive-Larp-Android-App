package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldSharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.managers.SkillManager
import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.ImageDownloader
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.RulebookManager

class OfflineMyAccountActivity : NoStatusBarActivity() {

    private lateinit var allSkillsNav: NavArrowButtonBlack
    private lateinit var skillTreeNav: NavArrowButtonBlack
    private lateinit var nativeSkillTreeNav: NavArrowButtonBlack
    private lateinit var personalSkillTreeNav: NavArrowButtonBlack
    private lateinit var allNPCsNav: NavArrowButtonBlack
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

        nativeSkillTreeNav = findViewById(R.id.myaccountoffline_nativeSkillTree)
        personalSkillTreeNav = findViewById(R.id.myaccountoffline_personalSkillTree)
        allNPCsNav = findViewById(R.id.myaccountoffline_npcs)

        playerStatsNav = findViewById(R.id.myaccountoffline_playerStatsNavArrow)
        charStatsNav = findViewById(R.id.myaccountoffline_characterStatsNavArrow)
        skillViewNav = findViewById(R.id.myaccountoffline_viewSkillsNavArrow)
        bioNav = findViewById(R.id.myaccountoffline_bioNavArrow)
        gearNav = findViewById(R.id.myaccountoffline_gearNavArrow)
        rulesNav = findViewById(R.id.myaccountoffline_rulesNavArrow)

        allSkillsNav.setOnClick {
            allSkillsNav.setLoading(true)
            OldDataManager.shared.unrelaltedUpdateCallback = {
                allSkillsNav.setLoading(false)
            }
            val intent = Intent(this, OfflineViewAllSkillsActivity::class.java)
            startActivity(intent)
        }
        personalSkillTreeNav.setOnClick {
            personalSkillTreeNav.setLoading(true)
            val intent = Intent(this, OfflinePersonalNativeSkillTreeActivity::class.java)
            startActivity(intent)
            personalSkillTreeNav.setLoading(false)
        }
        nativeSkillTreeNav.setOnClick {
            nativeSkillTreeNav.setLoading(true)
            val intent = Intent(this, OfflineNativeSkillTreeActivity::class.java)
            startActivity(intent)
            nativeSkillTreeNav.setLoading(false)
        }
        allNPCsNav.setOnClick {
            allNPCsNav.setLoading(true)
            OldDataManager.shared.allOfflineNPCCharacters = OldSharedPrefsManager.shared.getNPCs()
            val intent = Intent(this, OfflineNPCListActivity::class.java)
            startActivity(intent)
            allNPCsNav.setLoading(false)
        }
        if (FeatureFlag.OLD_SKILL_TREE_IMAGE.isActive()) {
            skillTreeNav.setOnClick {
                OldDataManager.shared.passedBitmap = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE.key)
                val intent = Intent(this, SAImageViewActivity::class.java)
                startActivity(intent)
            }
            skillTreeDarkNav.setOnClick {
                OldDataManager.shared.passedBitmap = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK.key)
                val intent = Intent(this, SAImageViewActivity::class.java)
                startActivity(intent)
            }
        }
        treatingWoundsNav.setOnClick {
            OldDataManager.shared.passedBitmap = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS.key)
            val intent = Intent(this, SAImageViewActivity::class.java)
            startActivity(intent)
        }

        playerStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldSharedPrefsManager.shared.getPlayer()
            val intent = Intent(this, OfflinePlayerStatsActivity::class.java)
            startActivity(intent)
        }
        charStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldSharedPrefsManager.shared.getPlayer()
            OldDataManager.shared.charForSelectedPlayer = OldSharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineCharacterStatsActivity::class.java)
            startActivity(intent)
        }
        skillViewNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldSharedPrefsManager.shared.getPlayer()
            OldDataManager.shared.charForSelectedPlayer = OldSharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineViewSkillsActivity::class.java)
            startActivity(intent)
        }
        bioNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldSharedPrefsManager.shared.getPlayer()
            OldDataManager.shared.charForSelectedPlayer = OldSharedPrefsManager.shared.getCharacter()
            val intent = Intent(this, OfflineViewBioActivity::class.java)
            startActivity(intent)
        }
        gearNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedChar = OldSharedPrefsManager.shared.getCharacter()?.getBaseModel()
            OldDataManager.shared.selectedCharacterGear = OldSharedPrefsManager.shared.getGear()
            val intent = Intent(this, OfflineViewGearActivity::class.java)
            startActivity(intent)
        }
        rulesNav.setOnClick {
            // Set info in data manager so that things populate correctly
            rulesNav.setLoading(true)
            OldDataManager.shared.unrelaltedUpdateCallback = {
                rulesNav.setLoading(false)
            }
            OldDataManager.shared.rulebook = RulebookManager.shared.getOfflineVersion()
            val intent = Intent(this, ViewRulesActivity::class.java)
            startActivity(intent)
        }
        
        buildView()
    }

    private fun buildView() {
        val player = OldSharedPrefsManager.shared.getPlayer()
        val character = OldSharedPrefsManager.shared.getCharacter()
        val rulebook = RulebookManager.shared.getOfflineVersion()
        val skills = SkillManager.shared.getSkillsOffline()
        val skillCategories = OldSharedPrefsManager.shared.getSkillCategories()

        playerStatsNav.isGone = player == null

        charStatsNav.isGone = character == null
        skillViewNav.isGone = character == null
        bioNav.isGone = character == null || !(character.approvedBio.toBoolean())
        gearNav.isGone = character == null
        rulesNav.isGone = rulebook == null

        allSkillsNav.isGone = skills.isEmpty()

        allNPCsNav.isGone = OldSharedPrefsManager.shared.getNPCs().isEmpty()
        nativeSkillTreeNav.isGone = skills.isEmpty() || skillCategories.isEmpty()
        personalSkillTreeNav.isGone = skills.isEmpty() || skillCategories.isEmpty() || character == null


        if (FeatureFlag.OLD_SKILL_TREE_IMAGE.isActive()) {
            skillTreeNav.isGone = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE.key) == null
            skillTreeDarkNav.isGone = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK.key) == null
        } else {
            skillTreeNav.isGone = true
            skillTreeDarkNav.isGone = true
        }

        treatingWoundsNav.isGone = OldSharedPrefsManager.shared.getBitmap(this, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS.key) == null

    }
}