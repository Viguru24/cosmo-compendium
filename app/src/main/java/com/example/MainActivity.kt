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
import com.example.ui.viewmodel.RecipeViewModel

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
                        CompendiumApp(viewModel = recipeViewModel)
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
fun CompendiumApp(viewModel: RecipeViewModel) {
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val context = LocalContext.current
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
}
