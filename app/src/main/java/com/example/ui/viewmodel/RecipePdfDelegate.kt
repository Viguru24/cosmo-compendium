package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.data.local.RecipeEntity
import com.example.data.model.UnitSystem
import com.example.data.repository.RecipeRepository
import com.example.ui.util.getDisplayTitle
import com.example.util.pdf.RecipePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Delegate responsible for PDF generation and exporting of single recipes and full cookbook.
 */
class RecipePdfDelegate(
    private val vm: RecipeViewModel,
    private val repository: RecipeRepository
) {
    val isExportingFullPdf = MutableStateFlow(false)

    fun exportFullCookbookPdf(context: Context) {
        if (isExportingFullPdf.value) return
        vm.viewModelScope.launch(Dispatchers.IO) {
            isExportingFullPdf.value = true
            try {
                val allRecipes = repository.getAllRecipesDirect()
                if (allRecipes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No recipes found to export!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val activeProfile = vm.activeProfile.value
                val title = if (activeProfile == "All" || activeProfile == "All Family") "Family Cookbook" else "$activeProfile's Cookbook"
                val uri = RecipePdfGenerator.createShareableFullCookbookPdfUri(
                    context = context,
                    recipes = allRecipes,
                    cookbookTitle = title,
                    profileName = activeProfile,
                    unitSystem = vm.unitSystem.value
                )
                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        RecipePdfGenerator.shareFullCookbookPdf(context, uri, title, allRecipes.size)
                    } else {
                        Toast.makeText(context, "Failed to generate master cookbook PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                isExportingFullPdf.value = false
            }
        }
    }

    fun shareRecipeAsPdf(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ) {
        vm.viewModelScope.launch {
            val uri = RecipePdfGenerator.createShareablePdfUri(
                context = context,
                recipe = recipe,
                multiplier = multiplier,
                unitSystem = unitSystem,
                includeLore = includeLore,
                includeTips = includeTips,
                includeNotes = includeNotes
            )
            if (uri != null) {
                RecipePdfGenerator.shareRecipePdf(context, uri, recipe.getDisplayTitle())
            }
        }
    }

    fun generatePdfFile(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ): File {
        return RecipePdfGenerator.generateRecipePdf(
            context = context,
            recipe = recipe,
            multiplier = multiplier,
            unitSystem = unitSystem,
            includeLore = includeLore,
            includeTips = includeTips,
            includeNotes = includeNotes
        )
    }

    fun savePdfToTargetUri(context: Context, targetUri: Uri, pdfFile: File): Boolean {
        return RecipePdfGenerator.savePdfToUri(context, targetUri, pdfFile)
    }
}
