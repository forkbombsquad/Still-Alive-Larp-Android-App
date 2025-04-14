package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.CharacterSkillService
import com.forkbombsquad.stillalivelarp.services.managers.CharacterManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.launch

class ManageNPCActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var manageStats: NavArrowButtonBlack
    private lateinit var manageSkills: NavArrowButtonBlack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_npcactivity)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.mannpc_title)
        manageStats = findViewById(R.id.mannpc_managestats)
        manageSkills = findViewById(R.id.mannpc_manageskills)

        manageStats.setOnClick {
            // TODO
        }

        manageSkills.setOnClick {
            manageSkills.setLoading(true)
            CharacterManager.shared.fetchFullCharacter(lifecycleScope, DataManager.shared.selectedNPCCharacter!!.id) { fullCharacter ->
                DataManager.shared.selectedPlannedCharacter = fullCharacter
                val request = CharacterSkillService.GetAllCharacterSkillsForCharacter()
                lifecycleScope.launch {
                    request.successfulResponse(IdSP(DataManager.shared.selectedNPCCharacter!!.id)).ifLet { charSkills ->
                        DataManager.shared.selectedPlannedCharacterCharSkills = charSkills.charSkills.toList()
                        val intent = Intent(this@ManageNPCActivity, NPCSkillListActivity::class.java)
                        manageSkills.setLoading(false)
                        startActivity(intent)
                    }
                }
            }
        }

        DataManager.shared.load(lifecycleScope, listOf(), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        title.text = "Manage NPC\n" + (DataManager.shared.selectedNPCCharacter?.fullName ?: "")
    }
}