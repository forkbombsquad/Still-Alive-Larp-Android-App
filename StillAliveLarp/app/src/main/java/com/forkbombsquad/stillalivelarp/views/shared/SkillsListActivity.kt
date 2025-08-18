package com.forkbombsquad.stillalivelarp.views.shared

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.utils.LoadingButton
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.views.rules.RulesFragment
import com.forkbombsquad.stillalivelarp.views.account.AddSkillActivity
import com.forkbombsquad.stillalivelarp.views.account.CharacterPlannerActivity
import com.forkbombsquad.stillalivelarp.views.account.admin.ManageNPCActivity
import com.forkbombsquad.stillalivelarp.utils.LoadingLayout
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.SkillCell
import com.forkbombsquad.stillalivelarp.utils.SkillFilterType
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.views.account.admin.DeleteSkillsActivity
import kotlin.reflect.KClass

class SkillsListActivity : NoStatusBarActivity() {

    enum class SkillsListActivityActions {
        NO_DELETE,
        ALLOW_DELETE
    }

    private lateinit var title: TextView
    private lateinit var searchBar: EditText
    private lateinit var addNewButton: Button
    private lateinit var skillListLayout: LinearLayout
    private lateinit var deleteButton: LoadingButton

    private lateinit var loadingLayout: LoadingLayout

    private var character: FullCharacterModel? = null
    private lateinit var skills: List<FullCharacterModifiedSkillModel>
    private var titleString: String? = null
    private lateinit var action: SkillsListActivityActions

    private val sourceClasses: List<KClass<*>> = listOf(MyAccountFragment::class, ViewPlayerActivity::class, ViewCharacterActivity::class, CharacterPlannerActivity::class, ManageNPCActivity::class, RulesFragment::class, ViewNPCStuffActivity::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skills_list)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.SELECTED_CHARACTER)
        character.ifLet ({ char ->
            skills = char.allPurchasedSkills().sortedBy { it.name }
        }, {
            skills = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.SKILL_LIST)!!
            skills = skills.sortedBy { it.name }
            titleString = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.VIEW_TITLE)!!
        })
        action = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.ACTION) ?: SkillsListActivityActions.NO_DELETE

        loadingLayout = findViewById(R.id.loadinglayout)

        title = findViewById(R.id.skills_title)
        searchBar = findViewById(R.id.skills_searchview)
        addNewButton = findViewById(R.id.skills_addnew)
        skillListLayout = findViewById(R.id.skills_layout)
        deleteButton = findViewById(R.id.skills_deleteButton)

        addNewButton.setOnClickListener {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character!!)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
                DataManager.shared.load(lifecycleScope) {
                    DataManager.shared.callUpdateCallbacks(sourceClasses)
                }
            }
            val intent = Intent(this, AddSkillActivity::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClick {
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character!!)
            DataManager.shared.setUpdateCallback(this::class) {
                reload()
                DataManager.shared.load(lifecycleScope) {
                    DataManager.shared.callUpdateCallbacks(sourceClasses)
                }
            }
            DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.ACTION, DeleteSkillsActivity.DeleteSkillsActivityActionType.JUST_DELETE)
            val intent = Intent(this, DeleteSkillsActivity::class.java)
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
            character.ifLet { char ->
                character = DataManager.shared.getCharacter(char.id)!!
                skills = character!!.getPurchasedSkillsFiltered(searchBar.text.toString().trim(), SkillFilterType.NONE).sortedBy { it.name }
            }
            buildView()
        })
        buildView()
    }

    private fun buildView() {
        character.ifLet({ character ->
            DataManager.shared.setTitleTextPotentiallyOffline(title, "${character.fullName}'s${(character.characterType() == CharacterType.PLANNER).ternary(" Planned", "")} Skills")
        }, {
            DataManager.shared.setTitleTextPotentiallyOffline(title, titleString ?: "Skills")
        })
        DataManager.shared.handleLoadingTextAndHidingViews(loadingLayout, listOf(searchBar, addNewButton, skillListLayout, deleteButton)) {
            if (character != null) {
                addNewButton.isGone = !(DataManager.shared.playerIsCurrentPlayer(character!!.playerId) && character!!.isAlive)
                deleteButton.isGone = !(action == SkillsListActivityActions.ALLOW_DELETE && DataManager.shared.playerIsCurrentPlayer(character!!.playerId) && character!!.isAlive)
            }

            if (DataManager.shared.offlineMode || character == null) {
                addNewButton.isGone = true
                deleteButton.isGone = true
            }

            skillListLayout.removeAllViews()
            skills.filter { it.includeInFilter(searchBar.text.toString(), SkillFilterType.NONE) }.forEachIndexed { index, skill ->
                val cell = SkillCell(this)
                cell.setup(skill)
                cell.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                cell.setPadding(8, (index == 0).ternary(32, 16), 8, 16)
                skillListLayout.addView(cell)
            }
        }
    }
}