package com.selfjourney.plugins

import com.selfjourney.routes.*
import com.selfjourney.repository.UserGoalRepository
import com.selfjourney.service.UserGoalService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userGoalRepository = UserGoalRepository()
    val userGoalService = UserGoalService(userGoalRepository)

    val routing = pluginOrNull(Routing) ?: install(Routing)
    routing.apply {
        healthRoutes()
        userRoutes()
        questionRoutes()
        answerRoutes()
        progressRoutes()
        userGoalRoutes(userGoalService)
    }
}
