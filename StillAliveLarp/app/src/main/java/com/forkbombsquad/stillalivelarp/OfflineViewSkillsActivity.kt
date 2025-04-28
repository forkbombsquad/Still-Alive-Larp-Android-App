package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineViewSkillsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var skillListLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var loadingView = true
    private var skillCells: MutableList<SkillCell> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_view_skills)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.offlineviewskills_title)
        searchBar = findViewById(R.id.offlineviewskills_searchview)
        skillListLayout = findViewById(R.id.offlineviewskills_layout)

        progressBar = findViewById(R.id.offlineviewskills_progressBar)

        searchBar.addTextChangedListener {
            lifecycleScope.launch(Dispatchers.IO) {
                createViews()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            createViews()
        }
    }

    @Synchronized
    private fun createViews() {
        loadingView = true
        runOnUiThread {
            buildView()
        }
        skillCells = mutableListOf()

        getSortedSkills(DataManager.shared.charForSelectedPlayer?.skills ?: arrayOf()).forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            skillCells.add(cell)
        }
        if (skillCells.isNotEmpty()) {
            runOnUiThread {
                loadingView = false
                buildView()
            }
        }
    }

    private fun buildView() {
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

}