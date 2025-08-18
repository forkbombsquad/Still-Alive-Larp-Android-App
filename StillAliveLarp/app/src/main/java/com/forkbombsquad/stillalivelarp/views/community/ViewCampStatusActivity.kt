package com.forkbombsquad.stillalivelarp.views.community

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CampFortification
import com.forkbombsquad.stillalivelarp.services.models.CampStatusModel
import com.forkbombsquad.stillalivelarp.utils.FortificationRingCell
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity

class ViewCampStatusActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var loadingLayout: LoadingLayout
    private lateinit var fortLayout: LinearLayout

    private lateinit var campStatus: CampStatusModel
    private lateinit var campFortifications: List<CampFortification>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_camp_status)
        setupView()
    }

    private fun setupView() {
        campStatus = DataManager.shared.getPassedData(CommunityFragment::class, DataManagerPassedDataKey.CAMP_STATUS)!!
        campFortifications = campStatus.campFortifications

        title = findViewById(R.id.campstatus_title)
        loadingLayout = findViewById(R.id.loadinglayout)
        fortLayout = findViewById(R.id.campstatus_fortlayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Camp Status")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, thingsToHideWhileLoading = listOf(fortLayout), runIfLoading = {}, runIfNotLoading = {
            fortLayout.removeAllViews()
            campFortifications.sortedBy { it.ring }.forEachIndexed { index, it ->
                val cell = FortificationRingCell(this)
                cell.setup(this, it)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                fortLayout.addView(cell)
            }
        })
    }
}