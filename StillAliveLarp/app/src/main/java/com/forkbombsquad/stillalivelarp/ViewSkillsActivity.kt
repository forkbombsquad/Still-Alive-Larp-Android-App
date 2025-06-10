package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.account.AddSkillActivity
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class ViewSkillsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var addNewButton: Button
    private lateinit var skillListLayout: LinearLayout

    private lateinit var loadingLayout: LoadingLayout

    private lateinit var character: FullCharacterModel
    private lateinit var skills: List<FullCharacterModifiedSkillModel>

    private val sourceClasses: List<KClass<*>> = listOf(MyAccountFragment::class, ViewPlayerActivity::class, ViewCharacterActivity::class, CharacterPlannerActivity::class, ManageNPCActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_skills)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.SELECTED_CHARACTER)!!
        skills = character.allPurchasedSkills().sortedBy { it.name }

        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.skills_title)
        searchBar = findViewById(R.id.skills_searchview)
        addNewButton = findViewById(R.id.skills_addnew)
        skillListLayout = findViewById(R.id.skills_layout)

        addNewButton.setOnClickListener {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
                DataManager.shared.load(lifecycleScope) {
                    DataManager.shared.callUpdateCallbacks(sourceClasses)
                }
            }
            val intent = Intent(this, AddSkillActivity::class.java)
            startActivity(intent)
        }

        searchBar.addTextChangedListener {
            buildView()
        }
        reload()
    }


    private fun reload() {
        DataManager.shared.load(lifecycleScope, stepFinished = {
            buildView()
        }, finished = {
            character = DataManager.shared.getCharacter(character.id)!!
            skills = character.getPurchasedSkillsFiltered(searchBar.text.toString().trim(), SkillFilterType.NONE).sortedBy { it.name }
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}'s${(character.characterType() == CharacterType.PLANNER).ternary(" Planned", "")} Skills")
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(searchBar, addNewButton, skillListLayout)) {
            addNewButton.isGone = !(DataManager.shared.playerIsCurrentPlayer(character.playerId) && character.isAlive)
            if (DataManager.shared.offlineMode) {
                addNewButton.isGone = true
            }

            skillListLayout.removeAllViews()
            skills.forEachIndexed { index, skill ->
                val cell = SkillCell(this)
                cell.setup(skill)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                skillListLayout.addView(cell)
            }
        }
    }
}