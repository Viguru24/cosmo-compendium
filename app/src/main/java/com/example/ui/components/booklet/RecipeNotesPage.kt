package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.delay

/**
 * Final Page: Cook's Journal & Family Notes.
 * Allows cooks to rate recipes, increment the times cooked count,
 * and save personalized notes, baking secrets, and family modifications.
 */
@Composable
fun RecipeCookJournalPage(
    recipe: RecipeEntity,
    onSaveJournal: (String, Int) -> Unit,
    onIncrementCooked: () -> Unit,
    onStartCookingMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var notesText by remember(recipe.id) { mutableStateOf(recipe.notes) }
    var rating by remember(recipe.id) { mutableStateOf(recipe.rating) }
    var showSavedBadge by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Auto-save debounce when typing notes or selecting stars
    LaunchedEffect(notesText, rating) {
        if (notesText != recipe.notes || rating != recipe.rating) {
            delay(500)
            onSaveJournal(notesText, rating)
            showSavedBadge = true
            delay(1500)
            showSavedBadge = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(10.dp, RoundedCornerShape(12.dp))
            .border(1.dp, ParchmentPageEdge, RoundedCornerShape(12.dp)),
        color = ParchmentPage
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Final Page • Notes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6B5B4E),
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showSavedBadge) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "✓ Saved",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Cook's Journal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

            // Family Star Rating
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2E8)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFE2D5C3))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Family Star Rating",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A4535)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (rating > 0) "$rating / 5 Stars" else "Tap to rate this recipe",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (rating > 0) TerracottaPrimary else Color(0xFF8C7A6B),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    rating = star
                                    onSaveJournal(notesText, star)
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= rating) TerracottaPrimary else Color(0xFFD1C7B7),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Times Cooked Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECE4D7), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Times Cooked",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535))
                    )
                    Text(
                        text = "${recipe.timesCooked} meals prepared",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF3B2E24))
                    )
                }

                Button(
                    onClick = onIncrementCooked,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cooked Today", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Personal Notes & secret adjustments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cook's Notes & Kitchen Secrets:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4535)
                    )
                )
                TextButton(
                    onClick = {
                        onSaveJournal(notesText, rating)
                        showSavedBadge = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Save Notes", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                singleLine = false,
                minLines = 5,
                maxLines = 12,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF1E140C),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                ),
                placeholder = {
                    Text(
                        "Write your notes here: e.g. baked for 5 extra minutes, added a dash of nutmeg, special oven setting...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF786250),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1E140C),
                    unfocusedTextColor = Color(0xFF1E140C),
                    focusedPlaceholderColor = Color(0xFF786250),
                    unfocusedPlaceholderColor = Color(0xFF786250),
                    focusedBorderColor = TerracottaPrimary,
                    unfocusedBorderColor = Color(0xFF8C7B6B),
                    focusedContainerColor = Color(0xFFFFFFFF),
                    unfocusedContainerColor = Color(0xFFFFFDF9),
                    cursorColor = TerracottaPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
