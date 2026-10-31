package com.Lia.assistant.data

import android.content.Context
import android.provider.Settings
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Client for the ALIRAJ MILI AI Admin API.
 * Set the HTTPS base URL in the app's deployment configuration before use.
 * Never ship an admin token or server secret inside the APK.
 */
class MiliAdminApi(private val baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    data class ActivationResult(val ok: Boolean, val status: String, val message: String)
    data class LicenseResult(val active: Boolean, val status: String, val plan: String = "Free")

    fun fetchPublicConfig(): JSONObject? = get("/api/public-config")

    fun requestActivation(context: Context, licenseKey: String, appVersion: String): ActivationResult {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        val payload = JSONObject().put("licenseKey", licenseKey.trim())
            .put("deviceId", deviceId).put("appVersion", appVersion)
        val result = post("/api/activation-request", payload)
        return ActivationResult(result?.optBoolean("ok") == true,
            result?.optString("status", "ERROR") ?: "ERROR",
            result?.optString("message", result?.optString("error", "Connection failed") ?: "Connection failed") ?: "Connection failed")
    }

    fun checkLicense(context: Context, licenseKey: String): LicenseResult {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        val result = post("/api/license/check", JSONObject().put("licenseKey", licenseKey.trim()).put("deviceId", deviceId))
        return LicenseResult(result?.optBoolean("active") == true,
            result?.optString("status", "ERROR") ?: "ERROR",
            result?.optString("plan", "Free") ?: "Free")
    }

    private fun get(path: String): JSONObject? = runCatching {
        val request = Request.Builder().url(baseUrl.trimEnd('/') + path).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONObject(response.body?.string().orEmpty())
        }
    }.getOrNull()

    private fun post(path: String, payload: JSONObject): JSONObject? = runCatching {
        val request = Request.Builder().url(baseUrl.trimEnd('/') + path)
            .post(payload.toString().toRequestBody(jsonType)).build()
        client.newCall(request).execute().use { response ->
            JSONObject(response.body?.string().orEmpty())
        }
    }.getOrNull()
}
