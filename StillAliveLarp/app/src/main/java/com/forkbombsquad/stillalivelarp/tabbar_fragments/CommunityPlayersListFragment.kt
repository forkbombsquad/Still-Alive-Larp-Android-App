package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.tabbar_fragments.community.ViewPlayerStuffFragment
import com.forkbombsquad.stillalivelarp.utils.*

class CommunityPlayersListFragment : Fragment() {
    private val TAG = "COMMUNITY_PLAYERS_LIST_FRAGMENT"
    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout

    private lateinit var pullToRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_community_player_list, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        progressBar = v.findViewById(R.id.community_loadingBar)
        layout = v.findViewById(R.id.community_playerlistlayout)

        pullToRefresh = v.findViewById(R.id.pulltorefresh_community)
        pullToRefresh.setOnRefreshListener {
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLAYERS), true) {
                buildView(v)
                pullToRefresh.isRefreshing = false
            }
            buildView(v)
        }

        buildView(v)
        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLAYERS), false) {
            buildView(v)
        }
    }

    private fun buildView(v: View) {
        layout.removeAllViews()
        progressBar.isGone = !DataManager.shared.loadingAllPlayers
        layout.isGone = DataManager.shared.loadingAllPlayers

        DataManager.shared.allPlayers.ifLet { players ->
            players.alphabetized().forEachIndexed { index, player ->
                val arrow = NavArrowButtonBlackBuildable(v.context)
                arrow.textView.text = "${player.fullName}" + (player.isAdmin.uppercase() == "TRUE").ternary(" (Staff)", "")
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick {
                    DataManager.shared.selectedPlayer = player
                    val frag = ViewPlayerStuffFragment.newInstance()
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.add(R.id.container, frag)
                    transaction.addToBackStack(TAG).commit()
                }
                layout.addView(arrow)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CommunityPlayersListFragment()
    }
}