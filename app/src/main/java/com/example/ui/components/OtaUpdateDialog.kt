package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppUpdateManager
import com.example.data.UpdateInfo
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.launch

@Composable
fun OtaUpdateDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateManager = remember { AppUpdateManager(context) }

    var isChecking by remember { mutableStateOf(true) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    fun runUpdateCheck() {
        isChecking = true
        errorMessage = null
        scope.launch {
            try {
                val result = updateManager.checkForUpdates()
                updateInfo = result
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to reach update server"
            } finally {
                isChecking = false
            }
        }
    }

    LaunchedEffect(Unit) {
        runUpdateCheck()
    }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFFDF9),
            border = BorderStroke(1.2.dp, Color(0xFFE8DFD5)),
            shadowElevation = 16.dp,
            modifier = modifier
                .fillMaxWidth(0.95f)
                .testTag("ota_update_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon & Title
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Over-The-Air (OTA) Updates",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF2C1E14),
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Current: v${updateManager.currentVersionName} (Build ${updateManager.currentVersionCode})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF786555),
                            fontSize = 11.sp
                        )
                    )
                }

                // Status Area
                when {
                    isChecking -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5EFEB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFF16A34A),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connecting to sovereign VPS...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF4A3E36),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Connection Notice",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    updateInfo != null -> {
                        // Update Found!
                        val info = updateInfo!!
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.2.dp, Color(0xFF6EE7B7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = Color(0xFF047857),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "New Build Available: v${info.versionName}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    )
                                }
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF047857),
                                        fontSize = 11.5.sp
                                    )
                                )

                                if (isDownloading) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        LinearProgressIndicator(
                                            progress = { downloadProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF10B981),
                                            trackColor = Color(0xFFA7F3D0)
                                        )
                                        Text(
                                            text = "Downloading update: ${(downloadProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF065F46),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Up to date!
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "You're completely up to date!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Running the latest sovereign build (v${updateManager.currentVersionName}).",
                                        color = Color(0xFF15803D),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (updateInfo != null && !isDownloading) {
                        Button(
                            onClick = {
                                isDownloading = true
                                scope.launch {
                                    val success = updateManager.downloadAndInstallUpdate(
                                        apkUrl = updateInfo!!.apkUrl,
                                        onProgress = { p -> downloadProgress = p }
                                    )
                                    isDownloading = false
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Failed to download update package.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("download_and_install_update_button")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Install Update Now (v${updateInfo!!.versionName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { runUpdateCheck() },
                            enabled = !isChecking && !isDownloading,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFD6C8B4))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Again", fontSize = 11.5.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = onDismiss,
                            enabled = !isDownloading
                        ) {
                            Text("Close", color = Color(0xFF786555), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
