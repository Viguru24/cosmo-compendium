package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle
import kotlinx.coroutines.delay

@Composable
internal fun EmptyAssistantState(
    activeProfile: String,
    onQuickActionClick: (String) -> Unit,
    onImportVideoClick: () -> Unit = {},
    languageMode: LanguageMode
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👋", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppLocalization.getSousChefWelcome(languageMode),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1E14)
                    )
                )
            }

            Text(
                text = AppLocalization.getSousChefSubtitle(languageMode),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF6B5B4E),
                    lineHeight = 18.sp
                )
            )

            HorizontalDivider(color = Color(0xFFF0EAE1))

            PromptSuggestionItem(
                icon = "✨",
                title = if (languageMode == LanguageMode.GERMAN) "Neues Rezept erstellen (z.B. 'Erstelle ein Rezept für Hafermilch')" else "Put together a recipe (e.g. 'Put together a recipe for Oat milk')",
                onClick = { onQuickActionClick(if (languageMode == LanguageMode.GERMAN) "Erstelle ein Rezept für Hafermilch" else "Put together a recipe for Oat milk") }
            )

            PromptSuggestionItem(
                icon = "📹",
                title = if (languageMode == LanguageMode.GERMAN) "Rezept aus Kochvideo / Reel extrahieren" else "Extract recipe from Cooking Video / Reel",
                onClick = onImportVideoClick
            )

            PromptSuggestionItem(
                icon = "📸",
                title = AppLocalization.getSousChefPillScan(activeProfile, languageMode),
                onClick = { onQuickActionClick("Scan recipe cards for $activeProfile") }
            )

            PromptSuggestionItem(
                icon = "🔗",
                title = AppLocalization.getSousChefPillImport(languageMode),
                onClick = { onQuickActionClick("Import recipe from web link") }
            )

            PromptSuggestionItem(
                icon = "🥛",
                title = AppLocalization.getSousChefPillSub(languageMode),
                onClick = { onQuickActionClick(if (languageMode == LanguageMode.GERMAN) "Was kann ich statt Milch verwenden?" else "What can I use instead of milk?") }
            )

            PromptSuggestionItem(
                icon = "⚖️",
                title = AppLocalization.getSousChefPillGrams(languageMode),
                onClick = { onQuickActionClick(if (languageMode == LanguageMode.GERMAN) "Wie viele Gramm sind 1 Tasse Butter?" else "How many grams in 1 cup of butter?") }
            )
        }
    }
}

@Composable
internal fun PromptSuggestionItem(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFAF7F2),
        border = BorderStroke(1.dp, Color(0xFFE8DFD4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(icon, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF2C1E14),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Text("›", fontSize = 18.sp, color = Color(0xFF9E8E81), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD6C8B4)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(15.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF451A03)
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatMessageBubble(
    message: ChatMessage,
    onSelectRecipe: (RecipeEntity) -> Unit,
    onOpenErrorLogs: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val copyToClipboard = {
        clipboardManager.setText(AnnotatedString(message.text))
        isCopied = true
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    val isUser = message.isUser
    val isError = !isUser && (message.text.startsWith("⚠️") || message.text.contains("Error", ignoreCase = true) || message.text.contains("Could not extract", ignoreCase = true))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = if (isError) Color(0xFFFEE2E2) else TerracottaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isError) "⚠️" else "👨‍🍳", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = when {
                    isUser -> TerracottaPrimary
                    isError -> Color(0xFFFFF8F8)
                    else -> Color.White
                },
                border = when {
                    isUser -> null
                    isError -> BorderStroke(1.5.dp, Color(0xFFFCA5A5))
                    else -> BorderStroke(1.dp, Color(0xFFE5DDD3))
                },
                shadowElevation = if (isUser) 1.dp else 2.dp,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = copyToClipboard
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when {
                                isUser -> Color.White
                                isError -> Color(0xFF7F1D1D)
                                else -> Color(0xFF2C1E14)
                            },
                            lineHeight = 20.sp
                        )
                    )

                    // Dedicated Copy Action Pill for Assistant Responses
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = copyToClipboard,
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCopied) Color(0xFFDCFCE7) else Color(0xFFFAF7F2),
                                border = BorderStroke(1.dp, if (isCopied) Color(0xFF86EFAC) else Color(0xFFE8DFD4))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Answer",
                                        tint = if (isCopied) Color(0xFF16A34A) else Color(0xFF786555),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isCopied) "Copied!" else "Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCopied) Color(0xFF16A34A) else Color(0xFF786555)
                                    )
                                }
                            }
                        }
                    }

                    if (message.matchingRecipes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            message.matchingRecipes.forEach { recipe ->
                                Surface(
                                    onClick = { onSelectRecipe(recipe) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFAF7F2),
                                    border = BorderStroke(1.dp, Color(0xFFE5DDD3)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = recipe.getDisplayTitle(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2C1E14)
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = TerracottaPrimary.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = recipe.getDisplayCategory(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TerracottaPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                if (recipe.profileName.isNotBlank()) {
                                                    Text(
                                                        text = "• ${recipe.profileName}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF786555)
                                                    )
                                                }
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = TerracottaPrimary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "Open",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text("›", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isError && onOpenErrorLogs != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onOpenErrorLogs,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Error Report & Diagnostic Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
