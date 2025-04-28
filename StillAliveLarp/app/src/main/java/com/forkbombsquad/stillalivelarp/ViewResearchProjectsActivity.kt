package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.ResearchProjectCell

class ViewResearchProjectsActivity : NoStatusBarActivity() {

    private lateinit var innerLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_research_projects)
        setupView()
    }

    private fun setupView() {
        innerLayout = findViewById(R.id.rp_innerLayout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.RESEARCH_PROJECTS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        innerLayout.removeAllViews()
        val projects = DataManager.shared.researchProjects?.sortedByDescending { it.id } ?: listOf()
        projects.forEach { rp ->
            val rpCell = ResearchProjectCell(this)
            rpCell.setup(rp)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 8, 0, 8)
            rpCell.layoutParams = params
            innerLayout.addView(rpCell)
        }
    }
}