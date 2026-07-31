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
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.PercentageCellBuildable
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.shared.SkillsListActivity
import com.forkbombsquad.stillalivelarp.views.shared.ViewCharacterActivity
import kotlin.math.truncate

class ViewSkillAggregationActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var includePlayers: CheckBox
    private lateinit var includeNPCs: CheckBox
    private lateinit var includeDead: CheckBox
    private lateinit var innerLayout: LinearLayout

    private lateinit var skill: FullCharacterModifiedSkillModel
    private lateinit var allChars: List<FullCharacterModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_skill_aggregation)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.skillag_title)
        searchBar = findViewById(R.id.skillag_searchview)
        includePlayers = findViewById(R.id.skillag_includePlayers)
        includeNPCs = findViewById(R.id.skillag_includeNPCs)
        includeDead = findViewById(R.id.skillag_includeDead)
        innerLayout = findViewById(R.id.skillag_innerlayout)

        searchBar.addTextChangedListener {
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

        skill = DataManager.shared.getPassedData(SkillsListActivity::class, DataManagerPassedDataKey.SELECTED_SKILL)!!
        allChars = DataManager.shared.getAllCharacters(listOf(CharacterType.STANDARD, CharacterType.NPC))

        buildView()
    }

    private fun buildView() {
        val chars = getCharacters()
        val charsWithSkill = getCharsWithSkill(chars, skill)
        DataManager.shared.setTitleTextPotentiallyOffline(title, "All Characters That Have:\n${skill.name}")
        innerLayout.removeAllViews()

        charsWithSkill.sortedBy { it.fullName }.forEachIndexed { index, character ->
            val playerName = DataManager.shared.getPlayerForCharacter(character).fullName
            val arrow = NavArrowButtonBlackBuildable(this@ViewSkillAggregationActivity)
            arrow.textView.text = "${character.fullName} - ($playerName)"
            arrow.setOnClick {
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
                val intent = Intent(this, ViewCharacterActivity::class.java)
                startActivity(intent)
            }
            arrow.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            arrow.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
            innerLayout.addView(arrow)
        }

    }

    private fun getCharsWithSkill(characters: List<FullCharacterModel>, skill: FullCharacterModifiedSkillModel): List<FullCharacterModel> {
        return characters.filter { char -> char.allPurchasedSkills().any { it.id == skill.id } }
    }

    private fun getCharacters(): List<FullCharacterModel> {
        var chars = allChars
        val searchText = searchBar.text.toString().trim()
        if (searchText.isNotEmpty()) {
            chars = chars.filter { it.fullName.contains(searchText) }
        }
        return if (includePlayers.isChecked && includeNPCs.isChecked) {
            includeDead.isChecked.ternary(chars, otherwise = chars.filter { it.isAlive })
        } else if (includePlayers.isChecked) {
            includeDead.isChecked.ternary(chars.filter { it.characterType() == CharacterType.STANDARD }, otherwise = chars.filter { it.characterType() == CharacterType.STANDARD }.filter { it.isAlive })
        } else if (includeNPCs.isChecked) {
            includeDead.isChecked.ternary(chars.filter { it.characterType() == CharacterType.NPC }, otherwise = chars.filter { it.characterType() == CharacterType.NPC }.filter { it.isAlive })
        } else {
            listOf()
        }
    }

}