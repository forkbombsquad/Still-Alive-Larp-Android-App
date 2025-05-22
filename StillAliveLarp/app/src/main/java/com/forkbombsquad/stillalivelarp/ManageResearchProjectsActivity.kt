package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectCreateModel
import com.forkbombsquad.stillalivelarp.services.models.ResearchProjectModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.ResearchProjectCell
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageResearchProjectsActivity : NoStatusBarActivity() {

    private lateinit var progressbar: ProgressBar
    private lateinit var innerLayout: LinearLayout
    private lateinit var outerLayout: LinearLayout
    private lateinit var addNew: NavArrowButtonGreen

    val nameKey = "Name"
    val descKey = "Description"
    val milestonesKey = "Milestones"
    val completeKey = "Complete"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_research_projects)
        setupView()
    }

    private fun setupView() {
        progressbar = findViewById(R.id.mrp_progressbar)
        innerLayout = findViewById(R.id.mrp_innerLayout)
        outerLayout = findViewById(R.id.mrp_outerLayout)
        addNew = findViewById(R.id.mrp_addNew)

        addNew.setOnClick {
            addNew.setLoading(true)

            AlertUtils.displayMessageWithInputs(
                context = this,
                title = "Create New Research Project",
                editTexts = mapOf(
                    Pair(nameKey, EditText(this).apply {
                        hint = "Research Project Name"
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    }),
                    Pair(milestonesKey, EditText(this).apply {
                        hint = "Completed Milestones"
                        inputType = InputType.TYPE_CLASS_NUMBER
                    }),
                    Pair(descKey, EditText(this).apply {
                        hint = "Research Project Description"
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
                        minLines = 5
                        isSingleLine = false
                        setHorizontallyScrolling(false)
                    })
                ),
                checkboxes = mapOf(
                    Pair(completeKey, CheckBox(this).apply {
                        text = "Is Complete?"
                        isChecked = false
                    })
                )
            ) { responses ->
                val researchProjectCreateModel = ResearchProjectCreateModel(
                    name = responses[nameKey] ?: "Unknown",
                    description = responses[descKey] ?: "Unknown",
                    milestones = responses[milestonesKey]?.toInt() ?: 0,
                    complete = responses[completeKey]?.uppercase() ?: "FALSE"
                )
                val request = AdminService.CreateResearchProject()
                lifecycleScope.launch {
                    request.successfulResponse(CreateModelSP(researchProjectCreateModel)).ifLet({ rpm ->
                        val newProjects = OldDataManager.shared.researchProjects?.toMutableList() ?: mutableListOf()
                        newProjects.add(rpm)
                        OldDataManager.shared.researchProjects = newProjects
                        buildView()
                        addNew.setLoading(false)
                    }, {
                        AlertUtils.displaySomethingWentWrong(this@ManageResearchProjectsActivity)
                        addNew.setLoading(false)
                    })
                }
            }
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.RESEARCH_PROJECTS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (OldDataManager.shared.loadingResearchProjects) {
            progressbar.isGone = false
            outerLayout.isGone = true
        } else {
            progressbar.isGone = true
            outerLayout.isGone = false

            innerLayout.removeAllViews()
            val projects = OldDataManager.shared.researchProjects?.sortedByDescending { it.id } ?: listOf()
            projects.forEach { rp ->
                val rpCell = ResearchProjectCell(this)
                rpCell.setup(rp)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 8, 0, 8)
                rpCell.layoutParams = params
                rpCell.setOnClick {
                    rpCell.setLoading(true)

                    AlertUtils.displayMessageWithInputs(
                        context = this,
                        title = "Update Research Project",
                        editTexts = mapOf(
                            Pair(nameKey, EditText(this).apply {
                                setText(rp.name)
                                hint = "Research Project Name"
                                inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                            }),
                            Pair(milestonesKey, EditText(this).apply {
                                setText(rp.milestones.toString())
                                hint = "Completed Milestones"
                                inputType = InputType.TYPE_CLASS_NUMBER
                            }),
                            Pair(descKey, EditText(this).apply {
                                setText(rp.description)
                                hint = "Research Project Description"
                                inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
                                minLines = 5
                                isSingleLine = false
                                setHorizontallyScrolling(false)
                            })
                        ),
                        checkboxes = mapOf(
                            Pair(completeKey, CheckBox(this).apply {
                                text = "Is Complete?"
                                isChecked = rp.complete.toBoolean()
                            })
                        )
                    ) { responses ->
                        val researchProjectModel = ResearchProjectModel(
                            id = rp.id,
                            name = responses[nameKey] ?: "Unknown",
                            description = responses[descKey] ?: "Unknown",
                            milestones = responses[milestonesKey]?.toInt() ?: 0,
                            complete = responses[completeKey]?.uppercase() ?: "FALSE"
                        )
                        val request = AdminService.UpdateResearchProject()
                        lifecycleScope.launch {
                            request.successfulResponse(UpdateModelSP(researchProjectModel)).ifLet({ rpm ->
                                val newProjects = OldDataManager.shared.researchProjects?.toMutableList() ?: mutableListOf()
                                newProjects.removeIf { it.id == rpm.id }
                                newProjects.add(rpm)
                                OldDataManager.shared.researchProjects = newProjects
                                rpCell.setLoading(false)
                                buildView()
                            }, {
                                AlertUtils.displaySomethingWentWrong(this@ManageResearchProjectsActivity)
                                rpCell.setLoading(false)
                            })
                        }
                    }
                }
                innerLayout.addView(rpCell)
            }
        }
    }
}