package com.example.ui.components.bookshelf

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageMode
import com.example.ui.components.VoiceInputOrb
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.util.AppLocalization

@Composable
fun BookshelfSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeProfile: String,
    activeProfileTotalCount: Int,
    activeProfileFavoritesCount: Int,
    selectedCategory: String,
    onlyFavorites: Boolean,
    rawCategories: List<String>,
    activeProfileCategoryCounts: Map<String, Int>,
    totalRecipeCount: Int,
    languageMode: LanguageMode,
    onSelectFavorites: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onExportCompleteCookbookPdf: () -> Unit,
    onOpenCategoryManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var searchTextFieldValue by remember(searchQuery) {
        mutableStateOf(TextFieldValue(searchQuery, selection = TextRange(searchQuery.length)))
    }

    Surface(
        color = Color(0xFFFFFDF9),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE8DFD5)),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 880.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Instant Fuzzy Search Bar with Push-to-Talk Voice Input
                OutlinedTextField(
                    value = searchTextFieldValue,
                    onValueChange = { newValue ->
                        val text = newValue.text
                        searchTextFieldValue = newValue.copy(selection = TextRange(text.length))
                        onSearchQueryChange(text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("recipe_search_input"),
                    placeholder = {
                        Text(
                            AppLocalization.getSearchPlaceholder(languageMode),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF5A4D41), fontSize = 13.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        searchTextFieldValue = TextFieldValue("", selection = TextRange(0))
                                    },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color(0xFF18120C)
                                    )
                                }
                            }
                            VoiceInputOrb(
                                onSpeechResult = { spoken ->
                                    onSearchQueryChange(spoken)
                                },
                                onPartialResult = { partial ->
                                    onSearchQueryChange(partial)
                                },
                                size = 36.dp,
                                iconSize = 18.dp,
                                modifier = Modifier.testTag("voice_search_button")
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF18120C),
                        unfocusedTextColor = Color(0xFF18120C),
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = Color(0xFF8C7B6B),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Profile Summary (Total count in active profile)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.GERMAN) {
                            "Gesamt: $activeProfileTotalCount ${if (activeProfileTotalCount == 1) "Rezept" else "Rezepte"} im $activeProfile Profil"
                        } else {
                            "Total $activeProfileTotalCount ${if (activeProfileTotalCount == 1) "recipe" else "recipes"} in $activeProfile's profile"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF786555)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Category & Favorites Pull-Down Dropdown Menu
                val activeFilterLabel = when {
                    onlyFavorites -> AppLocalization.getFavoritesLabel(languageMode)
                    selectedCategory == "All" -> AppLocalization.getCategoryLabel("All", languageMode)
                    else -> AppLocalization.getCategoryLabel(selectedCategory, languageMode)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(
                                1.5.dp,
                                if (onlyFavorites) Color(0xFF9F1239)
                                else if (selectedCategory != "All") TerracottaPrimary
                                else Color(0xFFD6C7B2)
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCategoryDropdownExpanded = true }
                                .testTag("category_filter_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = if (onlyFavorites) Color(0xFF9F1239) else TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = activeFilterLabel,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (onlyFavorites) Color(0xFF9F1239) else Color(0xFF2C2420),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Category Filter",
                                    tint = if (onlyFavorites) Color(0xFF9F1239) else TerracottaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFFFFFDF9))
                        ) {
                            // 1. Favorites
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = Color(0xFF9F1239),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "${AppLocalization.getFavoritesLabel(languageMode)} ($activeProfileFavoritesCount)",
                                            fontWeight = if (onlyFavorites) FontWeight.Bold else FontWeight.Medium,
                                            color = Color(0xFF9F1239),
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectFavorites()
                                    isCategoryDropdownExpanded = false
                                }
                            )

                            // 2. All Recipes
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("📚", fontSize = 16.sp)
                                        Text(
                                            text = "${AppLocalization.getCategoryLabel("All", languageMode)} ($activeProfileTotalCount)",
                                            fontWeight = if (!onlyFavorites && selectedCategory == "All") FontWeight.Bold else FontWeight.Medium,
                                            color = Color(0xFF2C2420),
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectCategory("All")
                                    isCategoryDropdownExpanded = false
                                }
                            )

                            HorizontalDivider(color = Color(0xFFE8DFD5))

                            // 3. Specific Categories
                            rawCategories.filter { it != "All" }.forEach { cat ->
                                val isSelected = !onlyFavorites && selectedCategory.equals(cat, ignoreCase = true)
                                val catCount = activeProfileCategoryCounts[cat]
                                    ?: activeProfileCategoryCounts.entries.find { it.key.equals(cat, ignoreCase = true) }?.value
                                    ?: 0
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    cat.contains("Baking", ignoreCase = true) || cat.contains("Dessert", ignoreCase = true) || cat.contains("Cake", ignoreCase = true) || cat.contains("Cookie", ignoreCase = true) -> "🍰"
                                                    cat.contains("Main", ignoreCase = true) || cat.contains("Dinner", ignoreCase = true) || cat.contains("Meat", ignoreCase = true) || cat.contains("Chicken", ignoreCase = true) || cat.contains("Beef", ignoreCase = true) -> "🍲"
                                                    cat.contains("Salad", ignoreCase = true) || cat.contains("Starter", ignoreCase = true) || cat.contains("Appetizer", ignoreCase = true) -> "🥗"
                                                    cat.contains("Soup", ignoreCase = true) || cat.contains("Stew", ignoreCase = true) || cat.contains("Chili", ignoreCase = true) -> "🥣"
                                                    cat.contains("Breakfast", ignoreCase = true) || cat.contains("Brunch", ignoreCase = true) || cat.contains("Egg", ignoreCase = true) || cat.contains("Pancake", ignoreCase = true) -> "🍳"
                                                    cat.contains("Sauce", ignoreCase = true) || cat.contains("Condiment", ignoreCase = true) || cat.contains("Dip", ignoreCase = true) -> "🫙"
                                                    cat.contains("Beverage", ignoreCase = true) || cat.contains("Drink", ignoreCase = true) || cat.contains("Cocktail", ignoreCase = true) -> "🍹"
                                                    cat.contains("Snack", ignoreCase = true) || cat.contains("Bread", ignoreCase = true) || cat.contains("Brot", ignoreCase = true) -> "🥨"
                                                    cat.contains("Holiday", ignoreCase = true) || cat.contains("Tradition", ignoreCase = true) || cat.contains("Christmas", ignoreCase = true) -> "🎄"
                                                    cat.contains("Classic", ignoreCase = true) -> "⭐"
                                                    else -> "🏷️"
                                                },
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "${AppLocalization.getCategoryLabel(cat, languageMode)} ($catCount)",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) TerracottaPrimary else Color(0xFF2C2420),
                                                fontSize = 14.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectCategory(cat)
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE8DFD5))

                            // Master PDF Full Export
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("📖", fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = "Export Complete Cookbook (PDF)",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8C2D19),
                                                fontSize = 13.5.sp
                                            )
                                            Text(
                                                text = "All $totalRecipeCount recipes with AI photos & TOC",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF78350F), fontSize = 10.sp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    isCategoryDropdownExpanded = false
                                    Toast.makeText(context, "Compiling Master Cookbook PDF (with AI Photos & Table of Contents)...", Toast.LENGTH_SHORT).show()
                                    onExportCompleteCookbookPdf()
                                }
                            )

                            HorizontalDivider(color = Color(0xFFE8DFD5))

                            // Manage Categories (Add, Edit, Reorder)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = if (languageMode == LanguageMode.GERMAN) "Kategorien verwalten (Neu, Bearbeiten, Ordnen)" else "Manage Categories (Add, Edit, Reorder)",
                                                fontWeight = FontWeight.Bold,
                                                color = TerracottaPrimary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = if (languageMode == LanguageMode.GERMAN) "Kategorien anpassen & sortieren" else "Add, rename, delete or reorder categories",
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF786555), fontSize = 10.sp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    isCategoryDropdownExpanded = false
                                    onOpenCategoryManager()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
