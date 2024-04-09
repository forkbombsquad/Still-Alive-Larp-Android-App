package com.forkbombsquad.stillalivelarp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreen

class ViewBioActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var edit: NavArrowButtonGreen
    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bio)
        setupView()
    }

    private fun setupView() {
        title = findViewById(R.id.bio_title)
        edit = findViewById(R.id.bio_edit)
        text = findViewById(R.id.bio_text)

        edit.setOnClick {
            edit.setLoading(true)
            DataManager.shared.unrelaltedUpdateCallback = {
                DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CHARACTER), true) {
                    edit.setLoading(false)
                    DataManager.shared.charForSelectedPlayer = DataManager.shared.character
                    buildView()
                }
            }
            val intent = Intent(this, EditBioActivity::class.java)
            startActivity(intent)
        }

        buildView()
    }

    private fun buildView() {
        val player = DataManager.shared.selectedPlayer
        val char = DataManager.shared.charForSelectedPlayer

        edit.isGone = player?.id != DataManager.shared.player?.id

        title.text = "${char?.fullName ?: ""}'s\nBio"
        text.text = char?.bio ?: ""
    }
}