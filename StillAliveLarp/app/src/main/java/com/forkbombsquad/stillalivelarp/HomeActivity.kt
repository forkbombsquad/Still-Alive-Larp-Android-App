package com.forkbombsquad.stillalivelarp

import android.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.AwardService
import com.forkbombsquad.stillalivelarp.services.IntrigueService
import com.forkbombsquad.stillalivelarp.services.PlayerService
import com.forkbombsquad.stillalivelarp.services.managers.*
import com.forkbombsquad.stillalivelarp.services.models.*
import com.forkbombsquad.stillalivelarp.services.utils.IdSP
import com.forkbombsquad.stillalivelarp.tabbar_fragments.CommunityFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.HomeFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.MyAccountFragment
import com.forkbombsquad.stillalivelarp.tabbar_fragments.RulesFragment
import com.forkbombsquad.stillalivelarp.utils.ViewUtils
import com.forkbombsquad.stillalivelarp.utils.globalGetContext
import com.forkbombsquad.stillalivelarp.utils.globalPrint
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch

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
        DataManager.shared.load(lifecycleScope, listOf(DataManagerType.PLAYER, DataManagerType.CHARACTER, DataManagerType.ANNOUNCEMENTS, DataManagerType.EVENTS, DataManagerType.AWARDS, DataManagerType.INTRIGUE, DataManagerType.SKILLS, DataManagerType.FEATURE_FLAGS), true) {
            DataManager.shared.selectedChar = DataManager.shared.character?.getBaseModel()
            DataManager.shared.load(lifecycleScope, listOf(DataManagerType.EVENT_PREREGS, DataManagerType.SELECTED_CHARACTER_GEAR), true) {
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