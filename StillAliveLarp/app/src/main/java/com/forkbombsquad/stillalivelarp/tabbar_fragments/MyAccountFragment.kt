package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.AdminPanelActivity
import com.forkbombsquad.stillalivelarp.CharacterPlannerActivity
import com.forkbombsquad.stillalivelarp.CharactersListActivity
import com.forkbombsquad.stillalivelarp.EditProfileImageActivity
import com.forkbombsquad.stillalivelarp.ManageAccountActivity
import com.forkbombsquad.stillalivelarp.PersonalNativeSkillTreeActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.ViewAwardsActivity
import com.forkbombsquad.stillalivelarp.ViewBioActivity
import com.forkbombsquad.stillalivelarp.ViewCharacterActivity
import com.forkbombsquad.stillalivelarp.ViewCharacterStatsActivity
import com.forkbombsquad.stillalivelarp.ViewGearActivity
import com.forkbombsquad.stillalivelarp.ViewPlayerStatsActivity
import com.forkbombsquad.stillalivelarp.ViewSkillsActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.utils.CharacterPanel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
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

    private lateinit var accountTitle: TextView

    private lateinit var profileImage: ImageView
    private lateinit var playerNameText: TextView
    private lateinit var playerStatsNav: NavArrowButtonBlack
    private lateinit var playerAwardsNav: NavArrowButtonBlack
    private lateinit var characterPanel: CharacterPanel

    private lateinit var manageAccountNav: NavArrowButtonBlack
    private lateinit var adminToolsNav: NavArrowButtonRed
    private lateinit var debugButton: NavArrowButtonRed
    private lateinit var signOutButton: LoadingButton

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var contentLayout: LinearLayout

    private lateinit var loadingLayout: LoadingLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_my_account, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        contentLayout = v.findViewById(R.id.contentlayout)
        loadingLayout = v.findViewById(R.id.loadinglayout)

        accountTitle = v.findViewById(R.id.account_title)
        playerAwardsNav = v.findViewById(R.id.myaccount_viewAwards)

        profileImage = v.findViewById(R.id.myAccountProfileImage)
        playerNameText = v.findViewById(R.id.myAccountPlayerName)
        playerStatsNav = v.findViewById(R.id.myaccount_playerStatsNavArrow)
        characterPanel = v.findViewById(R.id.myaccount_charPanel)

        manageAccountNav = v.findViewById(R.id.myaccount_manageAccountNavArrow)
        adminToolsNav = v.findViewById(R.id.myaccount_adminToolsNavArrow)
        signOutButton = v.findViewById(R.id.myaccount_signOutButton)
        debugButton = v.findViewById(R.id.myaccount_debugButton)

        profileImage.setOnClickListener {
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
            }
            val intent = Intent(v.context, EditProfileImageActivity::class.java)
            startActivity(intent)
        }
        playerStatsNav.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_PLAYER, DataManager.shared.getCurrentPlayer()!!)
            val intent = Intent(v.context, ViewPlayerStatsActivity::class.java)
            startActivity(intent)
        }
        playerAwardsNav.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, DataManager.shared.getCurrentPlayer()!!.awards)
            val intent = Intent(v.context, ViewAwardsActivity::class.java)
            startActivity(intent)

        }
        characterPanel.setOnClicks(
            viewStatsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, DataManager.shared.getActiveCharacter()!!)
                val intent = Intent(v.context, ViewCharacterStatsActivity::class.java)
                startActivity(intent)
            },
            viewSkillsTreeCallback = {
                DataManager.shared.setUpdateCallback(this::class) {
                    reload()
                }
                val intent = Intent(v.context, PersonalNativeSkillTreeActivity::class.java)
                startActivity(intent)
            },
            viewSkillsListCallback = {
                DataManager.shared.setUpdateCallback(this::class) {
                    reload()
                }
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, DataManager.shared.getActiveCharacter()!!)
                val intent = Intent(v.context, ViewSkillsActivity::class.java)
                startActivity(intent)
            },
            viewBioCallback = {
                DataManager.shared.setUpdateCallback(this::class) {
                    reload()
                }
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, DataManager.shared.getActiveCharacter()!!)
                val intent = Intent(v.context, ViewBioActivity::class.java)
                startActivity(intent)
            },
            viewGearCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, DataManager.shared.getActiveCharacter()!!)
                val intent = Intent(v.context, ViewGearActivity::class.java)
                startActivity(intent)
            },
            viewAwardsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.AWARDS_LIST, DataManager.shared.getActiveCharacter()!!.awards)
                val intent = Intent(v.context, ViewAwardsActivity::class.java)
                startActivity(intent)
            },
            viewInactiveCharsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getCurrentPlayer()!!.getInactiveCharacters())
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewCharacterActivity::class)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "${DataManager.shared.getCurrentPlayer()!!.fullName}'s Inactive Characters")
                val intent = Intent(v.context, CharactersListActivity::class.java)
                startActivity(intent)
            },
            viewPlannedCharsCallback = {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_PLAYER, DataManager.shared.getCurrentPlayer()!!)
                val intent = Intent(v.context, CharacterPlannerActivity::class.java)
                startActivity(intent)
            }
        )
        manageAccountNav.setOnClick {
            val intent = Intent(v.context, ManageAccountActivity::class.java)
            startActivity(intent)
        }
        adminToolsNav.setOnClick {
            val intent = Intent(v.context, AdminPanelActivity::class.java)
            startActivity(intent)
        }
        signOutButton.setOnClick {
            DataManager.forceReset()
            activity?.finish()
        }

        pullToRefresh = v.findViewById(R.id.pulltorefresh_account)
        pullToRefresh.setOnRefreshListener {
            reload()
        }

        debugButton.setOnClick {
            doDebugStuff()
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
            pullToRefresh.isRefreshing = false
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(accountTitle, "My Account")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(contentLayout)) {
            val player = DataManager.shared.getCurrentPlayer()!!
            characterPanel.setValuesAndHideViews(DataManager.shared.getActiveCharacter(), player)
            playerNameText.text = player.fullName.underline()
            adminToolsNav.isGone = !player.isAdmin

            player.profileImage.ifLet {
                profileImage.setImageBitmap(it.image.toBitmap())
            }

            debugButton.isGone = !Constants.Logging.showDebugButtonInAccountView
        }
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