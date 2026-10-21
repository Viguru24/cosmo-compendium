package com.example.ui.components.bookshelf

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.ProfilePill
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfTopBar(
    activeProfile: String,
    uncheckedShoppingCount: Int,
    soundEffectsEnabled: Boolean,
    languageMode: LanguageMode,
    onOpenProfileSwitcher: () -> Unit,
    onOpenSousChef: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onToggleSoundEffects: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    onCheckOtaUpdate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfilePill(
                    activeProfile = activeProfile,
                    onClick = onOpenProfileSwitcher
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TerracottaPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenSousChef() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("👨‍🍳", fontSize = 14.sp)
                        Text(
                            AppLocalization.getSousChefButtonLabel(languageMode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                // Shopping List button with badge
                IconButton(
                    onClick = onOpenShoppingList,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("open_shopping_list_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (uncheckedShoppingCount > 0) {
                                Badge(
                                    containerColor = TerracottaPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(text = "$uncheckedShoppingCount", fontSize = 9.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = AppLocalization.getShoppingListTitle(languageMode),
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                // Over-The-Air (OTA) Sovereign Updates button (Always Visible)
                IconButton(
                    onClick = { onCheckOtaUpdate?.invoke() },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("open_ota_update_button")
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Check for Over-The-Air Updates",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // More Options (Sound toggle, Guide, Settings)
                IconButton(
                    onClick = { showMoreMenu = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (soundEffectsEnabled) {
                                    if (languageMode == LanguageMode.GERMAN) "Ton stummschalten" else "Mute Sound Effects"
                                } else {
                                    if (languageMode == LanguageMode.GERMAN) "Ton aktivieren" else "Enable Sound Effects"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF9E8E81)
                            )
                        },
                        onClick = {
                            showMoreMenu = false
                            onToggleSoundEffects()
                            val message = if (!soundEffectsEnabled) "🔊 Sound Effects Enabled" else "🔇 Sound Effects Muted"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (languageMode == LanguageMode.GERMAN) "Anleitung & Hilfe" else "Cookbook Guide") },
                        leadingIcon = { Text("❓", fontSize = 16.sp) },
                        onClick = {
                            showMoreMenu = false
                            onOpenGuide()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(if (languageMode == LanguageMode.GERMAN) "Einstellungen" else "Settings & Preferences") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TerracottaPrimary) },
                        onClick = {
                            showMoreMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFFDF9)
        )
    )
}
