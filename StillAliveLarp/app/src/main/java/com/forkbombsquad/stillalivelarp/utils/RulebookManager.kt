package com.forkbombsquad.stillalivelarp.utils

import android.os.AsyncTask
import androidx.lifecycle.LifecycleCoroutineScope
import com.forkbombsquad.stillalivelarp.services.VersionService
import com.forkbombsquad.stillalivelarp.services.managers.SharedPrefsManager
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class RulebookManager private constructor()  {
    companion object {
        var shared = RulebookManager()
            private set
    }

    fun getOfflineVersion(): Rulebook? {
        val rulebookString = SharedPrefsManager.shared.getRulebook()
        return if (rulebookString == null) {
            null
        } else {
            parseDocAsRulebook(Jsoup.parse(rulebookString), SharedPrefsManager.shared.getRulebookVersion() ?: "Unknown Version")
        }
    }

    fun getOnlineVersion(lifecycleScope: LifecycleCoroutineScope, callback: (Rulebook?) -> Unit) {
        // Check Version
        val versionRequest = VersionService()
        lifecycleScope.launch {
            versionRequest.successfulResponse().ifLet({ versionModel ->
                val rulesVersion = SharedPrefsManager.shared.getRulebookVersion()
                if (rulesVersion != versionModel.rulebookVersion || SharedPrefsManager.shared.getRulebook() == null) {
                    // Download
                    SharedPrefsManager.shared.storeRulebookVersion(versionModel.rulebookVersion)
                    downloadPage(lifecycleScope, versionModel.rulebookVersion) { rulebook ->
                        callback(rulebook)
                    }
                } else {
                    // Load from shared prefs
                    callback(getOfflineVersion())
                }
            }, {
                callback(null)
            })
        }
    }

    private fun parseDocAsRulebook(document: Document?, version: String): Rulebook {
        val content = document?.getElementById("AppContent")
        var elements = content?.children()?.toList() ?: emptyList()
        val rulebook = Rulebook(version)
        var currentHeading: Heading? = null
        var currentSubHeading: SubHeading? = null
        var currentSubSubHeading: SubSubHeading? = null

        // Tags and consts
        val HEADING = "h1"
        val SUBHEADING = "h2"
        val SUBSUBHEADING = "h3"
        val TEXT = "p"
        val TABLE = "table"
        val LIST = "ul"
        val TABLEROW = "tr"
        val TABLEHEAD = "th"
        val TABLEDETAIL = "td"

        for (element in elements) {
            when (element.tagName()) {
                HEADING -> {
                    currentSubSubHeading.ifLet {
                        if (currentSubHeading == null) {
                            currentHeading?.subSubHeadings?.add(it)
                        } else {
                            currentSubHeading?.subSubHeadings?.add(it)
                        }
                        currentSubSubHeading = null
                    }
                    currentSubHeading.ifLet {
                        currentHeading?.subHeadings?.add(it)
                        currentSubHeading = null
                    }
                    currentHeading.ifLet {
                        rulebook.headings.add(it)
                        currentHeading = null
                    }
                    currentHeading = Heading()
                    currentHeading?.title = element.text()
                }
                SUBHEADING -> {
                    currentSubSubHeading.ifLet {
                        if (currentSubHeading == null) {
                            currentHeading?.subSubHeadings?.add(it)
                        } else {
                            currentSubHeading?.subSubHeadings?.add(it)
                        }
                        currentSubSubHeading = null
                    }
                    currentSubHeading.ifLet {
                        currentHeading?.subHeadings?.add(it)
                        currentSubHeading = null
                    }
                    currentSubHeading = SubHeading()
                    currentSubHeading?.title = element.text()
                }
                SUBSUBHEADING -> {
                    currentSubSubHeading.ifLet {
                        if (currentSubHeading == null) {
                            currentHeading?.subSubHeadings?.add(it)
                        } else {
                            currentSubHeading?.subSubHeadings?.add(it)
                        }
                        currentSubSubHeading = null
                    }
                    currentSubSubHeading = SubSubHeading()
                    currentSubSubHeading?.title = element.text()
                }
                TEXT -> {
                    if (currentSubSubHeading != null) {
                        currentSubSubHeading?.textsAndTables?.add(element.text())
                    } else if (currentSubHeading != null) {
                        currentSubHeading?.textsAndTables?.add(element.text())
                    } else {
                        currentHeading?.textsAndTables?.add(element.text())
                    }
                }
                TABLE -> {
                    val table = Table()
                    val keys: MutableList<String> = mutableListOf()
                    for (tableBody in element.children().toList()) {
                        for (tableRow in tableBody.children().toList()) {
                            var firstTd = true
                            for (tableElement in tableRow.children().toList()) {
                                when (tableElement.tagName()) {
                                    TABLEHEAD -> {
                                        keys.add(tableElement.text())
                                    }
                                    TABLEDETAIL -> {
                                        if (firstTd) {
                                            var count = 0
                                            for (tableCell in tableRow.children().toList()) {
                                                if (!table.contents.containsKey(keys[count])) {
                                                    table.contents[keys[count]] = mutableListOf()
                                                }
                                                table.contents[keys[count]]?.add(tableCell.toString()
                                                    .replaceHtmlTags(listOf("td", "small", "b", "i", "skill", "condition", "item", "combat", "talent", "profession"))
                                                    .replaceHtmlTag("br", "\n"))
                                                count += 1
                                            }
                                        }
                                        firstTd = false
                                    }
                                }
                            }
                        }
                    }
                    if (currentSubSubHeading != null) {
                        currentSubSubHeading?.textsAndTables?.add(table)
                    } else if (currentSubHeading != null) {
                        currentSubHeading?.textsAndTables?.add(table)
                    } else {
                        currentHeading?.textsAndTables?.add(table)
                    }
                }
                LIST -> {
                    val table = Table()
                    val keys: MutableList<String> = mutableListOf()
                    keys.add("Category")
                    keys.add("Change")
                    for (li in element.children().toList()) {
                        var text = ""
                        val count = if (li.children().first()?.tagName() == LIST) {
                            1
                        } else {
                            0
                        }

                        text = if (count == 1) {
                            li.children().first()?.children()?.first()?.text() ?: ""
                        } else {
                            li.text()
                        }

                        if (!table.contents.containsKey(keys[count])) {
                            table.contents[keys[count]] = mutableListOf()
                        }
                        table.contents[keys[count]]?.add(text)
                    }
                    if (currentSubSubHeading != null) {
                        currentSubSubHeading?.textsAndTables?.add(table)
                    } else if (currentSubHeading != null) {
                        currentSubHeading?.textsAndTables?.add(table)
                    } else {
                        currentHeading?.textsAndTables?.add(table)
                    }

                }
            }
        }
        currentSubSubHeading.ifLet {
            if (currentSubHeading == null) {
                currentHeading?.subSubHeadings?.add(it)
            } else {
                currentSubHeading?.subSubHeadings?.add(it)
            }
            currentSubSubHeading = null
        }
        currentSubHeading.ifLet {
            currentHeading?.subHeadings?.add(it)
            currentSubHeading = null
        }
        currentHeading.ifLet {
            rulebook.headings.add(it)
            currentHeading = null
        }
        return rulebook
    }

    private fun downloadPage(lifecycleScope: LifecycleCoroutineScope, version: String, onSuccess: (rulebook: Rulebook) -> Unit) {
        lifecycleScope.launch {
            val jsoupAsyncTask = JsoupAsyncTask(Constants.URLs.rulebookUrl) { doc ->
                SharedPrefsManager.shared.storeRulebook(doc.toString())
                onSuccess(parseDocAsRulebook(doc, version))
            }
            jsoupAsyncTask.execute()
        }
    }

}

