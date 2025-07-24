package com.forkbombsquad.stillalivelarp.utils

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModifiedSkillModel
import com.forkbombsquad.stillalivelarp.services.models.FullPlayerModel
import com.forkbombsquad.stillalivelarp.services.models.FullSkillModel
import com.forkbombsquad.stillalivelarp.services.models.PlayerModel
import com.forkbombsquad.stillalivelarp.services.models.XpReductionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class DropdownSpinner(context: Context): LinearLayout(context) {

    val title: TextView
    val spinner: Spinner

    init {
        inflate(context, R.layout.dropdownspinner, this)
        title = findViewById(R.id.dropdownspinner_title)
        spinner = findViewById(R.id.dropdownspinner_spinner)
    }

    fun setup(context: Context, titleText: String, options: List<String>, onItemSelected: () -> Unit) {
        title.text = titleText
        setOptions(context, options)
        setOnItemSelectedListener(onItemSelected)
    }

    fun setOptions(context: Context, options: List<String>) {
        val filterAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = filterAdapter
    }

    fun getSelectedItem(): String {
        return spinner.selectedItem.toString()
    }

    fun setSelectedItem(index: Int) {
        spinner.setSelection(index)
    }

    fun setOnItemSelectedListener(onItemSelected: () -> Unit) {
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}