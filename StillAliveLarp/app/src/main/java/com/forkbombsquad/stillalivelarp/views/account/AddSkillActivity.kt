package com.forkbombsquad.stillalivelarp.views.account

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType

import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.SkillSortType
import com.forkbombsquad.stillalivelarp.utils.sort
import com.forkbombsquad.stillalivelarp.utils.ternary

class AddSkillActivity : NoStatusBarActivity() {

    private var currentSort: SkillSortType = SkillSortType.AZ
    private var currentFilter: SkillFilterType = SkillFilterType.NONE

    private lateinit var title: TextView

    private lateinit var amountsLayout: LinearLayout
    private lateinit var xp: TextView
    private lateinit var pp: TextView
    private lateinit var ft1s: TextView
    private lateinit var inf: TextView

    private lateinit var sortSpinner: Spinner
    private lateinit var filterSpinner: Spinner
    private lateinit var searchBar: EditText

    private lateinit var layout: LinearLayout

    private lateinit var character: FullCharacterModel
    private lateinit var player: FullPlayerModel

    private lateinit var skills: List<FullCharacterModifiedSkillModel>

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_skill)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(SkillsListActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!
        player = DataManager.shared.getCurrentPlayer()!!
        sortAndFilterSkills()

        title = findViewById(R.id.title)

        xp = findViewById(R.id.addskill_xp)
        pp = findViewById(R.id.addskill_pp)
        ft1s = findViewById(R.id.addskill_ft1s)
        inf = findViewById(R.id.addskill_inf)

        sortSpinner = findViewById(R.id.addskill_sort)
        filterSpinner = findViewById(R.id.addskill_filter)
        searchBar = findViewById(R.id.addskill_searchview)

        layout = findViewById(R.id.addskill_layout)

        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillSortType.getAllStrings())
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSort = SkillSortType.getTypeForString(sortSpinner.getItemAtPosition(position).toString())
                sortAndFilterSkills()
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, SkillFilterType.getAllStrings())
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = SkillFilterType.getTypeForString(filterSpinner.getItemAtPosition(position).toString())
                sortAndFilterSkills()
                buildView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        searchBar.addTextChangedListener {
            sortAndFilterSkills()
            buildView()
        }

        buildView()
    }

    private fun sortAndFilterSkills() {
        skills = character.allPurchaseableSkills(searchBar.text.toString().trim().lowercase(), currentFilter).sort(currentSort)
    }

    private fun buildView() {
        val isPlanned = character.characterType() == CharacterType.PLANNER
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Add${isPlanned.ternary(" Planned", "")} Skills")

        xp.text = "Experience\n${player.experience}"
        pp.text = "Prestige\n${player.prestigePoints}"
        ft1s.text = "Free T1 Skills\n${player.freeTier1Skills}"
        inf.text = "Infection\n${character.infection}%"

        amountsLayout.isGone = isPlanned

        layout.removeAllViews()

        skills.forEachIndexed { index, skill ->
            val cell = SkillCell(this)
            cell.purchaseButton.setLoading(this@AddSkillActivity.loading || DataManager.shared.loading)
            cell.setupForPurchase(skill, player) {
                this@AddSkillActivity.loading = true
                character.attemptToPurchaseSkill(lifecycleScope, it) { succeeded ->
                    if (succeeded) {
                        DataManager.shared.load(lifecycleScope) {
                            this@AddSkillActivity.character = DataManager.shared.getCharacter(character.id)!!
                            this@AddSkillActivity.sortAndFilterSkills()
                            this@AddSkillActivity.loading = false
                            this@AddSkillActivity.buildView()
                            DataManager.shared.callUpdateCallback(SkillsListActivity::class)
                        }
                    } else {
                        this@AddSkillActivity.loading = false
                        this@AddSkillActivity.buildView()
                    }
                }
            }
            cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            layout.addView(cell)
        }
    }

}