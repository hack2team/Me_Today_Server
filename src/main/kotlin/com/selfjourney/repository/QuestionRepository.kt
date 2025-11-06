package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.time.LocalDateTime

class QuestionRepository {

    fun findAll(): List<QuestionDTO> =
        Questions.selectAll().orderBy(Questions.id to SortOrder.ASC).map { toDTO(it) }

    fun count(): Int = Questions.selectAll().count().toInt()

    fun findById(id: Long): QuestionDTO? = Questions.select { Questions.id eq id }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun findByInterestId(interestId: Long): List<QuestionDTO> =
        Questions.select { Questions.interestId eq interestId }.map { toDTO(it) }

    fun findByPersonaId(personaId: Long): List<QuestionDTO> =
        Questions.select { Questions.personaId eq personaId }.map { toDTO(it) }

    fun findBySequence(sequence: Int): QuestionDTO? =
        Questions.select { Questions.id eq sequence.toLong() }
            .mapNotNull { toDTO(it) }
            .singleOrNull()

    fun findByIds(ids: Set<Long>): Map<Long, QuestionDTO> {
        if (ids.isEmpty()) return emptyMap()
        return Questions
            .select { Questions.id inList ids.toList() }
            .map { toDTO(it) }
            .associateBy { it.questionId }
    }

    fun create(request: CreateQuestionRequest): Long {
        val questionId = Questions.insertAndGetId {
            it[content] = request.content
            it[createdBy] = CreatedBy.valueOf(request.createdBy)
            it[createdAt] = LocalDateTime.now()
        }
        return questionId.value
    }

    private fun toDTO(row: ResultRow) = QuestionDTO(
        questionId = row[Questions.id].value,
        content = row[Questions.content],
        createdBy = row[Questions.createdBy].name,
        createdAt = row[Questions.createdAt].toString()
    )
}
