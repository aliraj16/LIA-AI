package com.Lia.assistant.data

import android.content.Context
import android.content.SharedPreferences

object ApiKeyStore {
    private const val PREF_NAME = "nova_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI_API_KEY, "")?.trim() ?: ""
    }

    fun saveKey(context: Context, key: String) {
        val trimmed = key.trim()
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
    }

    fun hasKey(context: Context): Boolean {
        return getKey(context).isNotEmpty()
    }
}
