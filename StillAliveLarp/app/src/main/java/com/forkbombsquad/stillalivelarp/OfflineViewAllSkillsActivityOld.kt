package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import com.forkbombsquad.stillalivelarp.services.managers.SkillManager
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.SkillSortType
import com.forkbombsquad.stillalivelarp.utils.ternary

class OfflineViewAllSkillsActivityOld : NoStatusBarActivity() {

    private var currentSort: SkillSortType = SkillSortType.AZ
    private var currentFilter: SkillFilterType = SkillFilterType.NONE

    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var searchBar: EditText

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_view_all_skills_old)
        setupView()
    }

    private fun setupView() {

        sortSpinner = findViewById(R.id.skilllist_sort)
        filterSpinner = findViewById(R.id.skilllist_filter)
        searchBar = findViewById(R.id.skilllist_searchview)

        layout = findViewById(R.id.skilllist_layout)

        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillSortType.getAllStrings())
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = SkillSortType.getTypeForString(sortSpinner.getItemAtPosition(position).toString())
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillFilterType.getAllStrings())
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = SkillFilterType.getTypeForString(filterSpinner.getItemAtPosition(position).toString())
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchBar.addTextChangedListener {
            buildView()
        }

        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        getFilteredSkills(SkillManager.shared.getSkillsOffline()).forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            layout.addView(cell)
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
            SkillSortType.XPASC -> skills.sortedWith(compareBy({ it.xpCost.toInt() }, { it.name }))
            SkillSortType.XPDESC -> skills.sortedWith(compareByDescending { it.xpCost.toInt() })
            SkillSortType.TYPEASC -> skills.sortedWith(compareBy({ it.getTypeText() }, { it.name }))
            SkillSortType.TYPEDESC -> skills.sortedWith(compareByDescending { it.getTypeText() })
        }
        return sorted
    }

}