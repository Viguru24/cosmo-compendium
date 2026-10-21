package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.LanguageMode
import com.example.data.model.UnitSystem
import com.example.ui.theme.ParchmentPage
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.getDisplayTitle

/**
 * Vintage Handwritten Recipe Card view for quick scanning and rustic presentation.
 */
@Composable
fun VintageHandwrittenCardView(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(1.5.dp, Color(0xFFC7B198), RoundedCornerShape(16.dp)),
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
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "VINTAGE RECIPE CARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    )
                    Text(
                        text = "Heirloom Collection",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF6B5B4E)
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFECE4D7),
                    border = BorderStroke(1.dp, Color(0xFFD6C7B2)),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "HEIRLOOM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4535),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = Color(0xFFD6C7B2)
            )

            // Title
            Text(
                text = recipe.getDisplayTitle(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF3B2E24),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Servings: ${recipe.servings} • Prep: ${recipe.prepTimeMinutes}m • Cook: ${recipe.cookTimeMinutes}m",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF6B5B4E)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients
            Text(
                text = "Ingredients:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4535)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            recipe.ingredients.forEach { ing ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("• ", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${ing.getConvertedAmount(unitSystem)} ${ing.getDisplayName()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF292524)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Method:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4535)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            recipe.steps.forEachIndexed { idx, step ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "${idx + 1}. ${step.getInstruction()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF292524),
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Return to Book View", fontWeight = FontWeight.Bold)
            }
        }
    }
}
