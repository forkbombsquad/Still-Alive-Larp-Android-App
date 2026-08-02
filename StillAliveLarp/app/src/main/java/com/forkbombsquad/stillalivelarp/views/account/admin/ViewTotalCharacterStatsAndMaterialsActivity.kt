package com.forkbombsquad.stillalivelarp.views.account.admin

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import com.forkbombsquad.stillalivelarp.R
import com.forkbombsquad.stillalivelarp.services.managers.DataManager
import com.forkbombsquad.stillalivelarp.services.managers.DataManagerPassedDataKey
import com.forkbombsquad.stillalivelarp.services.models.CharacterType
import com.forkbombsquad.stillalivelarp.services.models.FullCharacterModel
import com.forkbombsquad.stillalivelarp.utils.Constants
import com.forkbombsquad.stillalivelarp.utils.KeyValueView
import com.forkbombsquad.stillalivelarp.utils.NoStatusBarActivity
import com.forkbombsquad.stillalivelarp.utils.ternary
import com.forkbombsquad.stillalivelarp.utils.yyyyMMddToMonthDayYear
import com.forkbombsquad.stillalivelarp.views.account.MyAccountFragment
import com.forkbombsquad.stillalivelarp.views.shared.ViewCharacterActivity
import com.forkbombsquad.stillalivelarp.views.shared.ViewPlayerActivity
import kotlin.math.roundToInt

class ViewTotalCharacterStatsAndMaterialsActivity : NoStatusBarActivity() {

    private lateinit var includeStaff: CheckBox
    private lateinit var includePlayers: CheckBox
    private lateinit var includeNPCs: CheckBox
    private lateinit var includeDead: CheckBox

    private lateinit var infBelow25: KeyValueView
    private lateinit var infBetween2549: KeyValueView
    private lateinit var infBetween5074: KeyValueView
    private lateinit var infAbove74: KeyValueView

    private lateinit var bullets: KeyValueView
    private lateinit var megas: KeyValueView
    private lateinit var rivals: KeyValueView
    private lateinit var rockets: KeyValueView
    private lateinit var totalBulletValue: KeyValueView

    private lateinit var bulletCasings: KeyValueView
    private lateinit var clothSupplies: KeyValueView
    private lateinit var woodSupplies: KeyValueView
    private lateinit var metalSupplies: KeyValueView
    private lateinit var techSupplies: KeyValueView
    private lateinit var medicalSupplies: KeyValueView

    private lateinit var numSkills: KeyValueView
    private lateinit var spentXp: KeyValueView
    private lateinit var spentFt1s: KeyValueView
    private lateinit var spentPp: KeyValueView

