package com.forkbombsquad.stillalivelarp

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import com.forkbombsquad.stillalivelarp.services.utils.nativeskilltree.SkillGrid

class OfflinePersonalNativeSkillTreeActivity : NoStatusBarActivity() {

    private lateinit var img: TouchImageView
    private lateinit var paint: Paint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_personal_native_skill_tree)
        setupView()
    }

    private var scaleFactor = 1f
    private var minScale = 0.1f
    private var maxScale = 100f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    private var activePointerId = -1

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        img = findViewById(R.id.nativeskilltree_img)

        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 10F
        img.invalidate()
        renderSkills()
    }

    private fun renderSkills() {
        DataManager.shared.charForSelectedPlayer = SharedPrefsManager.shared.getCharacter()
        img.updateDrawables(
            SkillGrid(
                DataManager.shared.skills!!,
                DataManager.shared.skillCategories!!.asList(),
                personal = true,
                allowPurchase = false
            ),
            lifecycleScope
        )
        img.invalidate()
    }

}