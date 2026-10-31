package com.Lia.assistant.agent.core

import java.util.UUID

object ScreenObserver {
    fun observe(captured: CapturedScreen): ScreenObservation {
        return ScreenObservation(
            observationId = "obs_${UUID.randomUUID()}",
            packageName = captured.packageName,
            windowClass = captured.windowClass,
            elements = captured.rawElements,
            screenWidth = captured.width,
            screenHeight = captured.height,
            windowStamp = captured.timestamp
        )
    }
}

object TargetResolver {
    fun resolve(specText: String, observation: ScreenObservation): UiElement? {
        val q = specText.lowercase().trim()
        if (q.isEmpty()) return null

        // 1. Exact text or description match
        val exact = observation.elements.firstOrNull {
            it.text.equals(q, ignoreCase = true) || it.contentDescription.equals(q, ignoreCase = true)
        }
        if (exact != null) return exact

        // 2. Starts with or contains
        val prefix = observation.elements.firstOrNull {
            it.text.lowercase().startsWith(q) || it.contentDescription.lowercase().startsWith(q)
        }
        if (prefix != null) return prefix

        return observation.elements.firstOrNull {
            it.text.lowercase().contains(q) || it.contentDescription.lowercase().contains(q)
        }
    }
}

class UiActionExecutor(private val driver: ScreenDriver) {

    suspend fun executeClick(
        targetSpec: String,
        observation: ScreenObservation
    ): ActionResult {
        val target = TargetResolver.resolve(targetSpec, observation)
            ?: return ActionResult.Failed(FailureReason.ELEMENT_NOT_FOUND, "Could not find element matching '$targetSpec'")

        val result = driver.clickElement(observation, target)
        return when (result) {
            DriverResult.OK -> ActionResult.Success
            DriverResult.STALE -> ActionResult.Failed(FailureReason.TARGET_STALE, "Observation is stale")
            DriverResult.NOT_FOUND -> ActionResult.Failed(FailureReason.ELEMENT_NOT_FOUND, "Element not found")
            else -> ActionResult.Failed(FailureReason.ACTION_TIMEOUT, "Click action failed")
        }
    }

    suspend fun executeType(
        targetSpec: String,
        textToType: String,
        observation: ScreenObservation
    ): ActionResult {
        val target = TargetResolver.resolve(targetSpec, observation)
            ?: return ActionResult.Failed(FailureReason.ELEMENT_NOT_FOUND, "Target field not found")

        val result = driver.setText(target, textToType)
        return if (result == DriverResult.OK) {
            ActionResult.Success
        } else {
            ActionResult.Failed(FailureReason.ACTION_TIMEOUT, "Failed to type into field")
        }
    }
}
