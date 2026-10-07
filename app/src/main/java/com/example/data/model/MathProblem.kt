package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "math_problems")
data class MathProblem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawQuery: String,
    val inputLatex: String,
    val domain: String,
    val stepsJson: String,
    val finalAnswer: String,
    val conceptExplanation: String,
    val isInvalid: Boolean = false,
    val invalidReason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)
