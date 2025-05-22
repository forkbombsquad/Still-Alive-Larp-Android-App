package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AdminService
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        setupView()
    }

    private fun setupView() {
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
            OldDataManager.shared.selectedContactRequest.ifLet {
                markAsRead.setLoading(true)
                val cr = it
                cr.read = it.read.toBoolean().ternary("FALSE", "TRUE")
                val updateContactRequest = AdminService.UpdateContactRequest()
                lifecycleScope.launch {
                    updateContactRequest.successfulResponse(UpdateModelSP(cr)).ifLet({
                        OldDataManager.shared.unrelaltedUpdateCallback()
                        AlertUtils.displaySuccessMessage(this@ContactDetailsActivity, "Marked as ${cr.read.toBoolean().ternary("read", "unread")}") { _, _ ->
                            finish()
                        }
                    }, {
                        markAsRead.setLoading(false)
                    })
                }
            }
        }

        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.selectedContactRequest.ifLet {
            name.set(it.fullName)
            email.set(it.emailAddress)
            postal.set(it.postalCode)
            message.text = it.message

            markAsRead.set(it.read.toBoolean().ternary("Mark as Unread", "Mark as Read"))
        }
    }

}