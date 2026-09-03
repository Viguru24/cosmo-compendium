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
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id AND isDeleted = 0")
    fun getRecipeById(id: Long): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeDirect(id: Long): RecipeEntity?

    @Query("""
        SELECT * FROM recipes 
        WHERE isDeleted = 0 AND (
           title LIKE '%' || :query || '%' 
           OR titleGerman LIKE '%' || :query || '%' 
           OR titleEnglish LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
           OR notesGerman LIKE '%' || :query || '%'
           OR originStory LIKE '%' || :query || '%'
        )
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

    @Query("UPDATE recipes SET isDeleted = 1, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun softDeleteRecipe(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun hardDeleteRecipe(id: Long)

    @Query("UPDATE recipes SET isFavorite = :isFavorite, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE recipes SET timesCooked = timesCooked + 1, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun incrementCooked(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM recipes WHERE isDeleted = 0")
    suspend fun getCount(): Int

    @Query("UPDATE recipes SET category = :newCategory, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE category = :oldCategory AND isDeleted = 0")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllRecipesDirect(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSyncRecipes(): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE title = :title LIMIT 1")
    suspend fun getRecipeByTitle(title: String): RecipeEntity?

    @Query("UPDATE recipes SET profileName = :profileName, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id = :id")
    suspend fun updateRecipeProfile(id: Long, profileName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recipes WHERE isDeleted = 0 AND (:profileName = 'All' OR profileName = :profileName) ORDER BY createdAt DESC")
    fun getRecipesByProfile(profileName: String): Flow<List<RecipeEntity>>

    @Query("SELECT DISTINCT profileName FROM recipes WHERE isDeleted = 0 AND profileName != ''")
    fun getDistinctProfiles(): Flow<List<String>>

    @Query("UPDATE recipes SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markRecipesSynced(ids: List<Long>)

    @Query("UPDATE recipes SET profileName = :targetProfile, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE isDeleted = 0 AND (:sourceProfile = 'All' OR :sourceProfile = 'All Family' OR profileName = :sourceProfile)")
    suspend fun bulkMoveRecipesProfile(sourceProfile: String, targetProfile: String, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE recipes SET profileName = :targetProfile, updatedAt = :updatedAt, syncStatus = 'PENDING' WHERE id IN (:ids) AND isDeleted = 0")
    suspend fun moveRecipesByIds(ids: List<Long>, targetProfile: String, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()
}
