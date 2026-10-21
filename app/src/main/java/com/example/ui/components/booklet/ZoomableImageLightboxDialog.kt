package com.example.ui.components.booklet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

/**
 * High-end cinematic Zoomable Image Lightbox Dialog.
 *
 * Replaces irregular, boxy dialog frames with a seamless, edge-to-edge
 * luxury viewer supporting:
 * - Fluid pinch-to-zoom (up to 5x magnification)
 * - 1:1 pan & drag gestures with boundary clamping
 * - Double-tap to smoothly expand / reset
 * - Single-tap to toggle distraction-free control HUD
 * - Floating glassmorphic top header with title and 90° rotation
 * - Floating glassmorphic bottom HUD with zoom controls & fit/reset action
 */
@Composable
fun ZoomableImageLightboxDialog(
    imageModel: Any?,
    title: String = "Photo Inspection",
    subtitle: String? = "Pinch to zoom • Drag to explore • Double-tap to expand",
    onDismiss: () -> Unit,
    onRotate: (() -> Unit)? = null
) {
    if (imageModel == null) return

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var localRotation by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFA15110E),
                            Color(0xFD0A0807),
                            Color(0xFF040303)
                        )
                    )
                )
        ) {
            // Interactive Zoomable / Pannable Image Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showControls = !showControls
                            },
                            onDoubleTap = {
                                if (scale > 1.1f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.5f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (scale * zoom).coerceIn(1f, 5f)
                            scale = nextScale
                            if (nextScale > 1f) {
                                val maxBound = 1600f * (nextScale - 1f)
                                offsetX = (offsetX + pan.x).coerceIn(-maxBound, maxBound)
                                offsetY = (offsetY + pan.y).coerceIn(-maxBound, maxBound)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 64.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                            rotationZ = localRotation
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // Top Floating Glassmorphic Header
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xD9080706),
                                    Color(0x80080706),
                                    Color.Transparent
                                )
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close Circular Glass Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0x66000000),
                            border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFFF3E7DA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Title & Subtitle Info Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF3E7DA),
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFB0A495),
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Rotate 90° Glass Action Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0x66000000),
                            border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (onRotate != null) {
                                        onRotate()
                                    } else {
                                        localRotation = (localRotation + 90f) % 360f
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Rotate 90°",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Floating Glassmorphic Control HUD
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x80080706),
                                    Color(0xD9080706)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xE61C1713),
                        border = BorderStroke(1.dp, Color(0x4DE5D4B8)),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Zoom Out (-) Button
                            IconButton(
                                onClick = {
                                    scale = (scale - 0.5f).coerceAtLeast(1f)
                                    if (scale == 1f) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                },
                                enabled = scale > 1f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Zoom Out",
                                    tint = if (scale > 1f) Color(0xFFF3E7DA) else Color(0x44FFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Dynamic Zoom Percentage Pill (tap to toggle 2x / reset)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (scale > 1.05f) Color(0x33FFD54F) else Color(0x22FFFFFF),
                                border = BorderStroke(
                                    0.75.dp,
                                    if (scale > 1.05f) Color(0x99FFD54F) else Color(0x33FFFFFF)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (scale > 1.05f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2.0f
                                        }
                                    }
                            ) {
                                Text(
                                    text = "${(scale * 100).toInt()}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (scale > 1.05f) Color(0xFFFFD54F) else Color(0xFFEADBCE),
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // Zoom In (+) Button
                            IconButton(
                                onClick = {
                                    scale = (scale + 0.5f).coerceAtMost(5f)
                                },
                                enabled = scale < 5f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Zoom In",
                                    tint = if (scale < 5f) Color(0xFFF3E7DA) else Color(0x44FFFFFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Fit / Reset Action
                            if (scale > 1.05f || offsetX != 0f || offsetY != 0f || localRotation != 0f) {
                                Box(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(1.dp)
                                        .background(Color(0x33FFFFFF))
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x33C47135),
                                    border = BorderStroke(0.75.dp, Color(0x66C47135)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                            localRotation = 0f
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.FitScreen,
                                            contentDescription = "Fit to screen",
                                            tint = Color(0xFFF3E7DA),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Fit",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFF3E7DA),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
