package com.forkbombsquad.stillalivelarp.tabbar_fragments.rules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.SkillSortType
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkillListFragment : Fragment() {
    private val TAG = "SKILL_LIST_FRAGMENT"

    private var currentSort: SkillSortType = SkillSortType.AZ
    private var currentFilter: SkillFilterType = SkillFilterType.NONE

    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var searchBar: EditText

    private lateinit var layout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var loadingView = true
    private var skillCells: MutableList<SkillCell> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_skill_list, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {

        sortSpinner = v.findViewById(R.id.skilllist_sort)
        filterSpinner = v.findViewById(R.id.skilllist_filter)
        searchBar = v.findViewById(R.id.skilllist_searchview)

        progressBar = v.findViewById(R.id.skilllist_progressBar)

        layout = v.findViewById(R.id.skilllist_layout)

        val sortAdapter = ArrayAdapter(v.context, android.R.layout.simple_spinner_dropdown_item, SkillSortType.getAllStrings())
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = SkillSortType.getTypeForString(sortSpinner.getItemAtPosition(position).toString())
                lifecycleScope.launch(Dispatchers.IO) {
                    createViews(v)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val filterAdapter = ArrayAdapter(v.context, android.R.layout.simple_spinner_dropdown_item, SkillFilterType.getAllStrings())
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = SkillFilterType.getTypeForString(filterSpinner.getItemAtPosition(position).toString())
                lifecycleScope.launch(Dispatchers.IO) {
                    createViews(v)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchBar.addTextChangedListener {
            lifecycleScope.launch(Dispatchers.IO) {
                createViews(v)
            }
        }
        DataManager.shared.load(lifecycleScope) {
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
            buildView(v)
        }
        skillCells = mutableListOf()

        getFilteredSkills(DataManager.shared.skills).forEachIndexed { index, it ->
            val cell = SkillCell(v.context)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            skillCells.add(cell)
        }
        if (skillCells.isNotEmpty()) {
            activity?.runOnUiThread {
                loadingView = false
                buildView(v)
            }
        }
    }

    private fun buildView(v: View) {
        layout.removeAllViews()
        progressBar.isGone = !loadingView || skillCells.isNotEmpty()
        if (!loadingView) {
            for (cell in skillCells) {
                layout.addView(cell)
            }
        }
    }

    private fun getFilteredSkills(skills: List<FullSkillModel>): List<FullSkillModel> {
        var filteredSkills = skills
        val text = searchBar.text.toString().trim().lowercase()
        if (text.isNotEmpty() || currentFilter != SkillFilterType.NONE) {
            filteredSkills = skills.filter { it.includeInFilter(text, currentFilter) }
        }
        return getSortedSkills(filteredSkills)
    }

    private fun getSortedSkills(skills: List<FullSkillModel>): List<FullSkillModel> {
        var sorted = skills
        sorted = when (currentSort) {
            SkillSortType.AZ -> skills.sortedWith(compareBy { it.name })
            SkillSortType.ZA -> skills.sortedWith(compareByDescending { it.name })
            SkillSortType.XPASC -> skills.sortedWith(compareBy({ it.xpCost }, { it.name }))
            SkillSortType.XPDESC -> skills.sortedWith(compareByDescending { it.xpCost })
            SkillSortType.TYPEASC -> skills.sortedWith(compareBy({ it.getTypeText() }, { it.name }))
            SkillSortType.TYPEDESC -> skills.sortedWith(compareByDescending { it.getTypeText() })
        }
        return sorted
    }

    companion object {
        @JvmStatic
        fun newInstance() = SkillListFragment()
    }
}