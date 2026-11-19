package com.example.studyplannerlite.settings

import android.content.SharedPreferences

class SettingsManager(private val preferences: SharedPreferences) {

    fun getSortOrder(): String =
        preferences.getString(KEY_SORT_ORDER, SORT_DEADLINE) ?: SORT_DEADLINE

    fun setSortOrder(value: String) {
        preferences.edit().putString(KEY_SORT_ORDER, value).apply()
    }

    companion object {
        const val PREFS_NAME = "settings"
        const val SORT_DEADLINE = "deadline"
        const val SORT_TITLE = "title"

        private const val KEY_SORT_ORDER = "sort_order"
    }
}
