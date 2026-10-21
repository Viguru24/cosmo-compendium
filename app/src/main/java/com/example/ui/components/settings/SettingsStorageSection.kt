package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization
import java.util.Locale

@Composable
fun SettingsStorageSection(
    currentLanguage: LanguageMode,
    totalRecipeCount: Int,
    recipesWithPhotos: Int,
    aiPhotosCount: Int,
    scannedCardsCount: Int,
    unphotographedCount: Int,
    photoStorageMb: Double,
    onOpenBatchCoverGen: (() -> Unit)?,
    onExportFullCookbookPdf: (() -> Unit)?,
    autoWeeklyBackupEnabled: Boolean,
    onToggleAutoWeeklyBackup: (Boolean) -> Unit,
    onOpenBackup: () -> Unit,
    onDeleteAllRecipes: () -> Unit,
    cardBackground: Color = Color(0xFFFDFBF7),
    cardBorder: Color = Color(0xFFE8DFD5),
    headerDark: Color = Color(0xFF2C1E14),
    textMuted: Color = Color(0xFF786555)
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete All Recipes?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all recipes from your cookbook library? All recipes will be removed immediately. (You can restore your recipes anytime from automatic weekly backups or reload starter recipes in Backup & Restore).",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF451A03), lineHeight = 20.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteAllRecipes()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_all_recipes_button")
                ) {
                    Text(if (currentLanguage == LanguageMode.GERMAN) "Ja, alle löschen" else "Yes, Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color(0xFF57534E))
                }
            },
            containerColor = Color(0xFFFFFDF9),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Overview Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = AppLocalization.getCollectionOverviewTitle(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = headerDark
                            )
                        )
                        val storageStr = if (photoStorageMb < 0.1) "< 0.1 MB" else String.format(Locale.US, "%.1f MB", photoStorageMb)
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "$totalRecipeCount Rezepte ($storageStr) • ✨ $aiPhotosCount KI • 📄 $scannedCardsCount Scans" else "$totalRecipeCount recipes ($storageStr) • ✨ $aiPhotosCount AI • 📄 $scannedCardsCount Scans" + if (unphotographedCount > 0) " • 📷 $unphotographedCount None" else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = textMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerracottaPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "$totalRecipeCount Rezepte" else "$totalRecipeCount Recipes",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                val missingAiCount = scannedCardsCount + unphotographedCount
                if (onOpenBatchCoverGen != null) {
                    Button(
                        onClick = onOpenBatchCoverGen,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_batch_cover_gen_from_collection")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (missingAiCount > 0) "Batch Generate AI Photos ($missingAiCount Ready)" else "Open Batch AI Photo Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }

                if (onExportFullCookbookPdf != null) {
                    OutlinedButton(
                        onClick = onExportFullCookbookPdf,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF8C2D19)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_master_pdf_settings_button")
                    ) {
                        Text("📖", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Gesamtes Kochbuch exportieren (PDF)" else "Export Complete Cookbook (Master PDF)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF8C2D19)
                        )
                    }
                }
            }
        }

        // Automatic Backup & Data Safety
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBackground,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentLanguage == LanguageMode.GERMAN) "Automatische wöchentliche Sicherung" else "Automatic Weekly Backup",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = headerDark
                                )
                            )
                            Text(
                                text = if (autoWeeklyBackupEnabled) "Enabled • Auto-saves every 7 days" else "Disabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (autoWeeklyBackupEnabled) SageGreen else textMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Switch(
                        checked = autoWeeklyBackupEnabled,
                        onCheckedChange = onToggleAutoWeeklyBackup,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TerracottaPrimary
                        ),
                        modifier = Modifier.testTag("toggle_auto_weekly_backup")
                    )
                }

                Text(
                    text = if (currentLanguage == LanguageMode.GERMAN) "Sichert deine Kochbuch-Bibliothek automatisch, um deine Familienerbstücke zu schützen. ZIP- und SQLite-Backups können jederzeit exportiert werden." else "Backs up your cookbook library automatically to protect against accidental loss. You can also export/import ZIP and SQLite archives anytime.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textMuted,
                        fontSize = 11.5.sp
                    )
                )

                OutlinedButton(
                    onClick = onOpenBackup,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, TerracottaPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_backup_from_settings_button")
                ) {
                    Icon(Icons.Default.Archive, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (currentLanguage == LanguageMode.GERMAN) "Datensicherungs- & Wiederherstellungs-Tool öffnen" else "Open Backup & Restore Tool", color = TerracottaPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Danger Zone: Reset & Delete All Recipes
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppLocalization.getDangerZoneTitle(currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    )
                }

                Text(
                    text = AppLocalization.getDangerZoneDesc(currentLanguage),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF7F1D1D),
                        fontSize = 11.sp
                    )
                )

                Button(
                    onClick = { showDeleteConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_all_recipes_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppLocalization.getDeleteAllTitle(currentLanguage), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
