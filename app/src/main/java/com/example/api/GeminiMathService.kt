package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.MathSolutionResult
import com.example.data.model.MathStep
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiMathResponseDto(
    val inputExpression: String = "",
    val domain: String = "Algebra",
    val steps: List<GeminiStepDto> = emptyList(),
    val finalAnswer: String = "",
    val conceptExplanation: String = "",
    val isInvalid: Boolean = false,
    val invalidReason: String = ""
)

@JsonClass(generateAdapter = true)
data class GeminiStepDto(
    val stepNumber: Int = 1,
    val title: String = "",
    val explanation: String = "",
    val latex: String = "",
    val tip: String? = null
)

class GeminiMathService {
    private val tag = "GeminiMathService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter = moshi.adapter(GeminiMathResponseDto::class.java)

    suspend fun solveMath(problemInput: String): MathSolutionResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Check if API key is present and not the dummy placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Attempt local solver first
            val localResult = LocalMathSolver.solveLocally(problemInput)
            if (localResult != null) {
                return@withContext localResult
            }
            // If local solver couldn't handle advanced problem, notify key is needed
            return@withContext MathSolutionResult(
                inputExpression = problemInput,
                domain = "General",
                steps = listOf(
                    MathStep(
                        stepNumber = 1,
                        title = "Offline Math Solver",
                        explanation = "Local engine attempted the expression. To unlock advanced multi-step calculus, word problems, and high-order matrices via AI, please add your Gemini API key in the AI Studio Secrets panel.",
                        latex = "\\text{Configure GEMINI\\_API\\_KEY in Secrets}",
                        tip = "Basic arithmetic, quadratics, linear equations, 2x2 matrices, and division-by-zero are supported offline."
                    )
                ),
                finalAnswer = "\\text{\\textbf{Configure Gemini API Key in Secrets}}",
                conceptExplanation = "AI-powered math reasoning requires an active GEMINI_API_KEY. Once added, Gemini will solve multi-domain calculus, systems of equations, matrix operations, and complex word problems step-by-step.",
                isInvalid = true,
                invalidReason = "API key not configured in AI Studio Secrets panel."
            )
        }

        try {
            val systemPrompt = """
                You are an expert AI Calculator and Math Assistant.
                Your task is to solve mathematical expressions, equations, word problems, calculus, matrices, trigonometry, and arithmetic step-by-step.
                
                CRITICAL RULES:
                1. Accuracy: Solve the math correctly step-by-step before showing the final result.
                2. Structured Output:
                   - Input Expression / Problem: Repeat the user query cleanly in LaTeX.
                   - Step-by-step Solution: Show clear, atomic intermediate steps.
                   - Final Answer: Highlight the final result clearly in bold LaTeX (e.g. \mathbf{x = 4} or \mathbf{42}).
                3. Multi-domain Support: Handle basic arithmetic, algebra, calculus, matrices, trigonometry, and word problems.
                4. Explanations: Explain the math concepts or formulas used in simple terms.
                5. Error Handling: If an expression is mathematically invalid (e.g., division by zero, square root of negative in real numbers if not specified complex, undefined matrix operation, inconsistent system), set isInvalid=true, explain why it cannot be calculated in invalidReason and steps, and show an undefined/invalid final answer.
                6. User Interface: Use LaTeX formatting for all mathematical expressions (including fractions, roots, matrices, integrals, sums, exponents).
                
                You must return STRICT JSON matching this schema:
                {
                   "inputExpression": "clean LaTeX expression or problem statement",
                   "domain": "Arithmetic" | "Algebra" | "Calculus" | "Matrices" | "Trigonometry" | "Word Problem",
                   "steps": [
                      {
                         "stepNumber": 1,
                         "title": "Short title of step",
                         "explanation": "Clear explanation of this step in simple terms",
                         "latex": "LaTeX formula for this step",
                         "tip": "Helpful rule or tip (optional)"
                      }
                   ],
                   "finalAnswer": "bold LaTeX final answer like \\mathbf{...}",
                   "conceptExplanation": "Clear, accessible explanation of the underlying mathematical concepts, theorems, or formulas used",
                   "isInvalid": false,
                   "invalidReason": ""
                }
            """.trimIndent()

            // Try primary model gemini-3.8-flash, fallback to gemini-3.5-flash
            val modelsToTry = listOf("gemini-3.8-flash", "gemini-3.5-flash")
            var lastException: Exception? = null

            for (model in modelsToTry) {
                try {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                    val jsonBody = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().put("text", "Solve this mathematical query:\n$problemInput"))
                                })
                            })
                        })
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", systemPrompt))
                            })
                        })
                        put("generationConfig", JSONObject().apply {
                            put("responseMimeType", "application/json")
                            put("temperature", 0.1)
                        })
                    }

                    val request = Request.Builder()
                        .url(url)
                        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        Log.w(tag, "Model $model returned error ${response.code}: $responseStr")
                        lastException = RuntimeException("Model $model error: ${response.code} $responseStr")
                        continue
                    }

                    val rootObj = JSONObject(responseStr)
                    val candidates = rootObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCand = candidates.getJSONObject(0)
                        val content = firstCand.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text") ?: ""

                        val parsedDto = responseAdapter.fromJson(text)
                        if (parsedDto != null) {
                            return@withContext MathSolutionResult(
                                inputExpression = parsedDto.inputExpression.ifBlank { problemInput },
                                domain = parsedDto.domain.ifBlank { "Algebra" },
                                steps = parsedDto.steps.map {
                                    MathStep(
                                        stepNumber = it.stepNumber,
                                        title = it.title,
                                        explanation = it.explanation,
                                        latex = it.latex,
                                        tip = it.tip
                                    )
                                },
                                finalAnswer = parsedDto.finalAnswer,
                                conceptExplanation = parsedDto.conceptExplanation,
                                isInvalid = parsedDto.isInvalid,
                                invalidReason = parsedDto.invalidReason
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Failed with model $model: ${e.message}")
                    lastException = e
                }
            }

            // If API calls failed, fallback to local solver
            val localResult = LocalMathSolver.solveLocally(problemInput)
            if (localResult != null) return@withContext localResult

            throw lastException ?: RuntimeException("Unable to solve expression")
        } catch (e: Exception) {
            Log.e(tag, "solveMath failed", e)
            val localFallback = LocalMathSolver.solveLocally(problemInput)
            if (localFallback != null) {
                return@withContext localFallback
            }

            return@withContext MathSolutionResult(
                inputExpression = problemInput,
                domain = "Error",
                steps = listOf(
                    MathStep(
                        stepNumber = 1,
                        title = "Calculation Error",
                        explanation = "An error occurred while calculating the result: ${e.localizedMessage}",
                        latex = "\\text{Error: } ${e.message?.take(50) ?: "Unknown"}",
                        tip = "Please verify the expression syntax or your internet connection."
                    )
                ),
                finalAnswer = "\\text{\\textbf{Calculation Error}}",
                conceptExplanation = "The system encountered an error connecting to the solver service.",
                isInvalid = true,
                invalidReason = e.localizedMessage ?: "Network or parsing error"
            )
        }
    }

    suspend fun explainConceptDeep(
        problem: MathSolutionResult,
        userQuestion: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext problem.conceptExplanation.ifBlank {
                "This problem relies on fundamental properties of ${problem.domain}. Formulas and inverse operations are applied systematically to balance expressions."
            }
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"
            val prompt = """
                The student is reviewing this math problem:
                Expression: ${problem.inputExpression}
                Domain: ${problem.domain}
                Final Answer: ${problem.finalAnswer}
                Steps:
                ${problem.steps.joinToString("\n") { "Step ${it.stepNumber} [${it.title}]: ${it.explanation} (${it.latex})" }}
                
                The student asks: "$userQuestion"
                
                Please answer like an inspiring, crystal-clear math tutor. 
                Explain the underlying concepts, intuition, theorems, and formulas in simple, intuitive terms. 
                Use LaTeX formatting for all mathematical expressions ($...$ or $$...$$).
                Keep the tone warm, clear, and educational.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""
            val rootObj = JSONObject(responseStr)
            val text = rootObj.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            return@withContext text ?: problem.conceptExplanation
        } catch (e: Exception) {
            return@withContext problem.conceptExplanation
        }
    }
}