class Rulebook {
    var version: String
    var headings: MutableList<Heading> = mutableListOf()

    constructor(version: String) {
        this.version = version
    }

    fun getAllFilterableHeadingNames(): MutableList<String> {
        var names: MutableList<String> = mutableListOf()
        headings.forEach { heading ->
            names.add(heading.title)
            heading.subSubHeadings.forEach { subsub ->
                names.add("            ${subsub.title}")
            }
            heading.subHeadings.forEach { sub ->
                names.add("      ${sub.title}")
                sub.subSubHeadings.forEach { subsub ->
                    names.add("            ${subsub.title}")
                }
            }
        }
        return names
    }
}

class Heading{
    // Display in this order
    var title: String = ""
    var textsAndTables: MutableList<Any> = mutableListOf()
    var subSubHeadings: MutableList<SubSubHeading> = mutableListOf()
    var subHeadings: MutableList<SubHeading> = mutableListOf()

    constructor(title: String, textsAndTables: MutableList<Any>, subSubHeadings: MutableList<SubSubHeading>, subHeadings: MutableList<SubHeading>) {
        this.title = title
        this.textsAndTables = textsAndTables
        this.subSubHeadings = subSubHeadings
        this.subHeadings = subHeadings
    }
    constructor(): this("", mutableListOf(), mutableListOf(), mutableListOf())

