package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MathStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val latex: String,
    val tip: String? = null
)

@JsonClass(generateAdapter = true)
data class MathSolutionResult(
    val inputExpression: String,
    val domain: String = "Algebra",
    val steps: List<MathStep> = emptyList(),
    val finalAnswer: String = "",
    val conceptExplanation: String = "",
    val isInvalid: Boolean = false,
    val invalidReason: String = ""
)
