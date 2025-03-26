package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.*
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.CharacterStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.PlayerStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.SkillManagementFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.admin.AdminPanelFragment
import com.forkbombsquad.stillalivelarp.utils.*

class CommunityFragment : Fragment() {

    private val TAG = "COMMUNITY_FRAGMENT"

    private lateinit var allPlayersButton: NavArrowButtonBlack
    private lateinit var campStatusButton: NavArrowButtonBlack
    private lateinit var allNPCsButton: NavArrowButtonBlack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_community, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        allPlayersButton = v.findViewById(R.id.community_allPlayersButton)
        campStatusButton = v.findViewById(R.id.community_campStatusButton)
        allNPCsButton = v.findViewById(R.id.community_npcsButton)

        allPlayersButton.setOnClick {
            val frag = CommunityPlayersListFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        campStatusButton.setOnClick {
            // TODO add this in the future
        }

        campStatusButton.isGone = !FeatureFlag.CAMP_STATUS.isActive()

        allNPCsButton.setOnClick {
            allNPCsButton.setLoading(true)
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_NPC_CHARACTERS)) {
                allNPCsButton.setLoading(false)
                val intent = Intent(v.context, NPCListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            CommunityFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}