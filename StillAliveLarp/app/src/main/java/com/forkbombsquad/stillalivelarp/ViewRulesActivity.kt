package com.forkbombsquad.stillalivelarp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.forkbombsquad.stillalivelarp.services.managers.OldDataManager
import com.forkbombsquad.stillalivelarp.utils.Heading
import com.forkbombsquad.stillalivelarp.utils.HeadingView
import com.forkbombsquad.stillalivelarp.utils.Rulebook
import com.forkbombsquad.stillalivelarp.utils.SubHeading
import com.forkbombsquad.stillalivelarp.utils.SubHeadingView
import com.forkbombsquad.stillalivelarp.utils.SubSubHeading
import com.forkbombsquad.stillalivelarp.utils.SubSubHeadingView
import com.forkbombsquad.stillalivelarp.utils.Table
import com.forkbombsquad.stillalivelarp.utils.ifLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewRulesActivity : NoStatusBarActivity() {

    private lateinit var search: EditText
    private lateinit var layout: LinearLayout
    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var filterSpinner: Spinner

    private var loadingView = false
    private var headings: MutableList<View> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_rules)
        setupView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        OldDataManager.shared.unrelaltedUpdateCallback()
    }

    private fun setupView() {
        title = findViewById(R.id.viewrules_title)
        search = findViewById(R.id.viewrules_searchview)
        layout = findViewById(R.id.viewrules_layout)
        progressBar = findViewById(R.id.viewrules_progressBar)
        filterSpinner = findViewById(R.id.rulebook_filter)

        val allFilters: MutableList<String> = mutableListOf()
        allFilters.add("No Filter")
        OldDataManager.shared.rulebook.ifLet {
            allFilters.addAll(it.getAllFilterableHeadingNames())
        }
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allFilters)
        filterSpinner.adapter = filterAdapter
        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                lifecycleScope.launch(Dispatchers.IO) {
                    createViews()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        search.doOnTextChanged { text, start, before, count ->
            lifecycleScope.launch(Dispatchers.IO) {
                createViews()
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            createViews()
        }
    }

    @Synchronized
    private fun createViews() {
        loadingView = true
        runOnUiThread {
            buildView()
        }
        headings = mutableListOf()
        filterSpinner.isGone = OldDataManager.shared.loadingRulebook
        OldDataManager.shared.rulebook.ifLet { rulebook ->
            for (heading in filterHeadings(rulebook)) {
                val headingView = HeadingView(this)
                headingView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                headingView.setPadding(2, 8, 2, 8)

                headingView.title.text = heading.title
                for (element in heading.textsAndTables) {
                    (element as? Table).ifLet({ table ->
                        headingView.texts.addView(createTableView(table))
                    }, {
                        headingView.texts.addView(createTextView(element as? String ?: ""))
                    })
                }

                for (subsubheading in heading.subSubHeadings) {
                    headingView.subsubheadings.addView(createSubSubHeading(subsubheading))
                }

                for (subheading in heading.subHeadings) {
                    headingView.subheadings.addView(createSubHeading(subheading))
                }

                headings.add(headingView)
            }
            loadingView = false
            runOnUiThread {
                buildView()
            }
        }
    }

    private fun buildView() {
        OldDataManager.shared.rulebook.ifLet { rulebook ->
            title.text = "Rulebook v${rulebook.version}"
        }
        layout.removeAllViews()
        progressBar.isGone = !loadingView
        if (!loadingView) {
            for (heading in headings) {
                layout.addView(heading)
            }
        }
    }

    private fun filterHeadings(rulebook: Rulebook): List<Heading> {
        var headings = rulebook.headings.toMutableList()
        val filter = filterSpinner.selectedItem.toString()
        if (filter != "No Filter") {
            headings = headings.filter { it.titlesContain(sanitizeFilter(filter))}.toMutableList()
            headings = getFilteredHeadings(headings, filter)
        }
        val searchText = search.text.trim().toString()
        return if (searchText.isNotEmpty()) {
            furtherFilterHeadings(headings.filter { it.contains(searchText) }, searchText)
        } else {
            headings
        }
    }

    private fun getFilteredHeadings(headings: MutableList<Heading>, filter: String): MutableList<Heading> {
        val newHeadings: MutableList<Heading> = mutableListOf()
        headings.forEach { heading ->
            newHeadings.add(heading.filterForHeadingsWithTitle(sanitizeFilter(filter)))
        }
        return newHeadings
    }
    private fun sanitizeFilter(filter: String): String {
        return filter.trim()
    }

    private fun furtherFilterHeadings(headings: List<Heading>, searchText: String): List<Heading> {
        val newHeadings = mutableListOf<Heading>()
        for (heading in headings) {
            newHeadings.add(heading.filterText_CallContainsFirst(searchText))
        }
        return newHeadings
    }

    private fun createSubSubHeading(subSubHeading: SubSubHeading): SubSubHeadingView {
        val subsubHeadingView = SubSubHeadingView(this)
        subsubHeadingView.title.text = subSubHeading.title
        subsubHeadingView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        for (element in subSubHeading.textsAndTables) {
            (element as? Table).ifLet({ table ->
                subsubHeadingView.texts.addView(createTableView(table))
            }, {
                subsubHeadingView.texts.addView(createTextView(element as? String ?: ""))
            })
        }
        return subsubHeadingView
    }

    private fun createSubHeading(subHeading: SubHeading): SubHeadingView {
        val subHeadingView = SubHeadingView(this)
        subHeadingView.title.text = subHeading.title
        subHeadingView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        for (element in subHeading.textsAndTables) {
            (element as? Table).ifLet({ table ->
                subHeadingView.texts.addView(createTableView(table))
            }, {
                subHeadingView.texts.addView(createTextView(element as? String ?: ""))
            })
        }

        for (subSubHeading in subHeading.subSubHeadings) {
            subHeadingView.subsubheadings.addView(createSubSubHeading(subSubHeading))
        }
        return subHeadingView
    }

    private fun createTableView(table: Table): View {

        val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            1000
        }
        val hsv = HorizontalScrollView(this)

        hsv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val tableView = TableLayout(this)
        tableView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        tableView.isStretchAllColumns = false
        val rows = table.convertToRows()
        var colorToggle = 0
        var firstRow = true
        for (row in rows) {
            colorToggle = if (colorToggle != 190) {
                190
            } else {
                230
            }
            val tableRow = TableRow(this)

            for (item in row) {
                val tv = TextView(this)
                tv.text = item
                if (firstRow) {
                    tv.setTypeface(tv.typeface, Typeface.BOLD)
                }
                tv.setTextColor(Color.rgb(0, 0, 0))
                tv.textSize = 16f
                tv.maxWidth = (screenWidth / 2.1).toInt()
                val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                tv.setPadding(16, 4, 16, 4)
                tv.layoutParams = params
                tableRow.addView(tv)
            }
            tableRow.setBackgroundColor(Color.rgb(colorToggle, colorToggle, colorToggle))
            tableView.addView(tableRow)
            firstRow = false
        }
        hsv.addView(tableView)
        hsv.setPadding(0, 16, 0, 16)
        return hsv
    }

    private fun createTextView(text: String): View {
        val tv = TextView(this)
        tv.textSize = 16f

        tv.setTextColor(Color.rgb(0, 0, 0))
        tv.text = text
        tv.setPadding(0, 0, 0, 16)
        tv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        return tv
    }
}