package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel

import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class ContactListActivity : NoStatusBarActivity() {

    private lateinit var noRequest: TextView
    private lateinit var layout: LinearLayout

    private lateinit var contactRequests: List<ContactRequestModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)
        setupView()
    }

    private fun setupView() {
        contactRequests = DataManager.shared.getPassedData(AdminPanelActivity::class, DataManagerPassedDataKey.CONTACT_REQUEST_LIST)!!

        noRequest = findViewById(R.id.contactrequestlist_norequests)
        layout = findViewById(R.id.contactrequestlist_layout)

        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        if (contactRequests.isEmpty()) {
            noRequest.isGone = false
            layout.isGone = true
        } else {
            noRequest.isGone = true
            layout.isGone = false
            for (request in contactRequests.sortedBy { !it.read.toBoolean() }) {
                val navarrow = NavArrowButtonBlackBuildable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params
                navarrow.textView.text = "${request.fullName}${request.read.toBoolean().ternary("", " *")}"
                navarrow.setOnClick {
                    DataManager.shared.addActivityToClose(this)
                    DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CONTACT_REQUEST, request)
                    val intent = Intent(this, ContactDetailsActivity::class.java)
                    startActivity(intent)
                }
                layout.addView(navarrow)
            }
        }
    }
}