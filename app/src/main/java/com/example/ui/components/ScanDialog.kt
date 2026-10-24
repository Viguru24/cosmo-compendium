package com.example.ui.components

import com.example.ui.components.scan.RecipeReviewAndSaveView
import com.example.ui.components.scan.DuplicateRecipeComparisonView
import com.example.ui.components.scan.DuplicateRecipeDialog
import com.example.ui.components.scan.ScanProgressView
import com.example.ui.components.scan.samplePresetRecipes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.util.ImageUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import java.io.File
import com.example.data.local.RecipeEntity
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.example.ui.components.booklet.ZoomableImageLightboxDialog
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TerracottaPrimary

data class ScannedPageThumbnail(
    val bitmap: Bitmap,
    val filePath: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRecipeBottomSheet(
    isScanning: Boolean,
    draftRecipe: RecipeEntity?,
    errorMessage: String? = null,
    duplicatePrompt: com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData? = null,
    lastAutoSavedRecipe: RecipeEntity? = null,
    scanBatchSuccessTimestamp: Long? = null,
    scanBatchCount: Int = 0,
    onResolveDuplicateUpdate: ((com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData) -> Unit)? = null,
    onResolveDuplicateSaveCopy: ((com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData) -> Unit)? = null,
    onDismissDuplicate: (() -> Unit)? = null,
    onClearError: () -> Unit = {},
    onScan: (List<Bitmap>, String?, String?) -> Unit,
    onSaveDraft: (RecipeEntity) -> Unit,
    onImportVideo: ((Uri) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scannedPages = remember { mutableStateListOf<ScannedPageThumbnail>() }
    var rawTextEntry by remember { mutableStateOf("") }

    // Video Reel Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportVideo?.invoke(uri)
            onDismiss()
        }
    }

    // Multi-Image Gallery Picker
    val multiGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    val bitmap = ImageUtils.loadAndDownscaleBitmap(context, uri)
                    if (bitmap != null) {
                        val savedPath = ImageUtils.saveLowResReferenceImage(context, bitmap) ?: ""
                        scannedPages.add(ScannedPageThumbnail(bitmap, savedPath))
                    }
                } catch (e: Throwable) {
                    Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (scannedPages.isNotEmpty()) {
                Toast.makeText(context, "${scannedPages.size} page(s) loaded! Ready to scan or add more.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var cameraLauncherRef by remember { mutableStateOf<androidx.activity.result.ActivityResultLauncher<Uri>?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && pendingCameraUri != null) {
            try {
                val bitmap = ImageUtils.loadAndDownscaleBitmap(context, pendingCameraUri!!)
                if (bitmap != null) {
                    val savedImagePath = ImageUtils.saveLowResReferenceImage(context, bitmap) ?: ""
                    scannedPages.add(ScannedPageThumbnail(bitmap, savedImagePath))
                    val count = scannedPages.size
                    Toast.makeText(context, "✓ Page $count captured! Opening camera for Page ${count + 1} (press Back when done)...", Toast.LENGTH_SHORT).show()
                    
                    // Immediately reopen camera for next page in sequence!
                    coroutineScope.launch {
                        delay(350)
                        try {
                            val uri = ImageUtils.createTempCameraUri(context)
                            pendingCameraUri = uri
                            cameraLauncherRef?.launch(uri)
                        } catch (e: Throwable) {
                            Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Could not process photo from camera", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                Toast.makeText(context, "Error processing photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else if (!success && scannedPages.isNotEmpty()) {
            // User pressed Back/cancelled camera -> they are finished capturing pages for this recipe!
            val count = scannedPages.size
            Toast.makeText(context, "Synthesizing $count captured page(s) into recipe...", Toast.LENGTH_SHORT).show()
            val allBitmaps = scannedPages.map { it.bitmap }
            val primaryPath = scannedPages.firstOrNull()?.filePath ?: ""
            onScan(allBitmaps, null, primaryPath)
        }
    }
    cameraLauncherRef = cameraLauncher

    fun launchHighResCamera() {
        try {
            val uri = ImageUtils.createTempCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Throwable) {
            Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchHighResCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to photograph recipe cards directly.", Toast.LENGTH_LONG).show()
        }
    }

    fun requestCameraOrLaunch() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            launchHighResCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var hasAutoOpenedCamera by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasAutoOpenedCamera && scannedPages.isEmpty() && draftRecipe == null && !isScanning) {
            hasAutoOpenedCamera = true
            requestCameraOrLaunch()
        }
    }

    // Continuous Batch Scanning loop: when a recipe is saved, clear page state and reopen camera immediately!
    LaunchedEffect(scanBatchSuccessTimestamp) {
        if (scanBatchSuccessTimestamp != null) {
            scannedPages.clear()
            rawTextEntry = ""
            val title = lastAutoSavedRecipe?.title?.ifBlank { "Recipe" } ?: "Recipe"
            Toast.makeText(context, "✓ Saved '$title' to Cookbook! Opening camera for next recipe...", Toast.LENGTH_SHORT).show()
            delay(400)
            requestCameraOrLaunch()
        }
    }

    // Preset Vintage Recipe Samples for instant one-click testing & translation demo
        if (duplicatePrompt != null) {
            DuplicateRecipeComparisonView(
                prompt = duplicatePrompt,
                onUpdate = { onResolveDuplicateUpdate?.invoke(duplicatePrompt) },
                onSaveCopy = { onResolveDuplicateSaveCopy?.invoke(duplicatePrompt) },
                onDiscard = { onDismissDuplicate?.invoke() ?: onDismiss() }
            )
        } else if (draftRecipe != null) {
            // Edit & Review AI Sorted Fields
            RecipeReviewAndSaveView(
                initialDraft = draftRecipe,
                onSave = { updatedRecipe ->
                    onSaveDraft(updatedRecipe)
                },
                onCancel = onDismiss
            )
        } else if (isScanning) {
            ScanProgressView(scannedPages = scannedPages)
        } else {
            // Options: Camera, Photo Pick, Multi-Page Preview, Presets, Paste Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📸 Continuous Recipe Scanner",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Auto-saves cards and loops directly into the next scan",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF18120C))
                    }
                }

                // Friendly How It Works Banner
                var showHelpTips by remember { mutableStateOf(false) }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFBF4EB),
                    border = BorderStroke(1.dp, Color(0xFFE8DAC8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { showHelpTips = !showHelpTips }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💡", fontSize = 14.sp)
                                Text(
                                    text = "How Scanning & Video Import Works",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5D4037)
                                    )
                                )
                            }
                            Text(
                                text = if (showHelpTips) "Hide ▲" else "Learn more ▼",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        if (showHelpTips) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("• 📸 Rapid Snapping: Snap Page 1, then Page 2 (Back), Page 3... Press Back when done to auto-save the full recipe!", fontSize = 12.sp, color = Color(0xFF6B5E52))
                                Text("• 📹 Cooking Videos: Select any Reel, TikTok, or 15-30s screen recording to extract the ingredients and recipe automatically.", fontSize = 12.sp, color = Color(0xFF6B5E52))
                                Text("• 🌍 Any Language Worldwide: Transcribe cards in any language (German, French, Italian, Spanish, Polish, etc.) or cursive with instant English translation.", fontSize = 12.sp, color = Color(0xFF6B5E52))
                                Text("• 🖼️ Food Photo Crop: Recipe card photos are auto-detected and saved as the cover picture.", fontSize = 12.sp, color = Color(0xFF6B5E52))
                            }
                        }
                    }
                }

                if (scanBatchCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$scanBatchCount recipe(s) saved to Cookbook!",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    )
                                }
                                if (lastAutoSavedRecipe != null) {
                                    Text(
                                        text = "Latest: ${lastAutoSavedRecipe.title.ifBlank { "Recipe" }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF8C7B6B))

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF87171)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF991B1B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "Scan Interrupted",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                IconButton(
                                    onClick = onClearError,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF991B1B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF7F1D1D),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            if (scannedPages.isNotEmpty() || rawTextEntry.isNotBlank()) {
                                Button(
                                    onClick = {
                                        onClearError()
                                        val allBitmaps = scannedPages.map { it.bitmap }
                                        val primaryPath = scannedPages.firstOrNull()?.filePath ?: ""
                                        onScan(allBitmaps, rawTextEntry.ifBlank { null }, primaryPath)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (scannedPages.isNotEmpty()) "🔄 Retry Scan (${scannedPages.size} Captured Page${if (scannedPages.size > 1) "s" else ""})" else "🔄 Retry AI Transcription",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // If pages are already captured, display the Multi-Page Strip & Smart Next-Page Assistant
                if (scannedPages.isNotEmpty()) {
                    val pageCount = scannedPages.size
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
                        border = BorderStroke(1.5.dp, TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (pageCount == 1) "📸 1 Page Captured" else "📑 $pageCount Pages Captured",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary
                                        )
                                    )
                                    Text(
                                        text = "Snap more pages or scan all pages into one recipe",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF431407),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                TextButton(
                                    onClick = { scannedPages.clear() }
                                ) {
                                    Text("Clear All", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal thumbnail list
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(scannedPages) { index, item ->
                                    Box(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF8C7B6B), RoundedCornerShape(8.dp))
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            AsyncImage(
                                                model = if (item.filePath.isNotBlank()) File(item.filePath) else item.bitmap,
                                                contentDescription = "Page ${index + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Surface(
                                                color = Color(0xFF431407),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Page ${index + 1}",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(vertical = 3.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                        // Rotate 90 degrees button
                                        IconButton(
                                            onClick = {
                                                val currentItem = scannedPages[index]
                                                val rotatedBmp = ImageUtils.rotateBitmap(currentItem.bitmap, 90f)
                                                var newPath = currentItem.filePath
                                                if (newPath.isNotBlank()) {
                                                    ImageUtils.rotateImageFile(newPath, 90f)
                                                } else {
                                                    newPath = ImageUtils.saveLowResReferenceImage(context, rotatedBmp) ?: ""
                                                }
                                                scannedPages[index] = ScannedPageThumbnail(rotatedBmp, newPath)
                                                Toast.makeText(context, "Rotated Page ${index + 1} 90°", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .size(26.dp)
                                                .padding(2.dp)
                                                .background(Color(0xCC000000), shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Rotate 90°",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        // Delete badge button
                                        IconButton(
                                            onClick = { scannedPages.removeAt(index) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(26.dp)
                                                .padding(2.dp)
                                                .background(Color(0xCC000000), shape = RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove page",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Unified Multi-Page Action Buttons
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        val bitmaps = scannedPages.map { it.bitmap }
                                        val primaryPath = scannedPages.firstOrNull()?.filePath
                                        onScan(bitmaps, null, primaryPath)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (pageCount == 1) "✨ Scan & Parse 1 Page" else "✨ Scan & Synthesize All $pageCount Pages",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { requestCameraOrLaunch() },
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerracottaPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Page ${pageCount + 1}", fontSize = 13.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { multiGalleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF5B21B6))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Photos", fontSize = 13.sp, color = Color(0xFF5B21B6), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Scan / Photo Pick Action Cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                requestCameraOrLaunch()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (scannedPages.isEmpty()) "📸 Photograph Recipe Pages" else "📸 Snap Page ${scannedPages.size + 1}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF431407)
                                    )
                                )
                                Text(
                                    text = "Snaps Page 1 -> Page 2 -> Page 3 -> ... auto-scans when Back is pressed",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        multiGalleryLauncher.launch("image/*")
                                    } catch (e: Throwable) {
                                        Toast.makeText(context, "Could not open gallery: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE9FE)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6D28D9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF5B21B6),
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = "From Photos",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E1065)
                                        )
                                    )
                                    Text(
                                        text = "Choose 1 or more",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF3B0764), fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        videoPickerLauncher.launch("video/*")
                                    } catch (e: Throwable) {
                                        Toast.makeText(context, "Could not open video gallery: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color(0xFF0369A1),
                                    modifier = Modifier.size(26.dp)
                                )
                                Column {
                                    Text(
                                        text = "Cooking Video",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0C4A6E)
                                        )
                                    )
                                    Text(
                                        text = "Reel / TikTok clip",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF075985), fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Presets section
                Text(
                    text = "✨ OR TEST WITH SAMPLE RECIPE & FORMULA CARDS:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF431407),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                samplePresetRecipes.forEach { (label, rawContent) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onScan(emptyList(), rawContent, null) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F5EC)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8C7B6B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF18120C)
                                    )
                                )
                                Text(
                                    text = "Tap to simulate multi-page scan, steps & fractions",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF431407),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual / Paste Text Area
                Text(
                    text = "✍️ OR PASTE RECIPE TEXT / HANDWRITTEN NOTES:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF431407),
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = rawTextEntry,
                    onValueChange = { rawTextEntry = it },
                    placeholder = { Text("Paste recipe text in any language across multiple pages...", color = Color(0xFF5A4D41)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF18120C),
                        unfocusedTextColor = Color(0xFF18120C),
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = Color(0xFF8C7B6B),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (rawTextEntry.isNotBlank()) {
                            onScan(emptyList(), rawTextEntry, null)
                        }
                    },
                    enabled = rawTextEntry.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Parse & Translate Recipe", fontWeight = FontWeight.Bold)
                }

                if (scanBatchCount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, SageGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SageGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish & View Bookshelf ($scanBatchCount saved)", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

