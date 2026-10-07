package com.example

import com.example.api.LocalMathSolver
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDivisionByZeroHandled() {
        val result = LocalMathSolver.solveLocally("42 / 0")
        assertNotNull(result)
        assertTrue(result!!.isInvalid)
        assertTrue(result.invalidReason.contains("Cannot divide by zero"))
        assertEquals(2, result.steps.size)
    }

    @Test
    fun testQuadraticEquationSolving() {
        val result = LocalMathSolver.solveLocally("2x^2 + 4x - 6 = 0")
        assertNotNull(result)
        assertFalse(result!!.isInvalid)
        assertEquals("Algebra", result.domain)
        assertTrue(result.steps.size >= 3)
        assertTrue(result.finalAnswer.contains("x_1"))
    }

    @Test
    fun testLinearEquationSolving() {
        val result = LocalMathSolver.solveLocally("3x - 9 = 15")
        assertNotNull(result)
        assertFalse(result!!.isInvalid)
        assertEquals("Algebra", result.domain)
        assertTrue(result.finalAnswer.contains("x = 8"))
    }

    @Test
    fun testDerivativeSolving() {
        val result = LocalMathSolver.solveLocally("d/dx(3x^3)")
        assertNotNull(result)
        assertFalse(result!!.isInvalid)
        assertEquals("Calculus", result.domain)
        assertTrue(result.finalAnswer.contains("9x^{2}"))
    }

    @Test
    fun testMatrixDeterminantSolving() {
        val result = LocalMathSolver.solveLocally("det([2, 5; 1, 3])")
        assertNotNull(result)
        assertFalse(result!!.isInvalid)
        assertEquals("Matrices", result.domain)
        assertTrue(result.finalAnswer.contains("1"))
    }

    @Test
    fun testBasicArithmetic() {
        val result = LocalMathSolver.solveLocally("12 + 4 * 2")
        assertNotNull(result)
        assertFalse(result!!.isInvalid)
        assertTrue(result.finalAnswer.contains("20"))
    }
}
