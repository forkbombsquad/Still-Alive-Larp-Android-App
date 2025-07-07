package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.ContactRequestModel

import com.forkbombsquad.stillalivelarp.services.utils.UpdateModelSP
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.utils.globalCopyToClipboard
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.launch

class ContactDetailsActivity : NoStatusBarActivity() {

    private lateinit var name: KeyValueView
    private lateinit var email: KeyValueView
    private lateinit var postal: KeyValueView
    private lateinit var message: TextView
    private lateinit var markAsRead: LoadingButton

    private lateinit var contactRequest: ContactRequestModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        setupView()
    }

    private fun setupView() {
        contactRequest = DataManager.shared.getPassedData(ContactListActivity::class, DataManagerPassedDataKey.SELECTED_CONTACT_REQUEST)!!

        name = findViewById(R.id.contactrequestdetails_name)
        email = findViewById(R.id.contactrequestdetails_email)
        postal = findViewById(R.id.contactrequestdetails_postalCode)
        message = findViewById(R.id.contactrequestdetails_message)
        markAsRead = findViewById(R.id.contactrequestdetails_markAsRead)

        name.makeCopyable()
        email.makeCopyable()
        postal.makeCopyable()

        message.setOnLongClickListener {
            globalCopyToClipboard(this, message)
            true
        }

        markAsRead.setOnClick {
            markAsRead.setLoading(true)
            contactRequest.read = contactRequest.read.toBoolean().ternary("FALSE", "TRUE")
            val updateContactRequest = AdminService.UpdateContactRequest()
            lifecycleScope.launch {
                updateContactRequest.successfulResponse(UpdateModelSP(contactRequest)).ifLet({
                    AlertUtils.displaySuccessMessage(this@ContactDetailsActivity, "Marked as ${contactRequest.read.toBoolean().ternary("read", "unread")}") { _, _ ->
                        DataManager.shared.callUpdateCallback(AdminPanelActivity::class)
                        DataManager.shared.closeActiviesToClose()
                        finish()
                    }
                }, {
                    markAsRead.setLoading(false)
                })
            }
        }

        buildView()
    }

    private fun buildView() {
        name.set(contactRequest.fullName)
        email.set(contactRequest.emailAddress)
        postal.set(contactRequest.postalCode)
        message.text = contactRequest.message

        markAsRead.set(contactRequest.read.toBoolean().ternary("Mark as Unread", "Mark as Read"))
    }

}