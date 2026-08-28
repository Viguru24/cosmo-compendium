package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.CoverTheme

/**
 * Realistic Heirloom Leather Cover with:
 * - Full-grain antique leather texture with ambient studio lighting
 * - Intense burnt and scorched corners (charred carbon gradient falloff)
 * - Aged kitchen oil, coffee, and grease patina stains
 * - Weathered crease lines and distressed micro-fissures
 * - Deep spine hinge groove and authentic waxed saddle stitching
 * - Antique weathered brass / iron corner brackets with rivets and bevel highlights
 */
@Composable
fun RealisticLeatherBackground(
    theme: CoverTheme,
    modifier: Modifier = Modifier,
    isCard: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            // 1. BASE RICH LEATHER FULL-GRAIN WITH AMBIENT HIGHLIGHT
            val baseColorPrimary = Color(theme.primaryHex)
            val baseColorDark = Color(theme.secondaryHex)

            // Warm rich leather gradient with top-center highlight
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColorPrimary.copy(alpha = 1.0f),
                        baseColorDark.copy(alpha = 1.0f),
                        Color(0xFF1E0A04)
                    ),
                    center = Offset(w * 0.45f, h * 0.38f),
                    radius = (w.coerceAtLeast(h) * 0.9f)
                ),
                size = size
            )

            // 2. AGED LEATHER GRAIN MOTTLING & MICRO-TEXTURE
            drawLeatherGrainPatina(w, h)

            // 3. AGED KITCHEN OIL & COFFEE PATINA STAINS
            drawKitchenStains(w, h, isCard)

            // 4. DEEP BURNT & SCORCHED CORNERS (Intense charred carbon burns)
            drawScorchedCorners(w, h, isCard)

            // 5. PERIMETER LEATHER EDGE VIGNETTE
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x33000000),
                        Color(0x88050100),
                        Color(0xCC000000)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = (w.coerceAtLeast(h) * 0.72f)
                ),
                size = size
            )

            // 6. BOOK SPINE HIGHLIGHT & HINGE GROOVE
            drawSpineAndStitching(w, h, isCard)

            // 7. ORNATE WEATHERED BRASS CORNER BRACKETS & BLIND STAMP
            drawAntiqueBrassCornersAndBorder(w, h, isCard)
        }

        content?.invoke()
    }
}

private fun DrawScope.drawLeatherGrainPatina(w: Float, h: Float) {
    // Subtle organic blotches simulating non-uniform aged hides
    val mottling = listOf(
        Triple(Offset(w * 0.25f, h * 0.2f), w * 0.35f, Color(0x18000000)),
        Triple(Offset(w * 0.75f, h * 0.35f), w * 0.4f, Color(0x22000000)),
        Triple(Offset(w * 0.3f, h * 0.7f), w * 0.45f, Color(0x1E000000)),
        Triple(Offset(w * 0.8f, h * 0.8f), w * 0.3f, Color(0x28000000)),
        Triple(Offset(w * 0.5f, h * 0.5f), w * 0.55f, Color(0x12FFFFFF))
    )

    mottling.forEach { (center, radius, color) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }

    // Natural leather surface creases
    val creasePath1 = Path().apply {
        moveTo(w * 0.05f, h * 0.38f)
        cubicTo(w * 0.35f, h * 0.42f, w * 0.65f, h * 0.35f, w * 0.95f, h * 0.4f)
    }
    drawPath(creasePath1, color = Color(0x26000000), style = Stroke(width = 1.2f))
    drawPath(creasePath1, color = Color(0x12FEF3C7), style = Stroke(width = 0.6f))

    val creasePath2 = Path().apply {
        moveTo(w * 0.08f, h * 0.68f)
        cubicTo(w * 0.4f, h * 0.64f, w * 0.7f, h * 0.72f, w * 0.92f, h * 0.66f)
    }
    drawPath(creasePath2, color = Color(0x26000000), style = Stroke(width = 1.2f))
}

private fun DrawScope.drawKitchenStains(w: Float, h: Float, isCard: Boolean) {
    // 1. Heritage Cup/Jar Base Ring Stain on the cover
    val ringCenter = if (isCard) Offset(w * 0.72f, h * 0.68f) else Offset(w * 0.68f, h * 0.72f)
    val ringRadius = if (isCard) w * 0.22f else w * 0.18f

    // Outer dark oil ring
    drawCircle(
        color = Color(0x28120601),
        radius = ringRadius,
        center = ringCenter,
        style = Stroke(width = if (isCard) 4f else 7f)
    )
    // Inner faint ring edge
    drawCircle(
        color = Color(0x1C1E0A03),
        radius = ringRadius * 0.92f,
        center = ringCenter,
        style = Stroke(width = if (isCard) 1.5f else 2.5f)
    )
    // Faint interior coffee wash
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x18180A03), Color(0x00000000)),
            center = ringCenter,
            radius = ringRadius
        ),
        radius = ringRadius,
        center = ringCenter
    )

    // 2. Oil / Sauce Droplet Splatter Mark near top right
    val dropCenter = Offset(w * 0.82f, h * 0.18f)
    val dropRadius = if (isCard) w * 0.12f else w * 0.10f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x40100401), Color(0x201A0802), Color.Transparent),
            center = dropCenter,
            radius = dropRadius
        ),
        radius = dropRadius,
        center = dropCenter
    )
}

