package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.ProgressRepository
import com.selfjourney.repository.QuestionRepository
import com.selfjourney.service.AnswerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.answerRoutes() {
    val answerRepository = AnswerRepository()
    val questionRepository = QuestionRepository()
    val progressRepository = ProgressRepository()
    val answerService = AnswerService(
        answerRepository,
        questionRepository,
        progressRepository
    )

    route("/api/answers") {
        get("/user/{userId}") {
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

            val answers = answerService.getAnswersByUserId(userId)
            call.respond(ApiResponse(success = true, data = answers))
        }

        get("/question/{questionId}") {
            val questionId = call.parameters["questionId"]?.toLongOrNull()
            if (questionId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid question ID")
                    )
                )
                return@get
            }

            val answers = answerService.getAnswersByQuestionId(questionId)
            call.respond(ApiResponse(success = true, data = answers))
        }

        post {
            val request = call.receive<CreateAnswerRequest>()
            val response = answerService.submitAnswer(request)
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = response))
        }

        get("/history/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val date = call.request.queryParameters["date"]
            val questionId = call.request.queryParameters["questionId"]?.toLongOrNull()

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

            val history = answerService.getAnswerHistory(userId, date, questionId)
            call.respond(ApiResponse(success = true, data = history))
        }

        get("/insights/{userId}") {
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

            val report = answerService.getUserReport(userId)
            call.respond(ApiResponse(success = true, data = report))
        }

        get("/user/{userId}/question/{questionId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
            val questionId = call.parameters["questionId"]?.toLongOrNull()
            if (userId == null || questionId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid user or question ID")
                    )
                )
                return@get
            }

            val history = answerService.getQuestionHistory(userId, questionId)
            call.respond(ApiResponse(success = true, data = history))
        }

        get("/report/{userId}") {
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

            val report = answerService.getUserReport(userId)
            call.respond(ApiResponse(success = true, data = report))
        }
    }
}
