package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FeatureFlagModel

import com.forkbombsquad.stillalivelarp.utils.FeatureFlagView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable

class FeatureFlagManagementActivity : NoStatusBarActivity() {
    private val TAG = "FEATURE_FLAG_MANAGEMENT_ACTIVITY"

    private lateinit var layout: LinearLayout
    private lateinit var featureFlags: List<FeatureFlagModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag_management)
        setupView()
    }

    private fun setupView() {
        featureFlags = DataManager.shared.getPassedData(AdminPanelActivity::class, DataManagerPassedDataKey.FEATURE_FLAG_LIST)!!
        layout = findViewById(R.id.featureflagmanagement_layout)
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        layout.isGone = false
        val arrow = NavArrowButtonRedBuildable(this)
        arrow.textView.text = "Add New Feature Flag"
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 32, 0, 16)
        arrow.layoutParams = params
        arrow.setLoading(false)
        arrow.setOnClick {
            DataManager.shared.addActivityToClose(this)
            val intent = Intent(this, CreateEditFeatureFlagActivity::class.java)
            startActivity(intent)
        }
        layout.addView(arrow)

        featureFlags.forEach { ff ->
            val flagView = FeatureFlagView(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 16)
            flagView.layoutParams = params

            flagView.set(ff)
            flagView.setOnClickEdit {
                DataManager.shared.addActivityToClose(this)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_FEATURE_FLAG, ff)
                val intent = Intent(this, CreateEditFeatureFlagActivity::class.java)
                startActivity(intent)
            }
            layout.addView(flagView)
        }
    }
}