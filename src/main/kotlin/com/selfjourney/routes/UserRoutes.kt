package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.userRoutes() {
    val userRepository = UserRepository()
    val allowedDurations = setOf(6, 12, 24)

    route("/api/users") {
        get {
            val users = transaction { userRepository.findAll() }
            call.respond(ApiResponse(success = true, data = users))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid user ID")
                    )
                )
                return@get
            }

            val user = transaction { userRepository.findById(id) }
            if (user == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("NOT_FOUND", "User not found")
                    )
                )
            } else {
                call.respond(ApiResponse(success = true, data = user))
            }
        }

        post {
            val request = call.receive<CreateUserRequest>()
            val planMonths = (request.planDurationMonths ?: 12)
            if (planMonths !in allowedDurations) {
                val allowedList = allowedDurations.joinToString(", ")
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_PLAN", "planDurationMonths must be one of $allowedList")
                    )
                )
                return@post
            }

            val normalizedRequest = request.copy(planDurationMonths = planMonths)
            val userId = transaction { userRepository.create(normalizedRequest) }
            val user = transaction { userRepository.findById(userId) }
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = user))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid user ID")
                    )
                )
                return@put
            }

            val request = call.receive<CreateUserRequest>()
            val planMonths = request.planDurationMonths
            if (planMonths != null && planMonths !in allowedDurations) {
                val allowedList = allowedDurations.joinToString(", ")
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_PLAN", "planDurationMonths must be one of $allowedList")
                    )
                )
                return@put
            }

            val updated = transaction { userRepository.update(id, request) }

            if (updated) {
                val user = transaction { userRepository.findById(id) }
                call.respond(ApiResponse(success = true, data = user))
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("NOT_FOUND", "User not found")
                    )
                )
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid user ID")
                    )
                )
                return@delete
            }

            val deleted = transaction { userRepository.delete(id) }
            if (deleted) {
                call.respond(ApiResponse(success = true, data = mapOf("deleted" to true)))
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("NOT_FOUND", "User not found")
                    )
                )
            }
        }
    }
}