    fun contains(text: String): Boolean {
        if (title.containsIgnoreCase(text)) {
            return true
        }
        for (tot in textsAndTables) {
            if ((tot as? Table)?.contains(text) == true) {
                return true
            } else if ((tot as? String)?.containsIgnoreCase(text) == true) {
                return true
            }
        }
        for (subsub in subSubHeadings) {
            if (subsub.contains(text)) {
                return true
            }
        }

        for (sub in subHeadings) {
            if (sub.contains(text)) {
                return true
            }
        }
        return false
    }

    fun filterText_CallContainsFirst(text: String): Heading {
        val newTextsAndTables: MutableList<Any> = mutableListOf()
        val newSubSubHeadings: MutableList<SubSubHeading> = mutableListOf()
        val newSubHeadings: MutableList<SubHeading> = mutableListOf()

        for (tot in textsAndTables) {
            if ((tot as? Table)?.contains(text) == true) {
                newTextsAndTables.add(tot)
            } else if ((tot as? String)?.containsIgnoreCase(text) == true) {
                newTextsAndTables.add(tot)
            }
        }
        for (subsub in subSubHeadings) {
            if (subsub.contains(text)) {
                newSubSubHeadings.add(subsub)
            }
        }

        for (sub in subHeadings) {
            if (sub.contains(text)) {
                newSubHeadings.add(sub)
            }
        }

        val newHeading = Heading()
        newHeading.title = title
        newHeading.textsAndTables = newTextsAndTables
        newHeading.subSubHeadings = newSubSubHeadings
        newHeading.subHeadings = newSubHeadings
        return newHeading
    }

    fun titlesContain(title: String): Boolean {
        return if (this.title.equalsIgnoreCase(title)) {
            true
        } else if (this.subSubHeadings.firstOrNull { it.title.equalsIgnoreCase(title) } != null) {
            true
        } else if (this.subHeadings.firstOrNull { it.title.equalsIgnoreCase(title) || (it.subSubHeadings.firstOrNull { ssh -> ssh.title.equalsIgnoreCase(title) } != null) } != null) {
            true
        } else {
            false
        }
    }

