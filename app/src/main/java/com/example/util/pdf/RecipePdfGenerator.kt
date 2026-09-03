package com.example.util.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.local.RecipeEntity
import com.example.data.model.LanguageMode
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

    private fun loadScaledRecipeCoverBitmap(imageUri: String?, targetWidth: Int, targetHeight: Int): Bitmap? {
        if (imageUri.isNullOrBlank()) return null
        val file = File(imageUri)
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            var inSampleSize = 1
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
                inSampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val raw = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null
            Bitmap.createScaledBitmap(raw, targetWidth, targetHeight, true)
        } catch (e: Throwable) {
            null
        }
    }

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
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val tipPaint = TextPaint().apply {
            color = Color.parseColor("#6B4423")
            textSize = 9f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val smallMetaPaint = TextPaint().apply {
            color = Color.parseColor("#5A4535")
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        fun drawPageDecorations(c: Canvas, pNum: Int, isMaster: Boolean = false) {
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            c.drawRect(MARGIN - 10, MARGIN - 10, PAGE_WIDTH - MARGIN + 10, PAGE_HEIGHT - MARGIN + 10, borderPaint)
            c.drawRect(MARGIN - 6, MARGIN - 6, PAGE_WIDTH - MARGIN + 6, PAGE_HEIGHT - MARGIN + 6, innerBorderPaint)

            val footerText = if (isMaster) "Master Compendium Archive • Page $pNum" else "Compendium Card • Page $pNum"
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

        // Start Page 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        drawPageDecorations(canvas, pageNumber)

        var curY = MARGIN + 15f

        // Top Banner Title
        canvas.drawText("COMPENDIUM CARD", MARGIN, curY, smallMetaPaint)
        curY += 18f

        // Title
        val mainTitle = recipe.getDisplayTitle()
        canvas.drawText(mainTitle, MARGIN, curY + 6f, titlePaint)
        curY += 26f

        // Subtitle (German / English variant)
        val secondaryTitle = if (recipe.titleGerman != null && recipe.titleGerman != mainTitle && recipe.titleGerman.isNotBlank()) {
            "Original: ${recipe.titleGerman}"
        } else if (recipe.titleEnglish != null && recipe.titleEnglish != mainTitle && recipe.titleEnglish.isNotBlank()) {
            "English: ${recipe.titleEnglish}"
        } else null

        if (secondaryTitle != null) {
            canvas.drawText(secondaryTitle, MARGIN, curY, subtitlePaint)
            curY += 16f
        }

        // Info Metadata Bar (Category, Servings, Times, Difficulty)
        val infoPillRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 22f)
        canvas.drawRoundRect(infoPillRect, 6f, 6f, badgeBgPaint)

        val servingsText = if (multiplier != 1.0f) {
            "Servings: ${recipe.servings} (${multiplier}x scaled)"
        } else {
            "Servings: ${recipe.servings}"
        }
        val metaInfo = "Category: ${recipe.category}   •   $servingsText   •   Prep: ${recipe.prepTimeMinutes}m   •   Cook: ${recipe.cookTimeMinutes}m   •   ${recipe.difficulty}"
        canvas.drawText(metaInfo, MARGIN + 10f, curY + 15f, smallMetaPaint)
        curY += 30f

        // Heritage Lore Box (if present)
        if (includeLore && recipe.originStory.isNotBlank() && !recipe.originStory.equals("Scanned recipe.", ignoreCase = true)) {
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

            val boxHeight = layout.height + 24f
            val loreRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + boxHeight)
            canvas.drawRoundRect(loreRect, 6f, 6f, loreBg)
            canvas.drawRoundRect(loreRect, 6f, 6f, loreBorder)

            canvas.drawText(loreHeader, MARGIN + 10f, curY + 13f, smallMetaPaint)
            canvas.save()
            canvas.translate(MARGIN + 10f, curY + 18f)
            layout.draw(canvas)
            canvas.restore()

            curY += boxHeight + 12f
        }

        // Two-Column Setup
        // Left Column: AI Food Photo + Ingredients (~195pt)
        // Right Column: Instructions & Steps (~310pt)
        val colGap = 16f
        val leftColWidth = 195f
        val rightColLeft = MARGIN + leftColWidth + colGap
        val rightColWidth = PAGE_WIDTH - MARGIN - rightColLeft

        var leftY = curY
        var rightY = curY

        // Draw AI Generated Dish Cover Photo at top of left column if present!
        val photoHeight = 130f
        val coverBitmap = loadScaledRecipeCoverBitmap(recipe.imageUri, (leftColWidth * 2).toInt(), (photoHeight * 2).toInt())
        if (coverBitmap != null) {
            val photoRect = RectF(MARGIN, leftY, MARGIN + leftColWidth, leftY + photoHeight)
            val photoPath = Path().apply {
                addRoundRect(photoRect, 6f, 6f, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(photoPath)
            canvas.drawBitmap(coverBitmap, null, photoRect, null)
            canvas.restore()
            canvas.drawRoundRect(photoRect, 6f, 6f, borderPaint)
            leftY += photoHeight + 14f
        }

        // Left Header: INGREDIENTS
        canvas.drawText("INGREDIENTS", MARGIN, leftY + 10f, sectionHeaderPaint)
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#C89B6D")
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, leftY + 14f, MARGIN + leftColWidth, leftY + 14f, dividerPaint)
        leftY += 24f

        // Render Ingredients with Clean Translated English Units (EL -> tbsp, TL -> tsp, VZ -> pkg)
        val grouped = recipe.ingredients.groupBy { it.group ?: "Main" }
        grouped.forEach { (groupName, items) ->
            if (groupName.isNotBlank() && grouped.size > 1 && groupName != "Main") {
                canvas.drawText("• $groupName", MARGIN + 4f, leftY + 8f, boldBodyPaint)
                leftY += 15f
            }

            items.forEach { ing ->
                // Checkbox
                val cbPaint = Paint().apply {
                    color = Color.parseColor("#8C7B6B")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                canvas.drawRect(MARGIN + 2f, leftY - 7f, MARGIN + 9f, leftY, cbPaint)

                val scaledAmount = try {
                    val rawVal = ing.amount.trim().toDoubleOrNull()
                    if (rawVal != null && multiplier != 1.0f) {
                        val scaled = rawVal * multiplier
                        if (scaled % 1.0 == 0.0) scaled.toInt().toString() else String.format(Locale.US, "%.1f", scaled)
                    } else ing.amount
                } catch (e: Exception) {
                    ing.amount
                }

                val displayUnit = ing.getDisplayUnit(LanguageMode.ENGLISH)
                val displayName = ing.getDisplayName(LanguageMode.ENGLISH)

                val ingLine = buildString {
                    if (scaledAmount.isNotBlank()) append("$scaledAmount ")
                    if (displayUnit.isNotBlank()) append("$displayUnit ")
                    append(displayName)
                    if (ing.isOptional) append(" (opt)")
                }

                val ingLayout = StaticLayout.Builder.obtain(
                    ingLine, 0, ingLine.length, bodyPaint, (leftColWidth - 14).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(MARGIN + 13f, leftY - 8f)
                ingLayout.draw(canvas)
                canvas.restore()

                leftY += ingLayout.height + 5f

                // Handle Left Column overflow to Page 2
                if (leftY > PAGE_HEIGHT - MARGIN - 60f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawPageDecorations(canvas, pageNumber)
                    leftY = MARGIN + 20f
                    canvas.drawText("${recipe.title} (Ingredients Cont.)", MARGIN, leftY, sectionHeaderPaint)
                    leftY += 22f
                }
            }
        }

        // Right Header: INSTRUCTIONS & METHOD
        canvas.drawText("METHOD & INSTRUCTIONS", rightColLeft, rightY + 10f, sectionHeaderPaint)
        canvas.drawLine(rightColLeft, rightY + 14f, PAGE_WIDTH - MARGIN, rightY + 14f, dividerPaint)
        rightY += 24f

        // Render Steps
        recipe.steps.forEachIndexed { idx, step ->
            val stepNumStr = "${idx + 1}."
            canvas.drawText(stepNumStr, rightColLeft, rightY + 2f, boldBodyPaint)

            val stepText = step.getInstruction(LanguageMode.ENGLISH, unitSystem)
            val stepLayout = StaticLayout.Builder.obtain(
                stepText, 0, stepText.length, bodyPaint, (rightColWidth - 18).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(rightColLeft + 15f, rightY - 8f)
            stepLayout.draw(canvas)
            canvas.restore()

            var stepAddedHeight = stepLayout.height + 5f

            // Step Timer badge
            if (step.timerMinutes > 0) {
                val timerStr = "⏱ ${step.timerMinutes} min timer"
                canvas.drawText(timerStr, rightColLeft + 15f, rightY - 8f + stepAddedHeight + 2f, smallMetaPaint)
                stepAddedHeight += 13f
            }

            // Step tip
            if (includeTips && !step.tip.isNullOrBlank()) {
                val tipStr = "💡 Secret: ${step.getLocalizedTip(LanguageMode.ENGLISH) ?: step.tip}"
                val tipLayout = StaticLayout.Builder.obtain(
                    tipStr, 0, tipStr.length, tipPaint, (rightColWidth - 18).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(rightColLeft + 15f, rightY - 8f + stepAddedHeight)
                tipLayout.draw(canvas)
                canvas.restore()

                stepAddedHeight += tipLayout.height + 4f
            }

            rightY += stepAddedHeight + 6f

            // Handle Right Column overflow to Page 2
            if (rightY > PAGE_HEIGHT - MARGIN - 60f && idx < recipe.steps.size - 1) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawPageDecorations(canvas, pageNumber)
                rightY = MARGIN + 20f
                canvas.drawText("${recipe.title} (Instructions Cont.)", rightColLeft, rightY, sectionHeaderPaint)
                rightY += 22f
            }
        }

        // Family Notes Section (at bottom of tallest column)
        val finalContentY = maxOf(leftY, rightY)
        if (includeNotes && recipe.notes.isNotBlank() && finalContentY < PAGE_HEIGHT - MARGIN - 65f) {
            val notesBoxY = finalContentY + 10f
            canvas.drawLine(MARGIN, notesBoxY, PAGE_WIDTH - MARGIN, notesBoxY, dividerPaint)
            canvas.drawText("FAMILY CHEF NOTES", MARGIN, notesBoxY + 13f, sectionHeaderPaint)

            val notesLayout = StaticLayout.Builder.obtain(
                recipe.notes, 0, recipe.notes.length, tipPaint, (PAGE_WIDTH - 2 * MARGIN).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(MARGIN, notesBoxY + 18f)
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

    /**
     * Generates a single complete multi-page PDF book containing EVERY recipe in the collection,
     * starting with an ornate Cover Page and Table of Contents!
     */
    fun generateFullCookbookPdf(
        context: Context,
        recipes: List<RecipeEntity>,
        cookbookTitle: String = "Compendium Collection",
        profileName: String = "Louis",
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS
    ): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        val bgPaint = Paint().apply { color = Color.parseColor("#FCF9F2") }
        val coverBgPaint = Paint().apply { color = Color.parseColor("#FAF6EC") }
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#C89B6D")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#E5D2BA")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val coverTitlePaint = TextPaint().apply {
            color = Color.parseColor("#661D00")
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val coverSubtitlePaint = TextPaint().apply {
            color = Color.parseColor("#8C5835")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val sectionHeaderPaint = TextPaint().apply {
            color = Color.parseColor("#782810")
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

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

        val tocItemPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val tocCategoryPaint = TextPaint().apply {
            color = Color.parseColor("#8C2D19")
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldBodyPaint = TextPaint().apply {
            color = Color.parseColor("#2C221E")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val tipPaint = TextPaint().apply {
            color = Color.parseColor("#6B4423")
            textSize = 9f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }

        val smallMetaPaint = TextPaint().apply {
            color = Color.parseColor("#5A4535")
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        fun drawRunningFooter(c: Canvas, pNum: Int) {
            c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)
            c.drawRect(MARGIN - 10, MARGIN - 10, PAGE_WIDTH - MARGIN + 10, PAGE_HEIGHT - MARGIN + 10, goldBorderPaint)
            c.drawRect(MARGIN - 6, MARGIN - 6, PAGE_WIDTH - MARGIN + 6, PAGE_HEIGHT - MARGIN + 6, innerBorderPaint)

            val footerText = "$cookbookTitle • Page $pNum"
            val footerPaint = TextPaint().apply {
                color = Color.parseColor("#9C8673")
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            c.drawText(footerText, MARGIN, PAGE_HEIGHT - MARGIN + 2, footerPaint)

            val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val dateWidth = footerPaint.measureText(dateStr)
            c.drawText(dateStr, PAGE_WIDTH - MARGIN - dateWidth, PAGE_HEIGHT - MARGIN + 2, footerPaint)
        }

        // ==========================================
        // PAGE 1: LUXURY ORNATE COVER PAGE
        // ==========================================
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), coverBgPaint)
        canvas.drawRect(MARGIN - 14, MARGIN - 14, PAGE_WIDTH - MARGIN + 14, PAGE_HEIGHT - MARGIN + 14, goldBorderPaint)
        canvas.drawRect(MARGIN - 8, MARGIN - 8, PAGE_WIDTH - MARGIN + 8, PAGE_HEIGHT - MARGIN + 8, innerBorderPaint)

        var coverY = PAGE_HEIGHT * 0.28f
        val emblemStr = "✦  COMPENDIUM ARCHIVE  ✦"
        val emblemWidth = smallMetaPaint.measureText(emblemStr)
        canvas.drawText(emblemStr, (PAGE_WIDTH - emblemWidth) / 2f, coverY, smallMetaPaint)
        coverY += 34f

        val titleWidth = coverTitlePaint.measureText(cookbookTitle)
        canvas.drawText(cookbookTitle, (PAGE_WIDTH - titleWidth) / 2f, coverY, coverTitlePaint)
        coverY += 24f

        val subtitle = "A Master Compendium of Formulas, Recipes & Maker's Notes"
        val subtitleWidth = coverSubtitlePaint.measureText(subtitle)
        canvas.drawText(subtitle, (PAGE_WIDTH - subtitleWidth) / 2f, coverY, coverSubtitlePaint)
        coverY += 50f

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#C89B6D")
            strokeWidth = 1.5f
        }
        canvas.drawLine(PAGE_WIDTH * 0.25f, coverY, PAGE_WIDTH * 0.75f, coverY, dividerPaint)
        coverY += 40f

        val countStr = "${recipes.size} Curated Recipes"
        val countWidth = sectionHeaderPaint.measureText(countStr)
        canvas.drawText(countStr, (PAGE_WIDTH - countWidth) / 2f, coverY, sectionHeaderPaint)
        coverY += 24f

        val compiledBy = "Family Collection • Compiled for $profileName"
        val compiledWidth = tipPaint.measureText(compiledBy)
        canvas.drawText(compiledBy, (PAGE_WIDTH - compiledWidth) / 2f, coverY, tipPaint)
        coverY += 18f

        val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val dateWidth = smallMetaPaint.measureText(dateStr)
        canvas.drawText(dateStr, (PAGE_WIDTH - dateWidth) / 2f, coverY, smallMetaPaint)

        pdfDocument.finishPage(page)
        pageNumber++

        // ==========================================
        // PAGE 2: TABLE OF CONTENTS
        // ==========================================
        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        drawRunningFooter(canvas, pageNumber)

        var tocY = MARGIN + 24f
        canvas.drawText("TABLE OF CONTENTS", MARGIN, tocY, coverTitlePaint)
        tocY += 16f
        canvas.drawLine(MARGIN, tocY, PAGE_WIDTH - MARGIN, tocY, dividerPaint)
        tocY += 24f

        val groupedCategories = recipes.groupBy { it.category.ifBlank { "Uncategorized" } }
        var estimatedRecipePage = 3 // Recipes start on page 3

        groupedCategories.forEach { (catName, catRecipes) ->
            if (tocY > PAGE_HEIGHT - MARGIN - 50f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawRunningFooter(canvas, pageNumber)
                tocY = MARGIN + 24f
                canvas.drawText("TABLE OF CONTENTS (Cont.)", MARGIN, tocY, sectionHeaderPaint)
                tocY += 20f
            }

            canvas.drawText("📂 $catName (${catRecipes.size})", MARGIN, tocY, tocCategoryPaint)
            tocY += 16f

            catRecipes.forEach { r ->
                val title = r.getDisplayTitle()
                val pStr = "Page $estimatedRecipePage"
                val pWidth = tocItemPaint.measureText(pStr)

                canvas.drawText("• $title", MARGIN + 12f, tocY, tocItemPaint)
                canvas.drawText(pStr, PAGE_WIDTH - MARGIN - pWidth, tocY, smallMetaPaint)

                // Dotted leader line
                val titleW = tocItemPaint.measureText("• $title")
                val startDots = MARGIN + 16f + titleW
                val endDots = PAGE_WIDTH - MARGIN - pWidth - 8f
                if (endDots > startDots + 20f) {
                    val dotPaint = Paint().apply {
                        color = Color.parseColor("#BFA58C")
                        strokeWidth = 1f
                    }
                    var dotX = startDots
                    while (dotX < endDots) {
                        canvas.drawCircle(dotX, tocY - 3f, 0.75f, dotPaint)
                        dotX += 6f
                    }
                }

                tocY += 15f
                estimatedRecipePage++

                if (tocY > PAGE_HEIGHT - MARGIN - 40f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawRunningFooter(canvas, pageNumber)
                    tocY = MARGIN + 24f
                }
            }
            tocY += 10f
        }

        pdfDocument.finishPage(page)
        pageNumber++

        // ==========================================
        // PAGES 3..N: EVERY SINGLE RECIPE CARD
        // ==========================================
        recipes.forEach { recipe ->
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            drawRunningFooter(canvas, pageNumber)

            var curY = MARGIN + 15f

            // Banner & Title
            canvas.drawText("COMPENDIUM CARD", MARGIN, curY, smallMetaPaint)
            curY += 16f

            val mainTitle = recipe.getDisplayTitle()
            canvas.drawText(mainTitle, MARGIN, curY + 6f, titlePaint)
            curY += 24f

            val secondaryTitle = if (recipe.titleGerman != null && recipe.titleGerman != mainTitle && recipe.titleGerman.isNotBlank()) {
                "Original: ${recipe.titleGerman}"
            } else if (recipe.titleEnglish != null && recipe.titleEnglish != mainTitle && recipe.titleEnglish.isNotBlank()) {
                "English: ${recipe.titleEnglish}"
            } else null

            if (secondaryTitle != null) {
                canvas.drawText(secondaryTitle, MARGIN, curY, subtitlePaint)
                curY += 15f
            }

            // Info Bar
            val badgeBg = Paint().apply { color = Color.parseColor("#EFE3D3") }
            val infoPillRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 22f)
            canvas.drawRoundRect(infoPillRect, 6f, 6f, badgeBg)
            val metaInfo = "Category: ${recipe.category}   •   Servings: ${recipe.servings}   •   Prep: ${recipe.prepTimeMinutes}m   •   Cook: ${recipe.cookTimeMinutes}m   •   ${recipe.difficulty}"
            canvas.drawText(metaInfo, MARGIN + 10f, curY + 15f, smallMetaPaint)
            curY += 30f

            // Lore Box (if present)
            if (recipe.originStory.isNotBlank() && !recipe.originStory.equals("Scanned recipe.", ignoreCase = true)) {
                val loreBg = Paint().apply { color = Color.parseColor("#F4ECE1") }
                val loreBorder = Paint().apply {
                    color = Color.parseColor("#DECDB8")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }
                val loreText = "\"${recipe.originStory}\""
                val layout = StaticLayout.Builder.obtain(
                    loreText, 0, loreText.length, tipPaint, (PAGE_WIDTH - 2 * MARGIN - 24).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                val boxHeight = layout.height + 22f
                val loreRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + boxHeight)
                canvas.drawRoundRect(loreRect, 6f, 6f, loreBg)
                canvas.drawRoundRect(loreRect, 6f, 6f, loreBorder)

                canvas.drawText("Heritage & Lore:", MARGIN + 10f, curY + 12f, smallMetaPaint)
                canvas.save()
                canvas.translate(MARGIN + 10f, curY + 16f)
                layout.draw(canvas)
                canvas.restore()

                curY += boxHeight + 12f
            }

            val colGap = 16f
            val leftColWidth = 195f
            val rightColLeft = MARGIN + leftColWidth + colGap
            val rightColWidth = PAGE_WIDTH - MARGIN - rightColLeft

            var leftY = curY
            var rightY = curY

            // Draw AI Dish Photo in Left Column
            val photoHeight = 125f
            val coverBitmap = loadScaledRecipeCoverBitmap(recipe.imageUri, (leftColWidth * 2).toInt(), (photoHeight * 2).toInt())
            if (coverBitmap != null) {
                val photoRect = RectF(MARGIN, leftY, MARGIN + leftColWidth, leftY + photoHeight)
                val photoPath = Path().apply {
                    addRoundRect(photoRect, 6f, 6f, Path.Direction.CW)
                }
                canvas.save()
                canvas.clipPath(photoPath)
                canvas.drawBitmap(coverBitmap, null, photoRect, null)
                canvas.restore()
                canvas.drawRoundRect(photoRect, 6f, 6f, goldBorderPaint)
                leftY += photoHeight + 12f
            }

            // Left: Ingredients
            canvas.drawText("INGREDIENTS", MARGIN, leftY + 10f, sectionHeaderPaint)
            canvas.drawLine(MARGIN, leftY + 14f, MARGIN + leftColWidth, leftY + 14f, dividerPaint)
            leftY += 24f

            val grouped = recipe.ingredients.groupBy { it.group ?: "Main" }
            grouped.forEach { (groupName, items) ->
                if (groupName.isNotBlank() && grouped.size > 1 && groupName != "Main") {
                    canvas.drawText("• $groupName", MARGIN + 4f, leftY + 8f, boldBodyPaint)
                    leftY += 15f
                }

                items.forEach { ing ->
                    val cbPaint = Paint().apply {
                        color = Color.parseColor("#8C7B6B")
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRect(MARGIN + 2f, leftY - 7f, MARGIN + 9f, leftY, cbPaint)

                    val displayUnit = ing.getDisplayUnit(LanguageMode.ENGLISH)
                    val displayName = ing.getDisplayName(LanguageMode.ENGLISH)

                    val ingLine = buildString {
                        if (ing.amount.isNotBlank()) append("${ing.amount} ")
                        if (displayUnit.isNotBlank()) append("$displayUnit ")
                        append(displayName)
                        if (ing.isOptional) append(" (opt)")
                    }

                    val ingLayout = StaticLayout.Builder.obtain(
                        ingLine, 0, ingLine.length, bodyPaint, (leftColWidth - 14).toInt()
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                    canvas.save()
                    canvas.translate(MARGIN + 13f, leftY - 8f)
                    ingLayout.draw(canvas)
                    canvas.restore()

                    leftY += ingLayout.height + 5f
                }
            }

            // Right: Instructions
            canvas.drawText("METHOD & INSTRUCTIONS", rightColLeft, rightY + 10f, sectionHeaderPaint)
            canvas.drawLine(rightColLeft, rightY + 14f, PAGE_WIDTH - MARGIN, rightY + 14f, dividerPaint)
            rightY += 24f

            recipe.steps.forEachIndexed { idx, step ->
                val stepNumStr = "${idx + 1}."
                canvas.drawText(stepNumStr, rightColLeft, rightY + 2f, boldBodyPaint)

                val stepText = step.getInstruction(LanguageMode.ENGLISH, unitSystem)
                val stepLayout = StaticLayout.Builder.obtain(
                    stepText, 0, stepText.length, bodyPaint, (rightColWidth - 18).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(rightColLeft + 15f, rightY - 8f)
                stepLayout.draw(canvas)
                canvas.restore()

                var stepAddedHeight = stepLayout.height + 5f

                if (step.timerMinutes > 0) {
                    val timerStr = "⏱ ${step.timerMinutes} min timer"
                    canvas.drawText(timerStr, rightColLeft + 15f, rightY - 8f + stepAddedHeight + 2f, smallMetaPaint)
                    stepAddedHeight += 13f
                }

                if (!step.tip.isNullOrBlank()) {
                    val tipStr = "💡 Secret: ${step.getLocalizedTip(LanguageMode.ENGLISH) ?: step.tip}"
                    val tipLayout = StaticLayout.Builder.obtain(
                        tipStr, 0, tipStr.length, tipPaint, (rightColWidth - 18).toInt()
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                    canvas.save()
                    canvas.translate(rightColLeft + 15f, rightY - 8f + stepAddedHeight)
                    tipLayout.draw(canvas)
                    canvas.restore()

                    stepAddedHeight += tipLayout.height + 4f
                }

                rightY += stepAddedHeight + 6f
            }

            // Notes
            val finalContentY = maxOf(leftY, rightY)
            if (recipe.notes.isNotBlank() && finalContentY < PAGE_HEIGHT - MARGIN - 65f) {
                val notesBoxY = finalContentY + 10f
                canvas.drawLine(MARGIN, notesBoxY, PAGE_WIDTH - MARGIN, notesBoxY, dividerPaint)
                canvas.drawText("FAMILY CHEF NOTES", MARGIN, notesBoxY + 13f, sectionHeaderPaint)

                val notesLayout = StaticLayout.Builder.obtain(
                    recipe.notes, 0, recipe.notes.length, tipPaint, (PAGE_WIDTH - 2 * MARGIN).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(MARGIN, notesBoxY + 18f)
                notesLayout.draw(canvas)
                canvas.restore()
            }

            pdfDocument.finishPage(page)
            pageNumber++
        }

        // Save Master PDF File
        val pdfDir = File(context.cacheDir, "recipe_pdfs").apply { mkdirs() }
        val safeFileName = cookbookTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val outputFile = File(pdfDir, "${safeFileName}_Master_Collection.pdf")

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

    fun createShareableFullCookbookPdfUri(
        context: Context,
        recipes: List<RecipeEntity>,
        cookbookTitle: String = "Compendium Collection",
        profileName: String = "Louis",
        unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS
    ): Uri? {
        return try {
            val file = generateFullCookbookPdf(context, recipes, cookbookTitle, profileName, unitSystem)
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
            putExtra(Intent.EXTRA_SUBJECT, "Recipe / Formula: $recipeTitle")
            putExtra(Intent.EXTRA_TEXT, "Here is the card for '$recipeTitle' from my Compendium!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Recipe / Formula PDF via..."))
    }

    fun shareFullCookbookPdf(context: Context, pdfUri: Uri, cookbookTitle: String, totalRecipes: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "$cookbookTitle (Complete $totalRecipes Items PDF)")
            putExtra(Intent.EXTRA_TEXT, "Here is the complete Master PDF for '$cookbookTitle' ($totalRecipes formulas & recipes with photos and instructions)!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Complete Compendium PDF via..."))
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

