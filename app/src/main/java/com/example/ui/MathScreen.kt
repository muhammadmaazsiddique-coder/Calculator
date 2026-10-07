package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MathSolutionResult
import com.example.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathScreen(
    viewModel: MathViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDomainFilter by viewModel.selectedDomainFilter.collectAsStateWithLifecycle()
    val explanationState by viewModel.explanationState.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HistoryDrawerContent(
                    historyList = historyList,
                    searchQuery = searchQuery,
                    onSearchChange = viewModel::onSearchQueryChanged,
                    selectedDomainFilter = selectedDomainFilter,
                    onDomainFilterChange = viewModel::onDomainFilterChanged,
                    onSelectProblem = viewModel::selectHistoryProblem,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onDeleteProblem = viewModel::deleteProblem,
                    onClearAll = viewModel::clearAllHistory,
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "∑",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                "AI Math Solver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            modifier = Modifier.testTag("history_drawer_toggle")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (historyList.isNotEmpty()) {
                                        Badge {
                                            Text(if (historyList.size > 99) "99+" else "${historyList.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.History, contentDescription = "History Sidebar")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = "Capabilities & Help")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Scrollable Area for Problem & Solutions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Input Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Enter Expression or Word Problem",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (inputText.isNotEmpty()) {
                                    Row {
                                        IconButton(
                                            onClick = viewModel::backspace,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Backspace",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = viewModel::clearInput,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputText,
                                onValueChange = viewModel::onInputTextChanged,
                                placeholder = {
                                    Text(
                                        "e.g. 2x^2 + 4x - 6 = 0, \\int \\sin(x)dx, det([1,2;3,4]), or word problems...",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("math_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                minLines = 2,
                                maxLines = 4,
                                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                            )
                        }
                    }

                    // Content Switcher based on State
                    when (val state = uiState) {
                        is MathUiState.Idle -> {
                            WelcomeDomainsOverview(
                                onSelectPreset = { preset ->
                                    viewModel.onInputTextChanged(preset)
                                    viewModel.solveCurrentProblem()
                                }
                            )
                        }

                        is MathUiState.Loading -> {
                            LoadingSolutionView()
                        }

                        is MathUiState.Success -> {
                            StepSolutionCard(
                                solution = state.solution,
                                isBookmarked = state.isBookmarked,
                                onToggleBookmark = viewModel::toggleCurrentBookmark,
                                onAskHow = {
                                    viewModel.openExplanation(state.solution)
                                }
                            )
                        }

                        is MathUiState.Error -> {
                            ErrorCard(
                                message = state.message,
                                onRetry = viewModel::solveCurrentProblem
                            )
                        }
                    }
                }

                // Docked Math Keypad
                MathKeypad(
                    onInsert = viewModel::insertKey,
                    onBackspace = viewModel::backspace,
                    onClear = viewModel::clearInput,
                    onSolve = viewModel::solveCurrentProblem
                )
            }
        }
    }

    // Explanation Bottom Sheet
    if (explanationState.isOpen && explanationState.solution != null) {
        ConceptExplanationSheet(
            solution = explanationState.solution!!,
            explanationContent = explanationState.explanationText,
            isLoading = explanationState.isLoading,
            onAskQuestion = viewModel::askExplanationQuestion,
            onDismiss = viewModel::closeExplanation
        )
    }

    // Capabilities Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("AI Math Assistant Guide")
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "This smart AI Calculator solves math step-by-step across all domains:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    DomainInfoBullet("📐 Algebra", "Linear equations, quadratics, systems, polynomials, factoring.")
                    DomainInfoBullet("📈 Calculus", "Derivatives, definite & indefinite integrals, limits, series.")
                    DomainInfoBullet("🔢 Matrices", "Determinants, matrix inverses, additions, multiplications.")
                    DomainInfoBullet("📐 Trigonometry", "Trig functions, inverse trig, identities, radians & degrees.")
                    DomainInfoBullet("➕ Arithmetic", "PEMDAS/BODMAS, fractions, powers, roots, scientific notation.")
                    DomainInfoBullet("💡 Word Problems", "Real-world physics kinematics, geometry, percentage, rate problems.")
                    DomainInfoBullet("⚠️ Error Handling", "Explains why invalid expressions (like division by zero) cannot be calculated.")
                    DomainInfoBullet("🎓 'How?' Concept Explanations", "Tap 'How?' on any problem for deep concept breakdowns and formula insights.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
private fun WelcomeDomainsOverview(
    onSelectPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Multi-Domain Math Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "Solve equations, calculus, matrices, and word problems with full intermediate steps and LaTeX formatting. Tap any example to start:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val examples = listOf(
                "Solve Quadratic" to "2x^2 + 4x - 6 = 0",
                "Derivative" to "d/dx(3x^3 - 5x + 2)",
                "Definite Integral" to "\\int_0^\\pi \\sin(x) dx",
                "Matrix Determinant" to "det([2, 5; 1, 3])",
                "Division by Zero" to "42 / 0",
                "Word Problem" to "A car travels 150 km in 3 hours. What is its average speed in m/s?"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                examples.forEach { (title, query) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPreset(query) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(query, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSolutionView() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(42.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Solving Step-by-Step...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Analyzing mathematical structure, deriving intermediate steps, and generating LaTeX formatting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(
                    "Calculation Error",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Retry Calculation")
            }
        }
    }
}

@Composable
private fun DomainInfoBullet(title: String, desc: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
