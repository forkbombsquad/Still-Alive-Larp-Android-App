package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.managers.DataManager

class FeatureFlag(var name: String) {
    companion object {
        // Name of feature flags
        val TEST = FeatureFlag("test")
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