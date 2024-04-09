package com.forkbombsquad.stillalivelarp.tabbar_fragments.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.FragmentTemplate
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.ifLet
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear

class CharacterStatsFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_character_stats, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        name = v.findViewById(R.id.characterstats_name)
        player = v.findViewById(R.id.characterstats_player)
        startDate = v.findViewById(R.id.characterstats_startdate)
        infection = v.findViewById(R.id.characterstats_infection)
        bullets = v.findViewById(R.id.characterstats_bullets)
        megas = v.findViewById(R.id.characterstats_megas)
        rivals = v.findViewById(R.id.characterstats_rivals)
        rockets = v.findViewById(R.id.characterstats_rockets)
        bulletCasings = v.findViewById(R.id.characterstats_bulletCasings)
        clothSupplies = v.findViewById(R.id.characterstats_cloth)
        woodSupplies = v.findViewById(R.id.characterstats_wood)
        metalSupplies = v.findViewById(R.id.characterstats_metal)
        techSupplies = v.findViewById(R.id.characterstats_tech)
        medicalSupplies = v.findViewById(R.id.characterstats_medical)
        armor = v.findViewById(R.id.characterstats_armor)
        mysteriousStranger = v.findViewById(R.id.characterstats_mysteriousStranger)
        unshakableResolve = v.findViewById(R.id.characterstats_unshakableResolve)

        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.CHAR_FOR_SELECTED_PLAYER), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {
        DataManager.shared.charForSelectedPlayer.ifLet({
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
            player.set(DataManager.shared.selectedPlayer?.fullName ?: "")
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

    companion object {
        @JvmStatic
        fun newInstance() = CharacterStatsFragment()
    }
}