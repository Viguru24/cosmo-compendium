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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                }
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                // Sous-Chef AI Copilot button
                IconButton(
                    onClick = onOpenSousChef,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_sous_chef_button")
                ) {
                    Text("👨‍🍳", fontSize = 20.sp)
                }

                // Shopping List button with badge
                IconButton(
                    onClick = onOpenShoppingList,
                    modifier = Modifier
                        .size(36.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Quick Sound Toggle (Mute / Unmute)
                IconButton(
                    onClick = {
                        onToggleSoundEffects()
                        val message = if (!soundEffectsEnabled) "🔊 Sound Effects Enabled" else "🔇 Sound Effects Muted"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (soundEffectsEnabled) "Mute Sound" else "Unmute Sound",
                        tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF9E8E81),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cookbook Guide Button
                IconButton(
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_guide_button")
                ) {
                    Text("❓", fontSize = 17.sp)
                }

                // Settings & Actions (3 Vertical Dots)
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Settings & More",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFFFFDF9)
        )
    )
}
