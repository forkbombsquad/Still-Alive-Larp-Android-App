package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.AddSkillActivity
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class OfflineViewSkillsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var skillListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_view_skills)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.offlineviewskills_title)
        searchBar = findViewById(R.id.offlineviewskills_searchview)
        skillListLayout = findViewById(R.id.offlineviewskills_layout)

        searchBar.addTextChangedListener {
            buildView()
        }
        
        buildView()
    }

    private fun buildView() {
        title.text = "${DataManager.shared.charForSelectedPlayer?.fullName ?: "Character"}'s\nSkills"
        skillListLayout.removeAllViews()

        DataManager.shared.charForSelectedPlayer?.skills.ifLet { skills ->
            getSortedSkills(skills).forEachIndexed { index, it ->
                val cell = SkillCell(this)
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

}