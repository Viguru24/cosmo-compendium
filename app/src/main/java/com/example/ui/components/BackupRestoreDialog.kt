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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupManifest
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

@Composable
fun BackupRestoreDialog(
    totalRecipeCount: Int,
    lastBackupDate: String?,
    pendingRestoreManifest: BackupManifest?,
    statusMessage: String?,
    isRestoring: Boolean,
    onExportToJson: ((String, Int) -> Unit) -> Unit,
    onSaveSuccess: (Int) -> Unit,
    onShareBackup: () -> Unit,
    onInspectFile: (Uri) -> Unit,
    onExecuteRestore: (Boolean) -> Unit,
    onClearPendingRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var replaceExisting by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingExportCount by remember { mutableStateOf(0) }

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
                            text = "Safe & effortless recipe preservation",
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
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (statusMessage.contains("failed", ignoreCase = true) || statusMessage.contains("error", ignoreCase = true)) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, if (statusMessage.contains("failed", ignoreCase = true) || statusMessage.contains("error", ignoreCase = true)) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (statusMessage.contains("failed", ignoreCase = true) || statusMessage.contains("error", ignoreCase = true)) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (statusMessage.contains("failed", ignoreCase = true) || statusMessage.contains("error", ignoreCase = true)) Color(0xFFDC2626) else Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (statusMessage.contains("failed", ignoreCase = true) || statusMessage.contains("error", ignoreCase = true)) Color(0xFF991B1B) else Color(0xFF166534),
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
                            text = if (lastBackupDate != null) "Last exported: $lastBackupDate" else "No backup exported yet on this device.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF78716C),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // SECTION 1: CREATE BACKUP
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "1. Create Backup (Export)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF431407)
                            )
                        )
                    }

                    Text(
                        text = "Exports your entire recipe collection into a single, standardized backup file with all ingredients, steps, notes, and photos.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF57534E), fontSize = 12.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportToJson { json, count ->
                                    pendingExportJson = json
                                    pendingExportCount = count
                                    createDocumentLauncher.launch(BackupManager.getSuggestedFileName())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_backup_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onShareBackup,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, TerracottaPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_backup_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send / Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE7DAC7))

                // SECTION 2: RESTORE FROM BACKUP
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. Restore from Backup (Import)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF431407)
                            )
                        )
                    }

                    Text(
                        text = "Pick an existing backup (.json) from your device storage, Google Drive, or email attachments.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF57534E), fontSize = 12.sp)
                    )

                    OutlinedButton(
                        onClick = {
                            openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF4B5563)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_backup_file_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Backup File (.json)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                    }
                }

                // PENDING RESTORE CONFIRMATION PANEL
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
                                        text = "Valid Backup Detected",
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
                                text = "Found ${pendingRestoreManifest.recipeCount} recipes (Exported: ${pendingRestoreManifest.exportedAtFormatted.ifEmpty { "Recent" }})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF166534))
                            )

                            // Preview list of first 3 recipes
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                pendingRestoreManifest.recipes.take(3).forEach { recipe ->
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
                                if (pendingRestoreManifest.recipes.size > 3) {
                                    Text(
                                        text = "+ ${pendingRestoreManifest.recipes.size - 3} more recipes...",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D), fontSize = 11.sp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFBBF7D0))

                            // Restore Option: Merge vs Replace
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Choose Restore Method:",
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
                                            text = "Merge (Recommended)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                        )
                                        Text(
                                            text = "Keeps your current recipes and adds the backup recipes.",
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
                                            text = "Overwrites current cookbook with this backup.",
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
                                        if (replaceExisting) "Replace & Restore Now" else "Merge & Restore Now",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
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
                            text = "Backups are stored as open standard JSON format. They work across phones, tablets, and can be stored in Google Drive indefinitely.",
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
