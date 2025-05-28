package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.core.view.isGone

import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class OfflineCharacterStatsActivity : NoStatusBarActivity() {

    private lateinit var name: KeyValueView
    private lateinit var player: KeyValueView
    private lateinit var startDate: KeyValueView
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
    private lateinit var armor: KeyValueView
    private lateinit var mysteriousStranger: KeyValueView
    private lateinit var unshakableResolve: KeyValueView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_character_stats)
        setupView()
    }

    private fun setupView() {
        name = findViewById(R.id.offlinecharacterstats_name)
        player = findViewById(R.id.offlinecharacterstats_player)
        startDate = findViewById(R.id.offlinecharacterstats_startdate)
        infection = findViewById(R.id.offlinecharacterstats_infection)
        bullets = findViewById(R.id.offlinecharacterstats_bullets)
        megas = findViewById(R.id.offlinecharacterstats_megas)
        rivals = findViewById(R.id.offlinecharacterstats_rivals)
        rockets = findViewById(R.id.offlinecharacterstats_rockets)
        bulletCasings = findViewById(R.id.offlinecharacterstats_bulletCasings)
        clothSupplies = findViewById(R.id.offlinecharacterstats_cloth)
        woodSupplies = findViewById(R.id.offlinecharacterstats_wood)
        metalSupplies = findViewById(R.id.offlinecharacterstats_metal)
        techSupplies = findViewById(R.id.offlinecharacterstats_tech)
        medicalSupplies = findViewById(R.id.offlinecharacterstats_medical)
        armor = findViewById(R.id.offlinecharacterstats_armor)
        mysteriousStranger = findViewById(R.id.offlinecharacterstats_mysteriousStranger)
        unshakableResolve = findViewById(R.id.offlinecharacterstats_unshakableResolve)

        buildView()
    }

    private fun buildView() {
        OldDataManager.shared.charForSelectedPlayer.ifLet({
            name.isGone = false
            player.isGone = false
            startDate.isGone = false
            infection.isGone = false
            bullets.isGone = false
            megas.isGone = false
            rivals.isGone = false
            rockets.isGone = false
            bulletCasings.isGone = false
            clothSupplies.isGone = false
            woodSupplies.isGone = false
            metalSupplies.isGone = false
            techSupplies.isGone = false
            medicalSupplies.isGone = false
            armor.isGone = false
            mysteriousStranger.isGone = it.mysteriousStrangerCount() == 0
            unshakableResolve.isGone = !it.hasUnshakableResolve()

            name.set(it.fullName)
            player.set(OldDataManager.shared.selectedPlayer?.fullName ?: "")
            startDate.set(it.startDate.yyyyMMddToMonthDayYear())
            infection.set("${it.infection}%")
            bullets.set(it.bullets)
            megas.set(it.megas)
            rivals.set(it.rivals)
            rockets.set(it.rockets)
            bulletCasings.set(it.bulletCasings)
            clothSupplies.set(it.clothSupplies)
            woodSupplies.set(it.woodSupplies)
            metalSupplies.set(it.metalSupplies)
            techSupplies.set(it.techSupplies)
            medicalSupplies.set(it.medicalSupplies)
            armor.set(it.armor)
            mysteriousStranger.set("Mysterious Stranger Uses (max ${it.mysteriousStrangerCount()})", it.mysteriousStrangerUses)
            unshakableResolve.set(it.unshakableResolveUses)

        }, {
            name.isGone = true
            player.isGone = true
            startDate.isGone = true
            infection.isGone = true
            bullets.isGone = true
            megas.isGone = true
            rivals.isGone = true
            rockets.isGone = true
            bulletCasings.isGone = true
            clothSupplies.isGone = true
            woodSupplies.isGone = true
            metalSupplies.isGone = true
            techSupplies.isGone = true
            medicalSupplies.isGone = true
            armor.isGone = true
            mysteriousStranger.isGone = true
            unshakableResolve.isGone = true
        })
    }
}