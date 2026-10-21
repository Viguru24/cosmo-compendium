package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppErrorBoundary
import com.example.ui.screens.BookletScreen
import com.example.ui.screens.BookshelfScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.data.AppUpdateManager
import com.example.data.UpdateInfo
import com.example.ui.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                AppErrorBoundary {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CookbookApp(viewModel = recipeViewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Instant pull delta sync when app opens or resumes to foreground
        recipeViewModel.onAppForegroundResume()
    }
}

@Composable
fun CookbookApp(viewModel: RecipeViewModel) {
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val updateManager = remember { AppUpdateManager(context) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val info = updateManager.checkForUpdates()
        if (info != null) {
            availableUpdate = info
        }
    }

    DisposableEffect(keepScreenOn) {
        val window = (context as? Activity)?.window
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val selectedRecipe by viewModel.selectedRecipe.collectAsStateWithLifecycle()
    val selectedRecipeId = selectedRecipe?.id

    BackHandler(enabled = selectedRecipe != null) {
        if (viewModel.isCookMode.value) {
            viewModel.isCookMode.value = false
        } else {
            viewModel.selectRecipe(null)
        }
    }

    AnimatedContent(
        targetState = selectedRecipeId,
        transitionSpec = {
            if (targetState != null && initialState == null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else if (targetState == null && initialState != null) {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            } else {
                fadeIn() togetherWith fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { recipeId ->
        if (recipeId == null) {
            BookshelfScreen(
                viewModel = viewModel,
                onRecipeClick = { clicked ->
                    viewModel.selectRecipe(clicked)
                }
            )
        } else {
            val currentRecipe = selectedRecipe
            if (currentRecipe != null) {
                BookletScreen(
                    viewModel = viewModel,
                    recipe = currentRecipe,
                    onBack = {
                        viewModel.selectRecipe(null)
                    }
                )
            }
        }
    }

    availableUpdate?.let { update ->
        AlertDialog(
            onDismissRequest = {
                if (!isDownloading) availableUpdate = null
            },
            title = {
                Text(text = "App Update Available", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A new version (${update.versionName}) is available on Sovereign VPS.\nCurrent version: ${updateManager.currentVersionName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (update.releaseNotes.isNotBlank()) {
                        Text(
                            text = "Release Notes:\n${update.releaseNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isDownloading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Downloading: ${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDownloading = true
                        scope.launch {
                            val success = updateManager.downloadAndInstallUpdate(update.apkUrl) { progress ->
                                downloadProgress = progress
                            }
                            isDownloading = false
                            if (success) {
                                availableUpdate = null
                            }
                        }
                    },
                    enabled = !isDownloading
                ) {
                    Text(if (isDownloading) "Downloading..." else "Download & Install")
                }
            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = { availableUpdate = null }) {
                        Text("Later")
                    }
                }
            }
        )
    }
}
