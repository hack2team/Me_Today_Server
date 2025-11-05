package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.service.UserGoalService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userGoalRoutes(userGoalService: UserGoalService) {
    route("/api/user-goals") {
        // Create user goal
        post {
            val request = call.receive<CreateUserGoalRequest>()
            val goalId = userGoalService.createUserGoal(request)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    success = true,
                    data = mapOf("goalId" to goalId)
                )
            )
        }

        // Get user goals by user ID
        get("/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "INVALID_USER_ID",
                            message = "User ID must be a valid number"
                        )
                    )
                )
                return@get
            }

            val goals = userGoalService.getUserGoalsByUserId(userId)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, data = goals)
            )
        }

        // Get active goal for user
        get("/{userId}/active") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "INVALID_USER_ID",
                            message = "User ID must be a valid number"
                        )
                    )
                )
                return@get
            }

            val goal = userGoalService.getActiveUserGoal(userId)
            if (goal == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "NO_ACTIVE_GOAL",
                            message = "No active goal found for this user"
                        )
                    )
                )
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(success = true, data = goal)
            )
        }

        // Update user goal
        put("/{goalId}") {
            val goalId = call.parameters["goalId"]?.toLongOrNull()
            if (goalId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "INVALID_GOAL_ID",
                            message = "Goal ID must be a valid number"
                        )
                    )
                )
                return@put
            }

            val request = call.receive<UpdateUserGoalRequest>()
            val updated = userGoalService.updateUserGoal(goalId, request)

            if (updated) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, data = mapOf("updated" to true))
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "GOAL_NOT_FOUND",
                            message = "Goal not found"
                        )
                    )
                )
            }
        }

        // Delete user goal
        delete("/{goalId}") {
            val goalId = call.parameters["goalId"]?.toLongOrNull()
            if (goalId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "INVALID_GOAL_ID",
                            message = "Goal ID must be a valid number"
                        )
                    )
                )
                return@delete
            }

            val deleted = userGoalService.deleteUserGoal(goalId)

            if (deleted) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(success = true, data = mapOf("deleted" to true))
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail(
                            code = "GOAL_NOT_FOUND",
                            message = "Goal not found"
                        )
                    )
                )
            }
        }
    }
}
