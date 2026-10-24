package com.example.ui.components

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.ai.ChatMessage
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle

@Composable
fun SousChefChatSheet(
    activeProfile: String,
    messages: List<ChatMessage>,
    isProcessing: Boolean,
    onSendMessage: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    onSelectRecipe: (RecipeEntity) -> Unit = {},
    onOpenErrorLogs: (() -> Unit)? = null,
    onCancelProcessing: (() -> Unit)? = null,
    onImportVideo: ((Uri) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    val context = LocalContext.current
    var detectedClipboardUrl by remember { mutableStateOf<String?>(null) }
    var detectedClipboardRecipeText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = clipboard?.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString()?.trim()
            if (!text.isNullOrBlank()) {
                if (text.startsWith("http://") || text.startsWith("https://")) {
                    detectedClipboardUrl = text
                } else if (com.example.ai.SousChefActionEngine.isPastedRecipeText(text)) {
                    detectedClipboardRecipeText = text
                }
            }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onSendMessage(spoken.trim())
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportVideo?.invoke(uri)
        }
    }

    androidx.activity.compose.BackHandler(onBack = onDismiss)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
    ) {
        val isTablet = maxWidth >= 600.dp

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isTablet) Alignment.Center else Alignment.BottomCenter
        ) {
            Surface(
                shape = if (isTablet) RoundedCornerShape(24.dp) else RoundedCornerShape(0.dp),
                color = Color(0xFFF9F5EE),
                shadowElevation = 24.dp,
                border = if (isTablet) BorderStroke(1.5.dp, Color(0xFFD6C7B2)) else null,
                modifier = if (isTablet) {
                    Modifier
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(0.90f)
                        .fillMaxHeight(0.85f)
                } else {
                    Modifier
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                        .fillMaxSize()
                }
            ) {
                SousChefContent(
                    activeProfile = activeProfile,
                    messages = messages,
                    isProcessing = isProcessing,
                    onSendMessage = onSendMessage,
                    onQuickActionClick = onQuickActionClick,
                    onSelectRecipe = onSelectRecipe,
                    onOpenErrorLogs = onOpenErrorLogs,
                    onCancelProcessing = onCancelProcessing,
                    onImportVideo = onImportVideo,
                    onDismiss = onDismiss,
                    detectedClipboardUrl = detectedClipboardUrl,
                    onClearClipboardUrl = { detectedClipboardUrl = null },
                    detectedClipboardRecipeText = detectedClipboardRecipeText,
                    onClearClipboardRecipeText = { detectedClipboardRecipeText = null },
                    speechLauncher = speechLauncher,
                    videoPickerLauncher = videoPickerLauncher,
                    languageMode = languageMode
                )
            }
        }
    }
}

