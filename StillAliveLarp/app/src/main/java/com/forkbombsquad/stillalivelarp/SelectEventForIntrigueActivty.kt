package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary

class SelectEventForIntrigueActivty : NoStatusBarActivity() {

    private lateinit var progressbar: ProgressBar
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_event_for_intrigue_activty)
        setupView()
    }

    private fun setupView() {
        progressbar = findViewById(R.id.selecteventforintrigue_progressbar)
        layout = findViewById(R.id.selecteventforintrigue_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENTS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        if (DataManager.shared.loadingEvents) {
            progressbar.isGone = false
            layout.isGone = true
        } else {
            progressbar.isGone = true
            layout.isGone = false

            layout.removeAllViews()

            DataManager.shared.events.ifLet { events ->
                events.forEachIndexed { index, event ->
                    val arrow = NavArrowButtonBlackBuildable(this)
                    arrow.textView.text = event.title
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
                    arrow.layoutParams = params
                    arrow.setLoading(false)
                    arrow.setOnClick {
                        DataManager.shared.selectedEvent = event
                        DataManager.shared.activityToClose = this
                        val intent = Intent(this, ManageIntrigueActivity::class.java)
                        startActivity(intent)
                    }
                    layout.addView(arrow)
                }
            }
        }
    }
}