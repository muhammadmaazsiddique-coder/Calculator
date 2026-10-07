package com.example.api

import com.example.data.model.MathSolutionResult
import com.example.data.model.MathStep
import kotlin.math.*

object LocalMathSolver {

    fun solveLocally(query: String): MathSolutionResult? {
        val trimmed = query.trim()
        val cleaned = trimmed.replace(" ", "")

        // Check for division by zero explicitly
        if (isDivisionByZero(cleaned)) {
            return MathSolutionResult(
                inputExpression = formatLatexQuery(trimmed),
                domain = "Arithmetic",
                steps = listOf(
                    MathStep(
                        stepNumber = 1,
                        title = "Analyze Denominator",
                        explanation = "Identify the divisor (denominator) in the expression.",
                        latex = "\\frac{a}{0}",
                        tip = "Division by zero is undefined in mathematics."
                    ),
                    MathStep(
                        stepNumber = 2,
                        title = "Mathematical Impossibility",
                        explanation = "Dividing a number a by b asks for a number x such that b * x = a. If b = 0, then 0 * x = 0 for all real numbers. Thus no unique finite solution exists when a != 0, and infinite solutions exist when a = 0.",
                        latex = "0 \\cdot x \\neq a \\quad (\\text{for } a \\neq 0)",
                        tip = "Limits approaching 0 may yield \\pm\\infty, but the arithmetic operation itself is strictly undefined."
                    )
                ),
                finalAnswer = "\\mathbf{\\text{Undefined (Division by Zero)}}",
                conceptExplanation = "In standard arithmetic and real analysis, division by zero is strictly undefined. When a quantity is divided by progressively smaller positive numbers, the quotient grows without bound towards positive infinity; when divided by negative numbers approaching zero, it decreases towards negative infinity. Because there is no single finite value, the expression cannot be calculated.",
                isInvalid = true,
                invalidReason = "Cannot divide by zero. In real arithmetic, division by zero produces an undefined result."
            )
        }

        // Try quadratic equation: e.g. 2x^2 + 4x - 6 = 0 or x^2 - 5x + 6 = 0
        val quadraticResult = trySolveQuadratic(trimmed)
        if (quadraticResult != null) return quadraticResult

        // Try linear equation: e.g. 2x + 5 = 15 or 3x - 9 = 0
        val linearResult = trySolveLinear(trimmed)
        if (linearResult != null) return linearResult

        // Try basic derivative: e.g. d/dx(3x^2) or d/dx(x^3)
        val derivativeResult = trySolveDerivative(trimmed)
        if (derivativeResult != null) return derivativeResult

        // Try 2x2 matrix determinant
        val matrixResult = trySolveMatrix(trimmed)
        if (matrixResult != null) return matrixResult

        // Try arithmetic expression
        val arithmeticResult = trySolveArithmetic(trimmed)
        if (arithmeticResult != null) return arithmeticResult

        return null
    }

    private fun isDivisionByZero(expr: String): Boolean {
        if (Regex("/0(\\D|$)").containsMatchIn(expr) && !Regex("/0\\.\\d+").containsMatchIn(expr)) {
            return true
        }
        if (expr.contains("/(0)") || expr.contains("/(0.0)") || (expr.contains("\\frac{") && expr.contains("}{0}"))) {
            return true
        }
        return false
    }

    private fun formatLatexQuery(query: String): String {
        return query
            .replace("*", " \\times ")
            .replace("/", " \\div ")
    }

