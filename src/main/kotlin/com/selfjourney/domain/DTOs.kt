package com.selfjourney.domain

import kotlinx.serialization.Serializable

// Common Response
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: String? = null
)

// User DTOs
@Serializable
data class UserDTO(
    val userId: Long,
    val name: String,
    val age: Int? = null,
    val email: String? = null,
    val createdAt: String
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val age: Int? = null,
    val email: String? = null,
    val password: String? = null
)

// Interest DTOs
@Serializable
data class InterestDTO(
    val interestId: Long,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null
)

@Serializable
data class CreateInterestRequest(
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null
)

// Persona DTOs
@Serializable
data class PersonaDTO(
    val personaId: Long,
    val name: String,
    val description: String? = null,
    val recommendedTraits: String? = null
)

@Serializable
data class CreatePersonaRequest(
    val name: String,
    val description: String? = null,
    val recommendedTraits: String? = null
)

// Question DTOs
@Serializable
data class QuestionDTO(
    val questionId: Long,
    val content: String,
    val createdBy: String,
    val createdAt: String
)

@Serializable
data class CreateQuestionRequest(
    val content: String,
    val createdBy: String = "system"
)

@Serializable
data class TodayQuestionResponse(
    val question: QuestionDTO?,
    val answeredCount: Int,
    val totalQuestions: Int,
    val remainingQuestions: Int
)

// Answer DTOs
@Serializable
data class AnswerDTO(
    val answerId: Long,
    val userId: Long,
    val questionId: Long,
    val content: String,
    val aiSummary: String? = null,
    val aiKeywords: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateAnswerRequest(
    val userId: Long,
    val questionId: Long,
    val content: String
)

@Serializable
data class CreateAnswerResponse(
    val answerId: Long,
    val prevAnswer: AnswerDTO? = null,
    val savedAt: String,
    val aiAnalysis: AiAnalysisResult? = null
)

@Serializable
data class AiAnalysisResult(
    val summary: String,
    val keywords: List<String>,
    val strengths: String,
    val weaknesses: String,
    val pathToIdeal: String,
    val relationshipMap: Map<String, String>,
    val comparison: String? = null
)

// Report DTOs
@Serializable
data class ReportDTO(
    val reportId: Long,
    val userId: Long,
    val startDate: String? = null,
    val endDate: String? = null,
    val strengths: String? = null,
    val weaknesses: String? = null,
    val valuesSummary: String? = null,
    val improvementSuggestions: String? = null,
    val relationshipMap: String? = null,
    val emotionTrends: String? = null,
    val keywordTrends: String? = null,
    val updatedAt: String
)

@Serializable
data class ReportListResponse(
    val items: List<ReportDTO>
)

@Serializable
data class GenerateReportRequest(
    val userId: Long,
    val startDate: String,
    val endDate: String
)

// Progress DTOs
@Serializable
data class ProgressDTO(
    val userId: Long,
    val totalAnswers: Int,
    val consecutiveDays: Int,
    val lastAnsweredAt: String? = null,
    val selfAwarenessLevel: Int,
    val lastReportId: Long? = null
)

// Notification DTOs
@Serializable
data class NotificationDTO(
    val notificationId: Long,
    val userId: Long,
    val type: String,
    val message: String? = null,
    val sentAt: String,
    val isRead: Boolean
)

@Serializable
data class NotificationListResponse(
    val items: List<NotificationDTO>,
    val unreadCount: Int
)

// User Goals DTOs
@Serializable
data class UserGoalDTO(
    val goalId: Long,
    val userId: Long,
    val startDate: String,
    val endDate: String,
    val idealPersonDescription: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateUserGoalRequest(
    val userId: Long,
    val startDate: String,
    val endDate: String,
    val idealPersonDescription: String
)

@Serializable
data class UpdateUserGoalRequest(
    val startDate: String? = null,
    val endDate: String? = null,
    val idealPersonDescription: String? = null
)

// AI Analysis DTOs
@Serializable
data class AiAnalysisDTO(
    val analysisId: Long,
    val userId: Long,
    val answerId: Long,
    val strengths: String? = null,
    val weaknesses: String? = null,
    val pathToIdeal: String? = null,
    val relationshipMap: String? = null,
    val analyzedAt: String
)

// Answer History DTOs
@Serializable
data class AnswerHistoryRequest(
    val userId: Long,
    val date: String? = null,
    val questionId: Long? = null
)

@Serializable
data class AnswerHistoryResponse(
    val answers: List<AnswerWithAnalysis>
)

@Serializable
data class AnswerWithAnalysis(
    val answer: AnswerDTO,
    val analysis: AiAnalysisDTO?
)

// Health Check
@Serializable
data class HealthResponse(
    val status: String,
    val database: String,
    val timestamp: String
)
