package com.example.ui.components.booklet

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecipeEntity
import com.example.data.model.UnitSystem
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.getDisplayTitle
import com.example.util.pdf.RecipePdfGenerator

/**
 * Share Recipe Card & PDF Generator Dialog.
 * Generates beautifully formatted A4 PDF cards with decorative parchment styling
 * and provides one-tap plain-text clipboard copying or system sharing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRecipeCardDialog(
    recipe: RecipeEntity,
    servingMultiplier: Float = 1.0f,
    unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
    onAddToShoppingList: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) } // 0: PDF Card, 1: Plain Text
    var includeLore by remember { mutableStateOf(true) }
    var includeTips by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var currentMultiplier by remember { mutableStateOf(servingMultiplier) }
    var currentUnitSystem by remember { mutableStateOf(unitSystem) }

    // Launcher for saving PDF to storage
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        if (targetUri != null) {
            try {
                val pdfFile = RecipePdfGenerator.generateRecipePdf(
                    context = context,
                    recipe = recipe,
                    multiplier = currentMultiplier,
                    unitSystem = currentUnitSystem,
                    includeLore = includeLore,
                    includeTips = includeTips,
                    includeNotes = includeNotes
                )
                val success = RecipePdfGenerator.savePdfToUri(context, targetUri, pdfFile)
                if (success) {
                    Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val shareableText = remember(recipe, includeLore, includeTips, includeNotes, currentMultiplier) {
        buildString {
            appendLine(recipe.getDisplayTitle())
            val servStr = if (currentMultiplier != 1.0f) "${recipe.servings} (${currentMultiplier}x scaled)" else recipe.servings
            appendLine("Category: ${recipe.category} | Servings: $servStr | Total Time: ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min")
            appendLine("\nINGREDIENTS:")
            var currentGroup: String? = null
            recipe.ingredients.forEach { ing ->
                val grp = ing.getLocalizedGroup()
                if (!grp.isNullOrBlank() && grp != currentGroup) {
                    currentGroup = grp
                    appendLine("\n[$grp]")
                }
                val name = ing.getDisplayName()
                val opt = if (ing.isOptional) " (optional)" else ""
                val scaledAmt = try {
                    val rawVal = ing.amount.trim().toDoubleOrNull()
                    if (rawVal != null && currentMultiplier != 1.0f) {
                        val scaled = rawVal * currentMultiplier
                        if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format(java.util.Locale.US, "%.1f", scaled)
                    } else ing.amount
                } catch (_: Exception) {
                    ing.amount
                }
                appendLine("• $scaledAmt ${ing.unit} $name$opt".trim())
            }
            appendLine("\nINSTRUCTIONS:")
            recipe.steps.forEachIndexed { i, s ->
                val instr = s.getInstruction()
                val timerStr = if (s.timerMinutes > 0) " (${s.timerMinutes} min)" else ""
                appendLine("${i + 1}. $instr$timerStr")
                if (includeTips && !s.tip.isNullOrBlank()) {
                    val tip = s.getLocalizedTip() ?: s.tip
                    appendLine("   Tip: $tip")
                }
            }
            if (includeLore && recipe.originStory.isNotBlank()) {
                appendLine("\nHERITAGE LORE:\n${recipe.originStory}")
            }
            if (includeNotes && recipe.notes.isNotBlank()) {
                appendLine("\nFAMILY NOTES:\n${recipe.notes}")
            }
            appendLine("\nShared from Cosmo Compendium")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        fontFamily = FontFamily.Serif
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Selection: PDF vs Plain Text
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF3EAD8),
                    contentColor = TerracottaPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Printable PDF Card", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Plain Text Card", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                }

                // Options
                Text("Customize Content:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Lore & Heritage Story", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeLore, onCheckedChange = { includeLore = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Chef & Secret Tips", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeTips, onCheckedChange = { includeTips = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Family Notes", style = MaterialTheme.typography.bodySmall)
                    Checkbox(checked = includeNotes, onCheckedChange = { includeNotes = it })
                }

                // Portion Multiplier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portions: ${if (currentMultiplier % 1f == 0f) "${currentMultiplier.toInt()}x" else "${currentMultiplier}x"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.5f, 1.0f, 2.0f, 3.0f).forEach { m ->
                            val isSel = currentMultiplier == m
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) TerracottaPrimary else Color(0xFFE5DCD0),
                                modifier = Modifier.clickable { currentMultiplier = m }
                            ) {
                                Text(
                                    text = "${if (m % 1f == 0f) m.toInt() else m}x",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color.White else Color(0xFF451A03),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // PDF Info Card Preview
                    Surface(
                        color = Color(0xFFFAF7F0),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFC89B6D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${recipe.getDisplayTitle()} (PDF)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF451A03)
                                        )
                                    )
                                    Text(
                                        text = "A4 Printable Format • Includes decorative borders, checkboxes & notes",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF78350F), fontSize = 10.5.sp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val uri = RecipePdfGenerator.createShareablePdfUri(
                                            context = context,
                                            recipe = recipe,
                                            multiplier = currentMultiplier,
                                            unitSystem = currentUnitSystem,
                                            includeLore = includeLore,
                                            includeTips = includeTips,
                                            includeNotes = includeNotes
                                        )
                                        if (uri != null) {
                                            RecipePdfGenerator.shareRecipePdf(context, uri, recipe.getDisplayTitle())
                                            onDismiss()
                                        } else {
                                            Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val safeName = recipe.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                                        savePdfLauncher.launch("${safeName}_Recipe.pdf")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Plain Text Preview Card
                    Text("Live Text Preview:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF5A4535)))
                    Surface(
                        color = Color(0xFFFAF7F0),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFD6C5AD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = shareableText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF3B2E24),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Recipe: ${recipe.title}")
                                    putExtra(Intent.EXTRA_TEXT, shareableText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Recipe via..."))
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Text", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareableText))
                                Toast.makeText(context, "Recipe text copied to clipboard", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Text", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Quick add to shopping list button inside share dialog
                if (onAddToShoppingList != null) {
                    HorizontalDivider(color = Color(0xFFE2D6C5))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF7F2E8),
                        border = BorderStroke(1.dp, Color(0xFFD6C5AD)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAddToShoppingList()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Ingredients to Shopping List",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF451A03))
                                )
                            }
                            Text(
                                text = "Add +",
                                style = MaterialTheme.typography.labelSmall.copy(color = TerracottaPrimary, fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