    private lateinit var allCharacters: List<FullCharacterModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_total_character_stats_and_materials)
        setupView()
    }

    private fun setupView() {
        allCharacters = DataManager.shared.getAllCharacters(listOf(CharacterType.STANDARD, CharacterType.NPC))

        includeStaff = findViewById(R.id.chartotal_includeStaff)
        includePlayers = findViewById(R.id.chartotal_includePlayers)
        includeNPCs = findViewById(R.id.chartotal_includeNPCs)
        includeDead = findViewById(R.id.chartotal_includeDead)

        infBelow25 = findViewById(R.id.chartotal_infBelow25)
        infBetween2549 = findViewById(R.id.chartotal_infBetween25_49)
        infBetween5074 = findViewById(R.id.chartotal_infBetween50_74)
        infAbove74 = findViewById(R.id.chartotal_infAbove74)

        bullets = findViewById(R.id.chartotal_bullets)
        megas = findViewById(R.id.chartotal_megas)
        rivals = findViewById(R.id.chartotal_rivals)
        rockets = findViewById(R.id.chartotal_rockets)
        totalBulletValue = findViewById(R.id.chartotal_value)

        bulletCasings = findViewById(R.id.chartotal_bulletCasings)
        clothSupplies = findViewById(R.id.chartotal_cloth)
        woodSupplies = findViewById(R.id.chartotal_wood)
        metalSupplies = findViewById(R.id.chartotal_metal)
        techSupplies = findViewById(R.id.chartotal_tech)
        medicalSupplies = findViewById(R.id.chartotal_medical)

        numSkills = findViewById(R.id.chartotal_numSkills)
        spentXp = findViewById(R.id.chartotal_spentXp)
        spentFt1s = findViewById(R.id.chartotal_spentFt1s)
        spentPp = findViewById(R.id.chartotal_spentPp)

        includeStaff.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includePlayers.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeNPCs.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        includeDead.setOnCheckedChangeListener { _, _ ->
            buildView()
        }

        buildView()
    }

    private fun buildView() {
        val characters = getCharacters()

        val numCharacters = characters.count()

        // 0 = 0-24 | 1 = 25-49 | 2 = 50-74 | 3 = 75+
        val infCounts: MutableList<Int> = mutableListOf(0, 0, 0, 0)

        // 0 = bullets | 1 = megas | 2 = rivals | 3 = rockets | 4 = totalCumulativeValue
        val ammoCounts: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0)

        // 0 = casing | 1 = cloth | 2 = wood | 3 = metal | 4 = tech | 5 = medical
        val materialCounts: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0)

        // 0 = all | 1 = combat | 2 = talent | 3 = prof
        val skillCounts: MutableList<Int> = mutableListOf(0, 0, 0, 0)

        // 0 = xp | 1 = ft1s | 2 = pp
        val spentXps: MutableList<Int> = mutableListOf(0, 0, 0)

        var inf = 0

        var bul = 0
        var meg = 0
        var riv = 0
        var roc = 0

        characters.forEach { char ->
            inf = char.getInfection()

            bul = char.bullets
            meg = char.megas
            riv = char.rivals
            roc = char.rockets

            when (inf) {
                in 0 until 25 -> infCounts[0] += 1
                in 26 until 50 -> infCounts[1] += 1
                in 51 until 75 -> infCounts[2] += 1
                else -> infCounts[3] += 1
            }

            ammoCounts[0] += bul
            ammoCounts[1] += meg
            ammoCounts[2] += riv
            ammoCounts[3] += roc
            ammoCounts[4] += bul + (meg * 3) + (riv * 5) + (roc * 10)

            materialCounts[0] += char.bulletCasings
            materialCounts[1] += char.clothSupplies
            materialCounts[2] += char.woodSupplies
            materialCounts[3] += char.metalSupplies
            materialCounts[4] += char.metalSupplies
            materialCounts[5] += char.medicalSupplies

            skillCounts[0] += char.allPurchasedSkills().count()
            skillCounts[1] += char.allPurchasedSkills().count { it.skillTypeId == Constants.SkillTypes.combat }
            skillCounts[2] += char.allPurchasedSkills().count { it.skillTypeId == Constants.SkillTypes.talent }
            skillCounts[3] += char.allPurchasedSkills().count { it.skillTypeId == Constants.SkillTypes.profession }

            spentXps[0] += char.getSpentXp()
            spentXps[1] += char.getSpentFt1s()
            spentXps[2] += char.getSpentPp()
        }

        infBelow25.set(infCounts[0])
        infBetween2549.set(infCounts[1])
        infBetween5074.set(infCounts[2])
        infAbove74.set(infCounts[3])

        bullets.set("${ammoCounts[0]} (${getAvg(ammoCounts[0], numCharacters)} avg)")
        megas.set("${ammoCounts[1]} (${getAvg(ammoCounts[1], numCharacters)} avg)")
        rivals.set("${ammoCounts[2]} (${getAvg(ammoCounts[2], numCharacters)} avg)")
        rockets.set("${ammoCounts[3]} (${getAvg(ammoCounts[3], numCharacters)} avg)")
        totalBulletValue.set("${ammoCounts[4]} (${getAvg(ammoCounts[4], numCharacters)} avg)")

        bulletCasings.set("${materialCounts[0]} (${getAvg(materialCounts[0], numCharacters)} avg)")
        clothSupplies.set("${materialCounts[1]} (${getAvg(materialCounts[1], numCharacters)} avg)")
        woodSupplies.set("${materialCounts[2]} (${getAvg(materialCounts[2], numCharacters)} avg)")
        metalSupplies.set("${materialCounts[3]} (${getAvg(materialCounts[3], numCharacters)} avg)")
        techSupplies.set("${materialCounts[4]} (${getAvg(materialCounts[4], numCharacters)} avg)")
        medicalSupplies.set("${materialCounts[5]} (${getAvg(materialCounts[5], numCharacters)} avg)")

        numSkills.set("${skillCounts[0]} Total\n\n(${skillCounts[1]} Combat Skills)\n(${skillCounts[2]} Talent Skills)\n(${skillCounts[3]} Profession Skills)")

        spentXp.set(spentXps[0])
        spentFt1s.set(spentXps[1])
        spentPp.set(spentXps[2])
    }

    private fun getAvg(num: Int, chars: Int): Int {
        return (num.toDouble() / chars.toDouble()).roundToInt()
    }

    private fun getCharacters(): List<FullCharacterModel> {
        var chars = allCharacters
        if (!includeDead.isChecked) {
            chars = chars.filter { it.isAlive }
        }
        if (!includeNPCs.isChecked) {
            chars = chars.filterNot { it.characterType() == CharacterType.NPC }
        }
        if (!includePlayers.isChecked) {
            chars = chars.filterNot { it.characterType() == CharacterType.STANDARD }
        }
        if (!includeStaff.isChecked) {
            chars = chars.filterNot { it.characterType() == CharacterType.STANDARD && DataManager.shared.getPlayerForCharacter(it).isAdmin }
        }
        return chars
    }
}