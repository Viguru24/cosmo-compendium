package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ImageGenEngine
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.BatchCoverFilter
import com.example.ui.util.RecipePhotoStats

@Composable
fun BatchCoverGenerationDialog(
    isGenerating: Boolean,
    progress: Pair<Int, Int>, // current, total
    currentTitle: String,
    successCount: Int,
    failCount: Int,
    stats: RecipePhotoStats,
    selectedFilter: BatchCoverFilter,
    engine: ImageGenEngine,
    statusLog: String?,
    onFilterChange: (BatchCoverFilter) -> Unit,
    onStartBatch: (BatchCoverFilter) -> Unit,
    onCancelBatch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (current, total) = progress
    val progressFraction = if (total > 0) current.toFloat() / total.toFloat() else 0f
    val isFinished = !isGenerating && (successCount > 0 || failCount > 0)

    val targetCount = when (selectedFilter) {
        BatchCoverFilter.MISSING_AI_PHOTOS -> stats.missingAiCount
        BatchCoverFilter.SCANNED_CARDS_ONLY -> stats.scannedCardCount
        BatchCoverFilter.NO_PHOTO_ONLY -> stats.unphotographedCount
        BatchCoverFilter.ALL_RECIPES -> stats.total
    }

    AlertDialog(
        onDismissRequest = {
            if (!isGenerating) onDismiss()
        },
        modifier = modifier.testTag("batch_cover_generation_dialog"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFFDFBF7),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Batch AI Food Photos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF451A03)
                            )
                        )
                        Text(
                            text = if (engine == ImageGenEngine.COMFY_UI) "Using Local ComfyUI" else "Using Cloud Gemini AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF786555),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF5A4535))
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
                if (isGenerating) {
                    // In-Progress State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.5.dp, Color(0xFFFED7AA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generating Photo $current of $total",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9A3412)
                                    )
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = TerracottaPrimary,
                                trackColor = Color(0xFFFFEDD5),
                                strokeCap = StrokeCap.Round
                            )

                            if (currentTitle.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF451A03)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "✓ $successCount created",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SageGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (failCount > 0) {
                                    Text(
                                        text = "✗ $failCount failed",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else if (isFinished) {
                    // Finished State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batch Process Complete!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                            Text(
                                text = "Successfully generated $successCount cover photos." + if (failCount > 0) " ($failCount failed)" else "",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF14532D))
                            )
                        }
                    }
                } else {
                    // Photo Classification Breakdown Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5ECE2),
                        border = BorderStroke(1.dp, Color(0xFFDCCBB5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cookbook Photo Analysis (${stats.total} Recipes)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF451A03)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // AI Photos
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "✨ AI Photos", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                        Text(text = "${stats.aiGeneratedCount}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF78350F))
                                    }
                                }

                                // Scanned Cards
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "📄 Card Scans", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                                        Text(text = "${stats.scannedCardCount}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1B4B))
                                    }
                                }

                                // Unphotographed
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF3F4F6),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "📷 No Photo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                                        Text(text = "${stats.unphotographedCount}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                                    }
                                }
                            }
                        }
                    }

                    // Target Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Select Target Batch",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF451A03)
                                )
                            )
                        }

                        BatchCoverFilter.values().forEach { filter ->
                            val countForFilter = when (filter) {
                                BatchCoverFilter.MISSING_AI_PHOTOS -> stats.missingAiCount
                                BatchCoverFilter.SCANNED_CARDS_ONLY -> stats.scannedCardCount
                                BatchCoverFilter.NO_PHOTO_ONLY -> stats.unphotographedCount
                                BatchCoverFilter.ALL_RECIPES -> stats.total
                            }
                            val isSelected = selectedFilter == filter

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFFFF7ED) else Color.White,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) TerracottaPrimary else Color(0xFFE5DDD0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFilterChange(filter) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onFilterChange(filter) },
                                        colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = filter.label,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color(0xFF7C2D12) else Color(0xFF292524)
                                                )
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (countForFilter > 0) TerracottaPrimary.copy(alpha = 0.12f) else Color(0xFFF3F4F6)
                                            ) {
                                                Text(
                                                    text = "$countForFilter",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (countForFilter > 0) TerracottaPrimary else Color(0xFF6B7280)
                                                )
                                            }
                                        }
                                        Text(
                                            text = filter.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF78716C),
                                                fontSize = 10.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Log Status Bar
                if (!statusLog.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusLog,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.padding(10.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isGenerating) {
                OutlinedButton(
                    onClick = onCancelBatch,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop Generation", fontWeight = FontWeight.Bold)
                }
            } else if (isFinished) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { onStartBatch(selectedFilter) },
                    enabled = targetCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_batch_cover_gen_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (targetCount > 0) "Generate $targetCount AI Photos" else "0 Recipes Selected",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (isGenerating) {
                TextButton(onClick = onDismiss) {
                    Text("Run in Background", color = Color(0xFF786555))
                }
            } else if (!isFinished) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF786555))
                }
            }
        }
    )
}
