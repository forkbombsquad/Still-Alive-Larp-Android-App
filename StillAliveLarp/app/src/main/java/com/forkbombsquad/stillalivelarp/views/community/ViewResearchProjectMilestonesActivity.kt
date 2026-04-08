package com.forkbombsquad.stillalivelarp.views.community

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectMilestoneJsonModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ResearchProjectCell
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.account.CharacterPlannerActivity
import com.forkbombsquad.stillalivelarp.views.account.admin.ManageResearchProjectsActivity
import com.forkbombsquad.stillalivelarp.views.shared.CharactersListActivity

class ViewResearchProjectMilestonesActivity : NoStatusBarActivity() {
    private lateinit var researchProject: ResearchProjectModel
    private lateinit var title: TextView
    private lateinit var milestoneHeading: TextView
    private lateinit var milestoneText: TextView
    private lateinit var completeText: TextView
    private lateinit var milestoneCountText: TextView
    private lateinit var prevButton: Button
    private lateinit var nextButton: Button

    private var currentMilestoneIndex: Int = 0
    private var milestones: List<ResearchProjectMilestoneJsonModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_research_project_milestones)
        setupView()
    }

    private fun setupView() {
        researchProject = DataManager.shared.getPassedData(listOf(ViewResearchProjectsActivity::class, ManageResearchProjectsActivity::class), DataManagerPassedDataKey.RESEARCH_PROJECT)!!
        milestones = researchProject.milestoneJsonModels ?: listOf()

        title = findViewById(R.id.rpm_title)
        milestoneHeading = findViewById(R.id.rpm_milestoneHeading)
        completeText = findViewById(R.id.rpm_complete)
        milestoneCountText = findViewById(R.id.rpm_milestones)
        milestoneText = findViewById(R.id.rpm_milestoneText)
        prevButton = findViewById(R.id.rpm_prevButton)
        nextButton = findViewById(R.id.rpm_nextButton)

        prevButton.setOnClickListener {
            runOnUiThread {
                currentMilestoneIndex -= 1
                buildView()
            }
        }

        nextButton.setOnClickListener {
            runOnUiThread {
                currentMilestoneIndex += 1
                buildView()
            }
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, researchProject.name)

        completeText.text = researchProject.complete.toBoolean().ternary("YES", "NO")
        milestoneCountText.text = researchProject.milestones.toString()

        val currentMilestone = milestones[currentMilestoneIndex]
        milestoneHeading.text = "Milestone ${currentMilestone.id}"
        milestoneText.text = currentMilestone.text

        prevButton.isGone = currentMilestoneIndex == 0
        nextButton.isGone = currentMilestoneIndex >= milestones.count() - 1
    }
}