package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.forkbombsquad.stillalivelarp.utils.FeatureFlagView
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable

class FeatureFlagManagementActivity : NoStatusBarActivity() {
    private val TAG = "FEATURE_FLAG_MANAGEMENT_ACTIVITY"

    private lateinit var loading: ProgressBar
    private lateinit var layout: LinearLayout

    private lateinit var pullToRefresh: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_flag_management)
        setupView()
    }

    private fun setupView() {
        loading = findViewById(R.id.featureflagmanagement_loadingBar)
        layout = findViewById(R.id.featureflagmanagement_layout)
        pullToRefresh = findViewById(R.id.pulltorefresh_featureflagmanagement)

        pullToRefresh.setOnRefreshListener {
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FEATURE_FLAGS), true) {
                buildView()
                pullToRefresh.isRefreshing = false
            }
        }

        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FEATURE_FLAGS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        if (OldDataManager.shared.loadingFeatureFlags) {
            loading.isGone = false
            layout.isGone = true
        } else {
            loading.isGone = true
            layout.isGone = false
            val arrow = NavArrowButtonRedBuildable(this)
            arrow.textView.text = "Add New Feature Flag"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 32, 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                OldDataManager.shared.unrelaltedUpdateCallback = {
                    OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FEATURE_FLAGS), true) {
                        buildView()
                    }
                    buildView()
                }
                OldDataManager.shared.selectedFeatureFlag = null
                val intent = Intent(this, CreateEditFeatureFlagActivity::class.java)
                startActivity(intent)
            }
            layout.addView(arrow)

            for (ff in OldDataManager.shared.featureFlags ?: arrayOf()) {
                val flagView = FeatureFlagView(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                flagView.layoutParams = params

                flagView.set(ff)
                flagView.setOnClickEdit {
                    OldDataManager.shared.unrelaltedUpdateCallback = {
                        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.FEATURE_FLAGS), true) {
                            buildView()
                        }
                        buildView()
                    }
                    OldDataManager.shared.selectedFeatureFlag = ff
                    val intent = Intent(this, CreateEditFeatureFlagActivity::class.java)
                    startActivity(intent)
                }
                layout.addView(flagView)
            }
        }
    }
}