package com.selfjourney.service

import com.selfjourney.domain.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GeminiService {
    private val apiKey = System.getenv("GEMINI_API_KEY")
        ?: throw IllegalStateException("GEMINI_API_KEY environment variable is not set")
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun analyzeUserAnswers(
        answerHistory: List<AnswerHistoryItem>,
        idealPersonDescription: String?
    ): ParsedAIAnalysis? {
        try {
            val prompt = buildPrompt(answerHistory, idealPersonDescription)
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )

            val response: GeminiResponse = client.post(apiUrl) {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            if (response.error != null) {
                println("Gemini API error: ${response.error.message}")
                return null
            }

            val generatedText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            return if (generatedText != null) {
                parseAnalysisResponse(generatedText)
            } else {
                println("No response text from Gemini")
                null
            }
        } catch (e: Exception) {
            println("Error calling Gemini API: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun buildPrompt(answerHistory: List<AnswerHistoryItem>, idealPersonDescription: String?): String {
        val answersText = answerHistory.joinToString("\n\n") { item ->
            "질문: ${item.questionContent}\n답변: ${item.content}\n날짜: ${item.createdAt}"
        }

        val idealPersonContext = if (!idealPersonDescription.isNullOrBlank()) {
            "\n\n사용자의 목표 인물상: $idealPersonDescription"
        } else {
            ""
        }

        return """
당신은 전문 심리 분석가입니다. 사용자가 작성한 일기 답변들을 분석하여 다음 4가지 항목에 대한 통찰을 제공해주세요.
내용적인 부분에서만 제공해주세요. 답변이 너무 짧습니다같은건 안됩니다.
\n* ** 이런것들 쓰지마시고 평문으로만 써주십시오



사용자의 답변 히스토리:
$answersText
$idealPersonContext

다음 형식으로 정확히 응답해주세요. 각 섹션은 반드시 포함되어야 하며, 구분자를 정확히 지켜주세요:

[장단점]
사용자의 강점과 약점을 분석하여 작성해주세요. 강점과 약점을 명확히 구분하여 작성하되, 구체적인 예시와 함께 설명해주세요.

[가치관]
사용자가 중요하게 생각하는 가치관과 신념을 분석하여 작성해주세요. 답변에서 반복적으로 나타나는 주제나 관심사를 토대로 작성해주세요.

[개선사항]
사용자가 개선할 수 있는 부분과 성장 방향을 구체적으로 제안해주세요. 실천 가능한 조언으로 작성해주세요.

[관계도]
사용자의 인간관계 패턴을 분석하여 JSON 형식으로 작성해주세요. 예: {"가족": "긍정적", "친구": "활발함", "동료": "협력적"}
        """.trimIndent()
    }

    private fun parseAnalysisResponse(responseText: String): ParsedAIAnalysis {
        val sections = mutableMapOf<String, String>()

        val strengthsWeaknessesRegex = """\[장단점\]\s*([\s\S]*?)(?=\[가치관\]|\z)""".toRegex()
        val valuesRegex = """\[가치관\]\s*([\s\S]*?)(?=\[개선사항\]|\z)""".toRegex()
        val improvementsRegex = """\[개선사항\]\s*([\s\S]*?)(?=\[관계도\]|\z)""".toRegex()
        val relationshipRegex = """\[관계도\]\s*([\s\S]*?)(?=\z)""".toRegex()

        sections["장단점"] = strengthsWeaknessesRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["가치관"] = valuesRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["개선사항"] = improvementsRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["관계도"] = relationshipRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "{}"

        return ParsedAIAnalysis(
            strengths = extractStrengths(sections["장단점"] ?: ""),
            weaknesses = extractWeaknesses(sections["장단점"] ?: ""),
            values = sections["가치관"] ?: "",
            improvementSuggestions = sections["개선사항"] ?: "",
            relationshipMap = sections["관계도"] ?: "{}"
        )
    }

    private fun extractStrengths(strengthsWeaknesses: String): String {
        val lines = strengthsWeaknesses.split("\n")
        val strengths = mutableListOf<String>()
        var isStrength = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("강점", ignoreCase = true) || trimmed.contains("장점", ignoreCase = true)) {
                isStrength = true
                continue
            }
            if (trimmed.contains("약점", ignoreCase = true) || trimmed.contains("단점", ignoreCase = true)) {
                isStrength = false
                continue
            }
            if (isStrength && trimmed.isNotEmpty()) {
                strengths.add(trimmed)
            }
        }

        return if (strengths.isNotEmpty()) {
            strengths.joinToString("\n")
        } else {
            strengthsWeaknesses.split("\n").take(3).joinToString("\n")
        }
    }

    private fun extractWeaknesses(strengthsWeaknesses: String): String {
        val lines = strengthsWeaknesses.split("\n")
        val weaknesses = mutableListOf<String>()
        var isWeakness = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("약점", ignoreCase = true) || trimmed.contains("단점", ignoreCase = true)) {
                isWeakness = true
                continue
            }
            if (trimmed.contains("강점", ignoreCase = true) || trimmed.contains("장점", ignoreCase = true)) {
                isWeakness = false
                continue
            }
            if (isWeakness && trimmed.isNotEmpty()) {
                weaknesses.add(trimmed)
            }
        }

        return if (weaknesses.isNotEmpty()) {
            weaknesses.joinToString("\n")
        } else {
            "분석 결과가 없습니다."
        }
    }

    fun close() {
        client.close()
    }
}
