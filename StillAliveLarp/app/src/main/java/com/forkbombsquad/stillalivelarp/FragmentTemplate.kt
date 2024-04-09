package com.forkbombsquad.stillalivelarp

import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerType
import com.forkbombsquad.stillalivelarp.utils.NavArrowButtonBlack

class FragmentTemplate : Fragment() {

    private val TAG = "FRAGMENT_TEMPLATE_FRAGMENT"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_template, container, false)
        setupView(v)
        return v
    }

    private fun setupView(v: View) {
        DataManager.shared.load(lifecycleScope, listOf(), false) {
            buildView()
        }
        buildView()
    }

    private fun buildView() {

    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentTemplate()
    }
}