package com.forkbombsquad.stillalivelarp.utils

enum class SkillFilterType(val text: String) {
    NONE("No Filter"),
    COMBAT("Combat"),
    PROFESSION("Profession"),
    TALENT("Talent"),
    XP0("0xp"),
    XP1("1xp"),
    XP2("2xp"),
    XP3("3xp"),
    XP4("4xp"),
    PP("Prestige Points"),
    INF("Infection Threshold");

    companion object {
        fun getAllStrings(): List<String> {
            var list = mutableListOf<String>()
            SkillFilterType.values().forEach {
                list.add(it.text)
            }
            return list
        }

        fun getTypeForString(string: String): SkillFilterType {
            return when (string) {
                NONE.text -> NONE
                COMBAT.text -> COMBAT
                PROFESSION.text -> PROFESSION
                TALENT.text -> TALENT
                XP0.text -> XP0
                XP1.text -> XP1
                XP2.text -> XP2
                XP3.text -> XP3
                XP4.text -> XP4
                PP.text -> PP
                INF.text -> INF
                else -> NONE
            }
        }
    }
}

enum class SkillSortType(val text: String) {
    AZ("A-Z"),
    ZA("Z-A"),
    XPASC("XP Asc"),
    XPDESC("XP Desc"),
    TYPEASC("Type Asc"),
    TYPEDESC("Type Desc");

    companion object {
        fun getAllStrings(): List<String> {
            var list = mutableListOf<String>()
            SkillSortType.values().forEach {
                list.add(it.text)
            }
            return list
        }

        fun getTypeForString(string: String): SkillSortType {
            return when (string) {
                AZ.text -> AZ
                ZA.text -> ZA
                XPASC.text -> XPASC
                XPDESC.text -> XPDESC
                TYPEASC.text -> TYPEASC
                TYPEDESC.text -> TYPEDESC
                else -> AZ
            }
        }
    }
}