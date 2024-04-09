package com.forkbombsquad.stillalivelarp.tabbar_fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.ViewBioActivity
import com.forkbombsquad.stillalivelarp.ViewRulesActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.PlayerStatsFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.rules.SkillListFragment
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class RulesFragment : Fragment() {

    private val TAG = "RULES_FRAGMENT"

    private lateinit var skillListNav: NavArrowButtonBlack
    private lateinit var skillTreeDiagramNav: NavArrowButtonBlack
    private lateinit var coreRulebookNav: NavArrowButtonBlack
    private lateinit var treatingWoundsNav: NavArrowButtonBlack

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
        skillListNav = v.findViewById(R.id.rules_skillListNav)
        skillTreeDiagramNav = v.findViewById(R.id.rules_skillTreeDiagramNav)
        coreRulebookNav = v.findViewById(R.id.rules_coreRulebookNav)
        treatingWoundsNav = v.findViewById(R.id.rules_treatingWoundsFlowchartNav)

        skillListNav.setOnClick {
            val frag = SkillListFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        skillTreeDiagramNav.setOnClick {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stillalivelarp.com/skilltree"))
            startActivity(browserIntent)
        }

        coreRulebookNav.setOnClick {
            coreRulebookNav.setLoading(true)
            DataManager.shared.unrelaltedUpdateCallback = {
                coreRulebookNav.setLoading(false)
            }
            val intent = Intent(v.context, ViewRulesActivity::class.java)
            startActivity(intent)
        }

        treatingWoundsNav.setOnClick {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://stillalivelarp.com/healing"))
            startActivity(browserIntent)
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.RULEBOOK), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        skillListNav.setLoading(DataManager.shared.loadingSkills)
        skillListNav.isGone = !DataManager.shared.loadingSkills && DataManager.shared.skills == null
        coreRulebookNav.setLoading(DataManager.shared.loadingRulebook)
        coreRulebookNav.isGone = !DataManager.shared.loadingRulebook && DataManager.shared.rulebook == null
    }

    companion object {
        fun newInstance() =
            RulesFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}