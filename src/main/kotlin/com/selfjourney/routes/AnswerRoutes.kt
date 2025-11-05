package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.ProgressRepository
import com.selfjourney.repository.AiAnalysisRepository
import com.selfjourney.repository.UserGoalRepository
import com.selfjourney.service.AnswerService
import com.selfjourney.service.GeminiService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.answerRoutes() {
    val answerRepository = AnswerRepository()
    val progressRepository = ProgressRepository()
    val aiAnalysisRepository = AiAnalysisRepository()
    val userGoalRepository = UserGoalRepository()

    val geminiApiKey = System.getenv("GEMINI_API_KEY") ?: "AIzaSyDFkhSf8TylOsBR2ZnYQDmwmmoJ1wbj5ec"
    val geminiService = GeminiService(geminiApiKey)

    val answerService = AnswerService(answerRepository, progressRepository, aiAnalysisRepository, userGoalRepository, geminiService)

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

        // Get answer history with AI analysis
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
    }
}
