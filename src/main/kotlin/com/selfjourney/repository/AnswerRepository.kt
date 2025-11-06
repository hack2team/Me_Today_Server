package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class AnswerRepository {

    fun findByUserId(userId: Long, ascending: Boolean = false): List<AnswerDTO> {
        val order = if (ascending) Answers.createdAt to SortOrder.ASC else Answers.createdAt to SortOrder.DESC
        return Answers.select { Answers.userId eq userId }
            .orderBy(order)
            .map { toDTO(it) }
    }

    fun findByQuestionId(questionId: Long): List<AnswerDTO> =
        Answers.select { Answers.questionId eq questionId }.map { toDTO(it) }

    fun findPreviousAnswer(userId: Long, questionId: Long): AnswerDTO? {
        return Answers.select {
            (Answers.userId eq userId) and (Answers.questionId eq questionId)
        }
            .orderBy(Answers.createdAt to SortOrder.DESC)
            .limit(1)
            .mapNotNull { toDTO(it) }
            .singleOrNull()
    }

    fun findByUserAndQuestion(userId: Long, questionId: Long, ascending: Boolean = true): List<AnswerDTO> {
        val order = if (ascending) SortOrder.ASC else SortOrder.DESC
        return Answers.select { (Answers.userId eq userId) and (Answers.questionId eq questionId) }
            .orderBy(Answers.createdAt to order)
            .map { toDTO(it) }
    }

    fun create(request: CreateAnswerRequest): Long {
        val answerId = Answers.insertAndGetId {
            it[userId] = request.userId
            it[questionId] = request.questionId
            it[content] = request.content
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        return answerId.value
    }

    fun countByUserId(userId: Long): Int =
        Answers.select { Answers.userId eq userId }.count().toInt()

    private fun toDTO(row: ResultRow) = AnswerDTO(
        answerId = row[Answers.id].value,
        userId = row[Answers.userId].value,
        questionId = row[Answers.questionId].value,
        content = row[Answers.content],
        aiSummary = row[Answers.aiSummary],
        aiKeywords = row[Answers.aiKeywords],
        createdAt = row[Answers.createdAt].toString(),
        updatedAt = row[Answers.updatedAt].toString()
    )
}
