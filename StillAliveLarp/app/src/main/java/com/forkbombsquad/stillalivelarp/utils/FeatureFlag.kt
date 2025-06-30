package com.forkbombsquad.stillalivelarp.utils

import com.forkbombsquad.stillalivelarp.services.managers.DataManager


class FeatureFlag(var name: String) {
    companion object {
        val CAMP_STATUS = FeatureFlag("campstatus")
    }

    fun isActive(): Boolean {
        return DataManager.shared.featureFlags.firstOrNull { it.name.equalsIgnoreCase(name) }?.activeAndroid == "TRUE"
    }

    fun isActiveIos(): Boolean {
        return DataManager.shared.featureFlags.firstOrNull { it.name.equalsIgnoreCase(name) }?.activeIos == "TRUE"
    }

    fun isActiveBoth(): Boolean {
        val ff = DataManager.shared.featureFlags.firstOrNull { it.name.equalsIgnoreCase(name) }
        return ff?.activeIos == "TRUE" && ff.activeAndroid == "TRUE"
    }
}