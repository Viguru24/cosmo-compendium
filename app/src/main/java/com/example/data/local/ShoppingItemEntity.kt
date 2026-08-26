package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long? = null,
    val recipeTitle: String? = null,
    val name: String,
    val amount: String = "",
    val unit: String = "",
    val isChecked: Boolean = false,
    val category: String = "Pantry", // Produce, Dairy & Refrigerated, Meat & Seafood, Bakery, Spices & Baking, Pantry, Other
    val createdAt: Long = System.currentTimeMillis()
)
