package com.Lia.assistant.agent.core

import android.graphics.Rect
import com.Lia.assistant.agent.NovaAccessibilityService
import java.util.UUID

interface ScreenDriver {
    suspend fun capture(): CapturedScreen?
    suspend fun clickElement(observation: ScreenObservation, element: UiElement): DriverResult
    suspend fun gestureTap(x: Float, y: Float): DriverResult
    suspend fun setText(element: UiElement, text: String): DriverResult
    suspend fun scroll(direction: String): DriverResult
    suspend fun back(): DriverResult
    suspend fun home(): DriverResult
}

class AccessibilityScreenDriver : ScreenDriver {
    private var lastObservationStamp = 0L

    override suspend fun capture(): CapturedScreen? {
        val service = NovaAccessibilityService.instance ?: return null
        val root = service.rootInActiveWindow ?: return null

        val elements = mutableListOf<UiElement>()
        val queue = java.util.LinkedList<android.view.accessibility.AccessibilityNodeInfo>()
        queue.add(root)

        val bounds = Rect()
        while (queue.isNotEmpty() && elements.size < 60) {
            val node = queue.poll() ?: continue
            node.getBoundsInScreen(bounds)

            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""
            val cls = node.className?.toString() ?: ""

            if (text.isNotEmpty() || desc.isNotEmpty() || node.isClickable || node.isEditable) {
                elements.add(
                    UiElement(
                        id = node.viewIdResourceName ?: UUID.randomUUID().toString(),
                        text = text,
                        contentDescription = desc,
                        className = cls,
                        bounds = Rect(bounds),
                        isClickable = node.isClickable,
                        isEditable = node.isEditable,
                        isScrollable = node.isScrollable
                    )
                )
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }

        lastObservationStamp = System.currentTimeMillis()
        return CapturedScreen(
            rawElements = elements,
            packageName = root.packageName?.toString() ?: "",
            windowClass = root.className?.toString() ?: "",
            width = 1080,
            height = 2400,
            timestamp = lastObservationStamp
        )
    }

    override suspend fun clickElement(observation: ScreenObservation, element: UiElement): DriverResult {
        if (observation.windowStamp < lastObservationStamp) {
            return DriverResult.STALE
        }
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        val tapRes = service.tapText(element.text.ifEmpty { element.contentDescription })
        return if (tapRes == "tapped") DriverResult.OK else DriverResult.FAILED
    }

    override suspend fun gestureTap(x: Float, y: Float): DriverResult {
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        val ok = service.gestureTap(x, y)
        return if (ok) DriverResult.OK else DriverResult.FAILED
    }

    override suspend fun setText(element: UiElement, text: String): DriverResult {
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        val res = service.typeText(text)
        return if (res == "typed") DriverResult.OK else DriverResult.FAILED
    }

    override suspend fun scroll(direction: String): DriverResult {
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        val res = service.scroll(direction)
        return if (res == "scrolled") DriverResult.OK else DriverResult.FAILED
    }

    override suspend fun back(): DriverResult {
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        return if (service.performBack() == "done") DriverResult.OK else DriverResult.FAILED
    }

    override suspend fun home(): DriverResult {
        val service = NovaAccessibilityService.instance ?: return DriverResult.FAILED
        return if (service.performHome() == "done") DriverResult.OK else DriverResult.FAILED
    }
}
