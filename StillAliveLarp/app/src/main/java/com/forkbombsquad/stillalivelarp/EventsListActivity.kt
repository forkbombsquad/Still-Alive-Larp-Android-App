package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreenBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRed
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonRedBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.reflect.KClass

class EventsListActivity : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var innerLayout: LinearLayout

    private lateinit var destClass: KClass<*>
    private var additionalDestClass: KClass<*>? = null
    private lateinit var events: List<FullEventModel>
    private lateinit var viewTitle: String

    private val sourceClasses: List<KClass<*>> = listOf(AdminPanelActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_list)
        setupView()
    }

    private fun setupView() {
        destClass = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.DESTINATION_CLASS)!!
        additionalDestClass = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.ADDITIONAL_DESTINATION_CLASS)
        events = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.EVENT_LIST)!!
        viewTitle = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.VIEW_TITLE)!!

        title = findViewById(R.id.charlist_title)
        innerLayout = findViewById(R.id.charlist_innerlayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, viewTitle)
        innerLayout.removeAllViews()
        var hadTop = false
        if (additionalDestClass != null) {
            hadTop = true
            val arrow = NavArrowButtonBlueBuildable(this)
            arrow.textView.text = "Create New event"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 32, 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                DataManager.shared.addActivityToClose(this)
                val intent = Intent(this, additionalDestClass!!.java)
                startActivity(intent)
            }
            innerLayout.addView(arrow)
        }
        events.sortedByDescending { it.id }.forEachIndexed { index, event ->
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (!hadTop && index == 0).ternary(32, 16), 0, 16)
            val onClick = {
                DataManager.shared.addActivityToClose(this)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_EVENT, event)
                val intent = Intent(this, destClass.java)
                startActivity(intent)
            }
            if (event.isOngoing()) {
                val arrow = NavArrowButtonGreenBuildable(this)
                arrow.textView.text = event.title
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick(onClick)
                innerLayout.addView(arrow)
            } else if (event.isFinished) {
                val arrow = NavArrowButtonRedBuildable(this)
                arrow.textView.text = event.title
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick(onClick)
                innerLayout.addView(arrow)
            } else {
                val arrow = NavArrowButtonBlackBuildable(this)
                arrow.textView.text = event.title
                arrow.layoutParams = params
                arrow.setLoading(false)
                arrow.setOnClick(onClick)
                innerLayout.addView(arrow)
            }
        }
    }
}