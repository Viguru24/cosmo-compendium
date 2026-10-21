package com.example.ui.components.booklet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerracottaPrimary

/**
 * Interactive 3D page curl dog-ear corner effect in the upper right.
 */
@Composable
fun PageCurlCorner(
    alignment: Alignment = Alignment.TopEnd,
    onCornerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = 6.dp, y = (-6).dp)
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCornerClick() },
        contentAlignment = alignment
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width, 0f)
                lineTo(0f, 0f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFD6C5AD),
                style = Fill
            )
            val shadowPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height)
                lineTo(size.width - 4f, size.height)
                lineTo(0f, 4f)
                close()
            }
            drawPath(
                path = shadowPath,
                color = Color(0x33000000),
                style = Fill
            )
        }
    }
}

/**
 * Floating kitchen countdown timer pill widget with pause/resume and quick reset.
 */
@Composable
fun FloatingKitchenTimerWidget(
    secondsRemaining: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onAddMinute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Surface(
        modifier = modifier
            .padding(16.dp)
            .shadow(12.dp, CircleShape),
        shape = CircleShape,
        color = if (secondsRemaining == 0) TerracottaPrimary else Color(0xFF292524),
        border = BorderStroke(2.dp, Color(0xFFD6C7B2))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            IconButton(
                onClick = { if (isRunning) onPause() else onResume() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onReset,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reset timer",
                    tint = Color(0xFFA8A29E),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
