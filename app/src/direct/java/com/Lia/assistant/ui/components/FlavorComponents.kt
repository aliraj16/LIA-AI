package com.Lia.assistant.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Lia.assistant.license.AccessKeyManager

@Composable
fun AccessibilitySettingsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF17143A))
            .clickable {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Screen Control (Accessibility)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEEE9FF)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Enables Lia to read the screen, tap buttons and type",
                fontSize = 13.sp,
                color = Color(0xFFA7A0CC)
            )
        }
        Text(
            text = "Configure",
            fontSize = 13.sp,
            color = Color(0xFFFFB547),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AccessKeyBanner(onActivateClick: () -> Unit, modifier: Modifier = Modifier) {
    val info by AccessKeyManager.info.collectAsState()
    if (!info.active || info.blocked) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF5A1E28))
                .clickable { onActivateClick() }
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (info.blocked) "Access Key Blocked - Tap to resolve" else "Access Key Required - Tap to activate",
                    color = Color(0xFFFFE0E3),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Activate",
                    color = Color(0xFFFFB547),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AccessKeySection(modifier: Modifier = Modifier) {
    val info by AccessKeyManager.info.collectAsState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF17143A))
            .padding(16.dp)
    ) {
        Text(
            text = "License & Access Key",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEEE9FF)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Plan: ${info.plan} (${info.statusText})",
            fontSize = 13.sp,
            color = Color(0xFFA7A0CC)
        )
    }
}
