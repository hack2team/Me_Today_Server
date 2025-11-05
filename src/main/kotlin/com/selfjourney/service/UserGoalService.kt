package com.selfjourney.service

import com.selfjourney.domain.*
import com.selfjourney.repository.UserGoalRepository
import org.jetbrains.exposed.sql.transactions.transaction

class UserGoalService(
    private val userGoalRepository: UserGoalRepository
) {

    fun createUserGoal(request: CreateUserGoalRequest): Long = transaction {
        userGoalRepository.create(request)
    }

    fun getUserGoalsByUserId(userId: Long): List<UserGoalDTO> = transaction {
        userGoalRepository.findByUserId(userId)
    }

    fun getActiveUserGoal(userId: Long): UserGoalDTO? = transaction {
        userGoalRepository.findActiveGoalByUserId(userId)
    }

    fun updateUserGoal(goalId: Long, request: UpdateUserGoalRequest): Boolean = transaction {
        userGoalRepository.update(goalId, request)
    }

    fun deleteUserGoal(goalId: Long): Boolean = transaction {
        userGoalRepository.delete(goalId)
    }
}
