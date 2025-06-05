package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class ViewCharacterStatsActivity : NoStatusBarActivity() {

    private lateinit var title: TextView
    private lateinit var name: KeyValueView
    private lateinit var player: KeyValueView
    private lateinit var startDate: KeyValueView
    private lateinit var eventsAttended: KeyValueView
    private lateinit var infection: KeyValueView
    private lateinit var bullets: KeyValueView
    private lateinit var megas: KeyValueView
    private lateinit var rivals: KeyValueView
    private lateinit var rockets: KeyValueView
    private lateinit var bulletCasings: KeyValueView
    private lateinit var clothSupplies: KeyValueView
    private lateinit var woodSupplies: KeyValueView
    private lateinit var metalSupplies: KeyValueView
    private lateinit var techSupplies: KeyValueView
    private lateinit var medicalSupplies: KeyValueView

    private lateinit var numSkills: KeyValueView
    private lateinit var spentXp: KeyValueView
    private lateinit var spentFt1s: KeyValueView
    private lateinit var spentPp: KeyValueView

    private lateinit var armor: KeyValueView
    private lateinit var mysteriousStranger: KeyValueView
    private lateinit var unshakableResolve: KeyValueView

    private lateinit var charId: KeyValueView

    private lateinit var character: FullCharacterModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_character_stats)
        setupView()
    }

    private fun setupView() {
        character = DataManager.shared.getPassedData(ViewPlayerActivity::class, DataManagerPassedDataKey.SELECTED_CHARACTER)!!

        title = findViewById(R.id.charstats_title)
        name = findViewById(R.id.charstats_name)
        player = findViewById(R.id.charstats_player)
        startDate = findViewById(R.id.charstats_startdate)
        eventsAttended = findViewById(R.id.charstats_numEvents)
        infection = findViewById(R.id.charstats_infection)
        bullets = findViewById(R.id.charstats_bullets)
        megas = findViewById(R.id.charstats_megas)
        rivals = findViewById(R.id.charstats_rivals)
        rockets = findViewById(R.id.charstats_rockets)
        bulletCasings = findViewById(R.id.charstats_bulletCasings)
        clothSupplies = findViewById(R.id.charstats_cloth)
        woodSupplies = findViewById(R.id.charstats_wood)
        metalSupplies = findViewById(R.id.charstats_metal)
        techSupplies = findViewById(R.id.charstats_tech)
        medicalSupplies = findViewById(R.id.charstats_medical)
        numSkills = findViewById(R.id.charstats_numSkills)
        spentXp = findViewById(R.id.charstats_spentXp)
        spentFt1s = findViewById(R.id.charstats_spentFt1s)
        spentPp = findViewById(R.id.charstats_spentPp)
        armor = findViewById(R.id.charstats_armor)
        mysteriousStranger = findViewById(R.id.charstats_mysteriousStranger)
        unshakableResolve = findViewById(R.id.charstats_unshakableResolve)
        charId = findViewById(R.id.charstats_charId)

        buildView()
    }

    private fun buildView() {
        DataManager.shared.setTitleTextPotentiallyOffline(title, "Character Stats")

        name.set(character.fullName)
        player.set(DataManager.shared.players.first { it.id == character.playerId }.fullName)
        startDate.set(character.startDate.yyyyMMddToMonthDayYear())
        eventsAttended.set(character.eventAttendees.count())
        var threshold = ""
        val infThresholds = character.infection.toInt() / 25
        if (infThresholds > 0) {
            threshold = " (Threshold $infThresholds)"
        }
        infection.set("${character.infection}%$threshold")

        bullets.set(character.bullets)
        megas.set(character.megas)
        rivals.set(character.rivals)
        rockets.set(character.rockets)

        bulletCasings.set(character.bulletCasings)
        clothSupplies.set(character.clothSupplies)
        woodSupplies.set(character.woodSupplies)
        metalSupplies.set(character.metalSupplies)
        techSupplies.set(character.techSupplies)
        medicalSupplies.set(character.medicalSupplies)

        val skills = character.allPurchasedSkills()
        val cskills = skills.filter { it.skillTypeId == Constants.SkillTypes.combat }.count()
        val tskills = skills.filter { it.skillTypeId == Constants.SkillTypes.talent }.count()
        val pskills = skills.filter { it.skillTypeId == Constants.SkillTypes.profession }.count()

        numSkills.set("${skills.count()}\n$cskills Combat\n$tskills Talent\n$pskills Profession")
        spentXp.set(character.getSpentXp())
        spentFt1s.set(character.getSpentFt1s())
        spentPp.set(character.getSpentPp())

        armor.set(character.armor)
        mysteriousStranger.set("${character.mysteriousStrangerUses} / ${character.mysteriousStrangerCount()}")
        unshakableResolve.set("${character.unshakableResolveUses} / ${character.hasUnshakableResolve().ternary("1", "0")}")
        charId.set(character.id)

        if (DataManager.shared.getCurrentPlayer()?.isAdmin == false) {
            charId.isGone = !DataManager.shared.playerIsCurrentPlayer(character.id)
        } else {
            charId.isGone = false
        }
    }
}