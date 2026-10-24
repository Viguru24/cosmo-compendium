package com.example.ui.components.bookshelf

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun BookshelfEmptyState(
    activeProfile: String,
    languageMode: LanguageMode,
    onScanClick: () -> Unit,
    onTourClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (activeProfile == AppLocalization.getFilterAllFamily(languageMode)) AppLocalization.getEmptyStateTitle(languageMode) else AppLocalization.getEmptyProfileTitle(activeProfile, languageMode),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1E14),
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppLocalization.getEmptyStateSubtitle(languageMode),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF786555),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        fontSize = 12.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action: Scan Recipe Cards
                Button(
                    onClick = onScanClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppLocalization.getScanPhysicalCardsButton(languageMode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Guide Tour Button
                Surface(
                    onClick = onTourClick,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("✨", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.GERMAN) "Schnelle 1-Minuten-Anleitung ansehen" else "Take the 1-Minute Cookbook Tour",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action: Restore Backup
                OutlinedButton(
                    onClick = onRestoreBackupClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFB0A294)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text(AppLocalization.getRestoreBackupButton(languageMode), color = Color(0xFF2C1E14), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}
