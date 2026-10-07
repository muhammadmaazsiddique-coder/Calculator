package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiMathService
import com.example.data.db.MathDatabase
import com.example.data.model.MathProblem
import com.example.data.model.MathSolutionResult
import com.example.data.repository.MathRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface MathUiState {
    object Idle : MathUiState
    object Loading : MathUiState
    data class Success(
        val solution: MathSolutionResult,
        val savedProblemId: Long = 0,
        val isBookmarked: Boolean = false
    ) : MathUiState
    data class Error(val message: String) : MathUiState
}

data class ExplanationState(
    val isOpen: Boolean = false,
    val solution: MathSolutionResult? = null,
    val explanationText: String = "",
    val isLoading: Boolean = false
)

class MathViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MathDatabase.getInstance(application)
    private val repository = MathRepository(database.mathDao())
    private val geminiService = GeminiMathService()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _uiState = MutableStateFlow<MathUiState>(MathUiState.Idle)
    val uiState: StateFlow<MathUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDomainFilter = MutableStateFlow("All")
    val selectedDomainFilter: StateFlow<String> = _selectedDomainFilter.asStateFlow()

    private val _explanationState = MutableStateFlow(ExplanationState())
    val explanationState: StateFlow<ExplanationState> = _explanationState.asStateFlow()

    val historyList: StateFlow<List<MathProblem>> = combine(
        repository.allHistory,
        _searchQuery,
        _selectedDomainFilter
    ) { all, query, domainFilter ->
        var filtered = all
        if (domainFilter == "Starred") {
            filtered = filtered.filter { it.isBookmarked }
        } else if (domainFilter != "All") {
            filtered = filtered.filter { it.domain.equals(domainFilter, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.rawQuery.contains(query, ignoreCase = true) ||
                        it.finalAnswer.contains(query, ignoreCase = true) ||
                        it.domain.contains(query, ignoreCase = true)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun insertKey(symbol: String) {
        _inputText.value = _inputText.value + symbol
    }

    fun backspace() {
        val cur = _inputText.value
        if (cur.isNotEmpty()) {
            _inputText.value = cur.dropLast(1)
        }
    }

    fun clearInput() {
        _inputText.value = ""
        _uiState.value = MathUiState.Idle
    }

    fun solveCurrentProblem() {
        val query = _inputText.value.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = MathUiState.Loading
            try {
                val solution = geminiService.solveMath(query)

                // Save into Room database
                val id = repository.saveProblem(
                    rawQuery = query,
                    inputLatex = solution.inputExpression,
                    domain = solution.domain,
                    steps = solution.steps,
                    finalAnswer = solution.finalAnswer,
                    conceptExplanation = solution.conceptExplanation,
                    isInvalid = solution.isInvalid,
                    invalidReason = solution.invalidReason
                )

                _uiState.value = MathUiState.Success(
                    solution = solution,
                    savedProblemId = id,
                    isBookmarked = false
                )
            } catch (e: Exception) {
                _uiState.value = MathUiState.Error(e.localizedMessage ?: "Failed to calculate solution")
            }
        }
    }

    fun selectHistoryProblem(problem: MathProblem) {
        val steps = repository.parseSteps(problem.stepsJson)
        val solution = MathSolutionResult(
            inputExpression = problem.inputLatex,
            domain = problem.domain,
            steps = steps,
            finalAnswer = problem.finalAnswer,
            conceptExplanation = problem.conceptExplanation,
            isInvalid = problem.isInvalid,
            invalidReason = problem.invalidReason
        )
        _inputText.value = problem.rawQuery
        _uiState.value = MathUiState.Success(
            solution = solution,
            savedProblemId = problem.id,
            isBookmarked = problem.isBookmarked
        )
    }

    fun toggleCurrentBookmark() {
        val current = _uiState.value
        if (current is MathUiState.Success) {
            viewModelScope.launch {
                val problem = repository.getProblemById(current.savedProblemId)
                if (problem != null) {
                    repository.toggleBookmark(problem)
                    _uiState.value = current.copy(isBookmarked = !current.isBookmarked)
                }
            }
        }
    }

    fun toggleBookmark(problem: MathProblem) {
        viewModelScope.launch {
            repository.toggleBookmark(problem)
            val current = _uiState.value
            if (current is MathUiState.Success && current.savedProblemId == problem.id) {
                _uiState.value = current.copy(isBookmarked = !problem.isBookmarked)
            }
        }
    }

    fun deleteProblem(id: Long) {
        viewModelScope.launch {
            repository.deleteProblem(id)
            val current = _uiState.value
            if (current is MathUiState.Success && current.savedProblemId == id) {
                _uiState.value = current.copy(savedProblemId = 0)
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onDomainFilterChanged(filter: String) {
        _selectedDomainFilter.value = filter
    }

    fun openExplanation(solution: MathSolutionResult) {
        _explanationState.value = ExplanationState(
            isOpen = true,
            solution = solution,
            explanationText = solution.conceptExplanation,
            isLoading = false
        )
    }

    fun closeExplanation() {
        _explanationState.value = _explanationState.value.copy(isOpen = false)
    }

    fun askExplanationQuestion(question: String) {
        val sol = _explanationState.value.solution ?: return
        viewModelScope.launch {
            _explanationState.value = _explanationState.value.copy(isLoading = true)
            val answer = geminiService.explainConceptDeep(sol, question)
            _explanationState.value = _explanationState.value.copy(
                explanationText = answer,
                isLoading = false
            )
        }
    }
}
