package com.forkbombsquad.stillalivelarp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.AlertButton
import com.forkbombsquad.stillalivelarp.utils.AlertUtils
import com.forkbombsquad.stillalivelarp.utils.ButtonType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlackBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlueBuildable
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonGreenBuildable
import com.forkbombsquad.stillalivelarp.utils.ifLet

class CharacterPlannerActivity : NoStatusBarActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_planner)
        setupView()
    }

    private fun setupView() {
        progressBar = findViewById(R.id.characterplanner_loading)
        layout = findViewById(R.id.characterplanner_layout)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.ALL_PLANNED_CHARACTERS), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        progressBar.isGone = !DataManager.shared.loadingAllPlannedCharacters
        layout.removeAllViews()
        DataManager.shared.allPlannedCharacters.ifLet { chars ->
            chars.forEach { char ->
                val navarrow = NavArrowButtonBlueBuildable(this)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 16)
                navarrow.layoutParams = params

                navarrow.textView.text = char.fullName
                navarrow.setOnClick {
                    // TODO
                }
                layout.addView(navarrow)
            }
            val navarrow = NavArrowButtonGreenBuildable(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 16, 0, 16)
            navarrow.layoutParams = params

            navarrow.textView.text = "Start A New Plan"
            navarrow.setOnClick {
                // TODO need to figure out how to add a picker to an alert
                AlertUtils.displayMessage(this, "Creating Plan", "Start fresh or from an existing Character or Plan?", buttons.toTypedArray())
            }
            layout.addView(navarrow)
        }
    }
}