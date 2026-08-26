package com.example.util.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.local.RecipeEntity
import com.example.data.model.UnitSystem
import com.example.ui.util.getDisplayTitle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecipePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 standard height in points (72 dpi)
    private const val MARGIN = 36f

    fun generateRecipePdf(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        // Initialize paints
        val bgPaint = Paint().apply { color = Color.parseColor("#FCF9F2") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#C89B6D")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#E5D2BA")
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
        }

        val headerBannerPaint = Paint().apply { color = Color.parseColor("#8C2D19") }
        val badgeBgPaint = Paint().apply { color = Color.parseColor("#EFE3D3") }

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#661D00")
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#8C5835")
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val sectionHeaderPaint = TextPaint().apply {
            color = Color.parseColor("#782810")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val tipPaint = TextPaint().apply {
            color = Color.parseColor("#6B4423")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val smallMetaPaint = TextPaint().apply {
            color = Color.parseColor("#5A4535")
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Start Page 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawPageDecorations(c: Canvas, pNum: Int) {
            // Background
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

            // Outer decorative borders
            c.drawRect(MARGIN - 10, MARGIN - 10, PAGE_WIDTH - MARGIN + 10, PAGE_HEIGHT - MARGIN + 10, borderPaint)
            c.drawRect(MARGIN - 6, MARGIN - 6, PAGE_WIDTH - MARGIN + 6, PAGE_HEIGHT - MARGIN + 6, innerBorderPaint)

            // Footer
            val footerText = "Vintage Heirloom Cookbook Archive • Page $pNum"
            val footerPaint = TextPaint().apply {
                color = Color.parseColor("#9C8673")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            c.drawText(footerText, MARGIN, PAGE_HEIGHT - MARGIN + 2, footerPaint)

            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
            val dateWidth = footerPaint.measureText(dateStr)
            c.drawText(dateStr, PAGE_WIDTH - MARGIN - dateWidth, PAGE_HEIGHT - MARGIN + 2, footerPaint)
        }

        drawPageDecorations(canvas, pageNumber)

        var curY = MARGIN + 15f

        // Top Banner Title
        canvas.drawText("VINTAGE HEIRLOOM RECIPE CARD", MARGIN, curY, smallMetaPaint)
        curY += 18f

        // Title
        val mainTitle = recipe.getDisplayTitle()
        canvas.drawText(mainTitle, MARGIN, curY + 6f, titlePaint)
        curY += 28f

        // Subtitle (German / English variant)
        val secondaryTitle = if (recipe.titleGerman != null && recipe.titleGerman != mainTitle) {
            "Original: ${recipe.titleGerman}"
        } else if (recipe.titleEnglish != null && recipe.titleEnglish != mainTitle) {
            "English: ${recipe.titleEnglish}"
        } else null

        if (secondaryTitle != null) {
            canvas.drawText(secondaryTitle, MARGIN, curY, subtitlePaint)
            curY += 16f
        }

        // Info Metadata Bar (Category, Servings, Times, Difficulty)
        val infoPillRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 24f)
        canvas.drawRoundRect(infoPillRect, 6f, 6f, badgeBgPaint)

        val servingsText = if (multiplier != 1.0f) {
            "Servings: ${recipe.servings} (${multiplier}x scaled)"
        } else {
            "Servings: ${recipe.servings}"
        }
        val metaInfo = "Category: ${recipe.category}   •   $servingsText   •   Prep: ${recipe.prepTimeMinutes}m   •   Cook: ${recipe.cookTimeMinutes}m   •   ${recipe.difficulty}"
        canvas.drawText(metaInfo, MARGIN + 10f, curY + 16f, smallMetaPaint)
        curY += 34f

        // Lore Box (if enabled & present)
        if (includeLore && recipe.originStory.isNotBlank()) {
            val loreBg = Paint().apply { color = Color.parseColor("#F4ECE1") }
            val loreBorder = Paint().apply {
                color = Color.parseColor("#DECDB8")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val loreHeader = "Heritage & Lore:"
            val loreText = "\"${recipe.originStory}\""

            val layout = StaticLayout.Builder.obtain(
                loreText, 0, loreText.length, tipPaint, (PAGE_WIDTH - 2 * MARGIN - 24).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            val boxHeight = layout.height + 26f
            val loreRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + boxHeight)
            canvas.drawRoundRect(loreRect, 6f, 6f, loreBg)
            canvas.drawRoundRect(loreRect, 6f, 6f, loreBorder)

            canvas.drawText(loreHeader, MARGIN + 10f, curY + 14f, smallMetaPaint)
            canvas.save()
            canvas.translate(MARGIN + 10f, curY + 20f)
            layout.draw(canvas)
            canvas.restore()

            curY += boxHeight + 14f
        }

        // Dividers / Two-Column Layout Setup
        // Left Column: Ingredients (~190pt)
        // Right Column: Instructions (~310pt)
        val colGap = 16f
        val leftColWidth = 195f
        val rightColLeft = MARGIN + leftColWidth + colGap
        val rightColWidth = PAGE_WIDTH - MARGIN - rightColLeft

        var leftY = curY
        var rightY = curY

        // Left Header: INGREDIENTS
        canvas.drawText("INGREDIENTS", MARGIN, leftY + 12f, sectionHeaderPaint)
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#C89B6D")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, leftY + 16f, MARGIN + leftColWidth, leftY + 16f, dividerPaint)
        leftY += 28f

        // Render Ingredients
        val grouped = recipe.ingredients.groupBy { it.group ?: "Main" }
        grouped.forEach { (groupName, items) ->
            if (groupName.isNotBlank() && grouped.size > 1 && groupName != "Main") {
                canvas.drawText("• $groupName", MARGIN + 4f, leftY + 8f, boldBodyPaint)
                leftY += 16f
            }

            items.forEach { ing ->
                // Checkbox
                val cbPaint = Paint().apply {
                    color = Color.parseColor("#8C7B6B")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRect(MARGIN + 2f, leftY - 7f, MARGIN + 10f, leftY + 1f, cbPaint)

                val scaledAmount = try {
                    val rawVal = ing.amount.trim().toDoubleOrNull()
                    if (rawVal != null && multiplier != 1.0f) {
                        val scaled = rawVal * multiplier
                        if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format(Locale.US, "%.1f", scaled)
                    } else ing.amount
                } catch (e: Exception) {
                    ing.amount
                }

                val ingLine = buildString {
                    if (scaledAmount.isNotBlank()) append("$scaledAmount ")
                    if (ing.unit.isNotBlank()) append("${ing.unit} ")
                    append(ing.getDisplayName())
                    if (ing.isOptional) append(" (opt)")
                }

                val ingLayout = StaticLayout.Builder.obtain(
                    ingLine, 0, ingLine.length, bodyPaint, (leftColWidth - 16).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(MARGIN + 14f, leftY - 9f)
                ingLayout.draw(canvas)
                canvas.restore()

                leftY += ingLayout.height + 6f
            }
        }

        // Right Header: INSTRUCTIONS & METHOD
        canvas.drawText("METHOD & INSTRUCTIONS", rightColLeft, rightY + 12f, sectionHeaderPaint)
        canvas.drawLine(rightColLeft, rightY + 16f, PAGE_WIDTH - MARGIN, rightY + 16f, dividerPaint)
        rightY += 28f

        // Render Steps
        recipe.steps.forEachIndexed { idx, step ->
            val stepNumStr = "${idx + 1}."
            canvas.drawText(stepNumStr, rightColLeft, rightY + 2f, boldBodyPaint)

            val stepText = step.getInstruction()
            val stepLayout = StaticLayout.Builder.obtain(
                stepText, 0, stepText.length, bodyPaint, (rightColWidth - 20).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(rightColLeft + 16f, rightY - 9f)
            stepLayout.draw(canvas)
            canvas.restore()

            var stepAddedHeight = stepLayout.height + 6f

            // Step Timer badge if applicable
            if (step.timerMinutes > 0) {
                val timerStr = "⏱ ${step.timerMinutes} min timer"
                canvas.drawText(timerStr, rightColLeft + 16f, rightY - 9f + stepAddedHeight + 2f, smallMetaPaint)
                stepAddedHeight += 14f
            }

            // Step tip if applicable
            if (includeTips && !step.tip.isNullOrBlank()) {
                val tipStr = "💡 Secret: ${step.getLocalizedTip() ?: step.tip}"
                val tipLayout = StaticLayout.Builder.obtain(
                    tipStr, 0, tipStr.length, tipPaint, (rightColWidth - 20).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(rightColLeft + 16f, rightY - 9f + stepAddedHeight)
                tipLayout.draw(canvas)
                canvas.restore()

                stepAddedHeight += tipLayout.height + 4f
            }

            rightY += stepAddedHeight + 8f

            // Check if page needs overflow handling
            if (rightY > PAGE_HEIGHT - MARGIN - 60f && idx < recipe.steps.size - 1) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageDecorations(canvas, pageNumber)
                rightY = MARGIN + 20f
                canvas.drawText("${recipe.title} (Continued)", MARGIN, rightY, sectionHeaderPaint)
                rightY += 24f
            }
        }

        // Family Notes Section (at bottom of tallest column)
        val finalContentY = maxOf(leftY, rightY)
        if (includeNotes && recipe.notes.isNotBlank() && finalContentY < PAGE_HEIGHT - MARGIN - 70f) {
            val notesBoxY = finalContentY + 12f
            canvas.drawLine(MARGIN, notesBoxY, PAGE_WIDTH - MARGIN, notesBoxY, dividerPaint)
            canvas.drawText("FAMILY CHEF NOTES", MARGIN, notesBoxY + 14f, sectionHeaderPaint)

            val notesLayout = StaticLayout.Builder.obtain(
                recipe.notes, 0, recipe.notes.length, tipPaint, (PAGE_WIDTH - 2 * MARGIN).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(MARGIN, notesBoxY + 20f)
            notesLayout.draw(canvas)
            canvas.restore()
        }

        pdfDocument.finishPage(page)

        // Write to Cache File
        val pdfDir = File(context.cacheDir, "recipe_pdfs").apply { mkdirs() }
        val safeFileName = recipe.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(pdfDir, "${safeFileName}_Recipe_Card.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    fun createShareablePdfUri(
        context: Context,
        recipe: RecipeEntity,
        multiplier: Float = 1.0f,
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
        includeLore: Boolean = true,
        includeTips: Boolean = true,
        includeNotes: Boolean = true
    ): Uri? {
        return try {
            val file = generateRecipePdf(context, recipe, multiplier, unitSystem, includeLore, includeTips, includeNotes)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareRecipePdf(context: Context, pdfUri: Uri, recipeTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Heirloom Recipe: $recipeTitle")
            putExtra(Intent.EXTRA_TEXT, "Here is the heirloom recipe card for '$recipeTitle' from my Vintage Cookbook!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Recipe PDF via..."))
    }

    fun savePdfToUri(context: Context, targetUri: Uri, pdfFile: File): Boolean {
        return try {
            context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                pdfFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
