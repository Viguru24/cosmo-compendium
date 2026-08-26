package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val ingredientListType = Types.newParameterizedType(List::class.java, RecipeIngredient::class.java)
    private val stepListType = Types.newParameterizedType(List::class.java, RecipeStep::class.java)

    private val ingredientAdapter = moshi.adapter<List<RecipeIngredient>>(ingredientListType)
    private val stepAdapter = moshi.adapter<List<RecipeStep>>(stepListType)

    @TypeConverter
    fun fromIngredientList(value: List<RecipeIngredient>?): String {
        return if (value != null) ingredientAdapter.toJson(value) else "[]"
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient> {
        return if (!value.isNullOrBlank()) {
            try {
                ingredientAdapter.fromJson(value) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStepList(value: List<RecipeStep>?): String {
        return if (value != null) stepAdapter.toJson(value) else "[]"
    }

    @TypeConverter
    fun toStepList(value: String?): List<RecipeStep> {
        return if (!value.isNullOrBlank()) {
            try {
                stepAdapter.fromJson(value) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
