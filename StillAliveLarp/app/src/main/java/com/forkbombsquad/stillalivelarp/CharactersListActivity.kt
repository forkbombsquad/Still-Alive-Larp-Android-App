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
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.alphabetized
import com.forkbombsquad.stillalivelarp.utils.ternary
import kotlin.reflect.KClass

class CharactersListActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var innerLayout: LinearLayout

    private lateinit var destClass: KClass<*>
    private lateinit var characters: List<FullCharacterModel>
    private lateinit var viewTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_characters_list)
        setupView()
    }

    private fun setupView() {
        val sourceClasses: List<KClass<*>> = listOf(ViewPlayerActivity::class, MyAccountFragment::class, AdminPanelActivity::class)
        destClass = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.DESTINATION_CLASS)!!
        characters = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.CHARACTER_LIST)!!
        viewTitle = DataManager.shared.getPassedData(sourceClasses, DataManagerPassedDataKey.VIEW_TITLE)!!

        title = findViewById(R.id.charlist_title)
        innerLayout = findViewById(R.id.charlist_innerlayout)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, viewTitle)
        innerLayout.removeAllViews()
        characters.alphabetized().forEachIndexed { index, character ->
            val playerName = DataManager.shared.players.first { character.playerId == it.id }.fullName
            val arrow = NavArrowButtonBlackBuildable(this)
            arrow.textView.text = "${character.fullName} - ($playerName)"
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, (index == 0).ternary(32, 16), 0, 16)
            arrow.layoutParams = params
            arrow.setLoading(false)
            arrow.setOnClick {
                DataManager.shared.addActivityToClose(this)
                DataManager.shared.setPassedData(this::class, DataManagerPassedDataKey.SELECTED_CHARACTER, character)
                val intent = Intent(this, destClass.java)
                startActivity(intent)
            }
            innerLayout.addView(arrow)
        }
    }
}