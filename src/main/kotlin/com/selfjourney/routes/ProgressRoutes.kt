package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.repository.ProgressRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.progressRoutes() {
    val progressRepository = ProgressRepository()

    route("/api/progress") {
        get("/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid user ID")
                    )
                )
                return@get
            }

            val progress = org.jetbrains.exposed.sql.transactions.transaction {
                progressRepository.findByUserId(userId)
            }

            if (progress == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("NOT_FOUND", "Progress not found for user")
                    )
                )
            } else {
                call.respond(ApiResponse(success = true, data = progress))
            }
        }
    }
}
