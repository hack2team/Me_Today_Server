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
당신은 전문 심리 분석가입니다. 사용자가 작성한 일기 답변들을 분석하여 다음 항목들에 대한 통찰을 제공해주세요.
내용적인 부분에서만 제공해주세요. 답변이 너무 짧습니다 같은 형식적 평가는 하지 마세요.
* ** 같은 마크다운 기호를 쓰지 말고 평문으로만 작성해주세요.
각 답변은 2~3문장으로 간결하게 작성해주세요.

사용자의 답변 히스토리:
$answersText
$idealPersonContext

다음 형식으로 정확히 응답해주세요. 각 섹션은 반드시 포함되어야 하며, 구분자를 정확히 지켜주세요:

[강점]
사용자의 강점만 분석하여 작성해주세요. 구체적인 예시와 함께 설명해주세요.

[약점]
사용자의 약점만 분석하여 작성해주세요. 구체적인 예시와 함께 설명해주세요.

[가치관]
사용자가 중요하게 생각하는 가치관과 신념을 분석하여 작성해주세요. 답변에서 반복적으로 나타나는 주제나 관심사를 토대로 작성해주세요.

[개선사항]
사용자가 개선할 수 있는 부분과 성장 방향을 구체적으로 제안해주세요. 실천 가능한 조언으로 작성해주세요.

[관계도]
사용자가 답변에서 언급한 구체적인 사람들과의 관계를 JSON 형식으로 작성해주세요.
사람 이름을 키로 사용하고, 그 사람과의 관계를 구체적으로 설명해주세요.
예시: {"민지": "함께 있으면 편하고 서로 격려해주는 사이", "부모님": "항상 응원해주시는 든든한 지지자", "팀장님": "존경하며 배우는 멘토"}
일반적인 '가족', '친구', '동료' 같은 카테고리가 아니라, 실제 답변에 등장한 구체적인 사람 이름과 관계 설명을 작성해주세요.
답변에 구체적인 사람이 언급되지 않았다면 빈 객체 {}를 반환해주세요.
        """.trimIndent()
    }

    private fun parseAnalysisResponse(responseText: String): ParsedAIAnalysis {
        val sections = mutableMapOf<String, String>()

        val strengthsRegex = """\[강점\]\s*([\s\S]*?)(?=\[약점\]|\z)""".toRegex()
        val weaknessesRegex = """\[약점\]\s*([\s\S]*?)(?=\[가치관\]|\z)""".toRegex()
        val valuesRegex = """\[가치관\]\s*([\s\S]*?)(?=\[개선사항\]|\z)""".toRegex()
        val improvementsRegex = """\[개선사항\]\s*([\s\S]*?)(?=\[관계도\]|\z)""".toRegex()
        val relationshipRegex = """\[관계도\]\s*([\s\S]*?)(?=\z)""".toRegex()

        sections["강점"] = strengthsRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["약점"] = weaknessesRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["가치관"] = valuesRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["개선사항"] = improvementsRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "분석 결과가 없습니다."
        sections["관계도"] = relationshipRegex.find(responseText)?.groupValues?.get(1)?.trim()
            ?: "{}"

        return ParsedAIAnalysis(
            strengths = sections["강점"] ?: "분석 결과가 없습니다.",
            weaknesses = sections["약점"] ?: "분석 결과가 없습니다.",
            values = sections["가치관"] ?: "분석 결과가 없습니다.",
            improvementSuggestions = sections["개선사항"] ?: "분석 결과가 없습니다.",
            relationshipMap = sections["관계도"] ?: "{}"
        )
    }

    fun close() {
        client.close()
    }
}
