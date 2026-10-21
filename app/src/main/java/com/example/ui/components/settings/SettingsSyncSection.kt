package com.example.ui.components.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.SageGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsSyncSection(
    currentLanguage: LanguageMode,
    isCloudSyncEnabled: Boolean,
    onToggleCloudSync: (Boolean) -> Unit,
    syncServerUrl: String,
    onSyncServerUrlChange: (String) -> Unit,
    syncSecretToken: String,
    onSyncSecretTokenChange: (String) -> Unit,
    onTestSyncConnection: () -> Unit,
    isTestingSyncConnection: Boolean,
    syncConnectionTestResult: Pair<Boolean, String>?,
    onTriggerSyncNow: () -> Unit,
    isSyncing: Boolean,
    lastSyncStatus: String?,
    lastSyncTimestamp: Long,
    autoSyncWifi: Boolean,
    onToggleAutoSyncWifi: (Boolean) -> Unit,
    cardBorder: Color = Color(0xFFE8DFD5),
    headerDark: Color = Color(0xFF2C1E14),
    textMuted: Color = Color(0xFF786555)
) {
    var showSecretToken by remember { mutableStateOf(false) }
    val syncDateStr = remember(lastSyncTimestamp) {
        if (lastSyncTimestamp <= 0L) {
            "Never"
        } else {
            val diff = System.currentTimeMillis() - lastSyncTimestamp
            if (diff < 60_000) {
                "Just now"
            } else {
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF6FAF7),
        border = BorderStroke(1.5.dp, if (isCloudSyncEnabled) SageGreen else cardBorder),
        modifier = Modifier.fillMaxWidth().testTag("settings_cloud_sync_section")
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
                        color = if (isCloudSyncEnabled) SageGreen.copy(alpha = 0.15f) else Color(0xFFE5E7EB),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (isCloudSyncEnabled) SageGreen else Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Cloud Hub & Multi-Geräte Sync" else "Cloud Hub & Multi-Device Sync",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = headerDark
                            )
                        )
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) (if (isCloudSyncEnabled) "Aktiv • Verbunden mit privatem VPS" else "Deaktiviert • 100% Offline-Modus") else (if (isCloudSyncEnabled) "Active • Connected to Private VPS" else "Disabled • 100% Offline Local Mode"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCloudSyncEnabled) SageGreen else Color(0xFF6B7280),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Switch(
                    checked = isCloudSyncEnabled,
                    onCheckedChange = onToggleCloudSync,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SageGreen
                    ),
                    modifier = Modifier.testTag("toggle_cloud_sync")
                )
            }

            Text(
                text = if (currentLanguage == LanguageMode.GERMAN) "Synchronisiert Rezepte, Titelbilder und Profile nahtlos zwischen Smartphone und Tablet. Schneller Delta-Sync über WLAN und Mobilfunk." else "Syncs recipes, covers, and family profiles between your phone and tablet seamlessly. Fast delta sync over 4G/5G and Wi-Fi.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )

            if (isCloudSyncEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFD1E7DD), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Server URL input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Server-Adresse (VPS / Domain)" else "Server URL (VPS / Domain)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                        )
                        OutlinedTextField(
                            value = syncServerUrl,
                            onValueChange = onSyncServerUrlChange,
                            placeholder = { Text("https://api.cosmowhisper.com/cookbook", fontSize = 11.5.sp, color = Color(0xFF9CA3AF)) },
                            leadingIcon = {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth().testTag("sync_server_url_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111827),
                                unfocusedTextColor = Color(0xFF111827),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SageGreen,
                                unfocusedBorderColor = Color(0xFFC3E0CC)
                            ),
                            singleLine = true
                        )
                    }

                    // Sync Secret Token
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Sync-Sicherheitsschlüssel (Token)" else "Sync Security Token",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = headerDark)
                        )
                        OutlinedTextField(
                            value = syncSecretToken,
                            onValueChange = onSyncSecretTokenChange,
                            placeholder = { Text(if (currentLanguage == LanguageMode.GERMAN) "Geheimer VPS API-Schlüssel" else "Secret VPS API key", fontSize = 11.5.sp, color = Color(0xFF9CA3AF)) },
                            leadingIcon = {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { showSecretToken = !showSecretToken }) {
                                    Icon(
                                        if (showSecretToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showSecretToken) "Hide token" else "Show token",
                                        tint = Color(0xFF4B5563),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showSecretToken) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("sync_secret_token_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111827),
                                unfocusedTextColor = Color(0xFF111827),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = SageGreen,
                                unfocusedBorderColor = Color(0xFFC3E0CC)
                            ),
                            singleLine = true
                        )
                    }

                    // Test Connection & Sync Now Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestSyncConnection,
                            enabled = !isTestingSyncConnection && syncServerUrl.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SageGreen),
                            modifier = Modifier.weight(1f).testTag("test_sync_connection_button")
                        ) {
                            if (isTestingSyncConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = SageGreen, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentLanguage == LanguageMode.GERMAN) "Teste..." else "Testing...", fontSize = 11.5.sp, color = SageGreen)
                            } else {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = SageGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLanguage == LanguageMode.GERMAN) "Verbindung testen" else "Test Connection", fontSize = 11.5.sp, color = SageGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onTriggerSyncNow,
                            enabled = !isSyncing && syncServerUrl.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sync_now_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (currentLanguage == LanguageMode.GERMAN) "Synchronisiere..." else "Syncing...", fontSize = 11.5.sp, color = Color.White)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (currentLanguage == LanguageMode.GERMAN) "Jetzt synchronisieren" else "Sync Now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Connection Test Result Badge
                    if (syncConnectionTestResult != null) {
                        val (isSuccess, message) = syncConnectionTestResult
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, if (isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth().testTag("sync_connection_status_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isSuccess) SageGreen else Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSuccess) Color(0xFF14532D) else Color(0xFF991B1B),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Last Synced Timestamp & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLanguage == LanguageMode.GERMAN) "Zuletzt synchronisiert: $syncDateStr" else "Last Synced: $syncDateStr",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        if (lastSyncStatus != null) {
                            Text(
                                text = lastSyncStatus,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SageGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // Auto-Sync on Wi-Fi Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == LanguageMode.GERMAN) "Automatisch im WLAN synchronisieren" else "Auto-Sync on Wi-Fi",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = headerDark
                                    )
                                )
                                Text(
                                    text = if (currentLanguage == LanguageMode.GERMAN) "Änderungen automatisch im Hintergrund übertragen" else "Syncs changes automatically in background",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textMuted,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                        Switch(
                            checked = autoSyncWifi,
                            onCheckedChange = onToggleAutoSyncWifi,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SageGreen
                            ),
                            modifier = Modifier.testTag("toggle_auto_sync_wifi")
                        )
                    }
                }
            }
        }
    }
}
