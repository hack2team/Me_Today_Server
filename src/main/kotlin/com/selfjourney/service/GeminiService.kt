package com.selfjourney.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GeminiService(private val apiKey: String) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun analyzeAnswer(
        content: String,
        previousAnswer: String? = null,
        idealPersonDescription: String? = null
    ): AnalysisResult {
        val prompt = buildPrompt(content, previousAnswer, idealPersonDescription)

        return try {
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                ))
            }.body()

            parseAnalysisResult(response)
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
            AnalysisResult(
                summary = "분석 중 오류가 발생했습니다.",
                keywords = listOf("오류"),
                strengths = "분석 불가",
                weaknesses = "분석 불가",
                pathToIdeal = "분석 불가",
                relationshipMap = emptyMap(),
                comparison = null
            )
        }
    }

    private fun buildPrompt(content: String, previousAnswer: String?, idealPersonDescription: String?): String {
        val idealPersonPrompt = if (idealPersonDescription != null) {
            "\n\n사용자가 되고 싶은 이상적인 사람: \"$idealPersonDescription\""
        } else {
            ""
        }

        val basePrompt = """
당신은 사용자의 자기 성찰을 돕는 AI 분석가입니다.
다음 답변을 분석하여 JSON 형식으로 응답해주세요:

답변 내용: "$content"$idealPersonPrompt

다음 형식으로 응답해주세요:
{
  "summary": "답변의 핵심 내용을 2-3문장으로 요약",
  "keywords": ["키워드1", "키워드2", "키워드3"],
  "strengths": "이 답변에서 발견되는 사용자의 장점과 긍정적인 측면",
  "weaknesses": "개선이 필요한 부분이나 극복해야 할 과제",
  "pathToIdeal": "이상적인 사람이 되기 위해 필요한 구체적인 행동이나 마음가짐",
  "relationshipMap": {"인물1": "관계설명", "인물2": "관계설명"},
  "comparison": "${if (previousAnswer != null) "이전 답변과의 차이점 분석" else "null"}"
}

relationshipMap은 답변에 언급된 사람들과의 관계를 정리한 것입니다. 언급된 사람이 없으면 빈 객체를 반환하세요.
"""

        return if (previousAnswer != null) {
            "$basePrompt\n\n이전 답변: \"$previousAnswer\"\n\n이전 답변과 현재 답변의 변화를 비교 분석해주세요."
        } else {
            basePrompt
        }
    }

    private fun parseAnalysisResult(response: GeminiResponse): AnalysisResult {
        return try {
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val jsonText = text.substringAfter("{").substringBeforeLast("}").let { "{$it}" }
            Json.decodeFromString<AnalysisResult>(jsonText)
        } catch (e: Exception) {
            AnalysisResult(
                summary = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.take(200) ?: "분석 결과를 파싱할 수 없습니다.",
                keywords = listOf("분석"),
                strengths = "분석 결과 파싱 실패",
                weaknesses = "분석 결과 파싱 실패",
                pathToIdeal = "분석 결과 파싱 실패",
                relationshipMap = emptyMap(),
                comparison = null
            )
        }
    }

    @Serializable
    data class GeminiRequest(
        val contents: List<Content>
    )

    @Serializable
    data class Content(
        val parts: List<Part>
    )

    @Serializable
    data class Part(
        val text: String
    )

    @Serializable
    data class GeminiResponse(
        val candidates: List<Candidate>
    )

    @Serializable
    data class Candidate(
        val content: Content
    )

    @Serializable
    data class AnalysisResult(
        val summary: String,
        val keywords: List<String>,
        val strengths: String,
        val weaknesses: String,
        val pathToIdeal: String,
        val relationshipMap: Map<String, String>,
        val comparison: String?
    )
}
