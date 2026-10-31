package com.Lia.assistant.agent

import android.graphics.Rect
import com.Lia.assistant.agent.core.CapturedScreen
import com.Lia.assistant.agent.core.DriverResult
import com.Lia.assistant.agent.core.ScreenDriver
import com.Lia.assistant.agent.core.ScreenObservation
import com.Lia.assistant.agent.core.UiElement

class FakeScreenDriver : ScreenDriver {
    var nextCaptureResult: CapturedScreen? = null
    var nextClickResult: DriverResult = DriverResult.OK
    var clickedElements = mutableListOf<UiElement>()
    var typedTexts = mutableMapOf<String, String>()
    var gestureTaps = mutableListOf<Pair<Float, Float>>()

    override suspend fun capture(): CapturedScreen? = nextCaptureResult

    override suspend fun clickElement(observation: ScreenObservation, element: UiElement): DriverResult {
        clickedElements.add(element)
        return nextClickResult
    }

    override suspend fun gestureTap(x: Float, y: Float): DriverResult {
        gestureTaps.add(Pair(x, y))
        return DriverResult.OK
    }

    override suspend fun setText(element: UiElement, text: String): DriverResult {
        typedTexts[element.id] = text
        return DriverResult.OK
    }

    override suspend fun scroll(direction: String): DriverResult = DriverResult.OK

    override suspend fun back(): DriverResult = DriverResult.OK

    override suspend fun home(): DriverResult = DriverResult.OK
}
