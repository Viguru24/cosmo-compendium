package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

/**
 * Top bar profile selector pill
 */
@Composable
fun ProfilePill(
    activeProfile: String,
    onClick: () -> Unit,
    languageMode: com.example.data.model.LanguageMode = com.example.data.model.LanguageMode.ENGLISH,
    modifier: Modifier = Modifier
) {
    val isAll = activeProfile.equals("All", ignoreCase = true) || activeProfile.equals("All Family", ignoreCase = true)
    val displayLabel = if (isAll) com.example.ui.util.AppLocalization.getFilterAllFamily(languageMode) else com.example.ui.util.AppLocalization.getProfileCookbookTitle(activeProfile, languageMode)
    val iconEmoji = getProfileEmoji(activeProfile)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.5.dp, TerracottaPrimary.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = modifier.testTag("top_bar_profile_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(text = iconEmoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = displayLabel,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TerracottaPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Switch Profile",
                tint = TerracottaPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Helper to get an appropriate emoji avatar for a profile name
 */
fun getProfileEmoji(name: String): String {
    val lower = name.lowercase()
    // Common female names → 👩‍🍳
    val femaleNames = listOf(
        "wife", "mom", "mother", "sarah", "emma", "annette", "isabel", "isabella",
        "anna", "anne", "marie", "maria", "clara", "claire", "julia", "julie",
        "sophie", "sophia", "laura", "linda", "lisa", "kate", "katie", "katherine",
        "mary", "margaret", "carol", "carolyn", "barbara", "betty", "patricia",
        "rose", "ruth", "helen", "grace", "alice", "amy", "jane", "jennifer",
        "jessica", "michelle", "nicole", "rachel", "rebecca", "susan", "karen",
        "elizabeth", "charlotte", "victoria", "hannah", "emily", "natalie", "lucy"
    )
    // Common male names → 👨‍🍳
    val maleNames = listOf(
        "dad", "father", "louis", "husband", "james", "john", "robert", "michael",
        "william", "david", "richard", "joseph", "thomas", "charles", "mark",
        "daniel", "paul", "steven", "andrew", "george", "edward", "kevin", "brian",
        "peter", "frank", "henry", "jack", "sam", "samuel", "ben", "benjamin",
        "chris", "christopher", "alex", "alexander", "ryan", "matt", "matthew"
    )
    return when {
        lower.contains("all") || lower.contains("family") -> "👨‍👩‍👧"
        lower.contains("grandma") || lower.contains("oma") || lower.contains("nana") || lower.contains("omi") -> "👵"
        lower.contains("grandpa") || lower.contains("opa") || lower.contains("opi") -> "👴"
        lower.contains("daughter") || lower.contains("girl") || lower.contains("sister") || lower.contains("lily") || lower.contains("mia") -> "👧"
        lower.contains("son") || lower.contains("boy") || lower.contains("brother") -> "👦"
        lower.contains("bake") || lower.contains("cake") || lower.contains("pastry") -> "🧁"
        femaleNames.any { lower.contains(it) } -> "👩‍🍳"
        maleNames.any { lower.contains(it) } -> "👨‍🍳"
        // Deterministic but pleasant fallback for any unknown name
        else -> if (lower.hashCode() % 2 == 0) "👩‍🍳" else "👨‍🍳"
    }
}

/**
 * "Who's Cooking?" Modal Profile Switcher Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherSheet(
    activeProfile: String,
    profiles: List<String>,
    recipeCounts: Map<String, Int>,
    defaultProfile: String = "Louis",
    onSelectProfile: (String) -> Unit,
    onSetDefaultProfile: (String) -> Unit = {},
    onAddProfile: (String) -> Unit,
    onRenameProfile: (String, String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onBulkMove: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    languageMode: com.example.data.model.LanguageMode = com.example.data.model.LanguageMode.ENGLISH
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddDialog by remember { mutableStateOf(false) }
    var profileToRename by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var profileToDelete by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDF9),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = com.example.ui.util.AppLocalization.getProfileSwitcherHeader(languageMode),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = com.example.ui.util.AppLocalization.getProfileSwitcherSubtitle(languageMode),
                        fontSize = 12.sp,
                        color = Color(0xFF6B5848),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = TerracottaPrimary,
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("add_family_member_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = com.example.ui.util.AppLocalization.getAddProfileButton(languageMode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profiles List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Individual Profiles
                items(profiles) { profileName ->
                    val isActive = activeProfile.equals(profileName, ignoreCase = true)
                    val isDefault = defaultProfile.equals(profileName, ignoreCase = true)
                    val count = recipeCounts[profileName] ?: 0
                    val emoji = getProfileEmoji(profileName)

                    ProfileCardItem(
                        profileName = profileName,
                        emoji = emoji,
                        recipeCount = count,
                        isActive = isActive,
                        isDefault = isDefault,
                        onSelect = {
                            onSelectProfile(profileName)
                            onDismiss()
                        },
                        onSetDefault = {
                            onSetDefaultProfile(profileName)
                        },
                        onRename = {
                            profileToRename = profileName
                            renameText = profileName
                        },
                        onDelete = {
                            profileToDelete = profileName
                        }
                    )
                }

                // "All Family Recipes" Combined Option
                item {
                    val isAllActive = activeProfile.equals("All", ignoreCase = true) || activeProfile.equals("All Family", ignoreCase = true)
                    val totalCount = recipeCounts["All"] ?: 0

                    Surface(
                        onClick = {
                            onSelectProfile("All Family")
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllActive) Color(0xFFF3E8FF) else Color(0xFFFAF7F2),
                        border = BorderStroke(
                            if (isAllActive) 2.dp else 1.dp,
                            if (isAllActive) Color(0xFF7E22CE) else Color(0xFFE8DFD5)
                        ),
                        shadowElevation = if (isAllActive) 3.dp else 0.dp,
                        modifier = Modifier.fillMaxWidth().testTag("profile_card_all_family")
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
                                        .background(Color(0xFFE9D5FF), CircleShape)
                                ) {
                                    Text(text = "👨‍👩‍👧", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = com.example.ui.util.AppLocalization.getAllFamilyProfileHeader(languageMode),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = if (isAllActive) Color(0xFF581C87) else Color(0xFF18120C)
                                    )
                                    Text(
                                        text = "$totalCount total recipes across all members",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF6B5848)
                                    )
                                }
                            }

                            if (isAllActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color(0xFF7E22CE),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Bulk Reassign Recipes Quick Tool
                item {
                    var showBulkMoveDialog by remember { mutableStateOf(false) }
                    var sourceProf by remember { mutableStateOf("All Family") }
                    var targetProf by remember { mutableStateOf(profiles.firstOrNull { !it.equals("Louis", ignoreCase = true) } ?: "Wife") }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFFFD8B3)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable { showBulkMoveDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TerracottaPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📦", fontSize = 18.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = com.example.ui.util.AppLocalization.getBulkMoveButton(languageMode),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2C1E14), fontSize = 13.5.sp)
                                )
                                Text(
                                    text = com.example.ui.util.AppLocalization.getBulkMoveDescription(languageMode),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF786555), fontSize = 11.sp)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (showBulkMoveDialog) {
                        AlertDialog(
                            onDismissRequest = { showBulkMoveDialog = false },
                            icon = { Text("📦", fontSize = 32.sp) },
                            title = {
                                Text(
                                    text = "Move Recipes Between Profiles",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Select source and target profile to reassign recipes instantly:",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5A4535))
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Move From (Source):", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF2C1E14))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            (listOf("All Family") + profiles).forEach { p ->
                                                val isSel = sourceProf == p
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSel) TerracottaPrimary.copy(alpha = 0.15f) else Color(0xFFF5F0EA),
                                                    border = BorderStroke(if (isSel) 1.5.dp else 0.5.dp, if (isSel) TerracottaPrimary else Color(0xFFE4D9CC)),
                                                    modifier = Modifier.clickable { sourceProf = p }
                                                ) {
                                                    Text(
                                                        text = p,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSel) TerracottaPrimary else Color(0xFF2C1E14),
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Move Into (Target):", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF2C1E14))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            profiles.forEach { p ->
                                                val isSel = targetProf == p
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSel) SageGreen.copy(alpha = 0.15f) else Color(0xFFF5F0EA),
                                                    border = BorderStroke(if (isSel) 1.5.dp else 0.5.dp, if (isSel) SageGreen else Color(0xFFE4D9CC)),
                                                    modifier = Modifier.clickable { targetProf = p }
                                                ) {
                                                    Text(
                                                        text = p,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSel) SageGreen else Color(0xFF2C1E14),
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        onBulkMove(sourceProf, targetProf)
                                        showBulkMoveDialog = false
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Move All to $targetProf", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBulkMoveDialog = false }) {
                                    Text("Cancel", color = Color(0xFF5A4535))
                                }
                            },
                            containerColor = Color(0xFFFFFDF9)
                        )
                    }
                }

                // Add New Member Card Button
                item {
                    Surface(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFBF5),
                        border = BorderStroke(1.5.dp, TerracottaPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("profile_card_add_new_member")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TerracottaPrimary.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+ " + com.example.ui.util.AppLocalization.getAddProfileButton(languageMode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Profile Dialog — richly themed
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        val quickSuggestions = listOf(
            "Annette", "Isabel", "Louis", "Sarah", "Emma", "Sophie",
            "Wife", "Husband", "Daughter", "Son", "Mom", "Dad", "Grandma", "Grandpa"
        )
        val liveEmoji = remember(newName) { if (newName.isBlank()) "👨‍🍳" else getProfileEmoji(newName) }

        Dialog(
            onDismissRequest = { showAddDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFFDF9))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar preview
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0E0))
                    ) {
                        Text(liveEmoji, fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Add a Family Member",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "They'll get their own personal cookbook",
                        fontSize = 12.sp,
                        color = Color(0xFF6B5848)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Themed TextField
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = {
                            Text(
                                "e.g. Sarah, Mom's Kitchen…",
                                color = Color(0xFFB39B8A)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = TerracottaPrimary.copy(alpha = 0.35f),
                            focusedLabelColor = TerracottaPrimary,
                            cursorColor = TerracottaPrimary,
                            focusedTextColor = Color(0xFF2C1F17),
                            unfocusedTextColor = Color(0xFF2C1F17),
                            focusedContainerColor = Color(0xFFFFF7F0),
                            unfocusedContainerColor = Color(0xFFFFF7F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick-pick chip rows (all suggestions, 2 rows)
                    Text(
                        "Quick picks:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B5848),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(quickSuggestions) { suggestion ->
                            val isSelected = newName.equals(suggestion, ignoreCase = true)
                            Surface(
                                onClick = { newName = suggestion },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFFFFF7ED),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) TerracottaPrimary else TerracottaPrimary.copy(alpha = 0.35f)
                                ),
                                shadowElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(getProfileEmoji(suggestion), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TerracottaPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onAddProfile(newName.trim())
                                showAddDialog = false
                                onDismiss()
                            }
                        },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerracottaPrimary,
                            disabledContainerColor = TerracottaPrimary.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create Cookbook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showAddDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Cancel",
                            color = Color(0xFF9B7B68),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }


    // Rename Profile Dialog
    if (profileToRename != null) {
        val oldName = profileToRename!!
        AlertDialog(
            onDismissRequest = { profileToRename = null },
            title = { Text("Rename Profile", fontWeight = FontWeight.Bold, color = TerracottaPrimary) },
            text = {
                Column {
                    Text("Rename '$oldName' cookbook:", fontSize = 12.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameProfile(oldName, renameText.trim())
                            profileToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Profile Dialog
    if (profileToDelete != null) {
        val target = profileToDelete!!
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete Profile '$target'?", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text(
                    "Are you sure you want to remove the profile '$target'? Recipes assigned to this profile will remain safely in the database and can be reassigned.",
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProfile(target)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileCardItem(
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

/**
 * 1-Tap Recipe Profile Assignment Dialog (from recipe card 3-dots menu)
 */
@Composable
fun AssignRecipeProfileDialog(
    recipe: RecipeEntity,
    profiles: List<String>,
    onAssign: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newCustomProfile by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Move to Family Cookbook",
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    fontSize = 17.sp
                )
                Text(
                    text = "Assign \"${recipe.title}\" to:",
                    fontSize = 12.sp,
                    color = Color(0xFF6B5848),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                profiles.forEach { profileName ->
                    val isCurrent = recipe.profileName.equals(profileName, ignoreCase = true)
                    val emoji = getProfileEmoji(profileName)

                    Surface(
                        onClick = {
                            onAssign(profileName)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) Color(0xFFFFEDD5) else Color(0xFFFFFDF9),
                        border = BorderStroke(1.dp, if (isCurrent) TerracottaPrimary else Color(0xFFE8DFD5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$profileName's Cookbook",
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isCurrent) Color(0xFF431407) else Color(0xFF18120C)
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = "Current", tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isAddingNew) {
                    TextButton(
                        onClick = { isAddingNew = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New Member", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = newCustomProfile,
                            onValueChange = { newCustomProfile = it },
                            placeholder = { Text("Name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCustomProfile.isNotBlank()) {
                                    onAssign(newCustomProfile.trim())
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Text("Move")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}