private fun DrawScope.drawScorchedCorners(w: Float, h: Float, isCard: Boolean) {
    val burnRadius = (w * (if (isCard) 0.55f else 0.42f)).coerceAtLeast(60f)

    // Intense multi-layer carbon burn colors:
    // Solid charred black -> singed dark roast -> scorched umber -> glowing singed ember -> transparent
    val burnColors = listOf(
        Color(0xF5050201),
        Color(0xDE0E0401),
        Color(0x99240A03),
        Color(0x446B2206),
        Color(0x00000000)
    )

    // Top-Left Scorched Corner
    drawCircle(
        brush = Brush.radialGradient(
            colors = burnColors,
            center = Offset(0f, 0f),
            radius = burnRadius
        ),
        radius = burnRadius,
        center = Offset(0f, 0f)
    )

    // Top-Right Scorched Corner
    drawCircle(
        brush = Brush.radialGradient(
            colors = burnColors,
            center = Offset(w, 0f),
            radius = burnRadius
        ),
        radius = burnRadius,
        center = Offset(w, 0f)
    )

    // Bottom-Left Scorched Corner
    drawCircle(
        brush = Brush.radialGradient(
            colors = burnColors,
            center = Offset(0f, h),
            radius = burnRadius
        ),
        radius = burnRadius,
        center = Offset(0f, h)
    )

    // Bottom-Right Scorched Corner
    drawCircle(
        brush = Brush.radialGradient(
            colors = burnColors,
            center = Offset(w, h),
            radius = burnRadius
        ),
        radius = burnRadius,
        center = Offset(w, h)
    )

    // Singed edges along the borders
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x99080200), Color.Transparent, Color(0x99080200))
        ),
        size = size
    )
}

