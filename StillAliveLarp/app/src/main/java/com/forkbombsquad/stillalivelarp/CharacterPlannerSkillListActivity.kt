package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CharacterPlannerSkillListActivity : NoStatusBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_planner_skill_list)
        setupView()
    }

    private val TAG = "SKILL_MANAGEMENT_FRAGMENT"

    private lateinit var title: TextView
    
    private lateinit var xpText: TextView
    private lateinit var ppText: TextView
    private lateinit var ft1sText: TextView
    
    private lateinit var searchBar: EditText
    private lateinit var addNewButton: Button
    private lateinit var skillListLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var loadingView = true
    private var skillCells: MutableList<SkillCell> = mutableListOf()

    private fun setupView() {
        title = findViewById(R.id.skillplan_title)
        xpText = findViewById(R.id.skillplan_xp)
        ppText = findViewById(R.id.skillplan_pp)
        ft1sText = findViewById(R.id.skillplan_ft1s)
        searchBar = findViewById(R.id.skillplan_searchview)
        addNewButton = findViewById(R.id.skillplan_addnew)
        skillListLayout = findViewById(R.id.skillplan_layout)

        progressBar = findViewById(R.id.skillplan_progressBar)

        addNewButton.setOnClickListener {
            if (!DataManager.shared.loadingSkills) {
                DataManager.shared.unrelaltedUpdateCallback = {
                    // Make sure this page updates when skills update
                    createViews()
                }
                val intent = Intent(this, AddPlannedSkillActivity::class.java)
                startActivity(intent)
            }
        }

        searchBar.addTextChangedListener {
            lifecycleScope.launch(Dispatchers.IO) {
                createViews()
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.SKILLS), false) {
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
        this.runOnUiThread {
            buildView()
        }
        skillCells = mutableListOf()

        getSortedSkills(DataManager.shared.selectedPlannedCharacter?.skills ?: arrayOf()).forEachIndexed { index, it ->
            val cell = SkillCell(this)
            cell.setup(it)
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            skillCells.add(cell)
        }
        if (skillCells.isNotEmpty()) {
            this.runOnUiThread {
                loadingView = false
                buildView()
            }
        }
    }

    private fun buildView() {
        title.text = "${DataManager.shared.selectedPlannedCharacter?.fullName ?: "Character"}'s\nPlanned Skills"
        DataManager.shared.selectedPlannedCharacterCharSkills.ifLet {
            var xpCount = 0
            var ppCount = 0
            var ft1sCount = 0
            it.forEach { characterSkillModel ->
                xpCount += characterSkillModel.xpSpent
                ppCount += characterSkillModel.ppSpent
                ft1sCount += characterSkillModel.fsSpent
            }
            xpText.text = "Spent Experience\n$xpCount"
            ppText.text = "Spent Prestige\n$ppCount"
            ft1sText.text = "Spent Free T1 Skills\n$ft1sCount"
        }
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