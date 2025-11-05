package com.selfjourney.routes

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.QuestionRepository
import com.selfjourney.service.QuestionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.questionRoutes() {
    val questionRepository = QuestionRepository()
    val answerRepository = AnswerRepository()
    val questionService = QuestionService(questionRepository, answerRepository)

    route("/api/questions") {
        get {
            val questions = questionService.getAllQuestions()
            call.respond(ApiResponse(success = true, data = questions))
        }

        get("/today") {
            val userId = call.request.queryParameters["userId"]?.toLongOrNull()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_PARAM", "userId is required")
                    )
                )
                return@get
            }

            val response = questionService.getTodayQuestions(userId)
            call.respond(ApiResponse(success = true, data = response))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid question ID")
                    )
                )
                return@get
            }

            val question = questionService.getQuestionById(id)
            if (question == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("NOT_FOUND", "Question not found")
                    )
                )
            } else {
                call.respond(ApiResponse(success = true, data = question))
            }
        }

        get("/interest/{interestId}") {
            val interestId = call.parameters["interestId"]?.toLongOrNull()
            if (interestId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        success = false,
                        error = ErrorDetail("INVALID_ID", "Invalid interest ID")
                    )
                )
                return@get
            }

            val questions = questionService.getQuestionsByInterestId(interestId)
            call.respond(ApiResponse(success = true, data = questions))
        }

        post {
            val request = call.receive<CreateQuestionRequest>()
            val questionId = questionService.createQuestion(request)
            val question = questionService.getQuestionById(questionId)
            call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = question))
        }
    }
}
