package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class AIAnalysisRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun findLatestByUserId(userId: Long): AIAnalysisDTO? {
        return AiAnalysis.select { AiAnalysis.userId eq userId }
            .orderBy(AiAnalysis.analyzedAt to SortOrder.DESC)
            .limit(1)
            .mapNotNull { toDTO(it) }
            .singleOrNull()
    }

    fun findByUserId(userId: Long): List<AIAnalysisDTO> {
        return AiAnalysis.select { AiAnalysis.userId eq userId }
            .orderBy(AiAnalysis.analyzedAt to SortOrder.DESC)
            .map { toDTO(it) }
    }

    fun create(
        userId: Long,
        answerId: Long,
        strengths: String,
        weaknesses: String,
        values: String,
        improvementSuggestions: String,
        relationshipMap: String
    ): Long {
        val analysisId = AiAnalysis.insertAndGetId {
            it[AiAnalysis.userId] = userId
            it[AiAnalysis.answerId] = answerId
            it[AiAnalysis.strengths] = strengths
            it[AiAnalysis.weaknesses] = weaknesses
            it[AiAnalysis.values] = values
            it[AiAnalysis.improvementSuggestions] = improvementSuggestions
            it[AiAnalysis.relationshipMap] = relationshipMap
            it[analyzedAt] = LocalDateTime.now()
        }
        return analysisId.value
    }

    fun update(
        analysisId: Long,
        strengths: String,
        weaknesses: String,
        values: String,
        improvementSuggestions: String,
        relationshipMap: String
    ): Int {
        return AiAnalysis.update({ AiAnalysis.id eq analysisId }) {
            it[AiAnalysis.strengths] = strengths
            it[AiAnalysis.weaknesses] = weaknesses
            it[AiAnalysis.values] = values
            it[AiAnalysis.improvementSuggestions] = improvementSuggestions
            it[AiAnalysis.relationshipMap] = relationshipMap
            it[analyzedAt] = LocalDateTime.now()
        }
    }

    fun deleteByUserId(userId: Long): Int {
        return AiAnalysis.deleteWhere { AiAnalysis.userId eq userId }
    }

    private fun toDTO(row: ResultRow): AIAnalysisDTO {
        val relationshipMapStr = row[AiAnalysis.relationshipMap]
        val relationshipMapParsed = if (!relationshipMapStr.isNullOrBlank()) {
            try {
                // JSON 코드 블록 제거
                val cleaned = relationshipMapStr
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                json.decodeFromString<Map<String, String>>(cleaned)
            } catch (e: Exception) {
                println("Failed to parse relationship map: ${e.message}")
                emptyMap()
            }
        } else {
            null
        }

        return AIAnalysisDTO(
            analysisId = row[AiAnalysis.id].value,
            userId = row[AiAnalysis.userId].value,
            strengths = row[AiAnalysis.strengths],
            weaknesses = row[AiAnalysis.weaknesses],
            values = row[AiAnalysis.values],
            improvementSuggestions = row[AiAnalysis.improvementSuggestions],
            relationshipMap = relationshipMapParsed,
            analyzedAt = row[AiAnalysis.analyzedAt].toString()
        )
    }
}
