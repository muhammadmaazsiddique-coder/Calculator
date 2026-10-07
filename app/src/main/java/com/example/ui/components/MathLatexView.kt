package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathLatexView(
    latex: String,
    modifier: Modifier = Modifier,
    isDisplayMode: Boolean = true,
    fontSizeSp: Int = 18,
    highlight: Boolean = false,
    showCopy: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.toArgb().let {
        // Quick luminance check
        val r = (it shr 16) and 0xFF
        val g = (it shr 8) and 0xFF
        val b = it and 0xFF
        (0.299 * r + 0.587 * g + 0.114 * b) < 128
    }

    val textColorHex = if (isDark) "#E2E8F0" else "#0F172A"
    val accentColorHex = if (highlight) (if (isDark) "#818CF8" else "#4F46E5") else textColorHex

    // Clean latex input for KaTeX
    val cleanedLatex = remember(latex) {
        latex.trim()
            .removePrefix("$")
            .removeSuffix("$")
            .removePrefix("$$")
            .removeSuffix("$$")
            .replace("\\", "\\\\")
            .replace("`", "\\`")
    }

    val htmlContent = remember(cleanedLatex, isDark, fontSizeSp, highlight) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <style>
                body {
                    margin: 0;
                    padding: 4px 6px;
                    background-color: transparent;
                    color: $accentColorHex;
                    font-size: ${fontSizeSp}px;
                    display: flex;
                    align-items: center;
                    justify-content: ${if (isDisplayMode) "center" else "flex-start"};
                    overflow-x: auto;
                    font-family: sans-serif;
                }
                #math-output {
                    width: 100%;
                    text-align: ${if (isDisplayMode) "center" else "left"};
                }
                .katex-display {
                    margin: 0.2em 0 !important;
                }
                .katex {
                    color: $accentColorHex !important;
                }
            </style>
        </head>
        <body>
            <div id="math-output"></div>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    try {
                        katex.render(`$cleanedLatex`, document.getElementById("math-output"), {
                            displayMode: $isDisplayMode,
                            throwOnError: false
                        });
                    } catch (e) {
                        document.getElementById("math-output").innerText = `$cleanedLatex`;
                    }
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    var webViewError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        if (!webViewError) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 38.dp, max = 220.dp),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                webViewError = true
                            }
                        }
                        loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            )
        } else {
            // Elegant Native Fallback if WebView fails or is in text-only mode
            NativeMathFallback(
                latex = latex,
                highlight = highlight,
                fontSizeSp = fontSizeSp
            )
        }

        if (showCopy) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(latex))
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy LaTeX",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NativeMathFallback(
    latex: String,
    highlight: Boolean,
    fontSizeSp: Int
) {
    // Converts basic LaTeX tokens into readable Unicode math symbols
    val unicodeMath = remember(latex) {
        latex
            .replace("\\times", "×")
            .replace("\\div", "÷")
            .replace("\\pm", "±")
            .replace("\\mp", "∓")
            .replace("\\cdot", "·")
            .replace("\\int", "∫")
            .replace("\\sum", "∑")
            .replace("\\prod", "∏")
            .replace("\\sqrt", "√")
            .replace("\\pi", "π")
            .replace("\\theta", "θ")
            .replace("\\infty", "∞")
            .replace("\\Delta", "Δ")
            .replace("\\leq", "≤")
            .replace("\\geq", "≥")
            .replace("\\neq", "≠")
            .replace("\\approx", "≈")
            .replace("\\mathbf", "")
            .replace("\\text", "")
            .replace("{", "")
            .replace("}", "")
            .replace("\\", "")
            .replace("$", "")
    }

    Text(
        text = unicodeMath,
        fontFamily = FontFamily.Serif,
        fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
        fontSize = fontSizeSp.sp,
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
