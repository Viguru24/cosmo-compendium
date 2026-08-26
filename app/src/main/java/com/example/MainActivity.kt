package com.example

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BookletScreen
import com.example.ui.screens.BookshelfScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HeirloomRecipeApp(viewModel = recipeViewModel)
                }
            }
        }
    }
}

@Composable
fun HeirloomRecipeApp(viewModel: RecipeViewModel) {
    val selectedRecipe by viewModel.selectedRecipe.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedRecipe != null) {
        if (viewModel.isCookMode.value) {
            viewModel.isCookMode.value = false
        } else {
            viewModel.selectRecipe(null)
        }
    }

    AnimatedContent(
        targetState = selectedRecipe,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { recipe ->
        if (recipe == null) {
            BookshelfScreen(
                viewModel = viewModel,
                onRecipeClick = { clicked ->
                    viewModel.selectRecipe(clicked)
                }
            )
        } else {
            BookletScreen(
                viewModel = viewModel,
                recipe = recipe,
                onBack = {
                    viewModel.selectRecipe(null)
                }
            )
        }
    }
}
