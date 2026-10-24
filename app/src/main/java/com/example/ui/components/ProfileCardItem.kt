package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerracottaPrimary

@Composable
internal fun ProfileCardItem(
    profileName: String,
    emoji: String,
    recipeCount: Int,
    isActive: Boolean,
    isDefault: Boolean = false,
    onSelect: () -> Unit,
    onSetDefault: () -> Unit = {},
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) Color(0xFFFFEDD5) else Color(0xFFFAF7F2),
        border = BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) TerracottaPrimary else Color(0xFFE8DFD5)
        ),
        shadowElevation = if (isActive) 3.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().testTag("profile_card_$profileName")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .background(if (isActive) TerracottaPrimary.copy(alpha = 0.2f) else Color(0xFFEADCCB), CircleShape)
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$profileName's Cookbook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = if (isActive) Color(0xFF431407) else Color(0xFF18120C)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (recipeCount == 1) "1 saved recipe" else "$recipeCount saved recipes",
                            fontSize = 11.5.sp,
                            color = if (isActive) TerracottaPrimary else Color(0xFF6B5848),
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isDefault) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(0.75.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text("⭐", fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        "Phone Default",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF8C7B6B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    if (isDefault) "Default Profile for this Phone ⭐" else "Set as Phone Default ⭐",
                                    fontSize = 13.sp,
                                    fontWeight = if (isDefault) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDefault) Color(0xFF92400E) else Color.Unspecified
                                ) 
                            },
                            leadingIcon = { 
                                Text(if (isDefault) "⭐" else "☆", fontSize = 15.sp)
                            },
                            onClick = {
                                showMenu = false
                                onSetDefault()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename Profile", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Profile", fontSize = 13.sp, color = Color(0xFFDC2626)) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
