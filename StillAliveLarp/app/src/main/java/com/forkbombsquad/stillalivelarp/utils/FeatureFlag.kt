package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.managers.DataManager

class FeatureFlag(var name: String) {
    companion object {
        // Name of feature flags
        val OLD_SKILL_TREE_IMAGE = FeatureFlag("oldskilltreeimage")
        val CAMP_STATUS = FeatureFlag("campstatus")
    }

    fun isActive(): Boolean {
        return DataManager.shared.featureFlags?.firstOrNull { it.name.lowercase() == name.lowercase() }?.activeAndroid == "TRUE"
    }

    fun isActiveIos(): Boolean {
        return DataManager.shared.featureFlags?.firstOrNull { it.name.lowercase() == name.lowercase() }?.activeIos == "TRUE"
    }

    fun isActiveBoth(): Boolean {
        val ff = DataManager.shared.featureFlags?.firstOrNull { it.name.lowercase() == name.lowercase() }
        return ff?.activeIos == "TRUE" && ff.activeAndroid == "TRUE"
    }
}