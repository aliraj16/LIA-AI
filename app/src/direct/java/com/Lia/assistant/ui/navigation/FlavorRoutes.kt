package com.Lia.assistant.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.registerAccessibilityDisclosureRoute(navController: NavController) {
    composable("accessibility_disclosure") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06050E))
                .padding(24.dp)
        ) {
            Text(
                text = "MILI AI Accessibility Disclosure\n\n" +
                        "MILI AI uses the Android AccessibilityService API strictly to perform actions requested by the user, such as reading screen elements, typing text, scrolling, and tapping buttons.\n\n" +
                        "No screen data is sent to external servers or logged permanently.",
                color = Color(0xFFEEE9FF)
            )
        }
    }
}
