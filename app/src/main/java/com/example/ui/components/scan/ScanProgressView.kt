package com.example.ui.components.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.ScannedPageThumbnail
import com.example.ui.theme.TerracottaPrimary
import java.io.File

@Composable
fun ScanProgressView(
    scannedPages: List<ScannedPageThumbnail>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Glowing Icon & Animated Spinner
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFF4EB),
                border = BorderStroke(2.dp, Color(0xFFFFD8BF)),
                shadowElevation = 4.dp,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        color = TerracottaPrimary,
                        modifier = Modifier.size(68.dp),
                        strokeWidth = 3.5.dp
                    )
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transcribing Recipe...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF431407),
                    fontFamily = FontFamily.Serif
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Deciphering handwriting, fractions & cooking instructions",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF785E48),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TerracottaPrimary,
                trackColor = Color(0xFFEADBCE)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Captured Pages Preview Container
            if (scannedPages.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (scannedPages.size == 1) "Captured Card (1 Page)" else "Captured Cards (${scannedPages.size} Pages)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF292524)
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✨", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Synthesizing",
                                        color = Color(0xFF92400E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            scannedPages.forEachIndexed { index, item ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, Color(0xFFE2D6C5)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(170.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = if (item.filePath.isNotBlank()) File(item.filePath) else item.bitmap,
                                            contentDescription = "Page ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            color = Color(0xDD292524),
                                            shape = RoundedCornerShape(topStart = 8.dp),
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                        ) {
                                            Text(
                                                text = if (index == 0 && scannedPages.size > 1) "Page 1 • Front" else if (index == 1) "Page 2 • Back" else "Page ${index + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clean Progress Step Indicators
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "🔍 Deciphering cursive handwriting" to true,
                        "🌐 Preserving German heritage & translating culinary terms" to true,
                        "⚖️ Standardizing vintage units & fractional measurements" to true,
                        "🥘 Detecting section headers (Dough, Filling, Glaze)" to true,
                        "✨ Extracting notes, oven temps & yield specifications" to true
                    ).forEach { (stepTitle, isDone) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDone) Color(0xFFDCFCE7) else Color(0xFFFFF7ED),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isDone) "✓" else "•",
                                        color = if (isDone) Color(0xFF16A34A) else TerracottaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = stepTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDone) Color(0xFF44403C) else TerracottaPrimary,
                                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