    private fun trySolveQuadratic(query: String): MathSolutionResult? {
        val clean = query.replace(" ", "").lowercase()
        val regex = Regex("^([+-]?\\d*\\.?\\d*)x\\^2([+-]\\d*\\.?\\d*)x([+-]\\d*\\.?\\d*)=0$")
        val match = regex.find(clean) ?: return null

        val aStr = match.groupValues[1]
        val bStr = match.groupValues[2]
        val cStr = match.groupValues[3]

        val a = when (aStr) {
            "", "+" -> 1.0
            "-" -> -1.0
            else -> aStr.toDoubleOrNull() ?: 1.0
        }
        val b = when (bStr) {
            "", "+" -> 1.0
            "-" -> -1.0
            else -> bStr.toDoubleOrNull() ?: 0.0
        }
        val c = cStr.toDoubleOrNull() ?: 0.0

        val discriminant = b * b - 4 * a * c
        val steps = mutableListOf<MathStep>()

        steps.add(
            MathStep(
                stepNumber = 1,
                title = "Identify Coefficients",
                explanation = "Identify the quadratic coefficients in standard form ax^2 + bx + c = 0.",
                latex = "a = $a, \\quad b = $b, \\quad c = $c",
                tip = "Standard quadratic equation form."
            )
        )

        steps.add(
            MathStep(
                stepNumber = 2,
                title = "Calculate Discriminant",
                explanation = "Use the discriminant formula \\Delta = b^2 - 4ac to determine the nature of the roots.",
                latex = "\\Delta = ($b)^2 - 4($a)($c) = ${b * b} - ${4 * a * c} = $discriminant",
                tip = if (discriminant > 0) "Two distinct real roots." else if (discriminant == 0.0) "One repeated real root." else "Two complex conjugate roots."
            )
        )

        val finalAns: String
        if (discriminant >= 0) {
            val sqrtD = sqrt(discriminant)
            val x1 = (-b + sqrtD) / (2 * a)
            val x2 = (-b - sqrtD) / (2 * a)

            steps.add(
                MathStep(
                    stepNumber = 3,
                    title = "Apply Quadratic Formula",
                    explanation = "Substitute values into the quadratic formula: x = (-b \\pm \\sqrt{\\Delta}) / (2a).",
                    latex = "x = \\frac{-($b) \\pm \\sqrt{$discriminant}}{2($a)} = \\frac{${-b} \\pm ${formatNum(sqrtD)}}{${2 * a}}",
                    tip = "Solve for both plus and minus signs."
                )
            )

            finalAns = if (x1 == x2) {
                "\\mathbf{x = ${formatNum(x1)}}"
            } else {
                "\\mathbf{x_1 = ${formatNum(x1)}, \\quad x_2 = ${formatNum(x2)}}"
            }
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-discriminant) / (2 * a)
            steps.add(
                MathStep(
                    stepNumber = 3,
                    title = "Complex Roots Formulation",
                    explanation = "Since the discriminant is negative (\\Delta < 0), the roots are complex conjugate pairs with imaginary unit i.",
                    latex = "x = \\frac{${-b} \\pm i\\sqrt{${-discriminant}}}{${2 * a}}",
                    tip = "Use imaginary number i where i^2 = -1."
                )
            )
            finalAns = "\\mathbf{x = ${formatNum(realPart)} \\pm ${formatNum(abs(imagPart))}i}"
        }

