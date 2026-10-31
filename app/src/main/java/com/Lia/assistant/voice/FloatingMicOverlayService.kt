package com.Lia.assistant.voice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.Lia.assistant.MainActivity

/** User-enabled floating microphone shortcut. It does not secretly record: tapping it
 * explicitly starts/stops the app's existing voice session. */
class FloatingMicOverlayService : Service() {
    companion object {
    const val ACTION_SHOW = "com.Lia.assistant.action.SHOW_FLOATING_MIC"
    const val ACTION_HIDE = "com.Lia.assistant.action.HIDE_FLOATING_MIC"
    private const val PREFS = "mili_floating_mic"
    private const val KEY_VOICE_ACTIVE = "voice_active"

    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Allow display over other apps, then enable Floating Microphone again.", Toast.LENGTH_LONG).show()
            return
        }
        context.startService(Intent(context, FloatingMicOverlayService::class.java).setAction(ACTION_SHOW))
    }
    fun hide(context: Context) {
        context.startService(Intent(context, FloatingMicOverlayService::class.java).setAction(ACTION_HIDE))
    }
    }

    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HIDE) {
            removeBubble()
            stopSelf()
            return START_NOT_STICKY
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf(); return START_NOT_STICKY
        }
        if (bubble == null) createBubble()
        return START_STICKY
    }

    private fun createBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = TextView(this).apply {
            text = "🎙"
            textSize = 25f
            gravity = Gravity.CENTER
            contentDescription = "MILI AI floating microphone. Tap to start or stop voice chat."
            setTextColor(android.graphics.Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(0xFFB34DFF.toInt(), 0xFF5C4DFF.toInt(), 0xFFFF5FA2.toInt())
                orientation = GradientDrawable.Orientation.TL_BR
                setStroke((2 * resources.displayMetrics.density).toInt(), 0xCCFFFFFF.toInt())
            }
            elevation = 12f * resources.displayMetrics.density
            setOnTouchListener(DragTouchListener())
        }
        // minSdk is 26, so TYPE_APPLICATION_OVERLAY is available on every supported device.
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val params = WindowManager.LayoutParams(
            (58 * resources.displayMetrics.density).toInt(),
            (58 * resources.displayMetrics.density).toInt(),
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = (12 * resources.displayMetrics.density).toInt()
            y = 0
        }
        try {
            windowManager?.addView(view, params)
            bubble = view
            layoutParams = params
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var startX = 0; private var startY = 0
        private var touchX = 0f; private var touchY = 0f
        private var moved = false
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val p = layoutParams ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = p.x; startY = p.y; touchX = event.rawX; touchY = event.rawY; moved = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt(); val dy = (event.rawY - touchY).toInt()
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > 8) moved = true
                    p.x = (startX - dx).coerceAtLeast(0); p.y = startY + dy
                    try { windowManager?.updateViewLayout(v, p) } catch (_: Exception) {}
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) toggleVoice()
                    return true
                }
            }
            return false
        }
    }

    private fun toggleVoice() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val active = prefs.getBoolean(KEY_VOICE_ACTIVE, false)
        if (active) {
            VoiceForegroundService.stop(this)
            prefs.edit().putBoolean(KEY_VOICE_ACTIVE, false).apply()
            Toast.makeText(this, "MILI voice stopped", Toast.LENGTH_SHORT).show()
            bubble?.text = "🎙"
            return
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Open MILI AI and allow Microphone permission first.", Toast.LENGTH_LONG).show()
            val open = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(open)
            return
        }
        try {
            VoiceForegroundService.start(this)
            prefs.edit().putBoolean(KEY_VOICE_ACTIVE, true).apply()
            bubble?.text = "🔴"
            Toast.makeText(this, "MILI voice started", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not start voice session. Open MILI AI and try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun removeBubble() {
        try { bubble?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        bubble = null; layoutParams = null; windowManager = null
    }

    override fun onDestroy() { removeBubble(); super.onDestroy() }
}
