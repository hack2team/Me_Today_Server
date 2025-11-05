package com.selfjourney.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import com.selfjourney.domain.ApiResponse
import com.selfjourney.domain.ErrorDetail

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeaders { true }
        allowCredentials = true
        allowSameOrigin = true
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(
                    success = false,
                    error = ErrorDetail(
                        code = "INTERNAL_ERROR",
                        message = "An internal error occurred",
                        details = cause.message
                    )
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse<Unit>(
                    success = false,
                    error = ErrorDetail(
                        code = "NOT_FOUND",
                        message = "Resource not found"
                    )
                )
            )
        }
    }
}
