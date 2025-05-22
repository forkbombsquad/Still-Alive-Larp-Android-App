package com.forkbombsquad.stillalivelarp.tabbar_fragments

import com.forkbombsquad.stillalivelarp.ViewResearchProjectsActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.NPCListActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class CommunityFragment : Fragment() {

    private val TAG = "COMMUNITY_FRAGMENT"

    private lateinit var allPlayersButton: NavArrowButtonBlack
    private lateinit var campStatusButton: NavArrowButtonBlack
    private lateinit var allNPCsButton: NavArrowButtonBlack
    private lateinit var researchProjects: NavArrowButtonBlack

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
        researchProjects = v.findViewById(R.id.community_researchProjects)

        allPlayersButton.setOnClick {
            val frag = CommunityPlayersListFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        campStatusButton.setOnClick {
            // TODO FUTURE - add this. It will display the Camp Fortification Rings
        }

        researchProjects.setOnClick {
            researchProjects.setLoading(true)
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.RESEARCH_PROJECTS), true) {
                researchProjects.setLoading(false)
                val intent = Intent(v.context, ViewResearchProjectsActivity::class.java)
                startActivity(intent)
            }
        }

        campStatusButton.isGone = !FeatureFlag.CAMP_STATUS.isActive()

        allNPCsButton.setOnClick {
            allNPCsButton.setLoading(true)
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.ALL_NPC_CHARACTERS)) {
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