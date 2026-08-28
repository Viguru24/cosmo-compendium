package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeDirect(id: Long): RecipeEntity?

    @Query("""
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
           OR titleGerman LIKE '%' || :query || '%' 
           OR titleEnglish LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
           OR notesGerman LIKE '%' || :query || '%'
           OR originStory LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE recipes SET timesCooked = timesCooked + 1 WHERE id = :id")
    suspend fun incrementCooked(id: Long)

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getCount(): Int

    @Query("UPDATE recipes SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String)

    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    suspend fun getAllRecipesDirect(): List<RecipeEntity>

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
