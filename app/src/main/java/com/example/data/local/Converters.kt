package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject

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
        return if (value != null) {
            try {
                ingredientAdapter.toJson(value)
            } catch (e: Exception) {
                manualSerializeIngredients(value)
            }
        } else "[]"
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient> {
        if (value.isNullOrBlank() || value.trim() == "[]") return emptyList()
        val trimmed = value.trim()
        return try {
            ingredientAdapter.fromJson(trimmed) ?: manualDeserializeIngredients(trimmed)
        } catch (e: Exception) {
            manualDeserializeIngredients(trimmed)
        }
    }

    @TypeConverter
    fun fromStepList(value: List<RecipeStep>?): String {
        return if (value != null) {
            try {
                stepAdapter.toJson(value)
            } catch (e: Exception) {
                manualSerializeSteps(value)
            }
        } else "[]"
    }

    @TypeConverter
    fun toStepList(value: String?): List<RecipeStep> {
        if (value.isNullOrBlank() || value.trim() == "[]") return emptyList()
        val trimmed = value.trim()
        return try {
            stepAdapter.fromJson(trimmed) ?: manualDeserializeSteps(trimmed)
        } catch (e: Exception) {
            manualDeserializeSteps(trimmed)
        }
    }

    private fun manualSerializeIngredients(list: List<RecipeIngredient>): String {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("amount", item.amount)
            obj.put("unit", item.unit)
            obj.put("nameGerman", item.nameGerman ?: "")
            obj.put("nameEnglish", item.nameEnglish ?: "")
            obj.put("isOptional", item.isOptional)
            obj.put("group", item.group ?: "")
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun manualDeserializeIngredients(json: String): List<RecipeIngredient> {
        val result = mutableListOf<RecipeIngredient>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val item = arr.opt(i)
                if (item is JSONObject) {
                    val name = item.optString("name", item.optString("nameEnglish", item.optString("nameGerman", "Ingredient")))
                    result.add(
                        RecipeIngredient(
                            name = name,
                            amount = item.optString("amount", item.optString("quantity", "")),
                            unit = item.optString("unit", ""),
                            nameGerman = if (item.has("nameGerman") && !item.isNull("nameGerman")) item.getString("nameGerman") else null,
                            nameEnglish = if (item.has("nameEnglish") && !item.isNull("nameEnglish")) item.getString("nameEnglish") else null,
                            isOptional = item.optBoolean("isOptional", false),
                            group = if (item.has("group") && !item.isNull("group")) item.getString("group") else null
                        )
                    )
                } else if (item is String && item.isNotBlank()) {
                    result.add(
                        RecipeIngredient(
                            name = item,
                            amount = "",
                            unit = "",
                            nameGerman = item,
                            nameEnglish = item
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun manualSerializeSteps(list: List<RecipeStep>): String {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("stepNumber", item.stepNumber)
            obj.put("instructionEnglish", item.instructionEnglish)
            obj.put("instructionGerman", item.instructionGerman)
            obj.put("timerMinutes", item.timerMinutes)
            obj.put("tip", item.tip ?: "")
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun manualDeserializeSteps(json: String): List<RecipeStep> {
        val result = mutableListOf<RecipeStep>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val item = arr.opt(i)
                if (item is JSONObject) {
                    val instr = item.optString("instructionEnglish", item.optString("instructionGerman", item.optString("text", "")))
                    result.add(
                        RecipeStep(
                            stepNumber = item.optInt("stepNumber", i + 1),
                            instructionEnglish = instr,
                            instructionGerman = instr,
                            timerMinutes = item.optInt("timerMinutes", 0),
                            tip = if (item.has("tip") && !item.isNull("tip")) item.getString("tip") else null
                        )
                    )
                } else if (item is String && item.isNotBlank()) {
                    result.add(
                        RecipeStep(
                            stepNumber = i + 1,
                            instructionEnglish = item,
                            instructionGerman = item,
                            timerMinutes = 0
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
