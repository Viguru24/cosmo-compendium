package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupManifest
import com.example.data.backup.SavedBackupFile
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import java.io.File

@Composable
fun BackupRestoreDialog(
    totalRecipeCount: Int,
    lastBackupDate: String?,
    pendingRestoreManifest: BackupManifest?,
    statusMessage: String?,
    isRestoring: Boolean,
    autoWeeklyBackupEnabled: Boolean = true,
    onToggleAutoWeeklyBackup: (Boolean) -> Unit = {},
    savedBackups: List<SavedBackupFile> = emptyList(),
    onCreateInstantBackup: () -> Unit = {},
    onDirectRestoreBackup: (File, Boolean) -> Unit = { _, _ -> },
    onDeleteSavedBackup: (File) -> Unit = {},
    onExportToJson: ((String, Int) -> Unit) -> Unit,
    onSaveSuccess: (Int) -> Unit,
    onShareBackup: () -> Unit,
    onInspectFile: (Uri) -> Unit,
    onInspectText: (String) -> Unit = {},
    onRestoreStarterRecipes: (Boolean) -> Unit = {},
    onExecuteRestore: (Boolean) -> Unit,
    onClearPendingRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var replaceExisting by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingExportCount by remember { mutableStateOf(0) }
    var showPasteSection by remember { mutableStateOf(false) }
    var pastedRecipeText by remember { mutableStateOf("") }
    
    // Direct restore confirmation state
    var selectedBackupForRestore by remember { mutableStateOf<SavedBackupFile?>(null) }
    var restoreModeForBackup by remember { mutableStateOf(false) } // false = merge, true = replace

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && pendingExportJson != null) {
            val success = BackupManager.writeToUri(context, uri, pendingExportJson!!)
            if (success) {
                onSaveSuccess(pendingExportCount)
                Toast.makeText(context, "Backup saved ($pendingExportCount recipes)!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to write backup file.", Toast.LENGTH_LONG).show()
            }
            pendingExportJson = null
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onInspectFile(uri)
        }
    }

    // Modal Confirmation for Direct Backup Restore
    if (selectedBackupForRestore != null) {
        val targetBackup = selectedBackupForRestore!!
        AlertDialog(
            onDismissRequest = { selectedBackupForRestore = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFFFFFDF9),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restore Backup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TerracottaPrimary
                        )
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Restore ${targetBackup.recipeCount} recipe(s) from \"${targetBackup.displayName}\" (${targetBackup.formattedDate})?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFBF7F0),
                        border = BorderStroke(1.dp, Color(0xFFE7DAC7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Select Restore Mode:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF451A03))
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { restoreModeForBackup = false }
                            ) {
                                RadioButton(
                                    selected = !restoreModeForBackup,
                                    onClick = { restoreModeForBackup = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF16A34A))
                                )
                                Column {
                                    Text("Merge into Library", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF14532D))
                                    Text("Adds restored recipes alongside existing ones.", fontSize = 11.sp, color = Color(0xFF166534))
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { restoreModeForBackup = true }
                            ) {
                                RadioButton(
                                    selected = restoreModeForBackup,
                                    onClick = { restoreModeForBackup = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFDC2626))
                                )
                                Column {
                                    Text("Replace Entire Library", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF991B1B))
                                    Text("Clears current library and restores from this backup.", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDirectRestoreBackup(targetBackup.file, restoreModeForBackup)
                        selectedBackupForRestore = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (restoreModeForBackup) Color(0xFFDC2626) else Color(0xFF16A34A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (restoreModeForBackup) "Replace & Restore" else "Merge & Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBackupForRestore = null }) {
                    Text("Cancel", color = Color(0xFF78716C))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("backup_restore_dialog"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFFFFDF9),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Backup & Restore",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = TerracottaPrimary
                            )
                        )
                        Text(
                            text = "Preserve & recover your family recipes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF78716C),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF18120C))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Banner / Feedback
                if (!statusMessage.isNullOrBlank()) {
                    val isError = statusMessage.contains("failed", ignoreCase = true) ||
                            statusMessage.contains("error", ignoreCase = true) ||
                            statusMessage.contains("invalid", ignoreCase = true) ||
                            statusMessage.contains("empty", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isError) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, if (isError) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isError) Color(0xFF991B1B) else Color(0xFF166534),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Current Library Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF7F0)),
                    border = BorderStroke(1.dp, Color(0xFFE7DAC7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cookbook Library",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF451A03)
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFEDD5)
                            ) {
                                Text(
                                    text = "$totalRecipeCount Recipes",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lastBackupDate != null) "Last backup: $lastBackupDate" else "Auto-safety snapshots saved on deletion & weekly.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF78716C),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // PENDING RESTORE CONFIRMATION PANEL (If a file or text was inspected)
                if (pendingRestoreManifest != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.5.dp, Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ready to Restore",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = onClearPendingRestore,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel Restore", tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = "Found ${pendingRestoreManifest.recipeCount} recipe(s) ready to import:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF166534))
                            )

                            // Preview list of recipes
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                pendingRestoreManifest.recipes.take(4).forEach { recipe ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = recipe.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF14532D)
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (pendingRestoreManifest.recipes.size > 4) {
                                    Text(
                                        text = "+ ${pendingRestoreManifest.recipes.size - 4} more recipes...",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontSize = 11.sp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFBBF7D0))

                            // Restore Option: Merge vs Replace
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Choose Restore Mode:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { replaceExisting = false }
                                ) {
                                    RadioButton(
                                        selected = !replaceExisting,
                                        onClick = { replaceExisting = false },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF16A34A))
                                    )
                                    Column {
                                        Text(
                                            text = "Merge into Library",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                        )
                                        Text(
                                            text = "Adds restored recipes alongside any existing ones.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF166534), fontSize = 11.sp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { replaceExisting = true }
                                ) {
                                    RadioButton(
                                        selected = replaceExisting,
                                        onClick = { replaceExisting = true },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFDC2626))
                                    )
                                    Column {
                                        Text(
                                            text = "Replace Entire Library",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                        )
                                        Text(
                                            text = "Deletes current list and replaces with these recipes.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF7F1D1D), fontSize = 11.sp)
                                        )
                                    }
                                }
                            }

                            // Confirm Button
                            Button(
                                onClick = { onExecuteRestore(replaceExisting) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (replaceExisting) Color(0xFFDC2626) else Color(0xFF16A34A)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isRestoring,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_restore_button")
                            ) {
                                if (isRestoring) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Restoring Recipes...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (replaceExisting) "Confirm & Replace Library" else "Confirm & Merge Recipes",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 1: CREATE A BACKUP
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Recipe Backup",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF431407)
                                )
                            )
                        }

                        Text(
                            text = "Save an instant snapshot to your phone, export as a .json file, or share to Google Drive.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF57534E), fontSize = 12.sp)
                        )

                        // 1-Tap Instant Backup
                        Button(
                            onClick = onCreateInstantBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("instant_backup_button")
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Instant Local Backup", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onExportToJson { json, count ->
                                        pendingExportJson = json
                                        pendingExportCount = count
                                        createDocumentLauncher.launch(BackupManager.getSuggestedFileName())
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, TerracottaPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_backup_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save File (.json)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onShareBackup,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, TerracottaPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_backup_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share / Cloud", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Auto Weekly Backup Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Weekly Backup",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                )
                                Text(
                                    text = "Automatically saves a snapshot weekly.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF92400E), fontSize = 10.5.sp)
                                )
                            }
                            Switch(
                                checked = autoWeeklyBackupEnabled,
                                onCheckedChange = onToggleAutoWeeklyBackup,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFD97706)
                                ),
                                modifier = Modifier.testTag("dialog_toggle_auto_weekly_backup")
                            )
                        }
                    }
                }

                // SECTION 2: AVAILABLE SAVED BACKUPS (RESTORE LIST)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saved Backups (${savedBackups.size})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                )
                            }
                        }

                        if (savedBackups.isEmpty()) {
                            Text(
                                text = "No local backups yet. Tap \"Instant Local Backup\" above to create one now!",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF166534), fontSize = 11.5.sp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                savedBackups.forEachIndexed { idx, backupFile ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = backupFile.displayName,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF14532D)
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFFDCFCE7)
                                                    ) {
                                                        Text(
                                                            text = "${backupFile.recipeCount} recipes",
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF15803D)
                                                            )
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${backupFile.formattedDate} • ${backupFile.formattedSize}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF78716C)
                                                    )
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Button(
                                                    onClick = {
                                                        selectedBackupForRestore = backupFile
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.testTag("restore_saved_backup_$idx")
                                                ) {
                                                    Text("Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                IconButton(
                                                    onClick = { onDeleteSavedBackup(backupFile.file) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete Backup",
                                                        tint = Color(0xFF9CA3AF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3: RESTORE FROM EXTERNAL FILE OR PASTE TEXT
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Import / Recover Recipes",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF431407)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_backup_file_button")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pick .json File", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showPasteSection = !showPasteSection },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, SageGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_paste_button")
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showPasteSection) "Hide Text" else "Paste Text/JSON", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Expandable Paste Section
                    if (showPasteSection) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFAF7F2),
                            border = BorderStroke(1.dp, Color(0xFFE5DACB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Paste Recipe JSON or Plain Recipe Text:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF451A03))
                                )
                                OutlinedTextField(
                                    value = pastedRecipeText,
                                    onValueChange = { pastedRecipeText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("paste_recipe_input"),
                                    placeholder = {
                                        Text(
                                            "Paste recipe JSON or recipe text (e.g. Grandma's Chocolate Chip Cookies with ingredients & steps)...",
                                            fontSize = 11.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF18120C),
                                        unfocusedTextColor = Color(0xFF18120C),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = SageGreen,
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            if (pastedRecipeText.isNotBlank()) {
                                                onInspectText(pastedRecipeText)
                                            }
                                        },
                                        enabled = pastedRecipeText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("detect_pasted_recipe_button")
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Detect & Restore", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Reload Starter Classics
                    OutlinedButton(
                        onClick = { onRestoreStarterRecipes(false) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFC2410C)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC2410C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reload_starter_recipes_button")
                    ) {
                        Icon(Icons.Default.Cookie, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reload Starter Classics & Formulas", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Safety Note
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F4)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF78716C), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Backups use standard JSON format. They work on any phone or tablet and can be safely archived in Google Drive.",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF57534E), fontSize = 10.5.sp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("close_backup_dialog_button")
            ) {
                Text(text = "Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