        return MathSolutionResult(
            inputExpression = "${aStr.ifEmpty { "1" }}x^2 ${bStr}x $cStr = 0",
            domain = "Algebra",
            steps = steps,
            finalAnswer = finalAns,
            conceptExplanation = "Quadratic equations are second-order polynomial equations. The quadratic formula is derived by completing the square on the general form ax^2 + bx + c = 0. The discriminant b^2 - 4ac reveals whether the parabola intersects the x-axis twice, once, or never.",
            isInvalid = false
        )
    }

    private fun trySolveLinear(query: String): MathSolutionResult? {
        val clean = query.replace(" ", "").lowercase()
        val regex = Regex("^([+-]?\\d*\\.?\\d*)x([+-]\\d*\\.?\\d*)=([+-]?\\d*\\.?\\d*)$")
        val match = regex.find(clean) ?: return null

        val aStr = match.groupValues[1]
        val bStr = match.groupValues[2]
        val cStr = match.groupValues[3]

        val a = when (aStr) {
            "", "+" -> 1.0
            "-" -> -1.0
            else -> aStr.toDoubleOrNull() ?: return null
        }
        val b = bStr.toDoubleOrNull() ?: return null
        val c = cStr.toDoubleOrNull() ?: return null

        if (a == 0.0) {
            return if (b == c) {
                MathSolutionResult(
                    inputExpression = query,
                    domain = "Algebra",
                    steps = listOf(
                        MathStep(1, "Simplify Equation", "All variable terms cancel out, leaving a true identity: $b = $c.", "$b = $c")
                    ),
                    finalAnswer = "\\mathbf{\\text{Infinite Solutions (Identity)}}",
                    conceptExplanation = "When the variable term cancels out and leaves an equality c = c, the equation is true for all real values of x."
                )
            } else {
                MathSolutionResult(
                    inputExpression = query,
                    domain = "Algebra",
                    steps = listOf(
                        MathStep(1, "Analyze Equation", "Variable terms cancel out, leaving a false statement: $b != $c.", "$b \\neq $c")
                    ),
                    finalAnswer = "\\mathbf{\\text{No Solution (Contradiction)}}",
                    conceptExplanation = "Since b != c, there is no value of x that can satisfy the equality."
                )
            }
        }

        val rhsAfterSubtract = c - b
        val x = rhsAfterSubtract / a

        val steps = listOf(
            MathStep(
                stepNumber = 1,
                title = "Isolate Variable Term",
                explanation = "Subtract $b from both sides of the equation.",
                latex = "${formatNum(a)}x = ${formatNum(c)} - (${formatNum(b)}) = ${formatNum(rhsAfterSubtract)}",
                tip = "Inverse operation of addition is subtraction."
            ),
            MathStep(
                stepNumber = 2,
                title = "Divide by Coefficient",
                explanation = "Divide both sides by the coefficient of x.",
                latex = "x = \\frac{${formatNum(rhsAfterSubtract)}}{${formatNum(a)}} = ${formatNum(x)}",
                tip = "Isolating x yields the single solution."
            )
        )

        return MathSolutionResult(
            inputExpression = "${formatNum(a)}x ${if (b >= 0) "+ $b" else "- ${abs(b)}"} = ${formatNum(c)}",
            domain = "Algebra",
            steps = steps,
            finalAnswer = "\\mathbf{x = ${formatNum(x)}}",
            conceptExplanation = "A linear equation in one variable has the general form ax + b = c. By applying the addition and multiplication properties of equality to maintain balance on both sides, we isolate the variable to determine the unique solution.",
            isInvalid = false
        )
    }

    private fun trySolveDerivative(query: String): MathSolutionResult? {
        val clean = query.replace(" ", "").lowercase()
        if (!clean.startsWith("d/dx") && !clean.contains("derivative")) return null

        var inner = clean.removePrefix("d/dx").removePrefix("derivativeof").removePrefix("derivative")
        if (inner.startsWith("(") && inner.endsWith(")")) {
            inner = inner.substring(1, inner.length - 1)
        }

        if (inner == "sin(x)" || inner == "sinx") {
            return MathSolutionResult(
                inputExpression = "\\frac{d}{dx}[\\sin(x)]",
                domain = "Calculus",
                steps = listOf(
                    MathStep(1, "Apply Trigonometric Derivative Rule", "Recall the fundamental trigonometric derivative rule: d/dx[sin(x)] = cos(x).", "\\frac{d}{dx}[\\sin(x)] = \\cos(x)", "Trigonometric differentiation identity.")
                ),
                finalAnswer = "\\mathbf{\\cos(x)}",
                conceptExplanation = "The derivative represents the instantaneous rate of change or the slope of the tangent line. For sin(x), as x increases, the slope oscillates smoothly with values exactly equal to cos(x)."
            )
        }

        if (inner == "cos(x)" || inner == "cosx") {
            return MathSolutionResult(
                inputExpression = "\\frac{d}{dx}[\\cos(x)]",
                domain = "Calculus",
                steps = listOf(
                    MathStep(1, "Apply Trigonometric Derivative Rule", "Recall that d/dx[cos(x)] = -sin(x).", "\\frac{d}{dx}[\\cos(x)] = -\\sin(x)", "Notice the negative sign.")
                ),
                finalAnswer = "\\mathbf{-\\sin(x)}",
                conceptExplanation = "Differentiating cos(x) produces -sin(x) because the cosine curve starts at a peak (slope 0) and slopes downwards into negative values."
            )
        }

        val powerRegex = Regex("^([+-]?\\d*\\.?\\d*)x\\^(\\d+)$")
        val match = powerRegex.find(inner)
        if (match != null) {
            val coeffStr = match.groupValues[1]
            val powerStr = match.groupValues[2]
            val coeff = when (coeffStr) {
                "", "+" -> 1.0
                "-" -> -1.0
                else -> coeffStr.toDoubleOrNull() ?: 1.0
            }
            val power = powerStr.toIntOrNull() ?: 1

            val newCoeff = coeff * power
            val newPower = power - 1

            val resultLatex = when {
                newPower == 0 -> "${formatNum(newCoeff)}"
                newPower == 1 -> "${formatNum(newCoeff)}x"
                else -> "${formatNum(newCoeff)}x^{$newPower}"
            }

            return MathSolutionResult(
                inputExpression = "\\frac{d}{dx}[${inner}]",
                domain = "Calculus",
                steps = listOf(
                    MathStep(
                        stepNumber = 1,
                        title = "Apply Power Rule",
                        explanation = "The Power Rule states that d/dx[c * x^n] = c * n * x^{n-1}.",
                        latex = "\\frac{d}{dx}[${formatNum(coeff)}x^{$power}] = ${formatNum(coeff)} \\cdot $power \\cdot x^{$power - 1}",
                        tip = "Multiply by the exponent, then subtract 1 from the exponent."
                    ),
                    MathStep(
                        stepNumber = 2,
                        title = "Simplify Coefficients",
                        explanation = "Multiply ${formatNum(coeff)} by $power to get $newCoeff, and reduce the power to $newPower.",
                        latex = "= $resultLatex",
                        tip = "Final differentiated polynomial."
                    )
                ),
                finalAnswer = "\\mathbf{$resultLatex}",
                conceptExplanation = "The Power Rule is one of the most foundational shortcuts in calculus, derived directly from the limit definition of a derivative: f'(x) = lim_{h -> 0} (f(x+h) - f(x))/h. Applying binomial expansion reveals that the first order term in h has coefficient n * x^{n-1}."
            )
        }

        return null
    }

    private fun trySolveMatrix(query: String): MathSolutionResult? {
        val clean = query.replace(" ", "").lowercase()
        if (clean.contains("det") || clean.contains("matrix")) {
            val nums = Regex("[-+]?\\d+\\.?\\d*").findAll(query).mapNotNull { it.value.toDoubleOrNull() }.toList()
            if (nums.size == 4) {
                val a = nums[0]
                val b = nums[1]
                val c = nums[2]
                val d = nums[3]

                val det = a * d - b * c
                val steps = listOf(
                    MathStep(
                        stepNumber = 1,
                        title = "Setup 2x2 Matrix",
                        explanation = "Represent the matrix in standard 2x2 form.",
                        latex = "A = \\begin{pmatrix} ${formatNum(a)} & ${formatNum(b)} \\\\ ${formatNum(c)} & ${formatNum(d)} \\end{pmatrix}",
                        tip = "Elements: a=${formatNum(a)}, b=${formatNum(b)}, c=${formatNum(c)}, d=${formatNum(d)}"
                    ),
                    MathStep(
                        stepNumber = 2,
                        title = "Apply Determinant Formula",
                        explanation = "The determinant of a 2x2 matrix is computed by subtracting the product of off-diagonals from the main diagonal: det(A) = ad - bc.",
                        latex = "\\det(A) = (${formatNum(a)})(${formatNum(d)}) - (${formatNum(b)})(${formatNum(c)}) = ${formatNum(a * d)} - ${formatNum(b * c)} = ${formatNum(det)}",
                        tip = "Cross-multiplication rule for 2x2 matrices."
                    )
                )

                return MathSolutionResult(
                    inputExpression = "\\det \\begin{pmatrix} ${formatNum(a)} & ${formatNum(b)} \\\\ ${formatNum(c)} & ${formatNum(d)} \\end{pmatrix}",
                    domain = "Matrices",
                    steps = steps,
                    finalAnswer = "\\mathbf{\\det(A) = ${formatNum(det)}}",
                    conceptExplanation = "The determinant is a scalar value that characterizes a square matrix. Geometrically, for a 2x2 matrix, the absolute value of the determinant represents the area of the parallelogram formed by the row or column vectors. A non-zero determinant implies the matrix is invertible."
                )
            }
        }
        return null
    }

    private fun trySolveArithmetic(query: String): MathSolutionResult? {
        val sanitized = query
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        return try {
            val parser = ArithmeticParser(sanitized)
            val result = parser.parse()
            val steps = mutableListOf<MathStep>()

            steps.add(
                MathStep(
                    stepNumber = 1,
                    title = "Parse Order of Operations",
                    explanation = "Follow PEMDAS/BODMAS: Parentheses, Exponents, Multiplication & Division, Addition & Subtraction.",
                    latex = query.replace("*", " \\times ").replace("/", " \\div "),
                    tip = "Always simplify innermost groupings first."
                )
            )

            steps.add(
                MathStep(
                    stepNumber = 2,
                    title = "Compute Result",
                    explanation = "Evaluate all intermediate binary operations step by step.",
                    latex = "= ${formatNum(result)}",
                    tip = "Calculation verified."
                )
            )

            MathSolutionResult(
                inputExpression = formatLatexQuery(query),
                domain = "Arithmetic",
                steps = steps,
                finalAnswer = "\\mathbf{${formatNum(result)}}",
                conceptExplanation = "Standard arithmetic evaluates expressions adhering strictly to operator precedence conventions (BODMAS/PEMDAS). This guarantees every mathematical expression evaluates to an unambiguous single value.",
                isInvalid = false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun formatNum(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }

    private class ArithmeticParser(private val expr: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            pos++
            ch = if (pos < expr.length) expr[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val res = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return res
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else {
                while (ch in 'a'.code..'z'.code) nextChar()
                val func = expr.substring(startPos, pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        "ln" -> ln(x)
                        "log" -> log10(x)
                        "abs" -> abs(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
            }

            if (eat('^'.code)) x = x.pow(parseFactor())

            return x
        }
    }
}
