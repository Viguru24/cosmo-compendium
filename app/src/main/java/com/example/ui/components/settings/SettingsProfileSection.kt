package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeIngredient
import com.example.data.model.UnitSystem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun SettingsProfileSection(
    currentLanguage: LanguageMode,
    onLanguageChange: (LanguageMode) -> Unit,
    defaultProfile: String,
    familyProfiles: List<String>,
    onSetDefaultProfile: (String) -> Unit,
    currentUnitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit,
    soundEffectsEnabled: Boolean,
    onSoundEffectsChange: (Boolean) -> Unit,
    keepScreenAwake: Boolean,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    onOpenGuide: (() -> Unit)?,
    onOpenSmartConverter: (() -> Unit)?,
    cardBackground: Color = Color(0xFFFDFBF7),
    cardBorder: Color = Color(0xFFE8DFD5),
    headerDark: Color = Color(0xFF2C1E14),
    textMuted: Color = Color(0xFF786555)
) {
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    var profileDropdownExpanded by remember { mutableStateOf(false) }
    val sampleIngredient = RecipeIngredient("Flour", "250", "g", nameEnglish = "All-Purpose Flour")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Language Selector
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(17.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppLocalization.getLanguageSectionTitle(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = headerDark, fontSize = 13.5.sp)
                        )
                        Text(
                            text = AppLocalization.getLanguageSectionSubtitle(currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    Surface(
                        onClick = { languageDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, if (languageDropdownExpanded) TerracottaPrimary else Color(0xFFD6C8B4)),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(currentLanguage.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentLanguage.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = headerDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = languageDropdownExpanded,
                        onDismissRequest = { languageDropdownExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFFFFFDF9))
                            .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                    ) {
                        LanguageMode.values().forEach { lang ->
                            val isSelected = currentLanguage == lang
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(lang.flag, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (lang) {
                                                    LanguageMode.GERMAN -> "Deutsch (German)"
                                                    LanguageMode.FRENCH -> "Français (French)"
                                                    LanguageMode.ITALIAN -> "Italiano (Italian)"
                                                    LanguageMode.SPANISH -> "Español (Spanish)"
                                                    LanguageMode.DUTCH -> "Nederlands (Dutch)"
                                                    LanguageMode.ENGLISH -> "English (US / UK)"
                                                },
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) TerracottaPrimary else Color(0xFF2C1E14)
                                            )
                                        }
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Active",
                                                tint = TerracottaPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    languageDropdownExpanded = false
                                    onLanguageChange(lang)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Default Device Profile Selector
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD97706).copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📱", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentLanguage == LanguageMode.GERMAN) "Standard-Profil dieses Geräts" else "This Phone's Default Profile",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = headerDark, fontSize = 13.5.sp)
                            )
                            Text(
                                text = if (currentLanguage == LanguageMode.GERMAN) "Wessen Kochbuch standardmäßig geöffnet wird" else "Default cookbook opened on this device",
                                style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        Surface(
                            onClick = { profileDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, if (profileDropdownExpanded) Color(0xFFD97706) else Color(0xFFD6C8B4)),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("⭐", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = defaultProfile,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = headerDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Default Profile",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = profileDropdownExpanded,
                            onDismissRequest = { profileDropdownExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFFFFFDF9))
                                .border(1.dp, Color(0xFFE5DDD3), RoundedCornerShape(12.dp))
                        ) {
                            val allAvailableProfiles = if (familyProfiles.isNotEmpty()) familyProfiles else listOf("Louis", "Annette", "Isabel")
                            allAvailableProfiles.forEach { prof ->
                                val isSelected = defaultProfile.equals(prof, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(com.example.ui.components.getProfileEmoji(prof), fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "$prof's Cookbook",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) Color(0xFFD97706) else Color(0xFF2C1E14)
                                                )
                                            }
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        profileDropdownExpanded = false
                                        onSetDefaultProfile(prof)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Cookbook Guide Trigger Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenGuide?.invoke() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📚", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Kochbuch-Anleitung & Funktionen" else "Cookbook Guide & How It Works",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        )
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Schritt-für-Schritt Tour: Scannen, Videos, Sous-Chef & mehr" else "Step-by-step tour: Card scanning, video extraction, Sous-Chef & tips",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF92400E),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Cooking & App Preferences (Keep Screen Awake & Sound Effects)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = AppLocalization.getCookingAndAppPrefsTitle(currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = headerDark
                    )
                )

                // Keep Screen Awake
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppLocalization.getKeepScreenAwakeTitle(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = headerDark)
                            )
                            Text(
                                text = AppLocalization.getKeepScreenAwakeDesc(currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                            )
                        }
                    }
                    Switch(
                        checked = keepScreenAwake,
                        onCheckedChange = onKeepScreenAwakeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SageGreen
                        )
                    )
                }

                HorizontalDivider(color = cardBorder)

                // Sound Effects
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppLocalization.getSoundEffectsTitle(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = headerDark)
                            )
                            Text(
                                text = AppLocalization.getSoundEffectsDesc(currentLanguage),
                                style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                            )
                        }
                    }
                    Switch(
                        checked = soundEffectsEnabled,
                        onCheckedChange = onSoundEffectsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TerracottaPrimary
                        )
                    )
                }
            }
        }

        // Measuring Units & Conversions
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = AppLocalization.getUnitSystemTitle(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = headerDark
                            )
                        )
                        Text(
                            text = AppLocalization.getUnitSystemDesc(currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(color = textMuted, fontSize = 11.sp)
                        )
                    }
                }

                UnitSystem.values().forEach { system ->
                    val isSelected = currentUnitSystem == system
                    val previewValue = sampleIngredient.getConvertedAmount(system)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) TerracottaPrimary.copy(alpha = 0.08f) else Color.White,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TerracottaPrimary else cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUnitSystemChange(system) }
                            .testTag("unit_system_${system.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = system.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${system.label} (e.g. $previewValue)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) TerracottaPrimary else headerDark
                                        )
                                    )
                                    Text(
                                        text = system.description,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textMuted,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (onOpenSmartConverter != null) {
                    OutlinedButton(
                        onClick = onOpenSmartConverter,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TerracottaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_smart_converter_settings_button")
                    ) {
                        Text("✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Smart Spoon & Knife-Tip Converter", color = TerracottaPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
