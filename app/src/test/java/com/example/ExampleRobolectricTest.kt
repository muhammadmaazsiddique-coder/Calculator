package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.MathDatabase
import com.example.data.model.MathProblem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Math Solver", appName)
    }

    @Test
    fun `test database insertion and history retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MathDatabase.getInstance(context)
        val dao = db.mathDao()

        val problem = MathProblem(
            rawQuery = "x^2 - 4 = 0",
            inputLatex = "x^2 - 4 = 0",
            domain = "Algebra",
            stepsJson = "[]",
            finalAnswer = "x = \\pm 2",
            conceptExplanation = "Difference of squares"
        )

        val id = dao.insertProblem(problem)
        assertNotNull(id)

        val retrieved = dao.getProblemById(id)
        assertNotNull(retrieved)
        assertEquals("Algebra", retrieved?.domain)
    }
}
