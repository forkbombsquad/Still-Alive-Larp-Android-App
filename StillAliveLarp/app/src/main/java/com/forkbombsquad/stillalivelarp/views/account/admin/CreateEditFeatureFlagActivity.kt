package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey

import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagCreateModel
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel
import com.forkbombsquad.stillalivelarp.services.utils.CreateModelSP
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateEditFeatureFlagActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var name: TextInputEditText
    private lateinit var desc: TextInputEditText
    private lateinit var androidCheck: CheckBox
    private lateinit var iOSCheck: CheckBox
    private lateinit var save: LoadingButton
    private lateinit var delete: LoadingButton

    private var featureFlag: FeatureFlagModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit_feature_flag)
        setupView()
    }

    private fun setupView() {
        featureFlag = DataManager.shared.getPassedData(FeatureFlagManagementActivity::class, DataManagerPassedDataKey.SELECTED_FEATURE_FLAG)

        title = findViewById(R.id.featureflag_title)
        name = findViewById(R.id.featureflag_flagName)
        desc = findViewById(R.id.featureflag_description)
        androidCheck = findViewById(R.id.featureflag_androidCheck)
        iOSCheck = findViewById(R.id.featureflag_iosCheck)
        save = findViewById(R.id.featureflag_save)
        delete = findViewById(R.id.featureflag_delete)

        desc.hint = "Feature Flag Description"

        save.setOnClick {
            save.setLoading(true)
            featureFlag.ifLet({ flag ->
                val updatedFlag = FeatureFlagModel(
                    id = flag.id,
                    name = this.name.text.toString(),
                    description = this.desc.text.toString(),
                    activeAndroid = androidCheck.isChecked.ternary("TRUE", "FALSE"),
                    activeIos = iOSCheck.isChecked.ternary("TRUE", "FALSE")
                )
                val request = AdminService.UpdateFeatureFlag()
                lifecycleScope.launch {
                    request.successfulResponse(UpdateModelSP(updatedFlag)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@CreateEditFeatureFlagActivity, "Updated feature flag!") { _, _ ->
                            save.setLoading(false)
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    }, {
                        save.setLoading(false)
                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                        DataManager.shared.closeActiviesToClose()
                        AlertUtils.displaySomethingWentWrong(this@CreateEditFeatureFlagActivity)
                    })
                }
            }, {
                val featureFlagCreateModel = FeatureFlagCreateModel(
                    name = this.name.text.toString(),
                    description = this.desc.text.toString(),
                    activeAndroid = androidCheck.isChecked.ternary("TRUE", "FALSE"),
                    activeIos = iOSCheck.isChecked.ternary("TRUE", "FALSE")
                )
                val request = AdminService.CreateFeatureFlag()
                lifecycleScope.launch {
                    request.successfulResponse(CreateModelSP(featureFlagCreateModel)).ifLet({ _ ->
                        AlertUtils.displaySuccessMessage(this@CreateEditFeatureFlagActivity, "Created feature flag!") { _, _ ->
                            save.setLoading(false)
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    }, {
                        save.setLoading(false)
                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                        DataManager.shared.closeActiviesToClose()
                        AlertUtils.displaySomethingWentWrong(this@CreateEditFeatureFlagActivity)
                    })
                }
            })
        }

        delete.setOnClick {
            featureFlag.ifLet {
                delete.setLoading(true)
                val request = AdminService.DeleteFeatureFlag()
                lifecycleScope.launch {
                    request.successfulResponse(IdSP(it.id)).ifLet({
                        AlertUtils.displaySuccessMessage(this@CreateEditFeatureFlagActivity, "Deleted feature flag!") { _, _ ->
                            delete.setLoading(false)
                            DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                            DataManager.shared.closeActiviesToClose()
                            finish()
                        }
                    }, {
                        delete.setLoading(false)
                        AlertUtils.displaySomethingWentWrong(this@CreateEditFeatureFlagActivity)
                    })
                }
            }
        }

        buildView()
    }

    private fun buildView() {
        featureFlag.ifLet({ flag ->
            title.text = "Edit Feature Flag"
            name.setText(flag.name)
            desc.setText(flag.description)
            androidCheck.isChecked = flag.isActiveAndroid()
            iOSCheck.isChecked = flag.isActiveIos()
            save.textView.text = "Update"
        }, {
            title.text = "Create Feature Flag"
            delete.isGone = true
            save.textView.text = "Create"
        })
    }
}