package com.selfjourney.domain

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate
import java.time.LocalDateTime

// Exposed Tables
object Users : LongIdTable("users", "user_id") {
    val name = varchar("name", 50)
    val age = integer("age").nullable()
    val email = varchar("email", 100).uniqueIndex().nullable()
    val passwordHash = varchar("password_hash", 255).nullable()
    val questionCycleMonths = integer("question_cycle_months").default(12)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Interests : LongIdTable("interests", "interest_id") {
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val iconUrl = varchar("icon_url", 255).nullable()
}

object GoalPersonas : LongIdTable("goal_personas", "persona_id") {
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val recommendedTraits = text("recommended_traits").nullable() // JSON stored as text
}

object Questions : LongIdTable("questions", "question_id") {
    val interestId = reference("interest_id", Interests).nullable()
    val personaId = reference("persona_id", GoalPersonas).nullable()
    val content = text("content")
    val createdBy = enumerationByName("created_by", 10, CreatedBy::class).default(CreatedBy.system)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

object Answers : LongIdTable("answers", "answer_id") {
    val userId = reference("user_id", Users)
    val questionId = reference("question_id", Questions)
    val content = text("content")
    val aiSummary = text("ai_summary").nullable()
    val aiKeywords = text("ai_keywords").nullable() // JSON stored as text
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Reports : LongIdTable("reports", "report_id") {
    val userId = reference("user_id", Users)
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val strengths = text("strengths").nullable()
    val weaknesses = text("weaknesses").nullable()
    val valuesSummary = text("values_summary").nullable()
    val improvementSuggestions = text("improvement_suggestions").nullable()
    val relationshipMap = text("relationship_map").nullable() // JSON
    val emotionTrends = text("emotion_trends").nullable() // JSON
    val keywordTrends = text("keyword_trends").nullable() // JSON
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object Notifications : LongIdTable("notifications", "notification_id") {
    val userId = reference("user_id", Users)
    val type = enumerationByName("type", 20, NotificationType::class)
    val message = varchar("message", 255).nullable()
    val sentAt = datetime("sent_at").clientDefault { LocalDateTime.now() }
    val isRead = bool("is_read").default(false)
}

object UserProgress : LongIdTable("user_progress", "user_id") {
    val totalAnswers = integer("total_answers").default(0)
    val consecutiveDays = integer("consecutive_days").default(0)
    val lastAnsweredAt = date("last_answered_at").nullable()
    val selfAwarenessLevel = integer("self_awareness_level").default(1)
    val lastReportId = reference("last_report_id", Reports).nullable()
}

object UserGoals : LongIdTable("user_goals", "goal_id") {
    val userId = reference("user_id", Users)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val idealPersonDescription = text("ideal_person_description")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object AiAnalysis : LongIdTable("ai_analysis", "analysis_id") {
    val userId = reference("user_id", Users)
    val answerId = reference("answer_id", Answers)
    val strengths = text("strengths").nullable()
    val weaknesses = text("weaknesses").nullable()
    val values = text("values").nullable()
    val improvementSuggestions = text("improvement_suggestions").nullable()
    val relationshipMap = text("relationship_map").nullable() // JSON
    val analyzedAt = datetime("analyzed_at").clientDefault { LocalDateTime.now() }
}

enum class CreatedBy { system, admin }
enum class NotificationType { daily_question, reminder, report_ready }
enum class SentimentContext { positive, neutral, negative }
