package com.selfjourney.repository

import com.selfjourney.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UserRepository {

    fun findAll(): List<UserDTO> = Users.selectAll().map { toDTO(it) }

    fun findById(id: Long): UserDTO? = Users.select { Users.id eq id }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun findByEmail(email: String): UserDTO? = Users.select { Users.email eq email }
        .mapNotNull { toDTO(it) }
        .singleOrNull()

    fun create(request: CreateUserRequest): Long {
        val userId = Users.insertAndGetId {
            it[name] = request.name
            it[age] = request.age
            it[email] = request.email
            it[passwordHash] = request.password?.let { pwd -> hashPassword(pwd) }
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }

        // Create progress entry for new user
        UserProgress.insert {
            it[UserProgress.id] = userId
            it[totalAnswers] = 0
            it[consecutiveDays] = 0
            it[selfAwarenessLevel] = 1
        }

        return userId.value
    }

    fun update(id: Long, request: CreateUserRequest): Boolean {
        val updated = Users.update({ Users.id eq id }) {
            it[name] = request.name
            request.age?.let { age -> it[Users.age] = age }
            request.email?.let { email -> it[Users.email] = email }
            it[updatedAt] = LocalDateTime.now()
        }
        return updated > 0
    }

    fun delete(id: Long): Boolean {
        val deleted = Users.deleteWhere { Users.id eq id }
        return deleted > 0
    }

    private fun toDTO(row: ResultRow) = UserDTO(
        userId = row[Users.id].value,
        name = row[Users.name],
        age = row[Users.age],
        email = row[Users.email],
        createdAt = row[Users.createdAt].toString()
    )

    private fun hashPassword(password: String): String {
        // In production, use BCrypt or similar
        return "\$2a\$10\$${password}DummyHash"
    }
}
