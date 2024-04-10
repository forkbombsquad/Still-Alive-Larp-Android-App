package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
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
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.FEATURE_FLAGS), true) {
                buildView()
                pullToRefresh.isRefreshing = false
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.FEATURE_FLAGS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        if (DataManager.shared.loadingFeatureFlags) {
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
                DataManager.shared.unrelaltedUpdateCallback = {
                    DataManager.shared.load(lifecycleScope, listOf(DataManagerType.FEATURE_FLAGS), true) {
                        buildView()
                    }
                    buildView()
                }
//                val intent = Intent(this, CreateNewFeatureFlagActivity::class.java)
                // TODO add this view
//                startActivity(intent)
            }
            layout.addView(arrow)

            for (ff in DataManager.shared.featureFlags ?: arrayOf()) {
                val flagView = FeatureFlagView(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                flagView.layoutParams = params

                flagView.set(ff)
                flagView.setOnClickEdit {
                    DataManager.shared.unrelaltedUpdateCallback = {
                        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.FEATURE_FLAGS), true) {
                            buildView()
                        }
                        buildView()
                    }
                    DataManager.shared.selectedFeatureFlag = ff
//                    val intent = Intent(this, EditFeatureFlagActivity::class.java)
                    // TODO add this view
//                    startActivity(intent)
                }
                layout.addView(flagView)
            }
        }
    }
}