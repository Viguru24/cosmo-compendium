package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

enum class GuideTopic(
    val title: String,
    val emoji: String,
    val icon: ImageVector,
    val summary: String
) {
    CARD_SCANNING(
        title = "Recipe Cards",
        emoji = "📸",
        icon = Icons.Default.CameraAlt,
        summary = "Continuous multi-page camera capture with instant AI synthesis"
    ),
    VIDEO_EXTRACTION(
        title = "Cooking Videos",
        emoji = "📹",
        icon = Icons.Default.Videocam,
        summary = "Extract full recipes & covers from Reels, TikTok clips & screen recordings"
    ),
    BILINGUAL_HEIRLOOM(
        title = "Any Language / Handwriting",
        emoji = "🌍",
        icon = Icons.Default.Language,
        summary = "Transcribe recipes from any language worldwide (German, French, Italian, Spanish, etc.) into English"
    ),
    SOUS_CHEF(
        title = "Sous-Chef Copilot",
        emoji = "👨‍🍳",
        icon = Icons.Default.Mic,
        summary = "Voice commands, recipe search, substitutions & unit questions"
    ),
    FAMILY_COOKBOOKS(
        title = "Family Cookbooks",
        emoji = "👥",
        icon = Icons.Default.Group,
        summary = "Individual member books (Louis, Annette, Isabel) & 1-tap transfers"
    ),
    COOK_MODE(
        title = "Kitchen Cook Mode",
        emoji = "🍳",
        icon = Icons.Default.Restaurant,
        summary = "Hands-free step readout, auto timers, and 0.5x / 1x / 2x scaling"
    ),
    SMART_CONVERTER(
        title = "Smart Converter",
        emoji = "⚖️",
        icon = Icons.Default.Scale,
        summary = "Instant kitchen volume-to-weight conversions (cups, grams, tbsp, oz)"
    ),
    BACKUP_SYNC(
        title = "Safety Backup & Sync",
        emoji = "🔄",
        icon = Icons.Default.CloudSync,
        summary = "Offline encrypted backups and multi-device private cloud sync"
    )
}

