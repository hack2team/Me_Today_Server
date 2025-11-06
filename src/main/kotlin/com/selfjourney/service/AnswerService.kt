package com.selfjourney.service

import com.selfjourney.domain.*
import com.selfjourney.repository.AnswerRepository
import com.selfjourney.repository.ProgressRepository
import com.selfjourney.repository.QuestionRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class AnswerService(
    private val answerRepository: AnswerRepository,
    private val questionRepository: QuestionRepository,
    private val progressRepository: ProgressRepository
) {

    suspend fun submitAnswer(request: CreateAnswerRequest): CreateAnswerResponse {
        val prevAnswer = transaction {
            answerRepository.findPreviousAnswer(request.userId, request.questionId)
        }

        val savedAt = LocalDateTime.now().toString()
        val answerId = transaction {
            val id = answerRepository.create(request = request)
            progressRepository.incrementAnswerCount(request.userId)
            id
        }

        return CreateAnswerResponse(
            answerId = answerId,
            prevAnswer = prevAnswer,
            savedAt = savedAt
        )
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
                answerRepository.findByUserAndQuestion(userId, questionId)
            }

            date != null -> {
                answerRepository.findByUserId(userId)
                    .filter { it.createdAt.startsWith(date) }
            }

            else -> {
                answerRepository.findByUserId(userId)
            }
        }

        val questionMap = questionRepository.findByIds(answers.map { it.questionId }.toSet())
        val historyItems = answers.map { answer ->
            AnswerHistoryItem(
                answerId = answer.answerId,
                questionId = answer.questionId,
                questionContent = questionMap[answer.questionId]?.content,
                content = answer.content,
                createdAt = answer.createdAt
            )
        }

        AnswerHistoryResponse(answers = historyItems)
    }

    fun getQuestionHistory(userId: Long, questionId: Long): AnswerHistoryResponse = transaction {
        val answers = answerRepository.findByUserAndQuestion(userId, questionId)
        val questionContent = questionRepository.findById(questionId)?.content
        val items = answers.map {
            AnswerHistoryItem(
                answerId = it.answerId,
                questionId = it.questionId,
                questionContent = questionContent,
                content = it.content,
                createdAt = it.createdAt
            )
        }
        AnswerHistoryResponse(items)
    }

    fun getUserReport(userId: Long): UserReportDTO = transaction {
        val answersDesc = answerRepository.findByUserId(userId)
        val totalAnswers = answersDesc.size
        val uniqueQuestions = answersDesc.map { it.questionId }.toSet().size
        val progress = progressRepository.findByUserId(userId)
        val questionMap = questionRepository.findByIds(answersDesc.map { it.questionId }.toSet())

        val recent = answersDesc.take(5).map {
            AnswerHistoryItem(
                answerId = it.answerId,
                questionId = it.questionId,
                questionContent = questionMap[it.questionId]?.content,
                content = it.content,
                createdAt = it.createdAt
            )
        }

        val topKeywords = extractTopKeywords(answersDesc.map { it.content })
        val relationships = extractRelationships(answersDesc.map { it.content })
        val highlights = buildHighlights(topKeywords, totalAnswers)
        val opportunities = buildOpportunities(topKeywords, relationships)

        UserReportDTO(
            totalAnswers = totalAnswers,
            uniqueQuestions = uniqueQuestions,
            streak = progress?.consecutiveDays ?: 0,
            lastAnsweredAt = progress?.lastAnsweredAt,
            topKeywords = topKeywords,
            relationships = relationships,
            highlights = highlights,
            opportunities = opportunities,
            recentAnswers = recent
        )
    }

    private fun extractTopKeywords(contents: List<String>): List<String> {
        if (contents.isEmpty()) return emptyList()
        val counts = mutableMapOf<String, Int>()
        contents.forEach { text ->
            text.split(" ", "\n", "\t", ",", ".", "!", "?")
                .map { it.trim() }
                .filter { it.length > 1 }
                .forEach { token -> counts[token] = counts.getOrDefault(token, 0) + 1 }
        }
        return counts.entries.sortedByDescending { it.value }.map { it.key }.take(5)
    }

    private fun extractRelationships(contents: List<String>): Map<String, String> {
        if (contents.isEmpty()) return emptyMap()
        val dictionary = mapOf(
            "가족" to "가족과의 시간을 소중히 여기고 있어요.",
            "부모" to "부모님과의 관계에 집중하고 있어요.",
            "친구" to "친구들과의 교류가 자주 언급되고 있어요.",
            "동료" to "동료와의 협업 경험이 기록되어 있어요.",
            "연인" to "연인과의 감정 변화가 드러나고 있어요.",
            "팀" to "팀원들과의 관계가 중요하게 다뤄지고 있어요."
        )
        val relationships = mutableMapOf<String, String>()
        contents.forEach { text ->
            dictionary.forEach { (keyword, summary) ->
                if (keyword in text) {
                    relationships.putIfAbsent(keyword, summary)
                }
            }
        }
        return relationships
    }

    private fun buildHighlights(topKeywords: List<String>, totalAnswers: Int): List<String> {
        val highlights = mutableListOf<String>()
        if (totalAnswers > 0) {
            highlights += "지금까지 ${totalAnswers}개의 답변을 남겼어요."
        }
        if (topKeywords.isNotEmpty()) {
            highlights += "최근 자주 언급한 키워드: ${topKeywords.joinToString(", ")}"
        }
        return highlights
    }

    private fun buildOpportunities(topKeywords: List<String>, relationships: Map<String, String>): List<String> {
        val suggestions = mutableListOf<String>()
        if (topKeywords.isEmpty()) {
            suggestions += "답변에 핵심 키워드를 더 구체적으로 남겨보세요."
        }
        if (relationships.isEmpty()) {
            suggestions += "사람들과의 관계나 감정을 기록해 보세요."
        }
        if (suggestions.isEmpty()) {
            suggestions += "지금의 기록 습관을 꾸준히 이어가 보세요."
        }
        return suggestions
    }
}
