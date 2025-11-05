package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class AiAnalysisRepository {

    fun findAll(): List<AiAnalysisDTO> = AiAnalysis.selectAll().map { toDTO(it) }

    fun findById(id: Long): AiAnalysisDTO? = AiAnalysis.select { AiAnalysis.id eq id }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun findByUserId(userId: Long): List<AiAnalysisDTO> = AiAnalysis.select { AiAnalysis.userId eq userId }
        .orderBy(AiAnalysis.analyzedAt, SortOrder.DESC)
        .map { toDTO(it) }

    fun findByAnswerId(answerId: Long): AiAnalysisDTO? = AiAnalysis.select { AiAnalysis.answerId eq answerId }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun findByUserIdWithPagination(userId: Long, limit: Int, offset: Int): List<AiAnalysisDTO> =
        AiAnalysis.select { AiAnalysis.userId eq userId }
            .orderBy(AiAnalysis.analyzedAt, SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { toDTO(it) }

    fun create(
        userId: Long,
        answerId: Long,
        strengths: String,
        weaknesses: String,
        pathToIdeal: String,
        relationshipMap: String
    ): Long {
        val analysisId = AiAnalysis.insertAndGetId {
            it[AiAnalysis.userId] = userId
            it[AiAnalysis.answerId] = answerId
            it[AiAnalysis.strengths] = strengths
            it[AiAnalysis.weaknesses] = weaknesses
            it[AiAnalysis.pathToIdeal] = pathToIdeal
            it[AiAnalysis.relationshipMap] = relationshipMap
            it[analyzedAt] = LocalDateTime.now()
        }
        return analysisId.value
    }

    fun delete(id: Long): Boolean {
        val deletedRows = AiAnalysis.deleteWhere { AiAnalysis.id eq id }
        return deletedRows > 0
    }

    private fun toDTO(row: ResultRow): AiAnalysisDTO {
        return AiAnalysisDTO(
            analysisId = row[AiAnalysis.id].value,
            userId = row[AiAnalysis.userId].value,
            answerId = row[AiAnalysis.answerId].value,
            strengths = row[AiAnalysis.strengths],
            weaknesses = row[AiAnalysis.weaknesses],
            pathToIdeal = row[AiAnalysis.pathToIdeal],
            relationshipMap = row[AiAnalysis.relationshipMap],
            analyzedAt = row[AiAnalysis.analyzedAt].toString()
        )
    }
}
