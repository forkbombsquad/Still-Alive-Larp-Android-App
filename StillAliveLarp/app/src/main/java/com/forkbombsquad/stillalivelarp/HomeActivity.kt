package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManagerType
import com.forkbombsquad.stillalivelarp.tabbar_fragments.CommunityFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.HomeFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.RulesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : NoStatusBarActivity() {

    private var currentTab = 0

    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        loadFragment(HomeFragment())

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    currentTab = 0
                    loadHomeFrag()
                }
                R.id.nav_rules -> {
                    currentTab = 1
                    loadFragment(RulesFragment())
                }
                R.id.nav_community -> {
                    currentTab = 2
                    loadFragment(CommunityFragment())
                }
                R.id.nav_myaccount -> {
                    currentTab = 3
                    loadFragment(MyAccountFragment())
                }
            }
            true
        }

        loadHomeFragServices()
    }

    private fun loadHomeFragServices() {
        OldDataManager.shared.loadingEventPreregs = true
        OldDataManager.shared.loadingSelectedCharacterGear = true
        OldDataManager.shared.loadingIntrigue = true
        OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.PLAYER, OldDataManagerType.CHARACTER, OldDataManagerType.ANNOUNCEMENTS, OldDataManagerType.EVENTS, OldDataManagerType.AWARDS, OldDataManagerType.SKILLS, OldDataManagerType.FEATURE_FLAGS, OldDataManagerType.ALL_CHARACTERS, OldDataManagerType.ALL_NPC_CHARACTERS), true, finishedStep = {
            loadHomeFrag()
        }) {
            OldDataManager.shared.selectedChar = OldDataManager.shared.character?.getBaseModel()
            OldDataManager.shared.load(lifecycleScope, listOf(OldDataManagerType.EVENT_PREREGS, OldDataManagerType.SELECTED_CHARACTER_GEAR, OldDataManagerType.INTRIGUE), true) {
                loadHomeFrag()
            }
        }
    }

    private fun loadHomeFrag() {
        if (currentTab == 0) {
            loadFragment(HomeFragment.newInstance())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}