package com.forkbombsquad.stillalivelarp.views.community

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity

import com.forkbombsquad.stillalivelarp.utils.ResearchProjectCell

class ViewResearchProjectsActivity : NoStatusBarActivity() {

    private lateinit var innerLayout: LinearLayout
    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_research_projects)
        setupView()
    }

    private fun setupView() {
        innerLayout = findViewById(R.id.rp_innerLayout)
        title = findViewById(R.id.rp_title)
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Research Projects")
        innerLayout.removeAllViews()
        val projects = DataManager.shared.researchProjects.sortedByDescending { it.id }
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