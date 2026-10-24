package com.example.ai

import android.util.Log
import com.example.data.model.Project
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiCodeService"

enum class ModelTier {
    HIGH_THINKING,   // gemini-4-argon with ThinkingLevel.HIGH
    GENERAL_SEARCH,  // gemini-3.8-flash with Google Search Tool
    FAST_TWEAK       // gemini-3.1-flash-lite for instant micro-fixes
}

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data class Generating(val partialText: String, val thinking: String? = null) : AiGenerationState
    data class Success(
        val explanation: String,
        val updatedHtml: String?,
        val updatedCss: String?,
        val updatedJs: String?,
        val otherCode: String? = null,
        val thinking: String? = null
    ) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

class AiCodeService {

    // Lazy initialization per Firebase AI Logic skill
    private val argonThinkingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig {
                    thinkingLevel = ThinkingLevel.HIGH
                }
            }
        )
    }

    private val flashSearchModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch())
        )
    }

    private val flashLiteModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.1-flash-lite"
        )
    }

    fun generateCode(
        userPrompt: String,
        project: Project,
        tier: ModelTier = ModelTier.GENERAL_SEARCH,
        isDiagnosticFix: Boolean = false
    ): Flow<AiGenerationState> = flow {
        emit(AiGenerationState.Generating(partialText = "Thinking and formulating solution..."))

        val selectedModel = when (tier) {
            ModelTier.HIGH_THINKING -> argonThinkingModel
            ModelTier.GENERAL_SEARCH -> flashSearchModel
            ModelTier.FAST_TWEAK -> flashLiteModel
        }

        val promptPayload = buildPrompt(userPrompt, project, isDiagnosticFix)

        val fullTextBuilder = StringBuilder()
        var thinkingExtracted: String? = null

        try {
            withTimeout(90_000L) {
                selectedModel.generateContentStream(promptPayload).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    fullTextBuilder.append(text)
                    emit(AiGenerationState.Generating(partialText = fullTextBuilder.toString()))
                }
            }

            val rawResult = fullTextBuilder.toString()
            if (rawResult.isBlank()) {
                emit(AiGenerationState.Error("Model returned an empty response. Please retry."))
                return@flow
            }

            // Parse response into explanation and code blocks
            val parsed = parseCodeResponse(rawResult, project)
            emit(
                AiGenerationState.Success(
                    explanation = parsed.explanation,
                    updatedHtml = parsed.html,
                    updatedCss = parsed.css,
                    updatedJs = parsed.js,
                    otherCode = parsed.other,
                    thinking = thinkingExtracted
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Code generation timed out", e)
            emit(AiGenerationState.Error("Code generation timed out after 90 seconds. Try a simpler prompt or switch to Fast mode."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            emit(AiGenerationState.Error(e.localizedMessage ?: "Failed to generate code with AI."))
        }
    }

    private fun buildPrompt(userPrompt: String, project: Project, isDiagnosticFix: Boolean): String {
        return """
You are CodeForge AI, an expert senior web architect and multi-language software engineer.
The user wants you to build, modify, or fix a project by conversational instruction.

Current Project:
Title: "${project.title}"
Language: "${project.activeLanguage}"

Existing HTML:
```html
${project.htmlContent.take(20000)}
```

Existing CSS:
```css
${project.cssContent.take(15000)}
```

Existing JavaScript:
```javascript
${project.jsContent.take(20000)}
```

${if (isDiagnosticFix) "DIAGNOSTIC TASK: Fix this runtime error and prevent recurrence:\n$userPrompt" else "USER INSTRUCTION: $userPrompt"}

INSTRUCTIONS:
1. Provide a concise explanation of what changes you made.
2. Return complete, working code inside Markdown code blocks:
   - For HTML: Use ```html ... ``` (include markup, Tailwind utility classes, or SVG components)
   - For CSS: Use ```css ... ```
   - For JavaScript: Use ```javascript ... ``` (ensure clean syntax, DOM event listeners, and console logging)
   - If writing Python or Java, use ```python ... ``` or ```java ... ```
3. Ensure the web application is modern, responsive, animated, beautiful, and directly runnable in a browser WebView.
        """.trimIndent()
    }

    private data class ParsedResult(
        val explanation: String,
        val html: String?,
        val css: String?,
        val js: String?,
        val other: String?
    )

    private fun parseCodeResponse(rawText: String, currentProject: Project): ParsedResult {
        var html: String? = null
        var css: String? = null
        var js: String? = null
        var other: String? = null

        val explanationBuilder = StringBuilder()
        val lines = rawText.lines()
        var inCodeBlock = false
        var currentLang = ""
        val currentBlock = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Ending code block
                    val content = currentBlock.toString().trim()
                    when (currentLang.lowercase()) {
                        "html" -> html = content
                        "css" -> css = content
                        "js", "javascript" -> js = content
                        "python", "py", "java" -> other = content
                        else -> {
                            // Detect by content if language tag was generic
                            if (content.contains("<div") || content.contains("<html") || content.contains("<body") || content.contains("<button")) {
                                html = content
                            } else if (content.contains("function ") || content.contains("const ") || content.contains("let ") || content.contains("document.")) {
                                js = content
                            } else if (content.contains("{") && content.contains(":") && content.contains(";")) {
                                css = content
                            }
                        }
                    }
                    currentBlock.clear()
                    inCodeBlock = false
                    currentLang = ""
                } else {
                    // Starting code block
                    inCodeBlock = true
                    currentLang = trimmed.removePrefix("```").trim()
                }
            } else {
                if (inCodeBlock) {
                    currentBlock.append(line).append("\n")
                } else {
                    explanationBuilder.append(line).append("\n")
                }
            }
        }

        val explanation = explanationBuilder.toString().trim().ifBlank {
            "Updated application code based on your instruction."
        }

        return ParsedResult(
            explanation = explanation,
            html = html,
            css = css,
            js = js,
            other = other
        )
    }
}
