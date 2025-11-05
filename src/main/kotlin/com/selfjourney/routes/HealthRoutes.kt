package com.selfjourney.routes

import com.selfjourney.domain.HealthResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun Route.healthRoutes() {
    get("/health") {
        val dbStatus = try {
            transaction {
                // Simple query to check database connectivity
                exec("SELECT 1") { }
            }
            "healthy"
        } catch (e: Exception) {
            "unhealthy"
        }

        call.respond(
            HttpStatusCode.OK,
            HealthResponse(
                status = if (dbStatus == "healthy") "healthy" else "degraded",
                database = dbStatus,
                timestamp = LocalDateTime.now().toString()
            )
        )
    }
}
