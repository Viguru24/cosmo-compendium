package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.getDisplayCategory
import com.example.ui.util.getDisplayTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookletTopBar(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    soundEffectsEnabled: Boolean,
    uncheckedShoppingCount: Int,
    onBack: () -> Unit,
    onStartCookMode: () -> Unit,
    onEditRecipe: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onShareRecipe: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGenerateAiPhoto: () -> Unit,
    onEditCustomPrompt: () -> Unit,
    onAdaptDietary: () -> Unit,
    onRecipeCosting: () -> Unit,
    onOpenSettings: () -> Unit,
    onDeleteRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = recipe.getDisplayTitle(languageMode),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TerracottaPrimary,
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = recipe.getDisplayCategory(languageMode),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6B5B4E),
                        fontSize = 10.sp
                    )
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("back_to_bookshelf_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF451A03), modifier = Modifier.size(18.dp))
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Start Kitchen Cook Mode Button (Top Action)
                Button(
                    onClick = onStartCookMode,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.size(height = 30.dp, width = 110.dp)
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = "Cook",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (languageMode == LanguageMode.GERMAN) "Kochen" else "Cook Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = Color.White
                    )
                }

                // Compact Edit Recipe Button
                OutlinedButton(
                    onClick = onEditRecipe,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFD6C8B8)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.size(height = 28.dp, width = 64.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF451A03),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Edit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF451A03)
                    )
                }

                // Shopping List button with badge
                IconButton(
                    onClick = onOpenShoppingList,
                    modifier = Modifier.size(28.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (uncheckedShoppingCount > 0) {
                                Badge(
                                    containerColor = TerracottaPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (uncheckedShoppingCount > 99) "99+" else "$uncheckedShoppingCount",
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Shopping List",
                            tint = Color(0xFF451A03),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Share Recipe Card
                IconButton(
                    onClick = onShareRecipe,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share Recipe",
                        tint = Color(0xFF451A03),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Quick Sound Toggle (Mute / Unmute)
                Surface(
                    onClick = onToggleSound,
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (soundEffectsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (soundEffectsEnabled) "Sound Effects: On" else "Sound Effects: Muted",
                            tint = if (soundEffectsEnabled) TerracottaPrimary else Color(0xFF8C7A6B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Favorite Toggle
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) Color(0xFFE11D48) else Color(0xFF451A03),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // More Actions Menu
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color(0xFF451A03),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        if (recipe.imageUri.isNullOrBlank())
                                            "Generate AI Food Photo"
                                        else
                                            "Regenerate AI Food Photo"
                                    )
                                    Text(
                                        "Using active AI provider",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF786555),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onGenerateAiPhoto()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Edit AI Photo Prompt...")
                                    Text(
                                        "Customize style, camera angle, garnish",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF786555),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onEditCustomPrompt()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Smart Dietary Adaptation")
                                    Text(
                                        "Make Gluten-Free, Vegan, Dairy-Free, Low-Carb",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF786555),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = TerracottaPrimary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onAdaptDietary()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Cost & Yield Calculator")
                                    Text(
                                        "Calculate ingredient costs & margin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF786555),
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = TerracottaPrimary
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onRecipeCosting()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TerracottaPrimary) },
                            onClick = {
                                showMoreMenu = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Recipe", color = Color(0xFFDC2626)) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
                            onClick = {
                                showMoreMenu = false
                                onDeleteRecipe()
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7F2))
    )
}
