package com.Lia.assistant.ui.fx

import android.content.Context
import android.os.Build
import android.provider.Settings

object OverlayEdgeGlowController {
    fun start(context: Context) {
        if (!isOverlayPermissionGranted(context)) return
        // Started by VoiceForegroundService when overlay permission is granted
    }

    fun stop(context: Context) {
        // Stop overlay view if attached
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}