private fun DrawScope.drawSpineAndStitching(w: Float, h: Float, isCard: Boolean) {
    val spineWidth = if (isCard) w * 0.16f else w * 0.12f

    // Spine 3D shadow falloff
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xAA000000),
                Color(0x55000000),
                Color(0x18000000),
                Color.Transparent
            ),
            startX = 0f,
            endX = spineWidth * 1.5f
        ),
        size = Size(spineWidth * 1.5f, h)
    )

    // Deep embossed flex groove
    val grooveX = spineWidth * 0.85f
    drawLine(
        color = Color(0x99000000),
        start = Offset(grooveX, 0f),
        end = Offset(grooveX, h),
        strokeWidth = if (isCard) 2f else 3.5f
    )
    drawLine(
        color = Color(0x22FBBF24),
        start = Offset(grooveX + 1.5f, 0f),
        end = Offset(grooveX + 1.5f, h),
        strokeWidth = 1f
    )

    // Authentic Waxed Saddle Stitching (Dotted linen thread)
    val stitchX = spineWidth * 0.5f
    val stitchWidth = if (isCard) 1.2f else 1.8f
    val dashEffect = PathEffect.dashPathEffect(
        floatArrayOf(if (isCard) 6f else 10f, if (isCard) 6f else 8f), 0f
    )

    // Stitch thread shadow in leather recess
    drawLine(
        color = Color(0xBB000000),
        start = Offset(stitchX + 0.8f, 10f),
        end = Offset(stitchX + 0.8f, h - 10f),
        strokeWidth = stitchWidth + 0.8f,
        pathEffect = dashEffect,
        cap = StrokeCap.Round
    )
    // Golden waxed thread highlight
    drawLine(
        color = Color(0xDDE2C499),
        start = Offset(stitchX, 10f),
        end = Offset(stitchX, h - 10f),
        strokeWidth = stitchWidth,
        pathEffect = dashEffect,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawAntiqueBrassCornersAndBorder(w: Float, h: Float, isCard: Boolean) {
    val pad = if (isCard) 8f else 18f
    val cornerSize = if (isCard) 26f else 54f

    // Blind debossed inner frame
    val frameRect = Rect(pad, pad, w - pad, h - pad)
    // Dark shadow of the deboss
    drawRect(
        color = Color(0x40000000),
        topLeft = Offset(frameRect.left - 0.8f, frameRect.top - 0.8f),
        size = Size(frameRect.width, frameRect.height),
        style = Stroke(width = if (isCard) 1f else 1.5f)
    )
    // Golden foil highlight of the deboss
    drawRect(
        color = Color(0x35E5D4B8),
        topLeft = Offset(frameRect.left, frameRect.top),
        size = Size(frameRect.width, frameRect.height),
        style = Stroke(width = if (isCard) 1f else 1.5f)
    )

    if (!isCard) {
        // Inner thin double frame for full-page book cover
        val innerPad = pad + 8f
        drawRect(
            color = Color(0x22E5D4B8),
            topLeft = Offset(innerPad, innerPad),
            size = Size(w - innerPad * 2, h - innerPad * 2),
            style = Stroke(width = 0.8f)
        )
    }

    // Weathered Brass Corner Protectors (Triangular hardware with patina and rivets)
    val brassDark = Color(0xFF6B4C1B)
    val brassGold = Color(0xFFC5A059)
    val brassHighlight = Color(0xFFF3E1A9)
    val rivetColor = Color(0xFF221105)

    // Top-Left Corner
    val pathTL = Path().apply {
        moveTo(2f, 2f)
        lineTo(cornerSize, 2f)
        cubicTo(cornerSize * 0.8f, cornerSize * 0.4f, cornerSize * 0.4f, cornerSize * 0.8f, 2f, cornerSize)
        close()
    }
    drawPath(pathTL, color = brassDark, style = Fill)
    drawPath(pathTL, color = brassGold, style = Stroke(width = if (isCard) 1.2f else 2f))
    drawCircle(
        color = rivetColor,
        radius = if (isCard) 2f else 3.5f,
        center = Offset(cornerSize * 0.38f, cornerSize * 0.38f)
    )
    drawCircle(
        color = brassHighlight,
        radius = if (isCard) 0.8f else 1.2f,
        center = Offset(cornerSize * 0.38f - 0.6f, cornerSize * 0.38f - 0.6f)
    )

    // Top-Right Corner
    val pathTR = Path().apply {
        moveTo(w - 2f, 2f)
        lineTo(w - cornerSize, 2f)
        cubicTo(w - cornerSize * 0.8f, cornerSize * 0.4f, w - cornerSize * 0.4f, cornerSize * 0.8f, w - 2f, cornerSize)
        close()
    }
    drawPath(pathTR, color = brassDark, style = Fill)
    drawPath(pathTR, color = brassGold, style = Stroke(width = if (isCard) 1.2f else 2f))
    drawCircle(
        color = rivetColor,
        radius = if (isCard) 2f else 3.5f,
        center = Offset(w - cornerSize * 0.38f, cornerSize * 0.38f)
    )
    drawCircle(
        color = brassHighlight,
        radius = if (isCard) 0.8f else 1.2f,
        center = Offset(w - cornerSize * 0.38f - 0.6f, cornerSize * 0.38f - 0.6f)
    )

    // Bottom-Left Corner
    val pathBL = Path().apply {
        moveTo(2f, h - 2f)
        lineTo(cornerSize, h - 2f)
        cubicTo(cornerSize * 0.8f, h - cornerSize * 0.4f, cornerSize * 0.4f, h - cornerSize * 0.8f, 2f, h - cornerSize)
        close()
    }
    drawPath(pathBL, color = brassDark, style = Fill)
    drawPath(pathBL, color = brassGold, style = Stroke(width = if (isCard) 1.2f else 2f))
    drawCircle(
        color = rivetColor,
        radius = if (isCard) 2f else 3.5f,
        center = Offset(cornerSize * 0.38f, h - cornerSize * 0.38f)
    )
    drawCircle(
        color = brassHighlight,
        radius = if (isCard) 0.8f else 1.2f,
        center = Offset(cornerSize * 0.38f - 0.6f, h - cornerSize * 0.38f - 0.6f)
    )

    // Bottom-Right Corner
    val pathBR = Path().apply {
        moveTo(w - 2f, h - 2f)
        lineTo(w - cornerSize, h - 2f)
        cubicTo(w - cornerSize * 0.8f, h - cornerSize * 0.4f, w - cornerSize * 0.4f, h - cornerSize * 0.8f, w - 2f, h - cornerSize)
        close()
    }
    drawPath(pathBR, color = brassDark, style = Fill)
    drawPath(pathBR, color = brassGold, style = Stroke(width = if (isCard) 1.2f else 2f))
    drawCircle(
        color = rivetColor,
        radius = if (isCard) 2f else 3.5f,
        center = Offset(w - cornerSize * 0.38f, h - cornerSize * 0.38f)
    )
    drawCircle(
        color = brassHighlight,
        radius = if (isCard) 0.8f else 1.2f,
        center = Offset(w - cornerSize * 0.38f - 0.6f, h - cornerSize * 0.38f - 0.6f)
    )
}
