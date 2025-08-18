package com.forkbombsquad.stillalivelarp.utils

sealed interface AwardType {
    fun getDisplayText(pluralize: Boolean): String
}

enum class AwardPlayerType(val text: String): AwardType {
    XP("XP"),
    PRESTIGEPOINTS("PP"),
    FREETIER1SKILLS("FREE-T1-SKILL");

    override fun getDisplayText(pluralize: Boolean): String {
        return when(this) {
            XP -> "Experience Point${pluralize.ternary("s", "")}"
            PRESTIGEPOINTS -> "Prestige Point${pluralize.ternary("s", "")}"
            FREETIER1SKILLS -> "Free Tier-1 Skill${pluralize.ternary("s", "")}"
        }
    }
}

enum class AwardCharType(val text: String): AwardType {
    INFECTION("INFECTION"),
    MATERIALCASINGS("MATERIAL_CASINGS"),
    MATERIALWOOD("MATERIAL_WOOD"),
    MATERIALCLOTH("MATERIAL_CLOTH"),
    MATERIALMETAL("MATERIAL_METAL"),
    MATERIALTECH("MATERIAL_TECH"),
    MATERIALMED("MATERIAL_MED"),
    AMMOBULLET("AMMO_BULLET"),
    AMMOMEGA("AMMO_MEGA"),
    AMMORIVAL("AMMO_RIVAL"),
    AMMOROCKET("AMMO_ROCKET");

    override fun getDisplayText(pluralize: Boolean): String {
         return when(this) {
             INFECTION -> "Infection Rating"
             MATERIALCASINGS -> "Bullet Casing${pluralize.ternary("s", "")}"
             MATERIALWOOD -> "Wood"
             MATERIALCLOTH -> "Cloth"
             MATERIALMETAL -> "Metal"
             MATERIALTECH -> "Tech Suppl${pluralize.ternary("ies", "y")}"
             MATERIALMED -> "Medical Suppl${pluralize.ternary("ies", "y")}"
             AMMOBULLET -> "Bullet${pluralize.ternary("s", "")}"
             AMMOMEGA -> "Mega${pluralize.ternary("s", "")}"
             AMMORIVAL -> "Rival${pluralize.ternary("s", "")}"
             AMMOROCKET -> "Rocket${pluralize.ternary("s", "")}"
         }
    }
}