    fun filterForHeadingsWithTitle(title: String): Heading {
        val newTextsAndTables: MutableList<Any> = mutableListOf()
        val newSubSubHeadings: MutableList<SubSubHeading> = mutableListOf()
        val newSubHeadings: MutableList<SubHeading> = mutableListOf()

        if (this.title.equalsIgnoreCase(title)) {
            newTextsAndTables.addAll(textsAndTables)
            newSubHeadings.addAll(subHeadings)
            newSubSubHeadings.addAll(subSubHeadings)
        }
        subSubHeadings.forEach { subsub ->
            if (subsub.title.equalsIgnoreCase(title)) {
                newSubSubHeadings.add(subsub)
            }
        }
        subHeadings.forEach { sub ->
            val subsub = sub.subSubHeadings.firstOrNull { it.title.equalsIgnoreCase(title) }
            if (sub.title.equalsIgnoreCase(title)) {
                newSubHeadings.add(sub)
            } else if (subsub != null) {
                newSubHeadings.add(SubHeading(sub.title, mutableListOf(), mutableListOf(subsub)))
            }
        }
        return Heading(this.title, newTextsAndTables, newSubSubHeadings, newSubHeadings)
    }

    override fun toString(): String {
        return "Heading: {title: ${title}, textsAndTables: [${textsAndTables}], subSubHeadings: [${subSubHeadings}], subHeadings: [${subHeadings}]}"
    }

}

class SubHeading{

    var title: String = ""
    var textsAndTables: MutableList<Any> = mutableListOf()
    var subSubHeadings: MutableList<SubSubHeading> = mutableListOf()
    constructor(title: String, textsAndTables: MutableList<Any>, subSubHeadings: MutableList<SubSubHeading>) {
        this.title = title
        this.textsAndTables = textsAndTables
        this.subSubHeadings = subSubHeadings
    }
    constructor(): this("", mutableListOf(), mutableListOf())

    fun contains(text: String): Boolean {
        if (title.containsIgnoreCase(text)) {
            return true
        }
        for (tot in textsAndTables) {
            if ((tot as? Table)?.contains(text) == true) {
                return true
            } else if ((tot as? String)?.containsIgnoreCase(text) == true) {
                return true
            }
        }
        for (subsub in subSubHeadings) {
            if (subsub.contains(text)) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "SubHeading: {title: ${title}, textsAndTables: [${textsAndTables}], subSubHeadings: [${subSubHeadings}]}"
    }

}

class SubSubHeading {
    var title: String = ""
    var textsAndTables: MutableList<Any> = mutableListOf()

    fun contains(text: String): Boolean {
        if (title.containsIgnoreCase(text)) {
            return true
        }
        for (tot in textsAndTables) {
            if ((tot as? Table)?.contains(text) == true) {
                return true
            } else if ((tot as? String)?.containsIgnoreCase(text) == true) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "SubSubHeading: {title: ${title}, textsAndTables: [${textsAndTables}]}"
    }

}

class Table {
    var contents: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun convertToRows(): List<List<String>> {
        val listOfRows: MutableList<MutableList<String>> = mutableListOf()
        val list = mutableListOf<String>()
        for (key in contents.keys) {
            list.add(key)
        }
        listOfRows.add(list)
        var counter = 0
        for (value in contents.values.first()) {
            val list = mutableListOf<String>()
            for (key in contents.keys) {
                list.add(contents[key]?.get(counter) ?: "")
            }
            counter += 1
            listOfRows.add(list)
        }
        return listOfRows
    }

    fun contains(text: String): Boolean {
        for (kv in contents) {
            if (kv.key.containsIgnoreCase(text)) {
                return true
            }
            for (value in kv.value) {
                if (value.containsIgnoreCase(text)) {
                    return true
                }
            }
        }
        return false
    }

    override fun toString(): String {
        return "SubSubHeading: {table: ${contents}}"
    }
}

class JsoupAsyncTask(private val url: String, private val callback: (Document?) -> Unit) :
    AsyncTask<Void, Void, Document>() {

    override fun doInBackground(vararg params: Void?): Document? {
        return try {
            Jsoup.connect(url).get()
        } catch (e: IOException) {
            null
        }
    }

    override fun onPostExecute(result: Document?) {
        super.onPostExecute(result)

        callback(result)
    }
}