package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.NativeSkillTreeActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.SAImageViewActivity
import com.forkbombsquad.stillalivelarp.ViewRulesActivity
import com.forkbombsquad.stillalivelarp.ViewSkillsActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class RulesFragment : Fragment() {

    // TODO reorg project to be in folders and stuff

    private val TAG = "RULES_FRAGMENT"

    private lateinit var loadingLayout: LoadingLayout
    private lateinit var rulesTitle: TextView
    private lateinit var skillListNav: NavArrowButtonBlack
    private lateinit var coreRulebookNav: NavArrowButtonBlack
    private lateinit var treatingWoundsNav: NavArrowButtonBlack
    private lateinit var nativeSkillTreeDiagramNav: NavArrowButtonBlack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_rules, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        loadingLayout = v.findViewById(R.id.loadinglayout)
        rulesTitle = v.findViewById(R.id.rules_rulesTitle)
        skillListNav = v.findViewById(R.id.rules_skillListNav)
        coreRulebookNav = v.findViewById(R.id.rules_coreRulebookNav)
        treatingWoundsNav = v.findViewById(R.id.rules_treatingWoundsFlowchartNav)
        nativeSkillTreeDiagramNav = v.findViewById(R.id.rules_skillTreeDiagramNativeNav)

        skillListNav.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SKILL_LIST, DataManager.shared.getSkillsAsFCMSM())
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.VIEW_TITLE, "All Skills")
            val intent = Intent(v.context, ViewSkillsActivity::class.java)
            startActivity(intent)
        }

        coreRulebookNav.setOnClick {
            val intent = Intent(v.context, ViewRulesActivity::class.java)
            startActivity(intent)
        }

        treatingWoundsNav.setOnClick {
            val intent = Intent(v.context, SAImageViewActivity::class.java)
            startActivity(intent)
        }

        nativeSkillTreeDiagramNav.setOnClick {
            val intent = Intent(v.context, NativeSkillTreeActivity::class.java)
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
        DataManager.shared.setTitleTextPotentiallyOffline(rulesTitle, "Rules and Reference")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf()) {}
        skillListNav.setLoading(DataManager.shared.loading)
        coreRulebookNav.setLoading(DataManager.shared.loading)
        treatingWoundsNav.setLoading(DataManager.shared.loading)
        nativeSkillTreeDiagramNav.setLoading(DataManager.shared.loading)
    }

    companion object {
        fun newInstance() =
            RulesFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}