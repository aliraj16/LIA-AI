package com.Lia.assistant.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.forge.ForgeIntent
import com.Lia.assistant.ui.components.ChatMessage
import com.Lia.assistant.ui.components.NovaMessageBubble
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTypingDots
import com.Lia.assistant.ui.fx.LiaOrb3D
import com.Lia.assistant.ui.theme.LocalNovaColors
import com.Lia.assistant.ui.theme.depthSurface
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.voice.ChatPromptBuilder
import com.Lia.assistant.voice.ChatReplyResult
import com.Lia.assistant.voice.ChatTurn
import com.Lia.assistant.voice.GeminiTextClient
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ChatScreen(
    navController: NavController,
    appState: NovaAppState,
    modifier: Modifier = Modifier
) {
    val colors = LocalNovaColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello! I am ${appState.assistantName}. How can I assist you today?",
                isUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // TTS lifecycle
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        messages.add(ChatMessage(text = trimmed, isUser = true))
        inputText = ""

        // Check Website Forge intent
        val forgePrompt = ForgeIntent.extract(trimmed)
        if (forgePrompt != null) {
            messages.add(
                ChatMessage(
                    text = "Opening the Website Forge for: \"$forgePrompt\"...",
                    isUser = false
                )
            )
            ForgeController.start(context, forgePrompt)
            navController.navigate("forge")
            return
        }

        // Call Gemini
        isTyping = true
        scope.launch(Dispatchers.IO) {
            val apiKey = ApiKeyStore.getKey(context)
            val systemInstruction = ChatPromptBuilder.build(
                personality = appState.personality,
                language = appState.language,
                assistantName = appState.assistantName
            )

            val history = messages.map { ChatTurn(isUser = it.isUser, text = it.text) }
            val result = GeminiTextClient.reply(apiKey, systemInstruction, history)

            withContext(Dispatchers.Main) {
                isTyping = false
                when (result) {
                    is ChatReplyResult.Success -> {
                        // Word-by-word reveal (20ms per word)
                        val fullReply = result.text
                        val words = fullReply.split(" ")
                        val placeholderId = java.util.UUID.randomUUID().toString()
                        val msg = ChatMessage(id = placeholderId, text = "", isUser = false, isStreaming = true)
                        messages.add(msg)

                        scope.launch {
                            val builder = StringBuilder()
                            for (w in words) {
                                if (builder.isNotEmpty()) builder.append(" ")
                                builder.append(w)
                                val idx = messages.indexOfFirst { it.id == placeholderId }
                                if (idx != -1) {
                                    messages[idx] = messages[idx].copy(text = builder.toString())
                                }
                                delay(20)
                            }
                            val finalIdx = messages.indexOfFirst { it.id == placeholderId }
                            if (finalIdx != -1) {
                                messages[finalIdx] = messages[finalIdx].copy(isStreaming = false)
                            }
                        }
                    }
                    is ChatReplyResult.Error -> {
                        messages.add(
                            ChatMessage(
                                text = "Error: ${result.message}",
                                isUser = false,
                                isError = true
                            )
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nightSky()
            .imePadding()
    ) {
        // Top Header
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

            // Small 40dp 3D orb in header
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { navController.navigate("voice") },
                contentAlignment = Alignment.Center
            ) {
                LiaOrb3D(
                    state = if (isTyping) NovaOrbState.THINKING else NovaOrbState.IDLE,
                    size = 40.dp,
                    amplitude = if (isTyping) 0.3f else 0f,
                    assistantName = appState.assistantName
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appState.assistantName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = if (isTyping) "Typing..." else "Here to help",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                NovaMessageBubble(
                    message = msg,
                    onSpeakClick = {
                        tts?.speak(msg.text, TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    onCopyClick = {
                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clip?.setPrimaryClip(ClipData.newPlainText("Lia message", msg.text))
                    },
                    onRetryClick = {
                        sendMessage(messages.lastOrNull { it.isUser }?.text ?: "")
                    }
                )
            }

            if (isTyping) {
                item {
                    NovaTypingDots(modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
                }
            }
        }

        // Floating Glass Input Bar
        val inputShape = RoundedCornerShape(28.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .depthSurface(shape = inputShape, elevation = 4.dp)
                .clip(inputShape)
                .background(colors.surfaceGlass)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NovaTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = "Message ${appState.assistantName}...",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .clickable { sendMessage(inputText) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = colors.onAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