@Composable
fun CookbookGuideDialog(
    initialTopic: GuideTopic = GuideTopic.CARD_SCANNING,
    onDismiss: () -> Unit,
    onStartScanCards: (() -> Unit)? = null,
    onStartImportVideo: (() -> Unit)? = null,
    onOpenSousChef: (() -> Unit)? = null,
    onOpenConverter: (() -> Unit)? = null
) {
    var selectedTopic by remember { mutableStateOf(initialTopic) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 840.dp)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFFFBF8F4),
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, Color(0xFFE5DDD0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(TerracottaPrimary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "Cookbook Guide & How It Works",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color(0xFF2C241E)
                                )
                            )
                            Text(
                                text = "Learn how to get the most out of your family cookbook",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF7A6E63)
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEFE8DE), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF5A4D41),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Topic Selector Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(GuideTopic.values()) { topic ->
                        val isSelected = topic == selectedTopic
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTopic = topic },
                            color = if (isSelected) TerracottaPrimary else Color(0xFFEFE8DE),
                            contentColor = if (isSelected) Color.White else Color(0xFF4A3E34),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFDFD5C8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(topic.emoji, fontSize = 16.sp)
                                Text(
                                    text = topic.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFEFE8DE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Topic Detail Content (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTopic) {
                        GuideTopic.CARD_SCANNING -> {
                            GuideCardSection(
                                emoji = "📸",
                                title = "Rapid Continuous Multi-Page Scanning",
                                badge = "Vision AI",
                                intro = "Capture physical recipe cards, handwritten index cards, magazine clippings, or entire notebook spreads without friction.",
                                steps = listOf(
                                    "Tap 'Scan Recipe' from the bookshelf or ask Sous-Chef 'Scan cards'." to "The camera opens immediately in fast-capture mode.",
                                    "Snap Page 1 (Front / Ingredients)" to "The camera instantly resets for Page 2 with zero delay.",
                                    "Snap Page 2 (Back / Steps), Page 3, Page 4..." to "Keep snapping as many pages or inserts as needed for that recipe.",
                                    "Press the Back button when finished" to "AI immediately synthesizes all captured pages together and saves the recipe into your cookbook automatically."
                                ),
                                tips = listOf(
                                    "Food Photos Auto-Cropping: If the recipe card has a photo of the dish, the AI automatically detects it, crops it, and sets it as the recipe's cover image.",
                                    "Offline Resilience: If network is unavailable, our built-in offline engine transcribes the text directly so no work is ever lost."
                                ),
                                actionLabel = "Try Scanning a Card",
                                onActionClick = onStartScanCards
                            )
                        }

                        GuideTopic.VIDEO_EXTRACTION -> {
                            GuideCardSection(
                                emoji = "📹",
                                title = "Cooking Video & Reel Recipe Extraction",
                                badge = "Multimodal AI",
                                intro = "Turn Instagram Reels, TikTok cooking videos, YouTube Shorts, or screen recordings into clean, formatted recipes—even if there is no recipe written in the caption!",
                                steps = listOf(
                                    "Save the Video or Screen Record" to "Use Instagram's built-in download button (Share > Download) or record a 15-30s screen recording of the clip.",
                                    "Tap the Video Button (📹)" to "Available in the Sous-Chef assistant or within the Scan Recipe dialog.",
                                    "Select the Video File" to "Pick the video from your phone/tablet gallery.",
                                    "Automatic AI Keyframe Synthesis" to "Our vision engine samples chronological cooking keyframes, extracts all ingredients and instructions, and saves the plated dish as the cover photo!"
                                ),
                                tips = listOf(
                                    "Works on Any Clip: Perfect for quick recipe videos where measurements are shown on screen or spoken by the chef.",
                                    "100% Private & Safe: Runs locally on your device without scraping external servers."
                                ),
                                actionLabel = "Import a Cooking Video",
                                onActionClick = onStartImportVideo
                            )
                        }

                        GuideTopic.BILINGUAL_HEIRLOOM -> {
                            GuideCardSection(
                                emoji = "🌍",
                                title = "Any Language & Vintage Handwriting",
                                badge = "Global Translation",
                                intro = "Preserve recipes and craft formulas in any language worldwide with instant translation into clear English.",
                                steps = listOf(
                                    "Scan Cards in Any Language" to "Supports German (including old Sütterlin / Kurrent cursive), French, Italian, Spanish, Polish, Russian, Japanese, etc.",
                                    "Dual-Language Storage" to "The app preserves the authentic original language text alongside a clean English translation.",
                                    "Instant Language Toggle" to "Switch between 'English Translation' and the original recipe text anytime from the recipe view or top bar."
                                ),
                                tips = listOf(
                                    "International Units Standardized: Metric, imperial, and historic measurements are neatly formatted for easy reading.",
                                    "Full Searchability: Search for recipes in either English or their original language."
                                ),
                                actionLabel = null,
                                onActionClick = null
                            )
                        }

                        GuideTopic.SOUS_CHEF -> {
                            GuideCardSection(
                                emoji = "👨‍🍳",
                                title = "Sous-Chef AI Copilot & Voice Commands",
                                badge = "Voice Assistant",
                                intro = "Your smart kitchen companion. Tap the Sous-Chef pill or microphone to speak or type naturally.",
                                steps = listOf(
                                    "Voice Commands" to "Say 'Scan 3 cards for Annette' or 'Switch to Isabel's cookbook'.",
                                    "Cooking Substitutions" to "Ask 'What can I substitute for buttermilk?' or 'Can I make this gluten-free?'.",
                                    "Measurement Questions" to "Ask 'How many grams in 1.5 cups of flour?' or 'How long to roast a 2kg chicken?'.",
                                    "Web Link Importing" to "Paste any recipe web URL (BBC Good Food, AllRecipes, NYT Cooking) to instantly import clean text."
                                ),
                                tips = listOf(
                                    "Hands-Free Kitchen Help: Tap the mic button anytime to ask quick questions while cooking.",
                                    "Error Log Transparency: If a scan or web import fails, tap 'Logs / Errors' in Sous-Chef to see exactly what happened."
                                ),
                                actionLabel = "Open Sous-Chef",
                                onActionClick = onOpenSousChef
                            )
                        }

                        GuideTopic.FAMILY_COOKBOOKS -> {
                            GuideCardSection(
                                emoji = "👥",
                                title = "Family Cookbooks & Profiles",
                                badge = "Multi-User",
                                intro = "Give every family member their own dedicated cookbook while keeping the entire collection in one place.",
                                steps = listOf(
                                    "Who's Cooking?" to "Tap the profile pill in the top-left (e.g. 'Louis's Cookbook') to switch books.",
                                    "Set Device Default" to "Designate which cookbook opens by default on your personal phone or tablet.",
                                    "1-Tap Move & Transfer" to "Easily transfer recipes between family members or view 'All Family Recipes' to see the full collection.",
                                    "Add & Rename Profiles" to "Create new cookbooks for any family member (kids, parents, grandparents)."
                                ),
                                tips = listOf(
                                    "Quick Assignment: Long-press or tap the 3-dot menu on any recipe card to reassign it to another family member instantly."
                                ),
                                actionLabel = null,
                                onActionClick = null
                            )
                        }

                        GuideTopic.COOK_MODE -> {
                            GuideCardSection(
                                emoji = "🍳",
                                title = "Kitchen Cook Mode & Hands-Free Timers",
                                badge = "Cooking Assistant",
                                intro = "A full-screen, distraction-free cooking interface designed for the counter next to your stove.",
                                steps = listOf(
                                    "Enter Cook Mode" to "Tap 'Start Cooking' on any recipe card.",
                                    "Interactive Checklists" to "Cross off ingredients as you prep them to stay organized.",
                                    "Voice Step Readout" to "Tap the speaker icon to have each step read aloud so you don't have to touch the screen with messy hands.",
                                    "Automatic Timers" to "The app automatically detects baking/boiling times (e.g., '25 minutes') and lets you start a timer with 1 tap.",
                                    "Portion Scaling" to "Scale recipes to 0.5x, 1x, 2x, or 3x with automatic ingredient measurement recalculations."
                                ),
                                tips = listOf(
                                    "Screen Stays On: The display prevents screen timeout while in Cook Mode so you never have to unlock your phone with flour on your fingers."
                                ),
                                actionLabel = null,
                                onActionClick = null
                            )
                        }

                        GuideTopic.SMART_CONVERTER -> {
                            GuideCardSection(
                                emoji = "⚖️",
                                title = "Smart Kitchen Unit Converter",
                                badge = "Baking Tools",
                                intro = "Convert any kitchen ingredient accurately between volume and weight.",
                                steps = listOf(
                                    "Open Converter" to "Tap the ⚖️ icon in the top bar or inside any recipe view.",
                                    "Ingredient Density Awareness" to "Converts accurately based on ingredient density (e.g., 1 cup of Flour is 120g, whereas 1 cup of Sugar is 200g).",
                                    "Instant Unit Switching" to "Easily toggle between Cups, Tablespoons, Teaspoons, Grams, Ounces, Pounds, and Milliliters."
                                ),
                                tips = listOf(
                                    "Direct Recipe Integration: Tap any measurement in a recipe ingredient list to convert it on the spot."
                                ),
                                actionLabel = "Open Kitchen Converter",
                                onActionClick = onOpenConverter
                            )
                        }

                        GuideTopic.BACKUP_SYNC -> {
                            GuideCardSection(
                                emoji = "🔄",
                                title = "Safety Backups & Private Cloud Sync",
                                badge = "Data Safety",
                                intro = "Keep your precious family recipes completely safe, backed up, and synchronized across all your devices.",
                                steps = listOf(
                                    "Automatic Local Backups" to "The app automatically generates secure, local backup files in your phone's storage.",
                                    "1-Tap Restore & Export" to "Export entire cookbooks as `.zip` or `.json` archives to share with family or restore to a new phone.",
                                    "Private Cloud Sync" to "Sync seamlessly between your Phone, Tablet, and home server using private Nextcloud / WebDAV sync."
                                ),
                                tips = listOf(
                                    "Local-First Guarantee: All recipes and photos are stored directly on your device and work 100% offline."
                                ),
                                actionLabel = null,
                                onActionClick = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Done Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerracottaPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text("Got It! Close Guide", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideCardSection(
    emoji: String,
    title: String,
    badge: String,
    intro: String,
    steps: List<Pair<String, String>>,
    tips: List<String>,
    actionLabel: String?,
    onActionClick: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Title & Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C241E)
                        )
                    )
                    Surface(
                        color = SageGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageGreen,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF6B5E52)
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Step-by-Step Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5EFE6)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE2D6C5))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "How It Works Step-by-Step:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A3E34)
                    )
                )

                steps.forEachIndexed { index, (stepTitle, stepDesc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(TerracottaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stepTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2C241E)
                                )
                            )
                            Text(
                                text = stepDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6B5E52)
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Pro Tips
        if (tips.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF4EE)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFD0E0D0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Text(
                            text = "Helpful Tips:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E4E2E)
                            )
                        )
                    }

                    tips.forEach { tip ->
                        Text(
                            text = "• $tip",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF385538)
                            )
                        )
                    }
                }
            }
        }

        // Action shortcut button if available
        if (actionLabel != null && onActionClick != null) {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
