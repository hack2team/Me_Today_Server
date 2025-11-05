package com.selfjourney.service

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.ProgressRepository
import com.selfjourney.repository.AiAnalysisRepository
import com.selfjourney.repository.UserGoalRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class AnswerService(
    private val answerRepository: AnswerRepository,
    private val progressRepository: ProgressRepository,
    private val aiAnalysisRepository: AiAnalysisRepository,
    private val userGoalRepository: UserGoalRepository,
    private val geminiService: GeminiService
) {

    suspend fun submitAnswer(request: CreateAnswerRequest): CreateAnswerResponse {
        // Find previous answer for the same question
        val prevAnswer = transaction {
            answerRepository.findPreviousAnswer(request.userId, request.questionId)
        }

        // Get user's ideal person description if available
        val userGoal = transaction {
            userGoalRepository.findActiveGoalByUserId(request.userId)
        }

        // AI analysis (outside transaction since it's a suspend function)
        val aiAnalysis = geminiService.analyzeAnswer(
            content = request.content,
            previousAnswer = prevAnswer?.content,
            idealPersonDescription = userGoal?.idealPersonDescription
        )

        // Save answer with AI analysis
        return transaction {
            val answerId = answerRepository.create(
                request = request,
                aiSummary = aiAnalysis.summary,
                aiKeywords = Json.encodeToString(aiAnalysis.keywords)
            )

            // Save AI analysis
            aiAnalysisRepository.create(
                userId = request.userId,
                answerId = answerId,
                strengths = aiAnalysis.strengths,
                weaknesses = aiAnalysis.weaknesses,
                pathToIdeal = aiAnalysis.pathToIdeal,
                relationshipMap = Json.encodeToString(aiAnalysis.relationshipMap)
            )

            // Update progress
            progressRepository.incrementAnswerCount(request.userId)

            CreateAnswerResponse(
                answerId = answerId,
                prevAnswer = prevAnswer,
                savedAt = LocalDateTime.now().toString(),
                aiAnalysis = AiAnalysisResult(
                    summary = aiAnalysis.summary,
                    keywords = aiAnalysis.keywords,
                    strengths = aiAnalysis.strengths,
                    weaknesses = aiAnalysis.weaknesses,
                    pathToIdeal = aiAnalysis.pathToIdeal,
                    relationshipMap = aiAnalysis.relationshipMap,
                    comparison = aiAnalysis.comparison
                )
            )
        }
    }

    fun getAnswersByUserId(userId: Long): List<AnswerDTO> = transaction {
        answerRepository.findByUserId(userId)
    }

    fun getAnswersByQuestionId(questionId: Long): List<AnswerDTO> = transaction {
        answerRepository.findByQuestionId(questionId)
    }

    fun getAnswerHistory(userId: Long, date: String?, questionId: Long?): AnswerHistoryResponse = transaction {
        val answers = when {
            questionId != null -> {
                // Get all answers for specific question from this user
                answerRepository.findByUserId(userId)
                    .filter { it.questionId == questionId }
            }
            date != null -> {
                // Get answers from specific date
                answerRepository.findByUserId(userId)
                    .filter { it.createdAt.startsWith(date) }
            }
            else -> {
                // Get all answers for this user
                answerRepository.findByUserId(userId)
            }
        }

        val answersWithAnalysis = answers.map { answer ->
            val analysis = aiAnalysisRepository.findByAnswerId(answer.answerId)
            AnswerWithAnalysis(
                answer = answer,
                analysis = analysis
            )
        }

        AnswerHistoryResponse(answers = answersWithAnalysis)
    }
}
