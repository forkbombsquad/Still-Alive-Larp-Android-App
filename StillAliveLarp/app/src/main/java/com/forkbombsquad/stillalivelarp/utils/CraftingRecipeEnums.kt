package com.forkbombsquad.stillalivelarp.utils

enum class CraftingRecipeFilterType(val text: String) {
    NONE("All Recipes"),
    CAN_POTENTIALLY_MAKE("Can Potentially Make"),
    CAN_MAKE_NOW("Can Make Now");

    companion object {
        fun getAllStrings(): List<String> {
            return CraftingRecipeFilterType.values().map { it.text }
        }

        fun getTypeForString(string: String): CraftingRecipeFilterType {
            return when (string) {
                NONE.text -> NONE
                CAN_POTENTIALLY_MAKE.text -> CAN_POTENTIALLY_MAKE
                CAN_MAKE_NOW.text -> CAN_MAKE_NOW
                else -> NONE
            }
        }
    }
}

enum class CraftingRecipeSortType(val text: String) {
    AZ("A-Z"),
    ZA("Z-A"),
    CATEGORY_ASC("Category A-Z"),
    CATEGORY_DESC("Category Z-A"),
    SKILL_ASC("Skill A-Z"),
    SKILL_DESC("Skill Z-A"),
    TIME_ASC("Time Fastest"),
    TIME_DESC("Time Longest");

    companion object {
        fun getAllStrings(): List<String> {
            return CraftingRecipeSortType.values().map { it.text }
        }

        fun getTypeForString(string: String): CraftingRecipeSortType {
            return when (string) {
                AZ.text -> AZ
                ZA.text -> ZA
                CATEGORY_ASC.text -> CATEGORY_ASC
                CATEGORY_DESC.text -> CATEGORY_DESC
                SKILL_ASC.text -> SKILL_ASC
                SKILL_DESC.text -> SKILL_DESC
                TIME_ASC.text -> TIME_ASC
                TIME_DESC.text -> TIME_DESC
                else -> AZ
            }
        }
    }
}