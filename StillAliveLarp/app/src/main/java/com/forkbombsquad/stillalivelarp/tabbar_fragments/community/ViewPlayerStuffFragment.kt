package com.forkbombsquad.stillalivelarp.tabbar_fragments.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.ViewBioActivity
import com.forkbombsquad.stillalivelarp.ViewGearActivity

import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.CharacterStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.PlayerStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.SkillManagementFragment
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap

class ViewPlayerStuffFragment : Fragment() {
    // TODO convert to Fragment
    private val TAG = "VIEW_PLAYER_STUFF_FRAGMENT"

    private lateinit var profileImage: ImageView
    private lateinit var profileImageProgressBar: ProgressBar
    private lateinit var title: TextView
    private lateinit var playerStats: NavArrowButtonBlack
    private lateinit var charStats: NavArrowButtonBlack
    private lateinit var skills: NavArrowButtonBlack
    private lateinit var bio: NavArrowButtonBlack
    private lateinit var gear: NavArrowButtonBlack
    private lateinit var nativeSkillTree: NavArrowButtonBlack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_view_player_stuff, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        profileImage = v.findViewById(R.id.playerstuff_profileImage)
        profileImageProgressBar = v.findViewById(R.id.playerstuff_profileImageProgressBar)

        title = v.findViewById(R.id.playerstuff_title)

        playerStats = v.findViewById(R.id.playerstuff_playerStatsNavArrow)
        charStats = v.findViewById(R.id.playerstuff_characterStatsNavArrow)
        skills = v.findViewById(R.id.playerstuff_skills)
        bio = v.findViewById(R.id.playerstuff_bioNavArrow)
        gear = v.findViewById(R.id.playerstuff_gearNavArrow)
        nativeSkillTree = v.findViewById(R.id.playerstuff_skilltreediagram)

        playerStats.setOnClick {
            val frag = PlayerStatsFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        charStats.setOnClick {
            val frag = CharacterStatsFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        skills.setOnClick {
            val frag = SkillManagementFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        bio.setOnClick {
            val intent = Intent(v.context, ViewBioActivity::class.java)
            startActivity(intent)
        }
        gear.setOnClick {
            OldDataManager.shared.selectedChar = OldDataManager.shared.charForSelectedPlayer?.getBaseModel()
            val intent = Intent(v.context, ViewGearActivity::class.java)
            startActivity(intent)
        }
        nativeSkillTree.setOnClick {
            val intent = Intent(v.context, OtherCharacterNativeSkillTreeActivity::class.java)
            startActivity(intent)
        }

        OldDataManager.shared.loadingProfileImage = true

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.CHAR_FOR_SELECTED_PLAYER, OldDataManagerType.SKILLS, OldDataManagerType.SKILL_CATEGORIES), false) {
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PROFILE_IMAGE), false) {
                buildView()
            }
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        val opPlayer = OldDataManager.shared.selectedPlayer

        nativeSkillTree.setLoading(OldDataManager.shared.loadingSkills || OldDataManager.shared.loadingSkillCategories)

        profileImageProgressBar.isGone = !OldDataManager.shared.loadingProfileImage
        OldDataManager.shared.profileImage.ifLet {
            if (it.playerId == OldDataManager.shared.selectedPlayer?.id) {
                profileImage.setImageBitmap(it.image.toBitmap())
            }
        }

        opPlayer.ifLet({ player ->
            title.text = player.fullName
            playerStats.isGone = false

            val charLoading = OldDataManager.shared.loadingCharForSelectedPlayer
            charStats.setLoading(charLoading)
            skills.setLoading(charLoading)
            bio.setLoading(charLoading)
            gear.setLoading(charLoading)

            OldDataManager.shared.charForSelectedPlayer.ifLet({ character ->
                charStats.isGone = false
                skills.isGone = false
                bio.isGone = !character.approvedBio.toBoolean()
                gear.isGone = false
            }, {
                charStats.isGone = !charLoading
                skills.isGone = !charLoading
                bio.isGone = !charLoading
                gear.isGone = !charLoading
            })

        }, {
            title.text = "Error"
            playerStats.isGone = true
            charStats.isGone = true
            skills.isGone = true
            bio.isGone = true
            gear.isGone = true
        })
    }

    

    companion object {
        @JvmStatic
        fun newInstance() = ViewPlayerStuffFragment()
    }
}