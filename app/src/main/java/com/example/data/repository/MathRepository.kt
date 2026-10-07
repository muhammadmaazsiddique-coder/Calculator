package com.example.data.repository

import com.example.data.db.MathDao
import com.example.data.model.MathProblem
import com.example.data.model.MathStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class MathRepository(private val mathDao: MathDao) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val stepListType = Types.newParameterizedType(List::class.java, MathStep::class.java)
    private val stepsAdapter = moshi.adapter<List<MathStep>>(stepListType)

    val allHistory: Flow<List<MathProblem>> = mathDao.getAllHistory()
    val bookmarkedHistory: Flow<List<MathProblem>> = mathDao.getBookmarkedHistory()

    fun getHistoryByDomain(domain: String): Flow<List<MathProblem>> =
        mathDao.getHistoryByDomain(domain)

    fun searchHistory(query: String): Flow<List<MathProblem>> =
        mathDao.searchHistory(query)

    suspend fun getProblemById(id: Long): MathProblem? =
        mathDao.getProblemById(id)

    suspend fun saveProblem(
        rawQuery: String,
        inputLatex: String,
        domain: String,
        steps: List<MathStep>,
        finalAnswer: String,
        conceptExplanation: String,
        isInvalid: Boolean,
        invalidReason: String
    ): Long {
        val stepsJson = stepsAdapter.toJson(steps)
        val problem = MathProblem(
            rawQuery = rawQuery,
            inputLatex = inputLatex,
            domain = domain,
            stepsJson = stepsJson,
            finalAnswer = finalAnswer,
            conceptExplanation = conceptExplanation,
            isInvalid = isInvalid,
            invalidReason = invalidReason,
            timestamp = System.currentTimeMillis()
        )
        return mathDao.insertProblem(problem)
    }

    suspend fun toggleBookmark(problem: MathProblem) {
        mathDao.updateProblem(problem.copy(isBookmarked = !problem.isBookmarked))
    }

    suspend fun deleteProblem(id: Long) {
        mathDao.deleteById(id)
    }

    suspend fun clearHistory() {
        mathDao.clearAll()
    }

    fun parseSteps(stepsJson: String): List<MathStep> {
        return try {
            stepsAdapter.fromJson(stepsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