@Composable
private fun SousChefContent(
    activeProfile: String,
    messages: List<ChatMessage>,
    isProcessing: Boolean,
    onSendMessage: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
    onSelectRecipe: (RecipeEntity) -> Unit = {},
    onOpenErrorLogs: (() -> Unit)? = null,
    onCancelProcessing: (() -> Unit)? = null,
    onImportVideo: ((Uri) -> Unit)? = null,
    onDismiss: () -> Unit,
    detectedClipboardUrl: String?,
    onClearClipboardUrl: () -> Unit,
    detectedClipboardRecipeText: String? = null,
    onClearClipboardRecipeText: () -> Unit = {},
    speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    videoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null,
    languageMode: LanguageMode = LanguageMode.ENGLISH
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size, isProcessing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Pinned Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👨‍🍳", fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppLocalization.getSousChefTitle(languageMode),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C1E14),
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "Active: $activeProfile's Cookbook",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF786555), fontSize = 12.sp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (videoPickerLauncher != null) {
                    IconButton(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFE0F2FE), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Import Video",
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onOpenErrorLogs != null) {
                    Surface(
                        onClick = onOpenErrorLogs,
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "System & Error Logs",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Logs / Errors",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFFEFE8DE), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF6B5B4E), modifier = Modifier.size(18.dp))
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE5DDD3))

        // Message Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyAssistantState(
                        activeProfile = activeProfile,
                        onQuickActionClick = onQuickActionClick,
                        onImportVideoClick = { videoPickerLauncher?.launch("video/*") },
                        languageMode = languageMode
                    )
                }
            }

            items(messages) { message ->
                ChatMessageBubble(
                    message = message,
                    onSelectRecipe = onSelectRecipe,
                    onOpenErrorLogs = onOpenErrorLogs
                )
            }

            if (isProcessing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = TerracottaPrimary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = if (languageMode == LanguageMode.GERMAN) "Küchen-Assistent überlegt..." else "Sous-Chef is thinking...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF786555),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }

                        if (onCancelProcessing != null) {
                            Surface(
                                onClick = onCancelProcessing,
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF3F4F6),
                                border = BorderStroke(0.5.dp, Color(0xFFD1D5DB))
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Clipboard Smart Bar (Links & Recipe Text)
        if (!detectedClipboardUrl.isNullOrBlank()) {
            Surface(
                color = Color(0xFFFFF8E7),
                border = BorderStroke(1.dp, Color(0xFFE6C875)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Recipe Link in Clipboard", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF78350F))
                            Text(detectedClipboardUrl, fontSize = 10.5.sp, color = Color(0xFF92400E), maxLines = 1)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            onClick = {
                                onQuickActionClick("Import this recipe: $detectedClipboardUrl")
                                onClearClipboardUrl()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text("Import", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        IconButton(onClick = onClearClipboardUrl, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF92400E), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        } else if (!detectedClipboardRecipeText.isNullOrBlank()) {
            Surface(
                color = Color(0xFFFFF8E7),
                border = BorderStroke(1.dp, Color(0xFFE6C875)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Recipe Text in Clipboard", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF78350F))
                            Text(detectedClipboardRecipeText.lines().firstOrNull { it.isNotBlank() } ?: "Pasted recipe", fontSize = 10.5.sp, color = Color(0xFF92400E), maxLines = 1)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            onClick = {
                                onSendMessage(detectedClipboardRecipeText)
                                onClearClipboardRecipeText()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFD97706)
                        ) {
                            Text("Import Text", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        IconButton(onClick = onClearClipboardRecipeText, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF92400E), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Quick Action Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                QuickActionChip(
                    icon = Icons.Default.AutoAwesome,
                    label = AppLocalization.getSousChefPillCreate(languageMode),
                    onClick = { inputText = if (languageMode == LanguageMode.GERMAN) "Erstelle ein Rezept für " else "Put together a recipe for " }
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.CameraAlt,
                    label = AppLocalization.getSousChefPillScan(activeProfile, languageMode),
                    onClick = { onQuickActionClick("Scan cards for $activeProfile") }
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.Link,
                    label = AppLocalization.getSousChefPillImport(languageMode),
                    onClick = { onQuickActionClick("Import from web") }
                )
            }
            if (videoPickerLauncher != null) {
                item {
                    QuickActionChip(
                        icon = Icons.Default.Videocam,
                        label = AppLocalization.getSousChefPillVideo(languageMode),
                        onClick = { videoPickerLauncher.launch("video/*") }
                    )
                }
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.Scale,
                    label = AppLocalization.getSousChefPillGrams(languageMode),
                    onClick = { onSendMessage(if (languageMode == LanguageMode.GERMAN) "Wie viele Gramm sind 1 Tasse Mehl?" else "How many grams in 1 cup of flour?") }
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Default.Restaurant,
                    label = AppLocalization.getSousChefPillSub(languageMode),
                    onClick = { onSendMessage(if (languageMode == LanguageMode.GERMAN) "Was kann ich statt Milch verwenden?" else "What can I use instead of milk?") }
                )
            }
            item {
                val switchLabel = when (languageMode) {
                    LanguageMode.GERMAN -> "Kochbuch wechseln"
                    LanguageMode.FRENCH -> "Changer de livre"
                    LanguageMode.ITALIAN -> "Cambia ricettario"
                    LanguageMode.SPANISH -> "Cambiar libro"
                    LanguageMode.DUTCH -> "Wissel kookboek"
                    else -> "Switch Cookbook"
                }
                QuickActionChip(
                    icon = Icons.Default.Person,
                    label = switchLabel,
                    onClick = { onQuickActionClick(if (languageMode == LanguageMode.GERMAN) "Zu anderem Kochbuch wechseln" else "Switch to another cookbook") }
                )
            }
        }

        // Sleek Compact Multi-line Bottom Input Surface
        Surface(
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.padding(bottom = 3.dp)) {
                    VoiceInputOrb(
                        onSpeechResult = { spoken ->
                            if (spoken.isNotBlank()) {
                                inputText = spoken
                                if (!isProcessing) {
                                    onSendMessage(spoken.trim())
                                    inputText = ""
                                }
                            }
                        },
                        onPartialResult = { partial ->
                            inputText = partial
                        },
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFFD6C8B4)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp, max = 130.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = AppLocalization.getSousChefPromptPlaceholder(languageMode),
                                fontSize = 13.sp,
                                color = Color(0xFF8C7A6B),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            singleLine = false,
                            maxLines = 5,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF1C120C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(TerracottaPrimary),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = {
                                    if (inputText.isNotBlank() && !isProcessing) {
                                        onSendMessage(inputText.trim())
                                        inputText = ""
                                    }
                                },
                                onSend = {
                                    if (inputText.isNotBlank() && !isProcessing) {
                                        onSendMessage(inputText.trim())
                                        inputText = ""
                                    }
                                }
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Default
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Box(modifier = Modifier.padding(bottom = 3.dp)) {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (inputText.isNotBlank() && !isProcessing) TerracottaPrimary else Color(0xFFE5DDD3),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isProcessing) Color.White else Color(0xFF9E8E81),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
