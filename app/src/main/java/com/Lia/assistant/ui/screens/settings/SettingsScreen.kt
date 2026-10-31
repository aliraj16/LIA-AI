package com.Lia.assistant.ui.screens.settings

import android.os.Build
import android.provider.Settings
import android.content.Intent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Lia.assistant.voice.FloatingMicOverlayService
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.AccessibilitySettingsRow
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonType
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaSectionHeader
import com.Lia.assistant.ui.components.NovaSettingsRow
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.SerifFontFamily
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun SettingsScreen(
    navController: NavController,
    appState: NovaAppState,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val context = LocalContext.current

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(ApiKeyStore.getKey(context)) }

    var showNameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(appState.assistantName) }
    var voiceMatchEnabled by remember { mutableStateOf(context.getSharedPreferences("mili_security", 0).getBoolean("voice_match_enabled", false)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Text(
                text = "Settings",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        }

        // Profile hero card
        NovaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { navController.navigate("profile") },
            elevation = 6.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(colors.accentGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", fontSize = 30.sp, fontWeight = FontWeight.Black, color = colors.accent)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("MILI AI", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                    Text("Your Personal AI Companion", fontSize = 12.sp, color = colors.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("POWERED BY ALIRAJ", fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accent, letterSpacing = 1.5.sp)
                }
            }
        }

        // Section 1: AI & Voice Intelligence
        NovaSectionHeader(title = "AI & Voice Intelligence")
        NovaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 4.dp
        ) {
            Column {
                NovaSettingsRow(
                    title = "Gemini API Key",
                    subtitle = if (ApiKeyStore.hasKey(context)) "Configured locally" else "Tap to configure",
                    icon = Icons.Default.Key,
                    onClick = { showApiKeyDialog = true }
                )
                NovaSettingsRow(
                    title = "Assistant Name",
                    subtitle = appState.assistantName,
                    icon = Icons.Default.Mic,
                    onClick = {
                        nameInput = appState.assistantName
                        showNameDialog = true
                    }
                )
                NovaSettingsRow(
                    title = "Personality",
                    subtitle = appState.personality.displayName,
                    icon = Icons.Default.Mood,
                    value = appState.personality.icon,
                    onClick = { navController.navigate("personality") }
                )
                NovaSettingsRow(
                    title = "Spoken Language",
                    subtitle = appState.language.displayName,
                    icon = Icons.Default.Language,
                    onClick = {
                        // Cycle languages
                        val nextLang = when (appState.language) {
                            LanguagePreference.AUTO -> LanguagePreference.BANGLA
                            LanguagePreference.BANGLA -> LanguagePreference.HINDI
                            LanguagePreference.HINDI -> LanguagePreference.HINGLISH
                            LanguagePreference.HINGLISH -> LanguagePreference.ENGLISH
                            LanguagePreference.ENGLISH -> LanguagePreference.AUTO
                        }
                        appState.applyLanguage(context, nextLang)
                    }
                )
                NovaSettingsRow(
                    title = "Gemini Voice",
                    subtitle = appState.voiceName,
                    icon = Icons.Default.RecordVoiceOver,
                    onClick = {
                        val nextVoice = when (appState.voiceName) {
                            "Aoede" -> "Puck"
                            "Puck" -> "Charon"
                            "Charon" -> "Kore"
                            "Kore" -> "Fenrir"
                            else -> "Aoede"
                        }
                        appState.applyVoiceName(context, nextVoice)
                    }
                )
            }
        }

        NovaSectionHeader(title = "Owner Security")
        NovaGlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 5.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VOICE MATCH · OWNER ONLY", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                        Text("Experimental setting — speaker verification model and secure enrollment are not connected yet.", color = colors.textSecondary, fontSize = 11.sp)
                    }
                    Switch(checked = voiceMatchEnabled, onCheckedChange = { enabled ->
                        voiceMatchEnabled = enabled
                        context.getSharedPreferences("mili_security", 0).edit().putBoolean("voice_match_enabled", enabled).apply()
                    })
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(if (voiceMatchEnabled) "Voice Match requested. Full owner-only blocking is not active until verification is integrated." else "Voice Match is off", color = colors.accent, fontSize = 12.sp)
            }
        }

        // Section 2: Look & Visuals
        NovaSectionHeader(title = "Look & Visuals")
        NovaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 6.dp
        ) {
            Column {
                NovaSettingsRow(
                    title = "3D Orb Style",
                    subtitle = appState.orbStyle,
                    icon = Icons.Default.ColorLens,
                    onClick = { navController.navigate("orb_style") }
                )
                NovaSettingsRow(
                    title = "Theme Mode",
                    subtitle = appState.themeMode,
                    icon = Icons.Default.DarkMode,
                    onClick = {
                        val nextMode = when (appState.themeMode) {
                            "DARK" -> "LIGHT"
                            "LIGHT" -> "SYSTEM"
                            else -> "DARK"
                        }
                        appState.applyThemeMode(context, nextMode)
                    }
                )
                NovaSettingsRow(
                    title = "Reduced Motion",
                    subtitle = "Minimize particle and parallax motion",
                    icon = Icons.Default.Animation,
                    switch = appState.reducedMotion,
                    onCheckedChange = { appState.applyReducedMotion(context, it) }
                )
            }
        }

        NovaSectionHeader(title = "Floating Assistant")
        NovaGlassCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 5.dp
        ) {
            val overlayAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
            val floatingEnabled = remember { mutableStateOf(context.getSharedPreferences("mili_floating_mic", 0).getBoolean("enabled", false)) }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FLOATING MICROPHONE", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                        Text(if (!overlayAllowed) "Allow display over other apps to show the floating mic." else if (floatingEnabled.value) "Floating mic is enabled. Tap it anywhere to start/stop MILI voice." else "Show a draggable microphone bubble over other apps." , color = colors.textSecondary, fontSize = 11.sp)
                    }
                    Switch(checked = floatingEnabled.value, onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}")))
                            } else {
                                floatingEnabled.value = true
                                context.getSharedPreferences("mili_floating_mic", 0).edit().putBoolean("enabled", true).apply()
                                FloatingMicOverlayService.show(context)
                            }
                        } else {
                            floatingEnabled.value = false
                            context.getSharedPreferences("mili_floating_mic", 0).edit().putBoolean("enabled", false).apply()
                            FloatingMicOverlayService.hide(context)
                        }
                    })
                }
                if (!overlayAllowed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NovaButton(text = "Grant Overlay Permission", onClick = {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}")))
                    })
                }
            }
        }

        // Section 3: Device & System
        NovaSectionHeader(title = "System & Permissions")
        NovaGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = 8.dp
        ) {
            Column {
                NovaSettingsRow(
                    title = "Permissions",
                    subtitle = "Microphone, Contacts, Calls",
                    icon = Icons.Default.Security,
                    onClick = { navController.navigate("permissions") }
                )
                // Flavor-specific Accessibility Row (active in direct, stub in play)
                AccessibilitySettingsRow()
            }
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text(text = "Gemini API Key", color = colors.textPrimary) },
            text = {
                Column {
                    Text(
                        text = "Enter your Google Gemini API key. It is stored securely on your device only.",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NovaTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = "AIzaSy..."
                    )
                }
            },
            confirmButton = {
                NovaButton(
                    text = "Save Key",
                    onClick = {
                        ApiKeyStore.saveKey(context, apiKeyInput)
                        showApiKeyDialog = false
                    }
                )
            },
            dismissButton = {
                NovaButton(
                    text = "Cancel",
                    onClick = { showApiKeyDialog = false },
                    type = NovaButtonType.TEXT
                )
            },
            containerColor = colors.surfaceRaised
        )
    }

    // Assistant Name Dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(text = "Rename Assistant", color = colors.textPrimary) },
            text = {
                NovaTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "Mili"
                )
            },
            confirmButton = {
                NovaButton(
                    text = "Save",
                    onClick = {
                        appState.applyAssistantName(context, nameInput)
                        showNameDialog = false
                    }
                )
            },
            dismissButton = {
                NovaButton(
                    text = "Cancel",
                    onClick = { showNameDialog = false },
                    type = NovaButtonType.TEXT
                )
            },
            containerColor = colors.surfaceRaised
        )
    }
}
