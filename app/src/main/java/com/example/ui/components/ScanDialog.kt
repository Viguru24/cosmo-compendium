package com.example.ui.components

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
import androidx.compose.material.icons.filled.ZoomIn
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
    onResolveDuplicateUpdate: ((com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData) -> Unit)? = null,
    onResolveDuplicateSaveCopy: ((com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData) -> Unit)? = null,
    onDismissDuplicate: (() -> Unit)? = null,
    onClearError: () -> Unit = {},
    onScan: (List<Bitmap>, String?, String?) -> Unit,
    onSaveDraft: (RecipeEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scannedPages = remember { mutableStateListOf<ScannedPageThumbnail>() }
    var rawTextEntry by remember { mutableStateOf("") }

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
                val allBitmaps = scannedPages.map { it.bitmap }
                val primaryPath = scannedPages.firstOrNull()?.filePath ?: ""
                Toast.makeText(context, "${scannedPages.size} page(s) loaded! Automatically scanning...", Toast.LENGTH_SHORT).show()
                onScan(allBitmaps, null, primaryPath)
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
                    
                    if (scannedPages.size == 1) {
                        Toast.makeText(context, "Page 1 captured! Opening camera for Page 2 (or tap back/cancel if 1 page)...", Toast.LENGTH_LONG).show()
                        coroutineScope.launch {
                            delay(400)
                            try {
                                val uri = ImageUtils.createTempCameraUri(context)
                                pendingCameraUri = uri
                                cameraLauncherRef?.launch(uri)
                            } catch (e: Throwable) {
                                Toast.makeText(context, "Could not open camera: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // 2 or more pages captured -> automatically synthesize and convert!
                        Toast.makeText(context, "${scannedPages.size} pages captured! Automatically scanning & transcribing...", Toast.LENGTH_SHORT).show()
                        val allBitmaps = scannedPages.map { it.bitmap }
                        val primaryPath = scannedPages.firstOrNull()?.filePath ?: savedImagePath
                        onScan(allBitmaps, null, primaryPath)
                    }
                } else {
                    Toast.makeText(context, "Could not process photo from camera", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Throwable) {
                Toast.makeText(context, "Error processing photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else if (!success && scannedPages.isNotEmpty()) {
            // User cancelled/backed out of camera after page 1 -> automatically process the 1 page captured!
            Toast.makeText(context, "1 page captured! Automatically scanning & transcribing...", Toast.LENGTH_SHORT).show()
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

    // Preset Vintage Recipe Samples for instant one-click testing & translation demo
    val samplePresetRecipes = listOf(
        Pair(
            "📜 Vintage Handwritten German Apple Strudel (Auto-Translates to English)",
            """
                --- Page 1: Oma's Apfelstrudel Zutaten & Teig ---
                Zutaten für den Strudelteig:
                - 250 g Weizenmehl
                - 1 Prise Salz
                - 1 Ei
                - 100 ml Lauwarmes Wasser
                - 2 EL Pflanzenöl
                - 1/4 / 1/2 spoon sugar (Zucker für den Teig)
                
                --- Page 2: Füllung & Zubereitung (Schritte 1 - 4) ---
                Zutaten für die Füllung:
                - 1 kg Säuerliche Äpfel (Boskoop)
                - 80 g Zucker
                - 1 TL Zimt
                - 50 g Rosinen
                - 50 g Butter
                - 60 g Semmelbrösel
                
                Zubereitung:
                1. Mehl, Salz, Ei, Öl, Wasser und 1/4 / 1/2 spoon sugar zu einem geschmeidigen Teig verkneten, mit Öl bestreichen und 30 Minuten ruhen lassen.
                2. Äpfel schälen, blättrig schneiden und mit Zucker, Zimt und Rosinen vermengen. Semmelbrösel in Butter goldbraun rösten.
                3. Den Teig auf einem bemehlten Tuch hauchdünn ausziehen. Mit Bröseln und Apfelfüllung belegen.
                4. Den Strudel mit Hilfe des Tuches einrollen, auf ein Blech legen, mit flüssiger Butter bestreichen und bei 180°C für 35 Minuten goldgelb backen.
            """.trimIndent()
        ),
        Pair(
            "📜 Traditional Sauerbraten Vintage Card (Auto-Translates to English)",
            """
                Omas Rheinischer Sauerbraten (1962)
                Zutaten für den Braten:
                - 1.5 kg Rindfleisch (Schulter oder Keule)
                - 500 ml Rotweinessig
                - 250 ml Wasser
                - 2 Zwiebeln in Scheiben
                - 2 Karotten
                - 4 Lorbeerblätter
                - 6 Nelken
                - 8 Wacholderbeeren
                - 100 g Aachener Printen oder Soßenkuchen
                - 2 EL Rosinen
                - 2 EL Butterschmalz
                - Salz & Pfeffer
                
                Zubereitung:
                1. Rindfleisch mit Essig, Wasser, Gemüse und Gewürzen aufkochen, abkühlen lassen und 4 Tage im Kühlschrank marinieren.
                2. Fleisch trocken tupfen, im Schmalz scharf rundherum anbraten (10 Min).
                3. Mit der durchgesiebten Marinade ablöschen und 2.5 Stunden bei schwacher Hitze schmoren.
                4. Printen und Rosinen einrühren, die Sauce sämig einkochen lassen.
                5. Mit Kartoffelklößen und Apfelkompott servieren.
            """.trimIndent()
        ),
        Pair(
            "📜 Grandmother's Bread & Butter Pudding (English Card)",
            """
                Grandmother's Rich Bread & Butter Pudding (1971)
                Ingredients:
                - 8 slices Brioche or White Country Bread (crusts removed)
                - 50 g Unsalted Butter (softened)
                - 60 g Sultanas (golden raisins)
                - 1 tsp Ground Nutmeg & Cinnamon
                - 3 Large Free-range Eggs
                - 300 ml Whole Milk
                - 150 ml Double Cream
                - 60 g Caster Sugar
                - 1 tsp Vanilla Extract
                - 2 tbsp Demerara Sugar (for topping)
                
                Directions:
                1. Butter the bread slices generously and cut into triangles.
                2. Layer bread in an oven dish, scattering sultanas and nutmeg between layers.
                3. Whisk eggs, milk, cream, caster sugar, and vanilla. Pour over bread and let soak for 30 minutes.
                4. Sprinkle top with crunchy Demerara sugar and bake at 180°C for 35 minutes until golden and puffed.
            """.trimIndent()
        )
    )

    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFDF8),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .testTag("scan_recipe_bottom_sheet")
    ) {
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
            // Clean Full-Page Live AI Scanning & Transcribing Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDFBF7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Glowing Icon & Animated Spinner
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFF4EB),
                        border = BorderStroke(2.dp, Color(0xFFFFD8BF)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                color = TerracottaPrimary,
                                modifier = Modifier.size(68.dp),
                                strokeWidth = 3.5.dp
                            )
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Transcribing Recipe...",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF431407),
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Deciphering handwriting, fractions & cooking instructions",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF785E48),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TerracottaPrimary,
                        trackColor = Color(0xFFEADBCE)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Captured Pages Preview Container
                    if (scannedPages.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (scannedPages.size == 1) "Captured Card (1 Page)" else "Captured Cards (${scannedPages.size} Pages)",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF292524)
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFD97706))
                                            )
                                            Text(
                                                text = "Analyzing",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color(0xFF92400E),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    scannedPages.forEachIndexed { index, item ->
                                        Card(
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.5.dp, Color(0xFFE2D6C5)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(170.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = if (item.filePath.isNotBlank()) File(item.filePath) else item.bitmap,
                                                    contentDescription = "Page ${index + 1}",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Surface(
                                                    color = Color(0xDD292524),
                                                    shape = RoundedCornerShape(topStart = 8.dp),
                                                    modifier = Modifier.align(Alignment.BottomEnd)
                                                ) {
                                                    Text(
                                                        text = if (index == 0 && scannedPages.size > 1) "Page 1 • Front" else if (index == 1) "Page 2 • Back" else "Page ${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clean Progress Step Indicators
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "✨ Deciphering cursive handwriting" to true,
                                "⚖️ Converting fractional measurements & spoons" to true,
                                "📖 Structuring into your Cookbook" to false
                            ).forEach { (stepTitle, isDone) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (isDone) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SageGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(15.dp),
                                            strokeWidth = 2.dp,
                                            color = TerracottaPrimary
                                        )
                                    }
                                    Text(
                                        text = stepTitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isDone) Color(0xFF44403C) else TerracottaPrimary,
                                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
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
                            text = "📸 Scan Recipe",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                fontFamily = FontFamily.Serif
                            )
                        )
                        Text(
                            text = "Supports multi-page recipe cards, notebooks & cursive",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF18120C))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF8C7B6B))

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
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
                                        text = if (pageCount == 1) "📸 Page 1 Captured" else "📑 $pageCount Pages Captured",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary
                                        )
                                    )
                                    Text(
                                        text = if (pageCount == 1) "Need a 2nd page (back of card / next steps)?" else "Front & back / multiple pages ready for AI parsing",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF431407),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                TextButton(
                                    onClick = { scannedPages.clear() }
                                ) {
                                    Text("Clear", color = Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                                    text = if (index == 0) "Page 1 (Front)" else if (index == 1) "Page 2 (Back)" else "Page ${index + 1}",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(vertical = 3.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
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

                            if (pageCount == 1) {
                                // Smart 2nd Page Prompt
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { requestCameraOrLaunch() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("📸 Take Page 2 Photo (Back of Card)", fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val bitmaps = scannedPages.map { it.bitmap }
                                                val primaryPath = scannedPages.firstOrNull()?.filePath
                                                onScan(bitmaps, null, primaryPath)
                                            },
                                            modifier = Modifier.weight(1.3f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Convert 1 Page Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
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
                            } else {
                                // Multi-page actions
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
                                        Text("✨ Scan & Synthesize All $pageCount Pages", fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { requestCameraOrLaunch() },
                                            modifier = Modifier.weight(1f),
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
                }

                // Primary Scan / Photo Pick Action Cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scannedPages.clear()
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
                                    text = "📸 Take Photo (1 or 2 Pages)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF431407)
                                    )
                                )
                                Text(
                                    text = "Snaps Page 1 then automatically opens for Page 2 (press Back/Cancel if 1 page)",
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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Presets section
                Text(
                    text = "✨ OR TEST WITH SAMPLE HEIRLOOM RECIPE CARDS:",
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
                    placeholder = { Text("Paste German or English recipe text across multiple pages...", color = Color(0xFF5A4D41)) },
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
            }
        }
    }
}

@Composable
fun RecipeReviewAndSaveView(
    initialDraft: RecipeEntity,
    onSave: (RecipeEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleEnglish by remember { mutableStateOf(initialDraft.titleEnglish.ifBlank { initialDraft.title }) }
    var titleGerman by remember { mutableStateOf(initialDraft.titleGerman) }
    var category by remember { mutableStateOf(initialDraft.category) }
    var servings by remember { mutableStateOf(initialDraft.servings) }
    var prepTime by remember { mutableStateOf(initialDraft.prepTimeMinutes.toString()) }
    var cookTime by remember { mutableStateOf(initialDraft.cookTimeMinutes.toString()) }
    var notes by remember { mutableStateOf(initialDraft.notes) }
    var ingredients by remember { mutableStateOf(initialDraft.ingredients) }
    var steps by remember { mutableStateOf(initialDraft.steps) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                    text = "✨ AI Extracted & Translated Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary,
                        fontFamily = FontFamily.Serif
                    )
                )
                Text(
                    text = "English translation is primary. Review fields before saving to your book.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF431407), fontWeight = FontWeight.Medium)
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFF18120C))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF8C7B6B))

        // Low-resolution Reference Image Card Preview
        if (!initialDraft.imageUri.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFullImageDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F4EB)),
                border = BorderStroke(1.5.dp, Color(0xFF8C7B6B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = File(initialDraft.imageUri),
                        contentDescription = "Original recipe photo reference",
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD1C7B7), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📸 Original Photo Stored",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ZoomIn,
                                contentDescription = "Inspect",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reference image saved for verification. Tap to inspect original card.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF431407),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // English Title (Primary)
        OutlinedTextField(
            value = titleEnglish,
            onValueChange = { titleEnglish = it },
            label = { Text("Recipe Title", color = Color(0xFF431407), fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Category & Times
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
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
            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it },
                label = { Text("Servings", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = prepTime,
                onValueChange = { prepTime = it },
                label = { Text("Prep (min)", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
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
            OutlinedTextField(
                value = cookTime,
                onValueChange = { cookTime = it },
                label = { Text("Cook (min)", color = Color(0xFF431407)) },
                modifier = Modifier.weight(1f),
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
        }

        val totalMinutes = (prepTime.toIntOrNull() ?: 0) + (cookTime.toIntOrNull() ?: 0)
        if (totalMinutes > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFFF9F5EC),
                border = BorderStroke(1.dp, Color(0xFF8C7B6B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏱️ Total Time (Prep + Cook):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF431407))
                    )
                    Text(
                        text = "$totalMinutes mins",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients summary
        Text(
            text = "🥕 Ingredients (${ingredients.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        ingredients.forEachIndexed { i, ing ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                color = Color(0xFFFAF6EE),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8C7B6B))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ing.amount} ${ing.unit} • ${ing.getDisplayName()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF18120C)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Steps summary
        Text(
            text = "📝 Preparation Steps (${steps.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        steps.forEachIndexed { i, step ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = Color(0xFFFAF6EE),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8C7B6B))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Step ${step.stepNumber}:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = TerracottaPrimary)
                    )
                    Text(
                        text = step.getInstruction(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF18120C),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Family Secrets / Chef Notes", color = Color(0xFF431407)) },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val finalRecipe = initialDraft.copy(
                    title = titleEnglish.ifBlank { titleGerman },
                    titleGerman = titleGerman,
                    titleEnglish = titleEnglish,
                    category = category,
                    servings = servings,
                    prepTimeMinutes = prepTime.toIntOrNull() ?: 20,
                    cookTimeMinutes = cookTime.toIntOrNull() ?: 30,
                    notes = notes,
                    ingredients = ingredients,
                    steps = steps,
                    imageUri = initialDraft.imageUri
                )
                onSave(finalRecipe)
            },
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("save_scanned_recipe_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Recipe", fontWeight = FontWeight.Bold)
        }
    }

    if (showFullImageDialog && !initialDraft.imageUri.isNullOrBlank()) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1610),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸 Captured Recipe Card",
                            color = Color(0xFFFEF3C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { showFullImageDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = File(initialDraft.imageUri),
                        contentDescription = "Original recipe photo reference",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun DuplicateRecipeComparisonView(
    prompt: com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData,
    onUpdate: () -> Unit,
    onSaveCopy: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existing = prompt.existingRecipe
    val scanned = prompt.scannedRecipe

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFFFDFBF7))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .testTag("duplicate_recipe_comparison_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning Icon Badge
        Surface(
            shape = CircleShape,
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier.size(68.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Duplicate Recipe Detected",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF431407),
                fontFamily = FontFamily.Serif
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You already have a recipe titled \"${existing.title}\" in your ${existing.category} collection.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF573C27),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Side-by-side / Comparison Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Existing in Library
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F2)),
                border = BorderStroke(1.5.dp, Color(0xFFD6C8B8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "📖", fontSize = 16.sp)
                            Text(
                                text = "Current Recipe in Cookbook",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF78350F)
                                )
                            )
                        }
                        if (existing.isFavorite) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "★ Favorite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = existing.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF292524),
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ ${existing.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                        Text(
                            text = "⏱️ ${existing.cookTimeMinutes}m cook",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                        Text(
                            text = "🍳 Cooked ${existing.timesCooked}x",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B5848))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredients (${existing.ingredients.size}): " + existing.ingredients.take(4).joinToString(", ") { it.name } + if (existing.ingredients.size > 4) "..." else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF78716C),
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 2
                    )
                }
            }

            // Newly Scanned Candidate
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                border = BorderStroke(1.5.dp, Color(0xFFFDBA74)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "📸", fontSize = 16.sp)
                            Text(
                                text = "Newly Scanned Version",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA))
                        ) {
                            Text(
                                text = "New Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = scanned.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF431407),
                            fontFamily = FontFamily.Serif
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ ${scanned.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                        Text(
                            text = "⏱️ ${scanned.cookTimeMinutes}m cook",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                        Text(
                            text = "📋 ${scanned.steps.size} steps",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF785E48))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredients (${scanned.ingredients.size}): " + scanned.ingredients.take(4).joinToString(", ") { it.name } + if (scanned.ingredients.size > 4) "..." else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8C7355),
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Update Existing Recipe
            Button(
                onClick = onUpdate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("duplicate_update_existing_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Existing Recipe",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Updates ingredients, steps, and photos while preserving your favorites status and cooking history.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Option 2: Save as New Copy (Variation)
            OutlinedButton(
                onClick = onSaveCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("duplicate_save_copy_button"),
                border = BorderStroke(1.5.dp, SageGreen),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save as New Copy (Variation)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Keeps both recipes separately in your collection as \"${scanned.title} (Variation)\".",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF78716C),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Option 3: Discard Scanned Recipe
            TextButton(
                onClick = onDiscard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("duplicate_discard_button")
            ) {
                Text(
                    text = "Discard Scanned Recipe",
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DuplicateRecipeDialog(
    prompt: com.example.ui.viewmodel.RecipeViewModel.DuplicatePromptData,
    onUpdate: () -> Unit,
    onSaveCopy: () -> Unit,
    onDiscard: () -> Unit
) {
    Dialog(onDismissRequest = onDiscard) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFFDF8),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            DuplicateRecipeComparisonView(
                prompt = prompt,
                onUpdate = onUpdate,
                onSaveCopy = onSaveCopy,
                onDiscard = onDiscard
            )
        }
    }
}
