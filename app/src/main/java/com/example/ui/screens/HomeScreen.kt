package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Message
import com.example.ui.theme.AccentGray
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.LightGray
import com.example.ui.theme.NearBlack
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextGray
import com.example.ui.viewmodel.ChatViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messagesState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userState.collectAsStateWithLifecycle()
    val activeMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val activeCreation by viewModel.activeCreationType.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var plusMenuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Scroll to latest message on insertion
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- TOP HEADER AREA ---
            HeaderBar(
                username = userProfile?.name ?: "User",
                onSettingsClick = { showSettings = true },
                onClearChatClick = {
                    viewModel.clearChat()
                    Toast.makeText(context, "History Cleared", Toast.LENGTH_SHORT).show()
                }
            )

            // --- CONVERSATION / DISPLAY AREA ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    // Empty Conversation Placeholder
                    EmptyConversationState(username = userProfile?.name ?: "User")
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(message = message)
                        }
                        if (isGenerating) {
                            item {
                                TypingShimmerIndicator()
                            }
                        }
                        // Spacer for bottom offset
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // --- SECTOR MODE SELECTOR ---
            ModeSelectorBar(
                selectedMode = activeMode,
                onModeSelected = { viewModel.selectMode(it) }
            )

            // --- BOTTOM ASKING BAR ---
            AskingBar(
                textInput = textInput,
                onTextChange = { textInput = it },
                activeCreationType = activeCreation,
                onCancelCreation = { viewModel.setCreationType(null) },
                onPlusClick = { plusMenuExpanded = true },
                onSendClick = {
                    viewModel.sendMessage(textInput)
                    textInput = ""
                },
                plusMenuExpanded = plusMenuExpanded,
                onPlusDismiss = { plusMenuExpanded = false },
                onPlusOptionSelected = { type ->
                    viewModel.setCreationType(type)
                    plusMenuExpanded = false
                    Toast.makeText(context, "Ready to generate: ${type?.uppercase()}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // --- SETTINGS OVERLAY PANEL ---
        if (showSettings) {
            SettingsOverlay(
                username = userProfile?.name ?: "User",
                phoneNumber = userProfile?.phoneNumber ?: "Not Added",
                onDismiss = { showSettings = false },
                onLogout = {
                    showSettings = false
                    viewModel.logout()
                },
                onSaveProfile = { newName ->
                    viewModel.login(newName, userProfile?.phoneNumber ?: "")
                    Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun HeaderBar(
    username: String,
    onSettingsClick: () -> Unit,
    onClearChatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("app_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hello",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(PureWhite, shape = CircleShape)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onClearChatClick,
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear History",
                    tint = TextGray
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PureWhite
                )
            }
        }
    }
}

@Composable
fun EmptyConversationState(
    username: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kya madad kar sakta hoon?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = PureWhite,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Welcome back, $username. Ask me any NCERT study doubt, or select creative media tools using the '+' button.",
            fontSize = 14.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ModeSelectorBar(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf("Echo", "Light", "Flow")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .background(NearBlack, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        modes.forEach { mode ->
            val isSelected = selectedMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PureWhite else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .testTag("mode_${mode.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PureBlack else TextGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskingBar(
    textInput: String,
    onTextChange: (String) -> Unit,
    activeCreationType: String?,
    onCancelCreation: () -> Unit,
    onPlusClick: () -> Unit,
    onSendClick: () -> Unit,
    plusMenuExpanded: Boolean,
    onPlusDismiss: () -> Unit,
    onPlusOptionSelected: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Active Creation Indicator Overlay
        AnimatedVisibility(visible = activeCreationType != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (activeCreationType) {
                            "image" -> Icons.Default.Image
                            "video" -> Icons.Default.Movie
                            "music" -> Icons.Default.LibraryMusic
                            "animation" -> Icons.Default.Animation
                            else -> Icons.Default.Add
                        },
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mode: Creating ${activeCreationType?.uppercase()} with AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PureWhite
                    )
                }
                IconButton(
                    onClick = onCancelCreation,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Creation",
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Liquid Glass styled Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .background(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plus (+) Button
            Box {
                IconButton(
                    onClick = onPlusClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .testTag("plus_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Open creation menu",
                        tint = PureWhite
                    )
                }

                // Custom styled Dropdown Menu
                DropdownMenu(
                    expanded = plusMenuExpanded,
                    onDismissRequest = onPlusDismiss,
                    modifier = Modifier
                        .background(NearBlack)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .width(220.dp)
                ) {
                    val items = listOf(
                        Triple("Upload Image", "upload_image", Icons.Default.Image),
                        Triple("Upload Video", "upload_video", Icons.Default.VideoFile),
                        Triple("Create Image (AI Gen)", "image", Icons.Default.Image),
                        Triple("Create Animation Video", "animation", Icons.Default.Animation),
                        Triple("Create Music (AI Gen)", "music", Icons.Default.LibraryMusic),
                        Triple("Create Video (AI Gen)", "video", Icons.Default.Movie)
                    )

                    items.forEach { (label, type, icon) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(label, color = PureWhite, fontSize = 14.sp)
                                }
                            },
                            onClick = { onPlusOptionSelected(type) },
                            modifier = Modifier.testTag("plus_option_$type")
                        )
                    }
                }
            }

            // Text Input field
            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = if (activeCreationType != null) "Describe what to generate..." else "Puchho kuch bhi...",
                        color = TextGray,
                        fontSize = 15.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = PureWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("asking_input")
            )

            // Send Button: Black circular button with white arrow inside (Matches prompt guidelines literally)
            IconButton(
                onClick = onSendClick,
                enabled = textInput.isNotBlank() || activeCreationType != null,
                modifier = Modifier
                    .size(44.dp)
                    .background(PureBlack, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Mode metadata tag
            Text(
                text = "${message.mode} Mode" + if (message.mediaType != null) " • ${message.mediaType.uppercase()}" else "",
                fontSize = 10.sp,
                color = AccentGray,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
            )

            // Main text message bubble
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) CardDark else SurfaceDark
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isUser) BorderDark else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.testTag(if (isUser) "user_message" else "model_message")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.text,
                        color = PureWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    // Rich Media Display (Custom AI Content Generation layouts)
                    if (message.mediaType != null && message.role == "model") {
                        Spacer(modifier = Modifier.height(12.dp))
                        when (message.mediaType) {
                            "image" -> {
                                ImageGenLayout(imageUrl = message.mediaUri)
                            }
                            "music" -> {
                                MusicGenPlayer(prompt = message.promptText ?: "Sitar Track")
                            }
                            "video" -> {
                                VideoGenPlayer(prompt = message.promptText ?: "Video Clip")
                            }
                            "animation" -> {
                                AnimationGenViewer(prompt = message.promptText ?: "3D Object")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingShimmerIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hello is typing",
                    color = TextGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    color = PureWhite,
                    trackColor = BorderDark,
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                )
            }
        }
    }
}

// --- RICH MEDIA CONTENT CREATION RENDERERS ---

@Composable
fun ImageGenLayout(imageUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .background(PureBlack)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "AI Generated Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Generating premium monochrome art...", color = TextGray, fontSize = 13.sp)
            }
        }

        // Overlay brand badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(PureBlack.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("Hello AI", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MusicGenPlayer(prompt: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableFloatStateOf(0f) }

    // Dynamic Waveform animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing_bars")
    val barHeightFactor1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "bar1"
    )
    val barHeightFactor2 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "bar2"
    )
    val barHeightFactor3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "bar3"
    )

    // Progress ticking
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (playProgress < 1f) {
                kotlinx.coroutines.delay(200)
                playProgress += 0.01f
            }
            isPlaying = false
            playProgress = 0f
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(36.dp)
                            .background(PureWhite, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Play Track",
                            tint = PureBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Sitar Electro Fusion",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "30s • Ambient Classical Track",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Dynamic Audio Bars
                Row(
                    modifier = Modifier.height(24.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val bars = listOf(barHeightFactor1, barHeightFactor2, barHeightFactor3, barHeightFactor1 * 0.8f)
                    bars.forEach { heightFactor ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.5.dp)
                                .width(3.dp)
                                .fillMaxHeight(if (isPlaying) heightFactor else 0.2f)
                                .background(PureWhite, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player seek progress
            Slider(
                value = playProgress,
                onValueChange = { playProgress = it },
                colors = SliderDefaults.colors(
                    activeTrackColor = PureWhite,
                    inactiveTrackColor = BorderDark,
                    thumbColor = PureWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun VideoGenPlayer(prompt: String) {
    var isPlaying by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "video_rotation")
    
    // Animate moving starry fields inside video Canvas
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ), label = "rotation"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Visual simulated player Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Draw a sleek circular scanner representing "video frames"
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 200f,
                    center = center
                )
                
                // Generative orbital rings mapping prompt parameters
                val radius = 100f + if (isPlaying) 20f * sin(rotationAngle * Math.PI / 180).toFloat() else 0f
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Render cinematic camera brackets
                val padding = 20f
                // Top-Left
                drawLine(PureWhite, Offset(padding, padding), Offset(padding + 20f, padding), strokeWidth = 3f)
                drawLine(PureWhite, Offset(padding, padding), Offset(padding, padding + 20f), strokeWidth = 3f)
                // Bottom-Right
                drawLine(PureWhite, Offset(size.width - padding, size.height - padding), Offset(size.width - padding - 20f, size.height - padding), strokeWidth = 3f)
                drawLine(PureWhite, Offset(size.width - padding, size.height - padding), Offset(size.width - padding, size.height - padding - 20f), strokeWidth = 3f)
            }

            // Controls Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = if (isPlaying) 0.8f else 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE PLAY", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("1080p • AI RENDER", color = TextGray, fontSize = 9.sp)
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.dp, BorderDark, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Play clip",
                        tint = PureWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = if (isPlaying) "Playing Rendered Video..." else "Press play to render animation",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun AnimationGenViewer(prompt: String) {
    var isRunning by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "cube_rotation")
    val rotX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ), label = "rx"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = PureBlack),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Interactive 3D Wireframe simulation in Compose Canvas!
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val scale = 80f
                
                // Rotated Projection coordinates representing a simple 3D wireframe cube
                val angle = if (isRunning) rotX * Math.PI / 180 else 45.0 * Math.PI / 180
                
                val vertices = listOf(
                    Offset((-1 * cos(angle) - -1 * sin(angle)).toFloat(), (-1 * sin(angle) + -1 * cos(angle)).toFloat()),
                    Offset((1 * cos(angle) - -1 * sin(angle)).toFloat(), (1 * sin(angle) + -1 * cos(angle)).toFloat()),
                    Offset((1 * cos(angle) - 1 * sin(angle)).toFloat(), (1 * sin(angle) + 1 * cos(angle)).toFloat()),
                    Offset((-1 * cos(angle) - 1 * sin(angle)).toFloat(), (-1 * sin(angle) + 1 * cos(angle)).toFloat())
                ).map { vertex ->
                    Offset(center.x + vertex.x * scale, center.y + vertex.y * scale)
                }

                // Draw wireframe connections
                if (vertices.size >= 4) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.08f),
                        topLeft = Offset(center.x - scale, center.y - scale),
                        size = Size(scale * 2, scale * 2)
                    )
                    
                    drawLine(PureWhite.copy(alpha = 0.4f), vertices[0], vertices[1], strokeWidth = 2f)
                    drawLine(PureWhite.copy(alpha = 0.4f), vertices[1], vertices[2], strokeWidth = 2f)
                    drawLine(PureWhite.copy(alpha = 0.4f), vertices[2], vertices[3], strokeWidth = 2f)
                    drawLine(PureWhite.copy(alpha = 0.4f), vertices[3], vertices[0], strokeWidth = 2f)

                    // Connect projection perspectives
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x - scale, center.y - scale), vertices[0])
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x + scale, center.y - scale), vertices[1])
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x + scale, center.y + scale), vertices[2])
                    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x - scale, center.y + scale), vertices[3])
                }
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .size(36.dp)
                        .background(PureWhite, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = "Animate",
                        tint = PureBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (isRunning) "Active Script Engine..." else "Static 3D Wireframe View",
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// --- SETTINGS OVERLAY COMPOSABLE ---

@Composable
fun SettingsOverlay(
    username: String,
    phoneNumber: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onSaveProfile: (String) -> Unit
) {
    var editName by remember { mutableStateOf(username) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.85f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) { }
                .testTag("settings_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hello Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PureWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1: Account
                Text(
                    text = "ACCOUNT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Display Name", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = BorderDark,
                        cursorColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSaveProfile(editName) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PureBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Security & Device Info
                Text(
                    text = "SECURITY & SERVICES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("OTP Secure Login", color = PureWhite, fontSize = 14.sp)
                    Text("Active", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Phone Number", color = PureWhite, fontSize = 14.sp)
                    Text("+91 $phoneNumber", color = TextGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Red
                    ),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button")
                ) {
                    Text("Sign Out from Hello", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
