package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class SelectEventForPreregViewActivity : NoStatusBarActivity() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_event_for_prereg_view)
        setupView()
    }

    private fun setupView() {
        layout = findViewById(R.id.selectEventView_layout)
        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS), true) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        layout.removeAllViews()
        DataManager.shared.events.ifLet { events ->
            events.forEach {
                if (it.isInFuture()) {
                    val navarrow = NavArrowButtonBlackBuildable(this)

                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 16)
                    navarrow.layoutParams = params

                    navarrow.textView.text = "${it.title} - ${it.date.yyyyMMddToMonthDayYear()}"
                    navarrow.setLoading(DataManager.shared.loadingEventPreregs)
                    navarrow.setOnClick {
                        DataManager.shared.selectedEvent = it
                        val intent = Intent(this, ViewPreregsForEventActivity::class.java)
                        startActivity(intent)
                    }
                    layout.addView(navarrow)
                } else {
                    val navarrow = NavArrowButtonRedBuildable(this)

                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 16, 0, 16)
                    navarrow.layoutParams = params

                    navarrow.textView.text = "${it.title} - ${it.date.yyyyMMddToMonthDayYear()}"
                    navarrow.setLoading(DataManager.shared.loadingEventPreregs)
                    navarrow.setOnClick {
                        DataManager.shared.selectedEvent = it
                        val intent = Intent(this, ViewPreregsForEventActivity::class.java)
                        startActivity(intent)
                    }
                    layout.addView(navarrow)
                }

            }
        }
    }

}