package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MathProblem
import kotlinx.coroutines.flow.Flow

@Dao
interface MathDao {
    @Query("SELECT * FROM math_problems ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<MathProblem>>

    @Query("SELECT * FROM math_problems WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedHistory(): Flow<List<MathProblem>>

    @Query("SELECT * FROM math_problems WHERE domain = :domain ORDER BY timestamp DESC")
    fun getHistoryByDomain(domain: String): Flow<List<MathProblem>>

    @Query("SELECT * FROM math_problems WHERE rawQuery LIKE '%' || :query || '%' OR finalAnswer LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<MathProblem>>

    @Query("SELECT * FROM math_problems WHERE id = :id LIMIT 1")
    suspend fun getProblemById(id: Long): MathProblem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: MathProblem): Long

    @Update
    suspend fun updateProblem(problem: MathProblem)

    @Delete
    suspend fun deleteProblem(problem: MathProblem)

    @Query("DELETE FROM math_problems WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM math_problems")
    suspend fun clearAll()
}
