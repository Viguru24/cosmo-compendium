package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.local.RecipeEntity
import com.example.data.model.GlossaryItem
import com.example.data.model.LanguageMode
import com.example.data.model.RecipeStep
import com.example.data.model.UnitSystem

/**
 * BookletComponents - High-level backward-compatible umbrella for booklet subsystem components.
 * Individual page components and dialogs are modularized under `com.example.ui.components.booklet.*`:
 * - [com.example.ui.components.booklet.RecipeCoverPage] (Hardcover leather Page 0)
 * - [com.example.ui.components.booklet.RecipeLoreTableOfContentsPage] (Overview & ToC Page 1)
 * - [com.example.ui.components.booklet.RecipeIngredientsPage] (Ingredients & Scaler Page 2)
 * - [com.example.ui.components.booklet.RecipeStepPage] (Step-by-step cooking Pages 3+)
 * - [com.example.ui.components.booklet.RecipeCookJournalPage] (Notes & Journal final page)
 * - [com.example.ui.components.booklet.VintageHandwrittenCardView] (Vintage recipe card view)
 * - [com.example.ui.components.booklet.GlossaryBottomSheet] (Culinary substitution glossary)
 * - [com.example.ui.components.booklet.ShareRecipeCardDialog] (PDF export & sharing dialog)
 * - [com.example.ui.components.booklet.EditRecipeDialog] (Full multi-tab recipe editor)
 * - [com.example.ui.components.booklet.FloatingKitchenTimerWidget] & [com.example.ui.components.booklet.PageCurlCorner]
 */

@Composable
fun RecipeCoverPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    onOpenBook: () -> Unit,
    onOpenStory: () -> Unit,
    onEditRecipe: (() -> Unit)? = null,
    onGenerateAiCover: (() -> Unit)? = null,
    onRemoveCoverPhoto: (() -> Unit)? = null,
    isGeneratingCover: Boolean = false,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.RecipeCoverPage(
    recipe = recipe,
    languageMode = languageMode,
    onOpenBook = onOpenBook,
    onOpenStory = onOpenStory,
    onEditRecipe = onEditRecipe,
    onGenerateAiCover = onGenerateAiCover,
    onRemoveCoverPhoto = onRemoveCoverPhoto,
    isGeneratingCover = isGeneratingCover,
    modifier = modifier
)

@Composable
fun RecipeLoreTableOfContentsPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    onJumpToPage: (Int) -> Unit,
    onEditDetails: (() -> Unit)? = null,
    onRotateOriginalPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.RecipeLoreTableOfContentsPage(
    recipe = recipe,
    languageMode = languageMode,
    onJumpToPage = onJumpToPage,
    onEditDetails = onEditDetails,
    onRotateOriginalPhoto = onRotateOriginalPhoto,
    modifier = modifier
)

@Composable
fun RecipeIngredientsPage(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onUnitSystemChange: (UnitSystem) -> Unit = {},
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float,
    onSetMultiplier: (Float) -> Unit,
    onOpenGlossary: (String) -> Unit,
    checkedIngredients: Set<Int>,
    onToggleCheck: (Int) -> Unit,
    onNextPage: (() -> Unit)? = null,
    onEditIngredients: (() -> Unit)? = null,
    onAddToShoppingList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.RecipeIngredientsPage(
    recipe = recipe,
    languageMode = languageMode,
    unitSystem = unitSystem,
    onUnitSystemChange = onUnitSystemChange,
    onOpenConverter = onOpenConverter,
    servingMultiplier = servingMultiplier,
    onSetMultiplier = onSetMultiplier,
    onOpenGlossary = onOpenGlossary,
    checkedIngredients = checkedIngredients,
    onToggleCheck = onToggleCheck,
    onNextPage = onNextPage,
    onEditIngredients = onEditIngredients,
    onAddToShoppingList = onAddToShoppingList,
    modifier = modifier
)

@Composable
fun RecipeStepPage(
    step: RecipeStep,
    totalSteps: Int,
    recipe: RecipeEntity? = null,
    unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
    onUnitSystemChange: ((UnitSystem) -> Unit)? = null,
    onOpenConverter: ((String?, String, String) -> Unit)? = null,
    servingMultiplier: Float = 1.0f,
    languageMode: LanguageMode,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onSpeak: (String, Boolean) -> Unit,
    onNextPage: (() -> Unit)? = null,
    onEditStep: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.RecipeStepPage(
    step = step,
    totalSteps = totalSteps,
    recipe = recipe,
    unitSystem = unitSystem,
    onUnitSystemChange = onUnitSystemChange,
    onOpenConverter = onOpenConverter,
    servingMultiplier = servingMultiplier,
    languageMode = languageMode,
    isCompleted = isCompleted,
    onToggleCompleted = onToggleCompleted,
    onStartTimer = onStartTimer,
    onSpeak = onSpeak,
    onNextPage = onNextPage,
    onEditStep = onEditStep,
    modifier = modifier
)

@Composable
fun RecipeCookJournalPage(
    recipe: RecipeEntity,
    onSaveJournal: (String, Int) -> Unit,
    onIncrementCooked: () -> Unit,
    onStartCookingMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.RecipeCookJournalPage(
    recipe = recipe,
    onSaveJournal = onSaveJournal,
    onIncrementCooked = onIncrementCooked,
    onStartCookingMode = onStartCookingMode,
    modifier = modifier
)

@Composable
fun VintageHandwrittenCardView(
    recipe: RecipeEntity,
    languageMode: LanguageMode,
    unitSystem: UnitSystem,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = com.example.ui.components.booklet.VintageHandwrittenCardView(
    recipe = recipe,
    languageMode = languageMode,
    unitSystem = unitSystem,
    onClose = onClose,
    modifier = modifier
)

@Composable
fun GlossaryBottomSheet(
    item: GlossaryItem,
    onDismiss: () -> Unit
) = com.example.ui.components.booklet.GlossaryBottomSheet(
    item = item,
    onDismiss = onDismiss
)

@Composable
fun ShareRecipeCardDialog(
    recipe: RecipeEntity,
    servingMultiplier: Float = 1.0f,
    unitSystem: UnitSystem = UnitSystem.METRIC_GRAMS,
    onAddToShoppingList: (() -> Unit)? = null,
    onDismiss: () -> Unit
) = com.example.ui.components.booklet.ShareRecipeCardDialog(
    recipe = recipe,
    servingMultiplier = servingMultiplier,
    unitSystem = unitSystem,
    onAddToShoppingList = onAddToShoppingList,
    onDismiss = onDismiss
)

@Composable
fun EditRecipeDialog(
    initialRecipe: RecipeEntity,
    initialTab: Int = 0,
    categories: List<String> = emptyList(),
    onSave: (RecipeEntity) -> Unit,
    onDelete: ((RecipeEntity) -> Unit)? = null,
    onOpenPromptStudio: (() -> Unit)? = null,
    onDismiss: () -> Unit
) = com.example.ui.components.booklet.EditRecipeDialog(
    initialRecipe = initialRecipe,
    initialTab = initialTab,
    categories = categories,
    onSave = onSave,
    onDelete = onDelete,
    onOpenPromptStudio = onOpenPromptStudio,
    onDismiss = onDismiss
)

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
) = com.example.ui.components.booklet.FloatingKitchenTimerWidget(
    secondsRemaining = secondsRemaining,
    totalSeconds = totalSeconds,
    isRunning = isRunning,
    onPause = onPause,
    onResume = onResume,
    onReset = onReset,
    onAddMinute = onAddMinute,
    modifier = modifier
)
