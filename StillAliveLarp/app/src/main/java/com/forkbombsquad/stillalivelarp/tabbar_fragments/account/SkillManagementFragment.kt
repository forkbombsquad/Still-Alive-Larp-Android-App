package com.forkbombsquad.stillalivelarp.tabbar_fragments.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkillManagementFragment : Fragment() {
    private val TAG = "SKILL_MANAGEMENT_FRAGMENT"

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var addNewButton: Button
    private lateinit var skillListLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var loadingView = true
    private var skillCells: MutableList<SkillCell> = mutableListOf()

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

        progressBar = v.findViewById(R.id.skillman_progressBar)

        addNewButton.setOnClickListener {
            if (!DataManager.shared.loadingSkills) {
                DataManager.shared.unrelaltedUpdateCallback = {
                    // Make sure this page updates when skills update
                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CHAR_FOR_SELECTED_PLAYER), true) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            createViews(v)
                        }
                    }
                }
                val intent = Intent(activity, AddSkillActivity::class.java)
                startActivity(intent)
            }
        }

        searchBar.addTextChangedListener {
            lifecycleScope.launch(Dispatchers.IO) {
                createViews(v)
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS, DataManagerType.PLAYER, DataManagerType.CHAR_FOR_SELECTED_PLAYER), false) {
            lifecycleScope.launch(Dispatchers.IO) {
                createViews(v)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            createViews(v)
        }
    }

    @Synchronized
    private fun createViews(v: View) {
        loadingView = true
        activity?.runOnUiThread {
            buildView()
        }
        skillCells = mutableListOf()

        getSortedSkills(DataManager.shared.charForSelectedPlayer?.skills ?: arrayOf()).forEachIndexed { index, it ->
            val cell = SkillCell(v.context)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            skillCells.add(cell)
        }
        if (skillCells.isNotEmpty()) {
            activity?.runOnUiThread {
                loadingView = false
                buildView()
            }
        }
    }

    private fun buildView() {
        addNewButton.isGone = DataManager.shared.player?.id != DataManager.shared.selectedPlayer?.id
        title.text = "${DataManager.shared.charForSelectedPlayer?.fullName ?: "Character"}'s\nSkills"
        skillListLayout.removeAllViews()
        progressBar.isGone = !loadingView || skillCells.isNotEmpty()

        if (!loadingView) {
            for (cell in skillCells) {
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