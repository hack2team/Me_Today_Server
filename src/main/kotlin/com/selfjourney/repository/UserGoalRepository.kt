package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
import java.time.LocalDateTime

class UserGoalRepository {

    fun findAll(): List<UserGoalDTO> = UserGoals.selectAll().map { toDTO(it) }

    fun findById(id: Long): UserGoalDTO? = UserGoals.select { UserGoals.id eq id }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun findByUserId(userId: Long): List<UserGoalDTO> = UserGoals.select { UserGoals.userId eq userId }
        .map { toDTO(it) }

    fun findActiveGoalByUserId(userId: Long): UserGoalDTO? {
        val today = LocalDate.now()
        return UserGoals.select {
            (UserGoals.userId eq userId) and
            (UserGoals.startDate lessEq today) and
            (UserGoals.endDate greaterEq today)
        }
            .orderBy(UserGoals.createdAt, SortOrder.DESC)
            .limit(1)
            .mapNotNull { toDTO(it) }
            .singleOrNull()
    }

    fun create(request: CreateUserGoalRequest): Long {
        val goalId = UserGoals.insertAndGetId {
            it[userId] = request.userId
            it[startDate] = LocalDate.parse(request.startDate)
            it[endDate] = LocalDate.parse(request.endDate)
            it[idealPersonDescription] = request.idealPersonDescription
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        return goalId.value
    }

    fun update(id: Long, request: UpdateUserGoalRequest): Boolean {
        val updatedRows = UserGoals.update({ UserGoals.id eq id }) {
            request.startDate?.let { date -> it[startDate] = LocalDate.parse(date) }
            request.endDate?.let { date -> it[endDate] = LocalDate.parse(date) }
            request.idealPersonDescription?.let { desc -> it[idealPersonDescription] = desc }
            it[updatedAt] = LocalDateTime.now()
        }
        return updatedRows > 0
    }

    fun delete(id: Long): Boolean {
        val deletedRows = UserGoals.deleteWhere { UserGoals.id eq id }
        return deletedRows > 0
    }

    private fun toDTO(row: ResultRow): UserGoalDTO {
        return UserGoalDTO(
            goalId = row[UserGoals.id].value,
            userId = row[UserGoals.userId].value,
            startDate = row[UserGoals.startDate].toString(),
            endDate = row[UserGoals.endDate].toString(),
            idealPersonDescription = row[UserGoals.idealPersonDescription],
            createdAt = row[UserGoals.createdAt].toString(),
            updatedAt = row[UserGoals.updatedAt].toString()
        )
    }
}
