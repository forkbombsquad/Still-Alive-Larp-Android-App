package com.forkbombsquad.stillalivelarp.tabbar_fragments

import com.forkbombsquad.stillalivelarp.ViewResearchProjectsActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.NPCListActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.Constants

import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.toBitmap
import com.forkbombsquad.stillalivelarp.utils.underline

class CommunityFragment : Fragment() {
    private val TAG = "COMMUNITY_FRAGMENT"

    private lateinit var allPlayersButton: NavArrowButtonBlack
    private lateinit var campStatusButton: NavArrowButtonBlack
    private lateinit var allNPCsButton: NavArrowButtonBlack
    private lateinit var researchProjects: NavArrowButtonBlack

    private lateinit var contentLayout: LinearLayout
    private lateinit var loadingLayout: LinearLayout
    private lateinit var loadingText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_community, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        contentLayout = v.findViewById(R.id.contentlayout)
        loadingLayout = v.findViewById(R.id.loadingView)
        loadingText = v.findViewById(R.id.loadingText)

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
            val intent = Intent(v.context, ViewResearchProjectsActivity::class.java)
            startActivity(intent)
        }

        allNPCsButton.setOnClick {
            val intent = Intent(v.context, NPCListActivity::class.java)
            startActivity(intent)
        }

        reload()
    }

    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        if (DataManager.shared.loading) {
            contentLayout.isGone = true
            loadingLayout.isGone = false
            loadingText.text = DataManager.shared.loadingText

        } else {
            contentLayout.isGone = false
            loadingLayout.isGone = true

            campStatusButton.isGone = !FeatureFlag.CAMP_STATUS.isActive()
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