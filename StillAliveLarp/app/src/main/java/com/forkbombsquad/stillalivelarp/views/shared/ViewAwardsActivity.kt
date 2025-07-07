package com.forkbombsquad.stillalivelarp.views.shared

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.AwardModel
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.google.android.material.divider.MaterialDivider

class ViewAwardsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var awardsLayout: LinearLayout

    private lateinit var awards: List<AwardModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_awards)
        setupView()
    }

    private fun setupView() {
        awards = DataManager.shared.getPassedData(listOf(ViewPlayerActivity::class, ViewCharacterActivity::class, MyAccountFragment::class), DataManagerPassedDataKey.AWARDS_LIST)!!

        title = findViewById(R.id.awards_title)
        awardsLayout = findViewById(R.id.awardsInnerLayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Awards")
        awardsLayout.removeAllViews()
        
        awards.forEachIndexed { index, award ->
            if (index != 0) {
                val divider = MaterialDivider(this)
                divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                awardsLayout.addView(divider)
            }
            val horLayout = LinearLayout(this)
            horLayout.setPadding(0, 8, 0, 0)
            horLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            horLayout.orientation = LinearLayout.HORIZONTAL

            val nameView = TextView(this)
            nameView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            nameView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
            DataManager.shared.getCurrentPlayer()?.characters?.firstOrNull { it.id == award.characterId }.ifLet({ char ->
                nameView.text = char.fullName
            }, {
                nameView.text = DataManager.shared.getCurrentPlayer()?.fullName ?: ""
            })

            val dateView = TextView(this)
            dateView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            dateView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            dateView.text = award.date.yyyyMMddToMonthDayYear()

            val amountView = TextView(this)
            amountView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            amountView.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
            amountView.text = award.getDisplayText()

            horLayout.addView(nameView)
            horLayout.addView(dateView)
            horLayout.addView(amountView)

            val reasonView = TextView(this)
            reasonView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            reasonView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            reasonView.setPadding(0, 0, 0, 8)
            reasonView.text = award.reason

            awardsLayout.addView(horLayout)
            awardsLayout.addView(reasonView)
        }

    }
}