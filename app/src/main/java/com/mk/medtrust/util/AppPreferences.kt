package com.yourpackage.app

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    private const val PREF_NAME = "APP_PREFERENCES"

    private lateinit var preferences: SharedPreferences

    // Initialize once in Application class
    fun init(context: Context) {
        preferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }

    // ---------- SAVE ----------
    fun setString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun setBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun setInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    // ---------- GET ----------
    fun getString(key: String, default: String = ""): String {
        return preferences.getString(key, default) ?: default
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return preferences.getBoolean(key, default)
    }

    fun getInt(key: String, default: Int = 0): Int {
        return preferences.getInt(key, default)
    }

    // ---------- CLEAR ----------
    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun clearAll() {
        preferences.edit().clear().apply()
    }
}
