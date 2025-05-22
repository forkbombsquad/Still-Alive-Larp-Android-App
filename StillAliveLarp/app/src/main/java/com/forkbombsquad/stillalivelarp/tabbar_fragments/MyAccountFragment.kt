package com.forkbombsquad.stillalivelarp.tabbar_fragments

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.CharacterPlannerActivity
import com.forkbombsquad.stillalivelarp.EditProfileImageActivity
import com.forkbombsquad.stillalivelarp.ManageAccountActivity
import com.forkbombsquad.stillalivelarp.PersonalSkillTreeActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.SpecialClassXpReductionsActivity
import com.forkbombsquad.stillalivelarp.ViewBioActivity
import com.forkbombsquad.stillalivelarp.ViewGearActivity
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.CharacterStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.PlayerStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.SkillManagementFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.admin.AdminPanelFragment
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlue
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap
import com.forkbombsquad.stillalivelarp.utils.underline

/**
 * A simple [Fragment] subclass.
 * Use the [MyAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyAccountFragment : Fragment() {

    private val TAG = "MY_ACCOUNT_FRAGMENT"

    private lateinit var profileImage: ImageView
    private lateinit var profileImageProgressBar: ProgressBar
    private lateinit var playerNameText: TextView
    private lateinit var playerStatsNav: NavArrowButtonBlack
    private lateinit var charStatsNav: NavArrowButtonBlack
    private lateinit var skillManagementNav: NavArrowButtonBlack
    private lateinit var specialClassXpRedNav: NavArrowButtonBlack
    private lateinit var bioNav: NavArrowButtonBlack
    private lateinit var gearNav: NavArrowButtonBlack
    private lateinit var characterPlannerNav: NavArrowButtonBlue
    private lateinit var manageAccountNav: NavArrowButtonBlack
    private lateinit var adminToolsNav: NavArrowButtonRed
    private lateinit var debugButton: NavArrowButtonRed
    private lateinit var signOutButton: LoadingButton

    private lateinit var personalSkillTree: NavArrowButtonBlack

    private lateinit var pullToRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_my_account, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        profileImage = v.findViewById(R.id.myAccountProfileImage)
        profileImageProgressBar = v.findViewById(R.id.myAccountProfileImageLoadingBar)
        playerNameText = v.findViewById(R.id.myAccountPlayerName)
        playerStatsNav = v.findViewById(R.id.myaccount_playerStatsNavArrow)
        charStatsNav = v.findViewById(R.id.myaccount_characterStatsNavArrow)
        skillManagementNav = v.findViewById(R.id.myaccount_skillManagementNavArrow)
        specialClassXpRedNav = v.findViewById(R.id.myaccount_specialClassXpReductionsNavArrow)
        bioNav = v.findViewById(R.id.myaccount_bioNavArrow)
        gearNav = v.findViewById(R.id.myaccount_gearNavArrow)
        characterPlannerNav = v.findViewById(R.id.myaccount_characterPlannerNavArrow)
        manageAccountNav = v.findViewById(R.id.myaccount_manageAccountNavArrow)
        adminToolsNav = v.findViewById(R.id.myaccount_adminToolsNavArrow)
        signOutButton = v.findViewById(R.id.myaccount_signOutButton)
        personalSkillTree = v.findViewById(R.id.myaccount_personalSkillTree)
        debugButton = v.findViewById(R.id.myaccount_debugButton)

        profileImage.setOnClickListener {
            OldDataManager.shared.unrelaltedUpdateCallback = {
                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PROFILE_IMAGE), true) {
                    buildView()
                }
                buildView()
            }
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            val intent = Intent(v.context, EditProfileImageActivity::class.java)
            startActivity(intent)
        }
        playerStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            val frag = PlayerStatsFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }
        charStatsNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.character
            val frag = CharacterStatsFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }
        skillManagementNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.character
            val frag = SkillManagementFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }
        personalSkillTree.setOnClick {
            personalSkillTree.setLoading(true)
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.XP_REDUCTIONS), false) {
                OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
                OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.character
                OldDataManager.shared.unrelaltedUpdateCallback = {
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER), true, {}, {})
                    buildView()
                }
                val intent = Intent(v.context, PersonalSkillTreeActivity::class.java)
                startActivity(intent)
                personalSkillTree.setLoading(false)
            }

        }
        specialClassXpRedNav.setOnClick {
            val intent = Intent(v.context, SpecialClassXpReductionsActivity::class.java)
            startActivity(intent)
        }
        bioNav.setOnClick {
            // Set info in data manager so that things populate correctly
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            OldDataManager.shared.charForSelectedPlayer = OldDataManager.shared.character
            val intent = Intent(v.context, ViewBioActivity::class.java)
            startActivity(intent)
        }
        gearNav.setOnClick {
            // set info in datamanager so things populate
            OldDataManager.shared.selectedChar = OldDataManager.shared.character?.getBaseModel()
            val intent = Intent(v.context, ViewGearActivity::class.java)
            startActivity(intent)
        }
        characterPlannerNav.setOnClick {
            val intent = Intent(v.context, CharacterPlannerActivity::class.java)
            startActivity(intent)
        }
        manageAccountNav.setOnClick {
            val intent = Intent(v.context, ManageAccountActivity::class.java)
            startActivity(intent)
        }
        adminToolsNav.setOnClick {
            val frag = AdminPanelFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }
        signOutButton.setOnClick {
            OldDataManager.forceReset()
            activity?.finish()
        }

        pullToRefresh = v.findViewById(R.id.pulltorefresh_account)
        pullToRefresh.setOnRefreshListener {
            OldDataManager.shared.loadingProfileImage = true
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.SKILL_CATEGORIES), true) {
                OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
                OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PROFILE_IMAGE), true) {
                    buildView()
                    pullToRefresh.isRefreshing = false
                }
                if (OldDataManager.shared.character != null) {
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.XP_REDUCTIONS), true) {
                        buildView()
                    }
                }
                buildView()
            }
            buildView()
        }

        debugButton.setOnClick {
            doDebugStuff()
        }

        OldDataManager.shared.loadingProfileImage = true
        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.SKILL_CATEGORIES), false) {
            OldDataManager.shared.selectedPlayer = OldDataManager.shared.player
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PROFILE_IMAGE), false) {
                buildView()
            }
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.player.ifLet({
            playerNameText.text = it.fullName.underline()
            adminToolsNav.isGone = !it.isAdmin.toBoolean()
        }, {
            playerNameText.text = ""
            adminToolsNav.isGone = true
        })

        profileImageProgressBar.isGone = !OldDataManager.shared.loadingProfileImage

        OldDataManager.shared.profileImage.ifLet {
            if (it.playerId == OldDataManager.shared.selectedPlayer?.id) {
                profileImage.setImageBitmap(it.image.toBitmap())
            }
        }

        playerStatsNav.isGone = !OldDataManager.shared.loadingPlayer && OldDataManager.shared.player == null
        manageAccountNav.isGone = !OldDataManager.shared.loadingPlayer && OldDataManager.shared.player == null

        playerStatsNav.setLoading(OldDataManager.shared.loadingPlayer)
        manageAccountNav.setLoading(OldDataManager.shared.loadingPlayer)
        personalSkillTree.setLoading(OldDataManager.shared.loadingCharacter)

        charStatsNav.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null
        skillManagementNav.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null
        specialClassXpRedNav.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null
        bioNav.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null
        gearNav.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null
        personalSkillTree.isGone = !OldDataManager.shared.loadingCharacter && OldDataManager.shared.character == null

        charStatsNav.setLoading(OldDataManager.shared.loadingCharacter)
        skillManagementNav.setLoading(OldDataManager.shared.loadingCharacter)
        specialClassXpRedNav.setLoading(OldDataManager.shared.loadingCharacter)
        bioNav.setLoading(OldDataManager.shared.loadingCharacter)
        gearNav.setLoading(OldDataManager.shared.loadingCharacter)

        characterPlannerNav.setLoading(OldDataManager.shared.loadingPlayer || OldDataManager.shared.loadingCharacter)

        debugButton.isGone = !Constants.Logging.showDebugButtonInAccountView
    }

    private fun doDebugStuff() {
        // Any debug stuff you need to do can be done here
        // TODO ALWAYS - remove all code here before launch
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            MyAccountFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}