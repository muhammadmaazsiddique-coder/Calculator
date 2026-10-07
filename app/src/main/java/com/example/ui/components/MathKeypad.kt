package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KeypadCategory(val label: String) {
    BASIC("Basic"),
    ALGEBRA("Algebra"),
    CALCULUS("Calculus"),
    TRIG("Trig"),
    MATRICES("Matrix"),
    EXAMPLES("Presets")
}

@Composable
fun MathKeypad(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(KeypadCategory.BASIC) }
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("math_keypad"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Header: Category Tabs & Collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(KeypadCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                isExpanded = true
                            },
                            label = {
                                Text(
                                    category.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("keypad_tab_${category.name.lowercase()}")
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (isExpanded) "Collapse Keypad" else "Expand Keypad"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (selectedCategory) {
                        KeypadCategory.BASIC -> BasicKeypadContent(onInsert, onBackspace, onClear, onSolve)
                        KeypadCategory.ALGEBRA -> AlgebraKeypadContent(onInsert, onBackspace, onClear, onSolve)
                        KeypadCategory.CALCULUS -> CalculusKeypadContent(onInsert, onBackspace, onClear, onSolve)
                        KeypadCategory.TRIG -> TrigKeypadContent(onInsert, onBackspace, onClear, onSolve)
                        KeypadCategory.MATRICES -> MatrixKeypadContent(onInsert, onBackspace, onClear, onSolve)
                        KeypadCategory.EXAMPLES -> PresetKeypadContent(onInsert, onSolve)
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicKeypadContent(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("C", Modifier.weight(1f), isDestructive = true, onClick = onClear)
            KeyButton("(", Modifier.weight(1f)) { onInsert("(") }
            KeyButton(")", Modifier.weight(1f)) { onInsert(")") }
            KeyButton("⌫", Modifier.weight(1f), isAction = true, onClick = onBackspace)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("7", Modifier.weight(1f)) { onInsert("7") }
            KeyButton("8", Modifier.weight(1f)) { onInsert("8") }
            KeyButton("9", Modifier.weight(1f)) { onInsert("9") }
            KeyButton("÷", Modifier.weight(1f), isOperation = true) { onInsert(" / ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("4", Modifier.weight(1f)) { onInsert("4") }
            KeyButton("5", Modifier.weight(1f)) { onInsert("5") }
            KeyButton("6", Modifier.weight(1f)) { onInsert("6") }
            KeyButton("×", Modifier.weight(1f), isOperation = true) { onInsert(" * ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("1", Modifier.weight(1f)) { onInsert("1") }
            KeyButton("2", Modifier.weight(1f)) { onInsert("2") }
            KeyButton("3", Modifier.weight(1f)) { onInsert("3") }
            KeyButton("-", Modifier.weight(1f), isOperation = true) { onInsert(" - ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("0", Modifier.weight(1f)) { onInsert("0") }
            KeyButton(".", Modifier.weight(1f)) { onInsert(".") }
            KeyButton("^", Modifier.weight(1f), isOperation = true) { onInsert("^") }
            KeyButton("+", Modifier.weight(1f), isOperation = true) { onInsert(" + ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("=", Modifier.weight(1f), isOperation = true) { onInsert(" = ") }
            Button(
                onClick = onSolve,
                modifier = Modifier
                    .weight(3f)
                    .height(44.dp)
                    .testTag("solve_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("SOLVE STEP-BY-STEP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun AlgebraKeypadContent(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("x", Modifier.weight(1f)) { onInsert("x") }
            KeyButton("y", Modifier.weight(1f)) { onInsert("y") }
            KeyButton("z", Modifier.weight(1f)) { onInsert("z") }
            KeyButton("=", Modifier.weight(1f), isOperation = true) { onInsert(" = ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("x²", Modifier.weight(1f)) { onInsert("x^2") }
            KeyButton("xⁿ", Modifier.weight(1f)) { onInsert("x^") }
            KeyButton("√x", Modifier.weight(1f)) { onInsert("\\sqrt{") }
            KeyButton("a/b", Modifier.weight(1f)) { onInsert("\\frac{a}{b}") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("|x|", Modifier.weight(1f)) { onInsert("|x|") }
            KeyButton("log", Modifier.weight(1f)) { onInsert("log(") }
            KeyButton("ln", Modifier.weight(1f)) { onInsert("ln(") }
            KeyButton("eˣ", Modifier.weight(1f)) { onInsert("e^") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("±", Modifier.weight(1f)) { onInsert("\\pm ") }
            KeyButton("π", Modifier.weight(1f)) { onInsert("\\pi") }
            KeyButton("⌫", Modifier.weight(1f), isAction = true, onClick = onBackspace)
            KeyButton("SOLVE", Modifier.weight(1f), isAccent = true, onClick = onSolve)
        }
    }
}

@Composable
private fun CalculusKeypadContent(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("d/dx", Modifier.weight(1f)) { onInsert("d/dx(") }
            KeyButton("d²y/dx²", Modifier.weight(1f)) { onInsert("d^2y/dx^2") }
            KeyButton("∫", Modifier.weight(1f)) { onInsert("\\int ") }
            KeyButton("∫ₐᵇ", Modifier.weight(1f)) { onInsert("\\int_0^1 ") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("lim", Modifier.weight(1f)) { onInsert("\\lim_{x \\to 0} ") }
            KeyButton("∑", Modifier.weight(1f)) { onInsert("\\sum_{n=1}^{\\infty} ") }
            KeyButton("∞", Modifier.weight(1f)) { onInsert("\\infty") }
            KeyButton("dx", Modifier.weight(1f)) { onInsert(" dx") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("x", Modifier.weight(1f)) { onInsert("x") }
            KeyButton("t", Modifier.weight(1f)) { onInsert("t") }
            KeyButton("⌫", Modifier.weight(1f), isAction = true, onClick = onBackspace)
            KeyButton("SOLVE", Modifier.weight(1f), isAccent = true, onClick = onSolve)
        }
    }
}

@Composable
private fun TrigKeypadContent(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("sin", Modifier.weight(1f)) { onInsert("sin(") }
            KeyButton("cos", Modifier.weight(1f)) { onInsert("cos(") }
            KeyButton("tan", Modifier.weight(1f)) { onInsert("tan(") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("arcsin", Modifier.weight(1f)) { onInsert("arcsin(") }
            KeyButton("arccos", Modifier.weight(1f)) { onInsert("arccos(") }
            KeyButton("arctan", Modifier.weight(1f)) { onInsert("arctan(") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("π", Modifier.weight(1f)) { onInsert("\\pi") }
            KeyButton("θ", Modifier.weight(1f)) { onInsert("\\theta") }
            KeyButton("deg", Modifier.weight(1f)) { onInsert("°") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("⌫", Modifier.weight(1f), isAction = true, onClick = onBackspace)
            KeyButton("SOLVE", Modifier.weight(2f), isAccent = true, onClick = onSolve)
        }
    }
}

@Composable
private fun MatrixKeypadContent(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSolve: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("2×2 Matrix", Modifier.weight(1.5f)) {
                onInsert("\\begin{pmatrix} 1 & 2 \\\\ 3 & 4 \\end{pmatrix}")
            }
            KeyButton("3×3 Matrix", Modifier.weight(1.5f)) {
                onInsert("\\begin{pmatrix} 1 & 0 & 0 \\\\ 0 & 1 & 0 \\\\ 0 & 0 & 1 \\end{pmatrix}")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("det(A)", Modifier.weight(1f)) { onInsert("det(") }
            KeyButton("A⁻¹", Modifier.weight(1f)) { onInsert("^{-1}") }
            KeyButton("Aᵀ", Modifier.weight(1f)) { onInsert("^T") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KeyButton("⌫", Modifier.weight(1f), isAction = true, onClick = onBackspace)
            KeyButton("SOLVE", Modifier.weight(2f), isAccent = true, onClick = onSolve)
        }
    }
}

@Composable
private fun PresetKeypadContent(
    onInsert: (String) -> Unit,
    onSolve: () -> Unit
) {
    val presets = listOf(
        "Solve Quadratic: 2x^2 + 4x - 6 = 0" to "2x^2 + 4x - 6 = 0",
        "Linear: 3x - 9 = 15" to "3x - 9 = 15",
        "Derivative: d/dx(3x^3 - 5x + 2)" to "d/dx(3x^3 - 5x + 2)",
        "Definite Integral: \\int_0^\\pi \\sin(x) dx" to "\\int_0^\\pi \\sin(x) dx",
        "Matrix Det: det([2, 5; 1, 3])" to "det([2, 5; 1, 3])",
        "Trig Identity: sin^2(x) + cos^2(x)" to "sin^2(x) + cos^2(x)",
        "Word Problem: Train speed (120 km in 2 hrs)" to "A train travels 120 km in 2 hours. What is its speed in km/h and m/s?",
        "Invalid Test: 42 / 0 (Division by zero)" to "42 / 0"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.forEach { (label, query) ->
            OutlinedButton(
                onClick = {
                    onInsert(query)
                    onSolve()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(label, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    isOperation: Boolean = false,
    isAction: Boolean = false,
    isDestructive: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        isDestructive -> MaterialTheme.colorScheme.errorContainer
        isAccent -> MaterialTheme.colorScheme.primary
        isAction -> MaterialTheme.colorScheme.secondaryContainer
        isOperation -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isDestructive -> MaterialTheme.colorScheme.onErrorContainer
        isAccent -> MaterialTheme.colorScheme.onPrimary
        isAction -> MaterialTheme.colorScheme.onSecondaryContainer
        isOperation -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .testTag("key_${text.trim().replace("/", "_").replace("*", "_").replace(" ", "_")}"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 4) 12.sp else 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
