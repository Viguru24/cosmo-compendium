package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlossaryItem
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

/**
 * Culinary Glossary Bottom Sheet displaying translations, culinary tips,
 * and reliable regional substitutes for traditional terms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryBottomSheet(
    item: GlossaryItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDF9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.germanName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TerracottaPrimary
                    )
                )

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "English: ${item.englishName}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B5B4E)
                )
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2D6C5))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF3B2E24), lineHeight = 20.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Best Substitutes:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535))
            )
            Spacer(modifier = Modifier.height(4.dp))
            item.substitutes.forEach { sub ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("• ", color = SageGreen, fontWeight = FontWeight.Bold)
                    Text(text = sub, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF292524)))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2D5C3))
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.culinaryTip,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic, color = Color(0xFF5A4535))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
