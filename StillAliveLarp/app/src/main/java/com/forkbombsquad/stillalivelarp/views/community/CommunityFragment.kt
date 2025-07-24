package com.forkbombsquad.stillalivelarp.views.community

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.views.shared.NPCListActivity
import com.forkbombsquad.stillalivelarp.views.shared.PlayersListActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.views.shared.ViewNPCStuffActivity
import com.forkbombsquad.stillalivelarp.views.shared.ViewPlayerActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType

import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class CommunityFragment : Fragment() {
    private val TAG = "COMMUNITY_FRAGMENT"

    private lateinit var communityTitle: TextView

    private lateinit var pullToRefresh: SwipeRefreshLayout

    private lateinit var allPlayersButton: NavArrowButtonBlack
    private lateinit var campStatusButton: NavArrowButtonBlack
    private lateinit var allNPCsButton: NavArrowButtonBlack
    private lateinit var researchProjects: NavArrowButtonBlack

    private lateinit var contentLayout: LinearLayout

    private lateinit var loadingLayout: LoadingLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_community, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        pullToRefresh = v.findViewById(R.id.pulltorefresh_community)

        contentLayout = v.findViewById(R.id.contentlayout)
        loadingLayout = v.findViewById(R.id.loadinglayout)

        communityTitle = v.findViewById(R.id.community_title)

        allPlayersButton = v.findViewById(R.id.community_allPlayersButton)
        campStatusButton = v.findViewById(R.id.community_campStatusButton)
        allNPCsButton = v.findViewById(R.id.community_npcsButton)
        researchProjects = v.findViewById(R.id.community_researchProjects)

        pullToRefresh.setOnRefreshListener {
            reload()
        }

        allPlayersButton.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.PLAYER_LIST, DataManager.shared.players)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewPlayerActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "All Players")
            val intent = Intent(v.context, PlayersListActivity::class.java)
            startActivity(intent)
        }

        campStatusButton.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CAMP_STATUS, DataManager.shared.campStatus!!)
            val intent = Intent(v.context, ViewCampStatusActivity::class.java)
            startActivity(intent)
        }

        researchProjects.setOnClick {
            val intent = Intent(v.context, ViewResearchProjectsActivity::class.java)
            startActivity(intent)
        }

        allNPCsButton.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.CHARACTER_LIST, DataManager.shared.getAllCharacters(CharacterType.NPC))
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.DESTINATION_CLASS, ViewNPCStuffActivity::class)
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "All NPCs")
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
            pullToRefresh.isRefreshing = false
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(communityTitle, "Community")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(contentLayout)) {}
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