package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CampFortification
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.services.models.Fortification
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.DropdownSpinner
import com.forkbombsquad.stillalivelarp.utils.FortificationRingCell
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.MessageInput
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.capitalizeOnlyFirstLetterOfEachWord
import com.forkbombsquad.stillalivelarp.utils.globalTestPrint
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.community.CommunityFragment
import kotlinx.coroutines.launch

class ManageCampFortificationsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var loadingLayout: LoadingLayout
    private lateinit var fortLayout: LinearLayout
    private lateinit var addNewButton: NavArrowButtonGreen
    private lateinit var submitButton: LoadingButton

    private lateinit var campStatus: CampStatusModel
    private lateinit var campFortifications: MutableList<CampFortification>
    private var modified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_camp_fortifications)
        setupView()
    }

    private fun setupView() {
        campStatus = DataManager.shared.getPassedData(AdminPanelActivity::class, DataManagerPassedDataKey.CAMP_STATUS)!!
        campFortifications = campStatus.campFortifications.toMutableList()

        title = findViewById(R.id.manfort_title)
        loadingLayout = findViewById(R.id.loadinglayout)
        fortLayout = findViewById(R.id.manfort_fortlayout)
        addNewButton = findViewById(R.id.manfort_addNewRing)
        submitButton = findViewById(R.id.manfort_submitButton)

        addNewButton.setOnClick {
            modified = true
            DataManager.shared.fortificationToEdit = CampFortification(getHighestRing() + 1, listOf())
            updateForts()
            buildView()
        }

        submitButton.setOnClick {
            val campStatus = CampStatusModel.initWithCampFortifications(campStatus.id, campFortifications)
            val request = AdminService.UpdateCampStatus()
            submitButton.setLoadingWithText("Updating Camp Status...")
            lifecycleScope.launch {
                request.successfulResponse(UpdateModelSP(campStatus)).ifLet { updatedCampStatus ->
                    DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                    this@ManageCampFortificationsActivity.campStatus = updatedCampStatus
                    this@ManageCampFortificationsActivity.campFortifications = campStatus.campFortifications.toMutableList()
                    modified = false
                    AlertUtils.displaySuccessMessage(this@ManageCampFortificationsActivity, "Camp Status Changes Committed!") { _, _ ->
                        submitButton.setLoading(false)
                        buildView()
                    }
                }
            }
        }

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Manage Fortifications")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, thingsToHideWhileLoading = listOf(fortLayout), runIfLoading = {}, runIfNotLoading = {
            fortLayout.removeAllViews()
            campFortifications.sortedBy { it.ring }.forEachIndexed { index, it ->
                val cell = FortificationRingCell(this)
                cell.setup(this, it)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                cell.setOnClick {
                    DataManager.shared.fortificationToEdit = it
                    DataManager.shared.setUpdateCallback(this::class) {
                        modified = true
                        updateForts()
                        buildView()
                    }

                    val messageInputs: MutableList<MessageInput> = mutableListOf()
                    var counter = 0
                    while (counter < it.ring) {
                        val fort = it.fortifications.getOrNull(counter)
                        val tv = TextView(this@ManageCampFortificationsActivity)
                        tv.text = "Fortification $counter"

                        val editText = EditText(this@ManageCampFortificationsActivity)
                        editText.hint = "Heath (Max L=5, M=10, H=15, A=20, MG=30)"
                        editText.inputType = InputType.TYPE_CLASS_NUMBER

                        val checkBox = CheckBox(this@ManageCampFortificationsActivity)
                        checkBox.text = "Is Built?"
                        checkBox.isChecked = fort != null

                        val dropdown = DropdownSpinner(this@ManageCampFortificationsActivity)
                        val options = Fortification.FortificationType.values().map { it.text.capitalizeOnlyFirstLetterOfEachWord() }
                        dropdown.setup(this@ManageCampFortificationsActivity, "Fortification Type", options) {}

                        if (fort != null) {
                            val index = options.indexOf(fort.fortificationType.text.capitalizeOnlyFirstLetterOfEachWord())
                            dropdown.setSelectedItem(index)

                            editText.setText(fort.health.toString())
                        }

                        val messageInput = MessageInput(
                            key = "$counter",
                            sectionTitle = tv,
                            editText = editText,
                            checkbox = checkBox,
                            spinner = dropdown
                        )
                        messageInputs.add(messageInput)
                        counter += 1
                    }

                    AlertUtils.displayMessageWithInputs(
                        context = this,
                        title = "Edit Ring ${it.ring}",
                        messageInputs = messageInputs,
                        response = { output ->
                            val forts: MutableList<Fortification> = mutableListOf()
                            var counter = 0
                            while (counter < it.ring) {
                                val op = output.getValuesForKey("$counter")
                                if (op?.checkboxValue == true) {
                                    val fort = Fortification(
                                        type = Fortification.FortificationType.getFortificationType(op?.selectedSpinnerItem()?.uppercase() ?: ""),
                                        health = op?.editTextValue?.toInt() ?: 5
                                    )
                                    forts.add(fort)
                                }
                                counter += 1
                            }
                            modified = true
                            DataManager.shared.fortificationToEdit = CampFortification(it.ring, forts)
                            runOnUiThread {
                                updateForts()
                                buildView()
                            }
                        }
                    )
                }
                fortLayout.addView(cell)
            }
            submitButton.isGone = !modified
        })
    }

    private fun updateForts() {
        DataManager.shared.fortificationToEdit.ifLet { fte ->
            val index = campFortifications.indexOfFirst { it.ring == fte.ring }
            if (index >= 0) {
                campFortifications[index] = fte
            } else {
                campFortifications.add(fte)
            }
        }
    }

    private fun getHighestRing(): Int {
        return campFortifications.maxOfOrNull { it.ring } ?: 1
    }

    override fun onBackPressed() {
        if (modified) {
            AlertUtils.displayYesNoMessage(this, "Are You Sure?", "You have unsaved changes. Are you sure you want to exit?", { _, _ ->
                DataManager.shared.fortificationToEdit = null
                DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                super.onBackPressed()
            }, { _, _ -> })
        } else {
            DataManager.shared.fortificationToEdit = null
            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
            super.onBackPressed()
        }
    }
}