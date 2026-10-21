package com.example.ui.components.dietary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.RecipeEntity
import com.example.data.model.DietaryTarget
import com.example.data.model.IngredientSubstitution
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeAdaptationEngine
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.ParchmentPageEdge
import com.example.ui.theme.TerracottaPrimary

/**
 * Interactive Dialog to inspect, toggle, and apply smart culinary substitutions
 * for Celiac-Safe, Vegetarian, Vegan, Dairy-Free, and Nut-Free diets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeAdaptationDialog(
    recipe: RecipeEntity,
    languageMode: LanguageMode = LanguageMode.ENGLISH,
    onDismiss: () -> Unit,
    onSaveAsVariation: (RecipeEntity) -> Unit,
    onApplyToCurrent: (RecipeEntity) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(DietaryTarget.CELIAC_SAFE) }

    val adaptationPlan = remember(recipe, selectedTarget) {
        RecipeAdaptationEngine.generateAdaptation(recipe, selectedTarget)
    }

    // Track toggled substitutions
    val selectedSubstitutions = remember(adaptationPlan) {
        mutableStateMapOf<Int, Boolean>().apply {
            adaptationPlan.substitutions.indices.forEach { put(it, true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFBF8F2),
            border = BorderStroke(1.5.dp, Color(0xFFD6C7B2)),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(selectedTarget.icon, fontSize = 20.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "Dietary Adaptations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1810)
                                )
                            )
                            Text(
                                text = "Custom Culinary Transformations & Substitutions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF78716C),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF78716C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dietary Target Selector Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DietaryTarget.values().forEach { target ->
                        val isSelected = target == selectedTarget
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) TerracottaPrimary else Color(0xFFEFE8DB),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) TerracottaPrimary else Color(0xFFD5C7B2)
                            ),
                            modifier = Modifier.clickable { selectedTarget = target }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(target.icon, fontSize = 13.sp)
                                Text(
                                    text = target.getTitle(languageMode),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFF3E2723),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF3EDE2),
                    border = BorderStroke(1.dp, Color(0xFFE2D6C5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = selectedTarget.getDescription(languageMode),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5A493B),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Substitutions List Header
                Text(
                    text = if (adaptationPlan.hasSubstitutions) {
                        "Proposed Substitutions (${adaptationPlan.substitutions.size})"
                    } else {
                        "✨ This recipe is already naturally ${selectedTarget.getTitle(languageMode)}!"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = if (adaptationPlan.hasSubstitutions) Color(0xFF2C1810) else Color(0xFF059669)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Substitution Cards Scrollable List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!adaptationPlan.hasSubstitutions) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎉", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No ingredients need replacing!",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    )
                                    Text(
                                        text = "All ingredients in this recipe align perfectly with ${selectedTarget.getTitle(languageMode)} guidelines.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF047857),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(adaptationPlan.substitutions.indices.toList()) { index ->
                            val sub = adaptationPlan.substitutions[index]
                            val isChecked = selectedSubstitutions[index] ?: true

                            SubstitutionItemCard(
                                substitution = sub,
                                isChecked = isChecked,
                                languageMode = languageMode,
                                onToggle = { selectedSubstitutions[index] = !isChecked }
                            )
                        }
                    }

                    // General Culinary Guidance Notes
                    val notes = if (languageMode == LanguageMode.GERMAN) {
                        adaptationPlan.generalNotesGerman
                    } else {
                        adaptationPlan.generalNotesEnglish
                    }

                    if (notes.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🧑‍🍳 Chef's Kitchen Tips for ${selectedTarget.getTitle(languageMode)}:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    notes.forEach { note ->
                                        Text(
                                            text = "• $note",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF78350F),
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFD6C7B2))
                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF9C8B77))
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF5A493B)
                        )
                    }

                    if (adaptationPlan.hasSubstitutions) {
                        Button(
                            onClick = {
                                val activeSubs = adaptationPlan.substitutions.filterIndexed { idx, _ ->
                                    selectedSubstitutions[idx] == true
                                }
                                val adapted = RecipeAdaptationEngine.applySelectedSubstitutions(
                                    recipe,
                                    activeSubs,
                                    selectedTarget
                                )
                                onSaveAsVariation(adapted)
                            },
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Variation",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubstitutionItemCard(
    substitution: IngredientSubstitution,
    isChecked: Boolean,
    languageMode: LanguageMode,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isChecked) Color(0xFFF9F5EC) else Color(0xFFEBE5D9)),
        border = BorderStroke(1.dp, if (isChecked) Color(0xFFD6C7B2) else Color(0xFFCCC0AE)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = TerracottaPrimary),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Original -> Replacement Header
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Original: ${substitution.original.amount} ${substitution.original.unit} ${substitution.original.getDisplayName(languageMode)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isChecked) Color(0xFF991B1B) else Color(0xFF78716C),
                            fontSize = 12.5.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${substitution.replacement.amount} ${substitution.replacement.unit} ${substitution.replacement.getDisplayName(languageMode)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Reason & Culinary Tip
            Text(
                text = substitution.getReason(languageMode),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF5A493B),
                    fontSize = 11.5.sp
                )
            )

            substitution.getCulinaryTip(languageMode)?.let { tip ->
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF2ECE1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Tip: $tip",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF78350F),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
