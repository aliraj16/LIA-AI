package com.Lia.assistant.agent

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay
import java.util.LinkedList

class NovaAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NovaAccessibilityService? = null
            private set

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val expectedServiceName = "${context.packageName}/${NovaAccessibilityService::class.java.name}"
            return enabledServices.contains(expectedServiceName) || enabledServices.contains(NovaAccessibilityService::class.java.simpleName)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Active event stream
    }

    override fun onInterrupt() {
        // Interrupted
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    fun readScreen(maxElements: Int = 40, maxLabelLength: Int = 40): List<String> {
        val root = rootInActiveWindow ?: return emptyList()
        val elements = mutableListOf<String>()
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        val seen = mutableSetOf<String>()

        while (queue.isNotEmpty() && elements.size < maxElements) {
            val node = queue.poll() ?: continue

            val text = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").trim()
            if (text.isNotEmpty() && text.length <= maxLabelLength) {
                val type = when {
                    node.isEditable -> "[input]"
                    node.isClickable -> "[button]"
                    node.isCheckable -> "[checkbox]"
                    else -> "[text]"
                }
                val line = "$type $text"
                if (seen.add(line)) {
                    elements.add(line)
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return elements
    }

    fun tapText(targetText: String): String {
        val root = rootInActiveWindow ?: return "element_not_found"
        val q = targetText.lowercase().trim()
        val node = findNodeMatchingText(root, q) ?: return "element_not_found"

        // Attempt accessibility click on the node or its clickable parents
        var curr: AccessibilityNodeInfo? = node
        while (curr != null) {
            if (curr.isClickable) {
                val clicked = curr.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (clicked) return "tapped"
            }
            curr = curr.parent
        }

        // Gesture tap fallback on node bounds
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val x = bounds.centerX().toFloat()
        val y = bounds.centerY().toFloat()
        return if (gestureTap(x, y)) "tapped" else "failed"
    }

    fun typeText(text: String): String {
        val root = rootInActiveWindow ?: return "no_focused_field"
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: findFirstEditable(root)
            ?: return "no_focused_field"

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val typed = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        return if (typed) "typed" else "failed"
    }

    fun scroll(direction: String): String {
        val root = rootInActiveWindow ?: return "nothing_scrollable"
        val scrollableNode = findFirstScrollable(root) ?: return "nothing_scrollable"
        val action = if (direction.equals("backward", ignoreCase = true) || direction.equals("up", ignoreCase = true)) {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }
        val scrolled = scrollableNode.performAction(action)
        return if (scrolled) "scrolled" else "failed"
    }

    fun performBack(): String {
        val ok = performGlobalAction(GLOBAL_ACTION_BACK)
        return if (ok) "done" else "failed"
    }

    fun performHome(): String {
        val ok = performGlobalAction(GLOBAL_ACTION_HOME)
        return if (ok) "done" else "failed"
    }

    fun gestureTap(x: Float, y: Float): Boolean {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        var completed = false
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                completed = true
            }
        }, null)
        return true
    }

    suspend fun autoTapSend(isWhatsApp: Boolean): String {
        for (i in 0 until 12) {
            delay(400)
            if (isWhatsApp) {
                tapText("Continue to chat")
            }
            val tappedSend = tapText("Send")
            if (tappedSend == "tapped") {
                return "sent"
            }
        }
        return "composer_opened_send_not_found"
    }

    private fun findNodeMatchingText(root: AccessibilityNodeInfo, query: String): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue
            val text = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").lowercase()
            if (text.contains(query)) {
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun findFirstEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue
            if (node.isEditable) return node
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    private fun findFirstScrollable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }
}
