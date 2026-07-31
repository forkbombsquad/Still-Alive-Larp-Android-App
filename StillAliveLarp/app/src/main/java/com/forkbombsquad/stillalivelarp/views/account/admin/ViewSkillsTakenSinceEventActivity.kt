package com.forkbombsquad.stillalivelarp.views.account.admin

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullEventModel
import com.forkbombsquad.stillalivelarp.utils.KeyValueViewBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.shared.EventsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.ViewCharacterActivity

class ViewSkillsTakenSinceEventActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var includeFreeSkills: CheckBox
    private lateinit var includePlayers: CheckBox
    private lateinit var includeNPCs: CheckBox
    private lateinit var includeDead: CheckBox
    private lateinit var innerLayout: LinearLayout

    private lateinit var event: FullEventModel
    private lateinit var allChars: List<FullCharacterModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_skills_taken_since_event)
        setupView()
    }
    private fun setupView() {
        title = findViewById(R.id.skillstakensince_title)
        searchBar = findViewById(R.id.skillstakensince_searchview)
        includeFreeSkills = findViewById(R.id.skillstakensince_includeFreeSkills)
        includePlayers = findViewById(R.id.skillstakensince_includePlayers)
        includeNPCs = findViewById(R.id.skillstakensince_includeNPCs)
        includeDead = findViewById(R.id.skillstakensince_includeDead)
        innerLayout = findViewById(R.id.skillstakensince_innerlayout)

        searchBar.addTextChangedListener {
            buildView()
        }

        includeFreeSkills.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includePlayers.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeNPCs.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeDead.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        event = DataManager.shared.getPassedData(EventsListActivity::class, DataManagerPassedDataKey.SELECTED_EVENT)!!
        allChars = DataManager.shared.getAllCharacters(listOf(CharacterType.STANDARD, CharacterType.NPC))

        buildView()
    }

    private fun buildView() {
        val chars = getCharacters()
        val skillsSinceEvent = getSkillsSinceEvent(chars, event)
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Skills Taken Since:\n${event.title}")
        innerLayout.removeAllViews()

        skillsSinceEvent.entries.sortedWith(compareByDescending<Map.Entry<FullCharacterModifiedSkillModel, Int>> { it.value }.thenBy { it.key.name }).associate { it.key to it.value }.entries.forEachIndexed { index, (key, value) ->
            val keyValueView = KeyValueViewBuildable(this@ViewSkillsTakenSinceEventActivity)
            keyValueView.set(key.name, "Num Chars: $value")
            keyValueView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            keyValueView.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            innerLayout.addView(keyValueView)
        }

    }

    private fun getSkillsSinceEvent(characters: List<FullCharacterModel>, event: FullEventModel): Map<FullCharacterModifiedSkillModel, Int> {
        return characters
            .flatMap { it.getSkillsTakenSinceEvent(event).filter { skl -> includeFreeSkills.isChecked.ternary(true, otherwise = skl.baseXpCost() > 0 ) } }
            .groupBy { it.id } // Make sure it's using id as the unique identifier
            .mapKeys { (_, skills) -> skills.first() } // Maps the first skill to the slot since they could be different
            .mapValues { (_, skills) -> skills.size } // Total number of times each skill appears across all chars
            .filter { it.key.includeInFilter(searchBar.text.toString(), SkillFilterType.NONE) }
    }

    private fun getCharacters(): List<FullCharacterModel> {
        return if (includePlayers.isChecked && includeNPCs.isChecked) {
            includeDead.isChecked.ternary(allChars, otherwise = allChars.filter { it.isAlive })
        } else if (includePlayers.isChecked) {
            includeDead.isChecked.ternary(allChars.filter { it.characterType() == CharacterType.STANDARD }, otherwise = allChars.filter { it.characterType() == CharacterType.STANDARD }.filter { it.isAlive })
        } else if (includeNPCs.isChecked) {
            includeDead.isChecked.ternary(allChars.filter { it.characterType() == CharacterType.NPC }, otherwise = allChars.filter { it.characterType() == CharacterType.NPC }.filter { it.isAlive })
        } else {
            listOf()
        }
    }

}