package com.Lia.assistant.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.AccessKeyBanner
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonType
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.fx.NovaOrbStyle
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.SerifFontFamily
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.VoiceSessionManager
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    appState: NovaAppState,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val orbState by VoiceSessionManager.state.collectAsState()
    val amplitude by VoiceSessionManager.amplitude.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val orbSize = screenWidth * 0.55f

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greetingPrefix = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Welcome back"
    }

    val trySayingQueries = listOf(
        "\"Lia, ek cafe ki website banao\"",
        "\"Call Mummy\"",
        "\"Open YouTube and search music\"",
        "\"What's the weather like today?\""
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .nightSky()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceGlass),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Logo",
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = appState.assistantName,
                    fontFamily = SerifFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceGlass)
                    .clickable { navController.navigate("profile") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Direct Access Key Banner (if not active in direct flavor)
        AccessKeyBanner(
            onActivateClick = { navController.navigate("profile") },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Time-based greeting in Serif font
        Text(
            text = "$greetingPrefix, ${appState.userName}",
            fontFamily = SerifFontFamily,
            fontSize = 28.sp,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Hero 3D Orb (~55% screen width)
        Box(
            modifier = Modifier
                .size(orbSize)
                .clickable { navController.navigate("voice") },
            contentAlignment = Alignment.Center
        ) {
            LiaOrb3D(
                state = orbState,
                size = orbSize,
                amplitude = amplitude,
                style = NovaOrbStyle.fromString(appState.orbStyle),
                assistantName = appState.assistantName
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tap me to talk",
            fontFamily = SerifFontFamily,
            fontSize = 15.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Floating action pills: Talk & Type
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NovaButton(
                text = "Talk",
                onClick = { navController.navigate("voice") },
                type = NovaButtonType.PRIMARY,
                icon = Icons.Default.Mic
            )
            NovaButton(
                text = "Type",
                onClick = { navController.navigate("chat") },
                type = NovaButtonType.SECONDARY
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // "Try saying" 3D tilted cards row
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TRY SAYING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = colors.accent,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                trySayingQueries.forEachIndexed { idx, query ->
                    NovaGlassCard(
                        modifier = Modifier
                            .width(220.dp)
                            .graphicsLayer {
                                rotationY = if (idx % 2 == 0) -4f else 4f
                                cameraDistance = 12f * density
                            }
                            .clickable {
                                navController.navigate("chat")
                            }
                    ) {
                        Text(
                            text = query,
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
