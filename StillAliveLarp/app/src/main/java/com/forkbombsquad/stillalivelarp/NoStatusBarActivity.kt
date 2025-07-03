package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.forkbombsquad.stillalivelarp.utils.StillAliveLarpApplication

open class NoStatusBarActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        StillAliveLarpApplication.setCurrentActivty(this)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applyFitsSystemWindowsToRoot()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        applyFitsSystemWindowsToRoot()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        applyFitsSystemWindowsToRoot()
    }

    private fun applyFitsSystemWindowsToRoot() {
        val content = findViewById<ViewGroup>(android.R.id.content)
        val rootView = content.getChildAt(0) ?: return

        // Option 1: Try to set fitsSystemWindows attribute programmatically
        rootView.fitsSystemWindows = true

        // Option 2: Alternatively, if fitsSystemWindows isn't enough on its own,
        // you can add top padding equal to status bar height like this:
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBarsInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        ViewCompat.requestApplyInsets(rootView)
    }

}

inline fun <reified T : NoStatusBarActivity> activityName(): String {
    return T::class.simpleName ?: "UnnamedActivity"
}