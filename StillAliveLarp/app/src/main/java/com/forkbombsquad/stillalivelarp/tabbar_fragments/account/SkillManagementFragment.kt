package com.forkbombsquad.stillalivelarp.tabbar_fragments.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.FragmentTemplate
import com.forkbombsquad.stillalivelarp.HomeActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.community.ViewPlayerStuffFragment
import com.forkbombsquad.stillalivelarp.utils.*

class SkillManagementFragment : Fragment() {
    private val TAG = "SKILL_MANAGEMENT_FRAGMENT"

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var addNewButton: Button
    private lateinit var skillListLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_skill_management, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        title = v.findViewById(R.id.skillman_title)
        searchBar = v.findViewById(R.id.skillman_searchview)
        addNewButton = v.findViewById(R.id.skillman_addnew)
        skillListLayout = v.findViewById(R.id.skillman_layout)

        addNewButton.setOnClickListener {
            if (!DataManager.shared.loadingSkills) {
                DataManager.shared.unrelaltedUpdateCallback = {
                    // Make sure this page updates when skills update
                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CHAR_FOR_SELECTED_PLAYER), true) {
                        buildView(v)
                    }
                }
                val intent = Intent(activity, AddSkillActivity::class.java)
                startActivity(intent)
            }
        }

        searchBar.addTextChangedListener {
            buildView(v)
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.PLAYER, DataManagerType.CHAR_FOR_SELECTED_PLAYER), false) {
            buildView(v)
        }
        buildView(v)
    }

    private fun buildView(v: View) {
        addNewButton.isGone = DataManager.shared.player?.id != DataManager.shared.selectedPlayer?.id
        title.text = "${DataManager.shared.charForSelectedPlayer?.fullName ?: "Character"}'s\nSkills"
        skillListLayout.removeAllViews()

        DataManager.shared.charForSelectedPlayer?.skills.ifLet { skills ->
            getSortedSkills(skills).forEachIndexed { index, it ->
                val cell = SkillCell(v.context)
                cell.setup(it)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                skillListLayout.addView(cell)
            }
        }
    }

    private fun getSortedSkills(skills: Array<FullSkillModel>): List<FullSkillModel> {
        var filteredSkills = skills.toList()
        if (searchBar.text.toString().trim().isNotEmpty()) {
            val text = searchBar.text.toString().trim().lowercase()
            filteredSkills = skills.filter { it.includeInFilter(text, SkillFilterType.NONE) }
        }
        return filteredSkills.sortedBy { it.name }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SkillManagementFragment()
    }
}