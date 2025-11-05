package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate

class ProgressRepository {

    fun findByUserId(userId: Long): ProgressDTO? =
        UserProgress.select { UserProgress.id eq userId }
            .mapNotNull { toDTO(it) }
            .singleOrNull()

    fun updateProgress(userId: Long, totalAnswers: Int, consecutiveDays: Int, lastAnsweredAt: LocalDate) {
        UserProgress.update({ UserProgress.id eq userId }) {
            it[UserProgress.totalAnswers] = totalAnswers
            it[UserProgress.consecutiveDays] = consecutiveDays
            it[UserProgress.lastAnsweredAt] = lastAnsweredAt
        }
    }

    fun incrementAnswerCount(userId: Long) {
        val current = findByUserId(userId)
        if (current != null) {
            val today = LocalDate.now()
            val lastDate = current.lastAnsweredAt?.let { LocalDate.parse(it) }

            val newConsecutiveDays = when {
                lastDate == null -> 1
                lastDate.plusDays(1) == today -> current.consecutiveDays + 1
                lastDate == today -> current.consecutiveDays
                else -> 1
            }

            updateProgress(
                userId = userId,
                totalAnswers = current.totalAnswers + 1,
                consecutiveDays = newConsecutiveDays,
                lastAnsweredAt = today
            )
        }
    }

    private fun toDTO(row: ResultRow) = ProgressDTO(
        userId = row[UserProgress.id].value,
        totalAnswers = row[UserProgress.totalAnswers],
        consecutiveDays = row[UserProgress.consecutiveDays],
        lastAnsweredAt = row[UserProgress.lastAnsweredAt]?.toString(),
        selfAwarenessLevel = row[UserProgress.selfAwarenessLevel],
        lastReportId = row[UserProgress.lastReportId]?.value
    )
}
