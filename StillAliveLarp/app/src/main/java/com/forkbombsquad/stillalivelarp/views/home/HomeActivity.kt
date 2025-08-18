package com.forkbombsquad.stillalivelarp.views.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.views.community.CommunityFragment
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.views.rules.RulesFragment
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
        DataManager.shared.load(lifecycleScope) {
            loadHomeFrag()
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