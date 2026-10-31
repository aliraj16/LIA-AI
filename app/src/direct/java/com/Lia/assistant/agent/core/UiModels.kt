package com.Lia.assistant.agent.core

import android.graphics.Rect

data class UiElement(
    val id: String,
    val text: String,
    val contentDescription: String,
    val className: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isScrollable: Boolean
)

data class ScreenObservation(
    val observationId: String,
    val packageName: String,
    val windowClass: String,
    val elements: List<UiElement>,
    val screenWidth: Int,
    val screenHeight: Int,
    val windowStamp: Long
)

data class CapturedScreen(
    val rawElements: List<UiElement>,
    val packageName: String,
    val windowClass: String,
    val width: Int,
    val height: Int,
    val timestamp: Long
)

enum class DriverResult {
    OK,
    FAILED,
    STALE,
    NOT_FOUND,
    UNSUPPORTED
}

enum class ExecPhase {
    PREPARING,
    EXECUTING,
    VERIFYING
}

enum class ActionMethod {
    ACCESSIBILITY_CLICK,
    GESTURE_ON_ELEMENT_BOUNDS,
    VISION_TAP,
    SET_TEXT
}

enum class Blocker {
    LOGIN,
    CAPTCHA,
    TWO_FACTOR,
    SECURITY_CHECK,
    PERMISSION_PROMPT
}

enum class FailureReason {
    ELEMENT_NOT_FOUND,
    TARGET_STALE,
    ACTION_TIMEOUT,
    APP_CRASHED,
    UNEXPECTED_SCREEN
}

sealed class ActionResult {
    object Success : ActionResult()
    data class Failed(val reason: FailureReason, val message: String) : ActionResult()
    data class BlockedResult(val blocker: Blocker) : ActionResult()
}
