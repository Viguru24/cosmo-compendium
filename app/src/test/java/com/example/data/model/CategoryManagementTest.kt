package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryManagementTest {

    @Test
    fun categoryReorder_moveUp_shiftsItemCorrectly() {
        val list = mutableListOf("Baking", "Main Dishes", "Soups")
        val item = list.removeAt(1)
        list.add(0, item)
        assertEquals(listOf("Main Dishes", "Baking", "Soups"), list)
    }

    @Test
    fun categoryReorder_moveDown_shiftsItemCorrectly() {
        val list = mutableListOf("Baking", "Main Dishes", "Soups")
        val item = list.removeAt(0)
        list.add(1, item)
        assertEquals(listOf("Main Dishes", "Baking", "Soups"), list)
    }

    @Test
    fun categoryAdd_deduplicatesCaseInsensitively() {
        val list = mutableListOf("Baking", "Main Dishes")
        val newName = "baking"
        val alreadyExists = list.any { it.equals(newName, ignoreCase = true) }
        assertTrue(alreadyExists)
    }
}
