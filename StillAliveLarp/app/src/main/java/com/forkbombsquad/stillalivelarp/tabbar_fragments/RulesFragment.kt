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
import com.forkbombsquad.stillalivelarp.NativeSkillTreeActivity
import com.forkbombsquad.stillalivelarp.SAImageViewActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.ViewRulesActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.tabbar_fragments.rules.SkillListFragment
import com.forkbombsquad.stillalivelarp.utils.FeatureFlag
import com.forkbombsquad.stillalivelarp.utils.ImageDownloader
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class RulesFragment : Fragment() {

    private val TAG = "RULES_FRAGMENT"

    private lateinit var skillListNav: NavArrowButtonBlack
    private lateinit var skillTreeDiagramNav: NavArrowButtonBlack
    private lateinit var skillTreeDiagramDarkNav: NavArrowButtonBlack
    private lateinit var coreRulebookNav: NavArrowButtonBlack
    private lateinit var treatingWoundsNav: NavArrowButtonBlack
    private lateinit var nativeSkillTreeDiagramNav: NavArrowButtonBlack

    private var loadingSkillTree = true
    private var loadingSkillTreeDark = true
    private var loadingTreatingWounds = true

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
        loadingSkillTree = true
        loadingSkillTreeDark = true
        loadingTreatingWounds = true

        skillListNav = v.findViewById(R.id.rules_skillListNav)
        skillTreeDiagramNav = v.findViewById(R.id.rules_skillTreeDiagramNav)
        skillTreeDiagramDarkNav = v.findViewById(R.id.rules_skillTreeDiagramDarkNav)
        coreRulebookNav = v.findViewById(R.id.rules_coreRulebookNav)
        treatingWoundsNav = v.findViewById(R.id.rules_treatingWoundsFlowchartNav)
        nativeSkillTreeDiagramNav = v.findViewById(R.id.rules_skillTreeDiagramNativeNav)

        skillListNav.setOnClick {
            val frag = SkillListFragment.newInstance()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(R.id.container, frag)
            transaction.addToBackStack(TAG).commit()
        }

        skillTreeDiagramNav.setOnClick {
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(v.context, ImageDownloader.Companion.ImageKey.SKILL_TREE.key)
            val intent = Intent(v.context, SAImageViewActivity::class.java)
            startActivity(intent)
        }

        skillTreeDiagramDarkNav.setOnClick {
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(v.context, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK.key)
            val intent = Intent(v.context, SAImageViewActivity::class.java)
            startActivity(intent)
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
            DataManager.shared.passedBitmap = SharedPrefsManager.shared.getBitmap(v.context, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS.key)
            val intent = Intent(v.context, SAImageViewActivity::class.java)
            startActivity(intent)
        }

        nativeSkillTreeDiagramNav.setOnClick {
            nativeSkillTreeDiagramNav.setLoading(true)
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.SKILL_CATEGORIES), false) {
                nativeSkillTreeDiagramNav.setLoading(false)
                val intent = Intent(v.context, NativeSkillTreeActivity::class.java)
                startActivity(intent)
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.RULEBOOK), false) {
            buildView(v)
        }
        buildView(v)
    }

    private fun buildView(v: View) {
        skillListNav.setLoading(DataManager.shared.loadingSkills)
        skillListNav.isGone = !DataManager.shared.loadingSkills && DataManager.shared.skills == null
        coreRulebookNav.setLoading(DataManager.shared.loadingRulebook)
        coreRulebookNav.isGone = !DataManager.shared.loadingRulebook && DataManager.shared.rulebook == null
        handleImages(v)

        skillTreeDiagramNav.isGone = !FeatureFlag.OLD_SKILL_TREE_IMAGE.isActive()
        skillTreeDiagramDarkNav.isGone = !FeatureFlag.OLD_SKILL_TREE_IMAGE.isActive()
    }

    private fun handleImages(v: View) {
        treatingWoundsNav.setLoading(loadingTreatingWounds)

        if (FeatureFlag.OLD_SKILL_TREE_IMAGE.isActive()) {
            skillTreeDiagramNav.setLoading(loadingSkillTree)
            skillTreeDiagramDarkNav.setLoading(loadingSkillTreeDark)
            ImageDownloader.download(v.context, lifecycleScope, ImageDownloader.Companion.ImageKey.SKILL_TREE) { success ->
                activity?.runOnUiThread {
                    skillTreeDiagramNav.isGone = !success
                    skillTreeDiagramNav.setLoading(false)
                }
            }
            ImageDownloader.download(v.context, lifecycleScope, ImageDownloader.Companion.ImageKey.SKILL_TREE_DARK) { success ->
                activity?.runOnUiThread {
                    skillTreeDiagramDarkNav.isGone = !success
                    skillTreeDiagramDarkNav.setLoading(false)
                }
            }
        }
        ImageDownloader.download(v.context, lifecycleScope, ImageDownloader.Companion.ImageKey.TREATING_WOUNDS) { success ->
            activity?.runOnUiThread {
                treatingWoundsNav.isGone = !success
                treatingWoundsNav.setLoading(false)
            }
        }
    }

    companion object {
        fun newInstance() =
            RulesFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}