package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.MathProblem

@Database(entities = [MathProblem::class], version = 1, exportSchema = false)
abstract class MathDatabase : RoomDatabase() {
    abstract fun mathDao(): MathDao

    companion object {
        @Volatile
        private var INSTANCE: MathDatabase? = null

        fun getInstance(context: Context): MathDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MathDatabase::class.java,
                    "ai_math_calculator.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
