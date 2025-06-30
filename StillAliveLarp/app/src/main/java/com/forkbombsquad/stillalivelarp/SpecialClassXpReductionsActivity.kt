package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel

import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.reflect.KClass

class SpecialClassXpReductionsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var loading: ProgressBar
    private lateinit var noRedsText: TextView
    private lateinit var layout: LinearLayout

    private lateinit var character: FullCharacterModel
    private val sourceClasses: List<KClass<*>> = listOf(ViewPlayerActivity::class, MyAccountFragment::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special_class_xp_reductions)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.xpredview_title)
        loading = findViewById(R.id.xpredview_loading)
        noRedsText = findViewById(R.id.xpredview_noredtext)
        layout = findViewById(R.id.xpredview_layout)

        buildView()
    }

    private fun buildView() {
        title.text = "Class Xp Reductions For\n${character.fullName}"
        if (character.xpReductions.isEmpty()) {
            loading.isGone = true
            noRedsText.isGone = false
            noRedsText.text = (DataManager.shared.getActiveCharacter()?.id == character.id).ternary("You have no Xp Reductions from classes you've taken. Try taking a Special Class with someone who has the Professor skill to reduce the xp cost of specific skills! Don't forget to pay them for their time!", "No Xp Reductions Found.")
            layout.isGone = true
        } else {
            layout.removeAllViews()
            loading.isGone = true
            noRedsText.isGone = true
            layout.isGone = false

            character.allNonPurchasedSkills().filter { it.hasXpReduction() }.forEach { skill ->
                val kvView = KeyValueViewBuildable(this)
                kvView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                kvView.set(skill.name, skill.getXpCostText(false))
                layout.addView(kvView)
            }
        }
    }
}