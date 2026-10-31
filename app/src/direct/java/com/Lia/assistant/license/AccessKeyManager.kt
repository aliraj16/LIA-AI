package com.Lia.assistant.license

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessKeyManager {
    private val _info = MutableStateFlow(LicenseInfo(active = true, plan = "Direct", statusText = "Active"))
    val info: StateFlow<LicenseInfo> = _info.asStateFlow()

    fun refreshInfo(context: Context) {
        val prefs = context.getSharedPreferences("lia_access_key", Context.MODE_PRIVATE)
        val key = prefs.getString("key", "") ?: ""
        val plan = prefs.getString("plan", "Direct") ?: "Direct"
        val expiresAt = prefs.getLong("expires_at", 0L)
        val active = prefs.getBoolean("active", true)
        val blocked = prefs.getBoolean("blocked", false)
        _info.value = LicenseInfo(
            active = active && !blocked,
            plan = plan,
            expiresAt = expiresAt,
            statusText = if (blocked) "Blocked" else if (active) "Active" else "Inactive",
            blocked = blocked,
            hasRememberedKey = key.isNotBlank()
        )
    }

    suspend fun activate(context: Context, key: String): Pair<Boolean, String> {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return Pair(false, "Key cannot be empty")
        val prefs = context.getSharedPreferences("lia_access_key", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("key", trimmed)
            .putString("plan", "Pro")
            .putLong("expires_at", System.currentTimeMillis() + 365L * 24 * 3600 * 1000)
            .putBoolean("active", true)
            .putBoolean("blocked", false)
            .putLong("last_remote_check", System.currentTimeMillis())
            .apply()
        refreshInfo(context)
        return Pair(true, "Key activated successfully")
    }

    suspend fun liveCheck(context: Context): Boolean {
        refreshInfo(context)
        return _info.value.active
    }

    fun isAccessGranted(): Boolean {
        return _info.value.active
    }
}
