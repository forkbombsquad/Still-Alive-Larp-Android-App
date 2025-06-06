package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class ContactListActivity : NoStatusBarActivity() {

    private lateinit var loading: ProgressBar
    private lateinit var noRequest: TextView
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        setupView()
    }

    private fun setupView() {
        loading = findViewById(R.id.contactrequestlist_loading)
        noRequest = findViewById(R.id.contactrequestlist_norequests)
        layout = findViewById(R.id.contactrequestlist_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CONTACT_REQUESTS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        if (DataManager.shared.loadingContactRequests) {
            loading.isGone = false
            noRequest.isGone = true
            layout.isGone = true
        } else if (DataManager.shared.contactRequests.isNullOrEmpty()) {
            loading.isGone = true
            noRequest.isGone = false
            layout.isGone = true
        } else {
            loading.isGone = true
            noRequest.isGone = true
            layout.isGone = false
            DataManager.shared.contactRequests.ifLet { requests ->
                for (request in requests.sortedBy { !it.read.toBoolean() }) {
                    val navarrow = NavArrowButtonBlackBuildable(this)
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 16)
                    navarrow.layoutParams = params
                    navarrow.textView.text = "${request.fullName}${request.read.toBoolean().ternary("", " *")}"
                    navarrow.setLoading(DataManager.shared.loadingEventPreregs)
                    navarrow.setOnClick {
                        DataManager.shared.unrelaltedUpdateCallback = {
                            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CONTACT_REQUESTS), true) {
                                buildView()
                            }
                            buildView()
                        }
                        DataManager.shared.selectedContactRequest = request
                        val intent = Intent(this, ContactDetailsActivity::class.java)
                        startActivity(intent)
                    }
                    layout.addView(navarrow)
                }
            }
        }
    }
}